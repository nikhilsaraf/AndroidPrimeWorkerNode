//package com.nikhilsaraf.distributed_primes.android;
//
//import java.util.*;
//
//class SievePrimeGenerator extends AbstractPrimeGenerator {
//	/*
//	 * Define recommended max in static variable. Corresponds to 64MB of memory needed
//	 */
//	public static final long RECOMMENDED_MAX_NUMBER_OF_PRIMES_FOR_WHICH_TO_ALLOCATE = 536870912; 
//	
//    // define the width of the array, helps overcome memory issues
//    private static final int WIDTH = 1024;
//    
//    // the max number to check for primality
//    private final long MAX_N;
//
//    // the working data structure
//    private final List<BitSet> arr;
//
//    // the state variable
//    private long nextPotentialPrimeNumber;
//
//    /**
//     * Creates a new sieve with which to generate prime numbers.
//     * 
//     */
//    public SievePrimeGenerator() {
//    	super();
//    	
//        this.MAX_N = RECOMMENDED_MAX_NUMBER_OF_PRIMES_FOR_WHICH_TO_ALLOCATE;
//
//        // use a LinkedList to avoid allocating a whole bunch of memory up front
//        this.primesFound = new LinkedList<Long>();
//
//        // using a LinkedList (and breaking up into a 2-D matrix) so that the bitsets are stored separately in memory.
//        // there is a minor CPU hit becuase of this, but the benefit is that we do not need a large chunk of contiguous
//        // memory, as that may not be available, especially since we plan to keep a moderately large N value.
//        arr = new LinkedList<BitSet>();
//    
//        // size is N+1 because we want the index to denote the number and the bit to denote whether it has been crossed off or not
//        for (int i = 0; i <= MAX_N/WIDTH; i++) {
//            BitSet array = new BitSet(WIDTH);
//            arr.add(array);
//            // BitSet is automatically set to false so no need to initialize, false means not canceled.
//        }
//        
//        // mark numbers 0 and 1 as not prime, by definition
//        arr.get(0).set(0);
//        arr.get(0).set(1);
//        this.nextPotentialPrimeNumber = 0;
//    
//        // debugging information
//        System.out.println("Size of list: " + arr.size());
//        System.out.println("Size of first element in list: " + arr.get(0).size());
//    }
//
//    /**
//     * Used to query whether we have completed finding all primes we had accounted for.
//     * This artificial limit is based on memory restrictions for the specific purpose of this app. This can be made to be input by the user
//     * but may require a different prime generating algorithm. I decided to not use another as we would eventually encounter memory constraints
//     * anyways, and this provides us with faster computation. Users are more likely to enter a relatively high N number and would prefer faster
//     * computation, where the sieve is fast enough. 
//     * 
//     * @return whether we have found all the <MAX_N> primes.
//     */
//    public boolean maxedOutPrimeSearch() {
//        // this is not really the next potential prime number, but more like the last prime number that was seen
//        return nextPotentialPrimeNumber >= MAX_N;
//    }
//
//    /**
//     * Returns the list of all the primes generated, and generates more primes if needed.
//     * It is recommended to check if we maxed out the prime search after calling this.
//     * Decided not to deal with resizing for the purpose of this app.
//     * 
//     * @param numberOfPrimesToFind
//     * @return
//     */
//    @Override
//    public List<Long> getAtLeastNPrimes(final int numberOfPrimesToFind) {
//        while (!maxedOutPrimeSearch() && primesFound.size() < numberOfPrimesToFind) {
//            // increment index so we can iterate through all the numbers. This needs to be done before the continue statement
//            nextPotentialPrimeNumber++;
//            
//            // not a prime number as its been crossed off, so continue. (short curcuit out)
//            if (arr.get((int)(nextPotentialPrimeNumber/WIDTH)).get((int)(nextPotentialPrimeNumber%WIDTH))) {
//                continue;
//            }
//
//            long primeNumber = nextPotentialPrimeNumber;
//
//            // cross off all the numbers that are divisible by the prime number
//            // we can start from primeNumber^2 because all those numbers between primeNumber and primeNumber^2 would have been crossed off by previous iterations, so we do not gain anything
//            // by checking them again. This ensures fast iteration. Note: However, they may contain primes, that is hanled when we find the next prime number above.
//            BitSet subArray = null;
//            for (long i = primeNumber * primeNumber; i <= MAX_N; i += primeNumber) {
//                try {
//                    subArray = arr.get((int)(i/WIDTH));
//                } catch (Exception e) {
//                    try {
//                        throw new RuntimeException("Caught error! why?", e);
//                    } finally {
//                        System.out.println("i = " + i + " | i/WIDTH = " + (i/WIDTH));
//                    }
//                }
//                if (subArray.size() != WIDTH) {
//                    throw new IllegalStateException("Width of array is not equal to WIDTH. Was it not initialized correctly?");
//                }
//                // implement sieve logic, cross off number, as it is divisible (by simple logic of divistion)
//                subArray.set((int)(i%WIDTH));
//            }
//
//            // save prime found once we've executed the sieve
//            primesFound.add(Long.valueOf(primeNumber));
//        }
//        
//        return Collections.unmodifiableList(primesFound.subList(0, numberOfPrimesToFind));
//    }
//}
