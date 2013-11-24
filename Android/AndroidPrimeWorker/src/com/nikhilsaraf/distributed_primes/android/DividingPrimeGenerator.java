package com.nikhilsaraf.distributed_primes.android;

import java.util.Iterator;

class DividingPrimeGenerator extends AbstractPrimeGenerator {
	// the state variable
    private long lastNumberFullyChecked;
    
    private volatile Boolean distributedHelperUpdated = false; 
    
    /**
     * Creates a new prime generator generate prime numbers.
     * 
     */
    public DividingPrimeGenerator() {
    	super();
    	// set it to 1 so we do not consider it as a prime.
        this.lastNumberFullyChecked = 1;
    }
    
	@Override
	protected void generateNextPrime() {
		// iterate through all the numbers we have
		Long currentNumber = lastNumberFullyChecked + 1;

		// some primes can take really long to generate. So we may want to stop with the hit of a Cancel Button.
		while (!wasHardCancelPressed()) {
			// user iterator to avoid ConcurrentModificationException
			final Iterator<Long> iter = primesFound.iterator();
			while (iter.hasNext()) {
				Long prime = iter.next();
				// make it more sensitive to the cancel button
				if (wasHardCancelPressed()) {
					return;
				}
				// this is where the iterate till root N part comes in.
                // if the prime we are dividing by is greater than root N than we know that it is a prime, so we just break.
                if (prime > Math.sqrt(currentNumber)) {
                	// Note: It is critical to add to the list before we update lastNumberFullyChecked because if the thread was to get stuck
                	// because of scheduling before that, and say we had a sweeper to constantly synchronize with the server, it would mark the
                	// currentNumber as not a prime because it is not in the prime list!
                	synchronized(primesFound) {
                		primesFound.add(currentNumber);
                		lastNumberFullyChecked = currentNumber;
                	}
                	return;
                }
                // if it divides, then not a prime.
                if (currentNumber % prime == 0) {
                	lastNumberFullyChecked = currentNumber;
                	currentNumber++;
                    break;
                }
                
                synchronized (distributedHelperUpdated) {
                	if (distributedHelperUpdated) {
                		// reset
                		distributedHelperUpdated = false;
                		// we found another prime with the help of a distributed helper so we don't need to go ahead with computation, we
                		// got it for free!!
                		return;
                	}
                }
        	}
			
			if (primesFound.isEmpty()) {
				synchronized(primesFound) {
            		primesFound.add(currentNumber);
            		lastNumberFullyChecked = currentNumber;
            	}
	        	return;
			}
			
			// this if condition allows one to update the lastNumberFully checked mid-processing if it was updated by a
			// distributed helper node! This happens right before we move onto the next number to check when we have not already
			// found a prime
			if (lastNumberFullyChecked > currentNumber) {
				currentNumber = lastNumberFullyChecked;
			}
		}
		
		// this will only happen if we cancel. But at least we would have made some headway by updating the last number fully checked
		return;
	}
}