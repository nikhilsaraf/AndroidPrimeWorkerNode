package com.nikhilsaraf.distributed_primes.android;

class DividingPrimeGenerator extends AbstractPrimeGenerator {
	// the state variable
    private long lastNumberFullyChecked;
    
    /**
     * Creates a new prime generator generate prime numbers.
     * 
     */
    public DividingPrimeGenerator(PrimeGeneratorDelegate delegate) {
    	super(delegate);
    	// set it to 1 so we do not consider it as a prime.
        this.lastNumberFullyChecked = 1;
    }
    
	@Override
	protected Long getNextPrime() {
		// iterate through all the numbers we have
		Long currentNumber = lastNumberFullyChecked + 1;

		// some primes can take really long to generate. So we may want to stop with the hit of a Cancel Button.
		while (!wasHardCancelPressed()) {
			for (Long prime: primesFound) {
				// this is where the iterate till root N part comes in.
                // if the prime we are dividing by is greater than root N than we know that it is a prime, so we just break.
                if (prime > Math.sqrt(currentNumber)) {
                	// Note: It is critical to add to the list before we update lastNumberFullyChecked because if the thread was to get stuck
                	// because of scheduling before that, and say we had a sweeper to constantly synchronize with the server, it would mark the
                	// currentNumber as not a prime because it is not in the prime list!
                	primesFound.add(currentNumber);
                	lastNumberFullyChecked = currentNumber;
                	return currentNumber;
                }
                // if it divides, then not a prime.
                if (currentNumber % prime == 0) {
                	lastNumberFullyChecked = currentNumber;
                	currentNumber++;
                    break;
                }
        	}
			
			if (primesFound.isEmpty()) {
				primesFound.add(currentNumber);
	        	lastNumberFullyChecked = currentNumber;
	        	return currentNumber;
			}
		}
		
		// this will only happen if we cancel. But at least we would have made some headway by updating the last number fully checked
		return null;
	}
}