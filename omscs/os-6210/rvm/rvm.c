#include "rvm.h"
//#include "seqsrchst.h"
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h> // for printf


//TODO: Full malloc + free, cleaning up metadata stuff, like segment_t
/*
   Helper file access methods, to lock access while accessing files
 */

char *concat(const char* str1, const char* str2) {
   size_t lenDir = strlen(str1);
   //const char *redoFN = "/rvm.redo";
   size_t lenFile = strlen(str2);
   char *fullFN = malloc(lenDir + lenFile + 1);
   strcpy(fullFN, str1);
   strcat(fullFN, str2);
   return fullFN;
}

char *segFileName(rvm_t rvm, const char* segname) {
  char *segFN = concat(segname, ".seg");
  char *suffix = concat("/", segFN);
  char *fileName = concat(rvm->prefix, suffix);
  free(segFN);
  free(suffix);
  printf("Segment File Name %s\n", fileName);
  return fileName;
}

int equals(seqsrchst_key a, seqsrchst_key b) {
  return a==b;
}

/*
  Initialize the library with the specified directory as backing store.
*/
rvm_t rvm_init(const char *directory){
  /*
      1. Create file for redo log with current timestamp -> Get fd
      2. rvm_t should contain
          a. Directory name
          b. fd
          c. New seqsrchst (seg base -> segment)
   */
   printf("Init Called\n");

   //size_t lenDir = strlen(directory);
   //const char *redoFN = "/rvm.redo";
   //size_t lenFile = strlen(redoFN);
   //char *fullFN = malloc(lenDir + lenFile + 1);
   //strcpy(fullFN, directory);
   //strcat(fullFN, redoFN);
   char *fullFN = concat(directory, "/rvm.redo");
   //printf("FILENAME - %s\n", fullFN);
   remove(fullFN);
   int fd = open(fullFN, O_RDWR | O_CREAT, S_IRWXU | S_IRGRP | S_IROTH);
   rvm_t rvm = (rvm_t) malloc(sizeof(struct _rvm_t));
   strcpy(rvm->prefix, directory);
   free(fullFN);
   rvm->redofd = fd;
   rvm->segst = *((seqsrchst_t *) malloc(sizeof(seqsrchst_t)));
   seqsrchst_init(&(rvm->segst), &equals);
   printf("Init Done\n");
   return rvm;

}
/*
  map a segment from disk into memory. If the segment does not already exist, then create it and give it size size_to_create. If the segment exists but is shorter than size_to_create, then extend it until it is long enough. It is an error to try to map the same segment twice.
*/
void *rvm_map(rvm_t rvm, const char *segname, int size_to_create){
  /*
      1. If segment not present
         a. Create segment file <segname>.seg
         b. malloc memory of size size_to_create -> segbase
      2. If segment present
         a. truncate_log()
         b. Read <segname>.seg -> size, segbase
         c. If size_to_create > size -> realloc segbase
      3. Create segment_t
      4. Add segbase -> segment_t to rvm_t.segst
      5. Return segbase
   */
  printf("Map Called\n");
  //char *segFN = concat(segname, ".seg");
  //char *suffix = concat("/", segFN);
  //char *fileName = concat(rvm->prefix, suffix);
  //free(segFN);
  //free(suffix);
  //printf("Segment File Name %s\n", fileName);
  char *segFN = segFileName(rvm, segname);
  void *segbase;
  struct stat st;
  int result = stat(segFN, &st);
  if(result == 0) {
    rvm_truncate_log(rvm);
    // Read size first and then read that much into segbase
    void *sizeBuffer = malloc(sizeof(int));
    int retVal = read(rvm->redofd, sizeBuffer, sizeof(int));
    if(retVal == -1) {
      printf("ERROR while trying to read size from segment file");
      exit(1);
    }
    int size = (int) *((int *) sizeBuffer);
    segbase = malloc(size);
    retVal = read(rvm->redofd, segbase, size);
    if (retVal == -1) {
      printf("ERROR while trying to read segment file");
      exit(1);
    }
  } else { // File is not Present
    int fd = open(segFN, O_RDWR | O_CREAT, S_IRWXU | S_IRGRP | S_IROTH);
    void* sizeBuffer;
    int size = 0;
    sizeBuffer = (void *) (&size);
    write(fd, sizeBuffer, sizeof(int));
    //close(fd);
    segbase = malloc(size_to_create);
  }

  // 3. Create segment_t
  segment_t segment = (segment_t) malloc(sizeof(struct _segment_t));
  strcpy(segment->segname, segname);
  segment->segbase = segbase;
  segment->size = size_to_create;
  segment->cur_trans = NULL;
  steque_t* mods = (steque_t *) malloc(sizeof(steque_t));
  steque_init(mods);
  segment->mods = *mods;
  
  // 4. Add segbase -> segment_t to rvm_t.segst
  seqsrchst_put(&(rvm->segst), segbase, segment);

  printf("Map Done\n");
  return segbase;
}

/*
  unmap a segment from memory.
*/
void rvm_unmap(rvm_t rvm, void *segbase){
  /*
      1. Get segment_t from rvm_t.srchst[segbase]
      2. Zero out segment_t queue of modifications
   */
  printf("Unmap called\n");
  segment_t segment = seqsrchst_get(&(rvm->segst), segbase);
  steque_t* mods = &(segment->mods);
  steque_t* temp = (steque_t*) malloc(sizeof(steque_t));
  while(!(steque_isempty(mods))) {
    steque_push(temp, steque_pop(mods));
  }
  while(!(steque_isempty(temp))) { // == 0) {
    mod_t* mod = steque_pop(temp);
    //TODO - Apply undo in reverse order!
    //Possibly reverse all the mods order using a separate queue
    memcpy((segbase + mod->offset), mod->undo, mod->size);
    free(mod->undo);
    free(mod);
  }
  free(mods);
  mods = (steque_t *) malloc(sizeof(steque_t));
  steque_init(mods);
  segment->mods = *mods;
  //free(segment) -> Do that, but then factor out code to reuse for commit_trans
  printf("Unmap done\n");
}

/*
  destroy a segment completely, erasing its backing store. This function should not be called on a segment that is currently mapped.
 */
void rvm_destroy(rvm_t rvm, const char *segname){
  /*
      1. Delete <segname>.seg
   */
  printf("Destroy Called\n");
  char* segFN = segFileName(rvm, segname);
  remove(segFN);
  free(segFN);
  printf("Destory Done\n");
}

/*
  begin a transaction that will modify the segments listed in segbases. If any of the specified segments is already being modified by a transaction, then the call should fail and return (trans_t) -1. Note that trant_t needs to be able to be typecasted to an integer type.
 */
trans_t rvm_begin_trans(rvm_t rvm, int numsegs, void **segbases){
  /*
      1. Create trans_t with [rvm, numsegs, segment_t[numsegs]]
      2. For cur in 1 to numsegs
         a. Get segment_t = rvm_t.srst[segbase]
         b. trans_t.segments[cur] = segment_t
         c. if segment_t->cur_trans != NULL
            i. Free trans_t + roll back the transactions
            ii. Return (trans_t) -1
         d. segment_t->trans_t = tid
      3. return tid
   */
   printf("Begin Transaction Called\n");
   trans_t trans = (trans_t) malloc(sizeof(struct _trans_t));
   trans->rvm = rvm;
   trans->numsegs = numsegs;
   trans->segments = (segment_t*) malloc(numsegs * sizeof(segment_t));
   int cur;
   for(cur=0; cur<numsegs; cur++) {
     void* segbase = segbases[cur];
     segment_t current_segment = seqsrchst_get(&(rvm->segst), segbase);
     if(current_segment->cur_trans != NULL) {
       //ERROR
       for(cur=cur-1; cur>=0; cur--) {
         segbase = segbases[cur];
         current_segment = seqsrchst_get(&(rvm->segst), segbase);
         current_segment->cur_trans = NULL;
       }
       free(trans->segments);
       free(trans);
       printf("Begin Transaction Failed\n");
       return (trans_t) -1;
     } else {
       current_segment->cur_trans = trans;
     }
   }
   printf("Begin Transaction Done\n");
   return trans;
}

/*
  declare that the library is about to modify a specified range of memory in the specified segment. The segment must be one of the segments specified in the call to rvm_begin_trans. Your library needs to ensure that the old memory has been saved, in case an abort is executed. It is legal call rvm_about_to_modify multiple times on the same memory area.
*/
void rvm_about_to_modify(trans_t tid, void *segbase, int offset, int size){
  /*
      1. segment_t = tid->rvm.srchst[segbase]
      2. undo = segbase[offset:offset+size]
      3. mod = [offset, size, undo]
      4. segment_t->mods.enqueue(mod)
   */
  printf("About to Modify Called\n");
  segment_t segment = seqsrchst_get(&(tid->rvm->segst), segbase);
  void* undo = malloc(size);
  memcpy((segbase + offset), undo, size);
  mod_t* mod = (mod_t *) malloc(sizeof(mod_t));
  mod->offset = offset;
  mod->size = size;
  mod->undo = undo;
  steque_enqueue(&(segment->mods), (steque_item) mod);
  printf("About to Modify Done\n");
}

/*
commit all changes that have been made within the specified transaction. When the call returns, then enough information should have been saved to disk so that, even if the program crashes, the changes will be seen by the program when it restarts.
*/
void rvm_commit_trans(trans_t tid){
  /*
     1. Create segentry_t out of each tid->segment->mod
     2. Write segentry_t + data to redo file
     3. Free segentries, mods(+ undo segments) + tid [NOT segment_t]
   */
  printf("Commit Transaction Called\n");
  int segmentId;
  int fd = tid->rvm->redofd;
  lseek(fd, 0, SEEK_SET);
  void* sizeBuffer;
  sizeBuffer = malloc(sizeof(int));
  read(fd, sizeBuffer, sizeof(int));
  int entries = *((int*) sizeBuffer);
  lseek(fd, 0, SEEK_END);
  for(segmentId=0; segmentId < tid->numsegs; segmentId++) {
    segment_t segment = tid->segments[segmentId];

    segentry_t* segentry = (segentry_t*) malloc(sizeof(segentry_t));
    strcpy(segentry->segname, segment->segname);
    segentry->segsize = segment->size;
    int size = steque_size(&(segment->mods));
    segentry->numupdates = size;
    segentry->offsets = (int*) malloc(size * sizeof(int));
    segentry->sizes = (int*) malloc(size * sizeof(int));
    segentry->updatesize = 0;
    segentry->data = NULL;

    while(!(steque_isempty(&(segment->mods)))) { // == 0) {
      mod_t* mod = steque_pop(&(segment->mods));
      //Apply undo
      int currentsize = segentry->updatesize;
      segentry->updatesize = segentry->updatesize + mod->size;
      segentry->data = realloc(segentry->data, segentry->updatesize);
      memcpy((segentry->data + currentsize), 
             (segment->segbase + mod->offset), mod->size);
      free(mod->undo);
      free(mod);
    }
    write(fd, &(segentry->segname), 128);
    write(fd, &(segentry->segsize), sizeof(int));
    write(fd, &(segentry->updatesize), sizeof(int));
    write(fd, &(segentry->numupdates), sizeof(int));
    write(fd, segentry->offsets, segentry->numupdates * sizeof(int));
    write(fd, segentry->sizes, segentry->numupdates * sizeof(int));
    write(fd, segentry->data, segentry->updatesize);
    entries++;
  }
  lseek(fd, 0, SEEK_SET);
  write(fd, &entries, sizeof(int));
 
  printf("Commit Transaction Done\n");  
}

/*
  undo all changes that have happened within the specified transaction.
 */
void rvm_abort_trans(trans_t tid){
  /*
     1. For each tid->segment->mod(in reverse order)
     2. Apply undo segment to offset, size of segbase
     3. Free mods, undo segments, tid
   */
  printf("Abort Transaction Called\n");
  int segmentId;
  for(segmentId=0; segmentId < tid->numsegs; segmentId++){
    segment_t segment = tid->segments[segmentId];
    rvm_unmap(tid->rvm, segment->segbase);
    segment->cur_trans = NULL;
  }
  free(tid);
  printf("Abort Transaction Done\n");
}

/*
 play through any committed or aborted items in the log file(s) and shrink the log file(s) as much as possible.
*/
void rvm_truncate_log(rvm_t rvm){
  /*
     1. Read redo log file to create redo log + segentries
     2. Apply segentries to segname.seg files
     3. Truncate redo log file
     4. Free _redo_t + segentries
   */
  int fd = rvm->redofd;
  void* sizeBuffer = malloc(sizeof(int));
  read(fd, sizeBuffer, sizeof(int));
  int size = *((int *) sizeBuffer);
  int entry;
  for(entry=0; entry<size; entry++) {
    segentry_t* segentry = (segentry_t *) malloc(sizeof(segentry));
    read(fd, &(segentry->segname), 128);
    read(fd, &(segentry->segsize), sizeof(int));
    read(fd, &(segentry->updatesize), sizeof(int));
    read(fd, &(segentry->numupdates), sizeof(int));
    segentry->offsets = (int*) malloc(segentry->numupdates * sizeof(int));
    segentry->sizes = (int*) malloc(segentry->numupdates * sizeof(int));
    read(fd, segentry->offsets, segentry->numupdates * sizeof(int));
    read(fd, segentry->sizes, segentry->numupdates * sizeof(int));
    segentry->data = malloc(segentry->updatesize);
    read(fd, segentry->data, segentry->updatesize);


    char *segFN = segFileName(rvm, segentry->segname);
    int segFD = open(segFN, O_RDWR | O_CREAT, S_IRWXU | S_IRGRP | S_IROTH);
    // Read size first and then read that much into segbase
    void *sizeBuffer = malloc(sizeof(int));
    int retVal = read(segFD, sizeBuffer, sizeof(int));
    if(retVal == -1) {
      printf("ERROR while trying to read size from segment file");
      exit(1);
    }
    int size = *((int *) sizeBuffer);
    void* segbase = malloc(size);
    retVal = read(segFD, segbase, size);
    if (retVal == -1) {
      printf("ERROR while trying to read segment file");
      exit(1);
    }

    // TODO: See what happens if updates are at the end? How do they work?
    // Where is segsize increased?
    if(segentry->segsize > size) {
      segbase = realloc(segbase, segentry->segsize);
      size = segentry->segsize;
    }

    int dataOffset = 0;
    // Applying Updates
    int update;
    for(update=0; update<segentry->numupdates; update++) {
      memcpy((segbase + segentry->offsets[update]), 
             (segentry->data + dataOffset), segentry->sizes[update]);
      dataOffset = dataOffset + segentry->sizes[update];
    }
    lseek(segFD, 0, SEEK_SET);
    write(segFD, &size, sizeof(int));
    write(segFD, &segbase, size);
    free(segentry->offsets);
    free(segentry->sizes);
    free(segentry->data);
    free(segentry);
  }
  close(fd);
  char *fullFN = concat(rvm->prefix, "/rvm.redo");
  //printf("FILENAME - %s\n", fullFN);
  remove(fullFN);
  fd = open(fullFN, O_RDWR | O_CREAT, S_IRWXU | S_IRGRP | S_IROTH);
  rvm->redofd = fd;  
}

