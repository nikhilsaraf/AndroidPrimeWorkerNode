/**
 * 
 */
package com.nikhilsaraf.distributed_primes.android;

import java.util.LinkedList;
import java.util.List;

/**
 * @author nikhilsaraf
 *
 */
public abstract class AbstractPrimeGenerator {
    // the result data structure
    protected final List<Long> primesFound;
    
    // the result data structure
    protected final PrimeGeneratorDelegate delegate;
    
    // the state variable
    private volatile Boolean isRunning;
    // the state variable
    private volatile Boolean hardCancelPressed;
    
    public AbstractPrimeGenerator(PrimeGeneratorDelegate delegate) {
    	// use a LinkedList to avoid allocating a whole bunch of memory up front
        primesFound = new LinkedList<Long>();
        isRunning = Boolean.valueOf(false);
        hardCancelPressed = Boolean.valueOf(false);
    	this.delegate = delegate;
    }
    
    public void generatePrime() {
		final Long newPrime = getNextPrime();
    	if (newPrime != null) {
    		delegate.onReceivePrime(newPrime);
    	}
    }

    /**
     * Returns the next prime by computation
     * 
     * @return
     */
    protected abstract Long getNextPrime();
    
    public List<Long> getPrimesFound() {
    	return primesFound;
    }
    
    protected void hardCancel() {
    	synchronized (hardCancelPressed) {
    		hardCancelPressed = true;
    	}
    }
    
    protected void markQueueEmpty() {
    	synchronized (hardCancelPressed) {
    		hardCancelPressed = false;
    	}
    	synchronized (isRunning) {
    		isRunning = false;
    	}
    }
    
    protected boolean wasHardCancelPressed() {
    	synchronized (hardCancelPressed) {
    		return hardCancelPressed;
    	}
    }
    
    protected void setIsRunning() {
    	synchronized (isRunning) {
    		isRunning = true;
    	}
    }
    
    protected boolean isRunning() {
    	synchronized (isRunning) {
    		return isRunning;
    	}
    }
}