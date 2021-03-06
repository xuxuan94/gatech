/**********************************************************************
gtthread_sched.c.  

This file contains the implementation of the scheduling subset of the
gtthreads library.  A simple round-robin queue should be used.
 **********************************************************************/
/*
  Include as needed
*/

#include "gtthread.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <ucontext.h>
#include "steque.h"
#include <errno.h>
#include <signal.h>
#include <sys/time.h>
#include <string.h>
/* 
   Students should define global variables and helper functions as
   they see fit.
 */

typedef struct wrapper_input {
  void *(*start_routine)(void *);
  void *arg;
} wrapper_input;

static steque_t threads;
static sigset_t vtalrm;
static struct sigaction act;
/*
 * If current thread is blocking something(on join, unblock it)
 * 
 */
static void schedule() {
  gtthread_t current = gtthread_self();
  steque_cycle(&threads);
  while(!steque_isempty(&threads) && gtthread_self()->state != READY) {
    // If joining on something, check if finished?
    // If children == 0 move to READY and schedule it
    if(gtthread_self()->state == CANCELED || gtthread_self()->state == FINISHED) {
      steque_pop(&threads);
      continue;
    }
    if(gtthread_self()->state == WAITING && gtthread_self()->waiting_on) {
      if(*(gtthread_self()->waiting_on) == UNLOCKED) {
        gtthread_self()->state = READY;
        break;
      }
    }
    if(gtthread_self()->children == 0) {
      gtthread_self()->state = READY;
      break;
    }
    steque_cycle(&threads);
  }
  if(!steque_isempty(&threads)) {
    swapcontext(&(current->context),&(gtthread_self()->context));
  }
}

static void alrm_handler(int sig) {
  schedule();
}

static void wrapper(void *input) {
  wrapper_input* in = (wrapper_input *) input;
  void* retval = (*in->start_routine) (in->arg);
  free(in);
  gtthread_exit(retval);
}

static void addToQueue(gtthread_t thread) {
  sigprocmask(SIG_BLOCK, &vtalrm, NULL);
  steque_enqueue(&threads, (steque_item) thread);
  sigprocmask(SIG_UNBLOCK, &vtalrm, NULL);
}

/*
  The gtthread_init() function does not have a corresponding pthread equivalent.
  It must be called from the main thread before any other GTThreads
  functions are called. It allows the caller to specify the scheduling
  period (quantum in micro second), and may also perform any other
  necessary initialization.  If period is zero, then thread switching should
  occur only on calls to gtthread_yield().

  Recall that the initial thread of the program (i.e. the one running
  main() ) is a thread like any other. It should have a
  gtthread_t that clients can retrieve by calling gtthread_self()
  from the initial thread, and they should be able to specify it as an
  argument to other GTThreads functions. The only difference in the
  initial thread is how it behaves when it executes a return
  instruction. You can find details on this difference in the man page
  for pthread_create.
 */
void gtthread_init(long period){
  steque_init(&threads);

  struct itimerval *T;
  sigemptyset(&vtalrm);
  sigaddset(&vtalrm, SIGVTALRM);
  sigprocmask(SIG_UNBLOCK, &vtalrm, NULL);
  T = (struct itimerval*) malloc(sizeof(struct itimerval));
  T->it_value.tv_sec = T->it_interval.tv_sec = 0;
  T->it_value.tv_usec = T->it_interval.tv_usec = period;

  setitimer(ITIMER_VIRTUAL, T, NULL);

  memset (&act, '\0', sizeof(act));
  act.sa_handler = &alrm_handler;
  if (sigaction(SIGVTALRM, &act, NULL) < 0) {
    perror ("sigaction");
    return;
  }


  gtthread_t main_thread = (gtthread_t) malloc(sizeof(__gtthread_t));
  /*
     TODO: In the presence of signals, protect steque operations
   */
  steque_init(&(main_thread->locks));
  addToQueue(main_thread);
  
}


/*
  The gtthread_create() function mirrors the pthread_create() function,
  only default attributes are always assumed.
 */
int gtthread_create(gtthread_t *thread,
		    void *(*start_routine)(void *),
		    void *arg){

  gtthread_t new_thread = (gtthread_t) malloc(sizeof(__gtthread_t));
  if (getcontext(&(new_thread->context)) == -1){
    perror("getcontext");
    return EAGAIN;
  }
  /*
   Allocating new stack for the scheduler.
   */
  new_thread->context.uc_stack.ss_sp = (char*) malloc(SIGSTKSZ);
  new_thread->context.uc_stack.ss_size = SIGSTKSZ;

  new_thread->context.uc_link = NULL;
  new_thread->parent = gtthread_self();
  new_thread->parent->children++;
  wrapper_input* input = (wrapper_input*) malloc(sizeof(wrapper_input));
  input->start_routine = start_routine;
  input->arg = arg;
  makecontext(&(new_thread->context), (void (*)(void))wrapper, 1, input);
  steque_init(&(new_thread->locks));
  addToQueue(new_thread);
  *thread = new_thread;
  return 0;
}

/*
  The gtthread_join() function is analogous to pthread_join.
  All gtthreads are joinable.
  Cleanup thing you joined on!
 */
int gtthread_join(gtthread_t thread, void **status){
  // TODO: Flip the state to be waiting on thread
  gtthread_t current = gtthread_self();
  current->state = WAITING;
  thread->joiner = current;
  current->joining_on = thread;
  while (thread->state != FINISHED &&
         thread->state != CANCELED) {
    schedule();
  }
  if(thread->state == FINISHED){
    if(status != NULL) {
      *status = thread->retval;
    }
  } else if(thread->state == CANCELED) {
    if(status !=NULL) {
      *status = NULL;
    }
  }
  // TODO: More to be done once you return from this? When done waiting?
  free(thread);
  return 0;
}

/*
 * The gtthread_exit() function is analogous to pthread_exit.
 * Wait for any child threads it's waiting on?
 *   Perhaps inspect all threads that are dead, and update the parent, reduce child count
 *   and if joined on, return value
 * Cleanup if nothing is joined on you.
 * When to cleanup memory?
 */
void gtthread_exit(void* retval){
  // Cycle and keep on list? Move to finished list?
  gtthread_t current = gtthread_self();
  current->retval = retval;
  if (current->parent != NULL) {
    current->parent->children--;
  }
  // Notify any thread joining on self
  free(current->context.uc_stack.ss_sp);
  if (current->joiner != NULL) {
    current->joiner->state = READY;
  }
  if (current->children == 0) {
    current->state = FINISHED;
    schedule();
  } else {
    current->state = WAITING;
    // Go to scheduler -> Cycle
    schedule();
  }
  

}


/*
  The gtthread_yield() function is analogous to pthread_yield, causing
  the calling thread to relinquish the cpu and place itself at the
  back of the schedule queue.
 */
void gtthread_yield(void){
  schedule();
}

/*
  The gtthread_yield() function is analogous to pthread_equal,
  returning zero if the threads are the same and non-zero otherwise.
 */
int  gtthread_equal(gtthread_t t1, gtthread_t t2){
  return t1 == t2;
}

/*
  The gtthread_cancel() function is analogous to pthread_cancel,
  allowing one thread to terminate another asynchronously.
 */
int  gtthread_cancel(gtthread_t thread){ 
  //gtthread_t current = gtthread_self();
  if(thread->state != FINISHED && thread->state != CANCELED) {
    thread->state = CANCELED;
    thread->retval = NULL;
    thread->parent->children--;
  }
  free(thread->context.uc_stack.ss_sp);
  if (thread->joiner != NULL) {
    thread->joiner->state = READY;
  }
  while(!steque_isempty(&(thread->locks))) {
    gtthread_mutex_t* lock = (gtthread_mutex_t *) steque_front(&(thread->locks));
    gtthread_mutex_unlock(lock);
  }
  //printf("Reaching the end?\n");
  return 0;
}

/*
  Returns calling thread.
  TODO: In the presence of signals, protect steque operations
 */
gtthread_t gtthread_self(void){
  return (gtthread_t) steque_front(&threads);
}

