/*
 * Implement an LFU replacement cache policy
 *
 * See the comments in gtcache.h for API documentation.
 *
 * The entry in the cache with the fewest hits since it entered the
 * cache (the most recent time) is evicted.
 *
 */

#include <stdlib.h>
#include "gtcache.h"
/*
 * Include headers for data structures as you see fit
 */



int gtcache_init(size_t min_entry_size, int num_levels, size_t capacity){


}

void* gtcache_get(char *key, size_t *val_size){

}

int gtcache_set(char *key, void *value, size_t val_size){
 
}

int gtcache_memused(){

}

void gtcache_destroy(){
 
}
