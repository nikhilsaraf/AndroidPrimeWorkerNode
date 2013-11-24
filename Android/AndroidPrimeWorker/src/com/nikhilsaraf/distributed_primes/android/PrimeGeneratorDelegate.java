/**
 * 
 */
package com.nikhilsaraf.distributed_primes.android;

/**
 * @author nikhilsaraf
 *
 */
public interface PrimeGeneratorDelegate {
	/**
	 * Called by the PrimeGenerator when we generate a new prime number
	 */
	public void onReceivePrime(Long prime);
}
