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
    
    // yields the background thread every millis, so that we do not hog cpu resources.
    // this should happen automatically as it is run in the background, but that is not
    // happening for some reason, and not worth trying to debug now as other things to do;
    // need to focus on completion.
    public static final long yieldEveryMillisPrimeGenerator = 5000;
    
    // the state variable
    private volatile Boolean isRunning;
    // the state variable
    private volatile Boolean hardCancelPressed;
    
    public AbstractPrimeGenerator() {
    	// use a LinkedList to avoid allocating a whole bunch of memory up front
        primesFound = new LinkedList<Long>();
        isRunning = Boolean.valueOf(false);
        hardCancelPressed = Boolean.valueOf(false);
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