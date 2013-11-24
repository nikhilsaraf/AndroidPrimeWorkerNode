/**
 * 
 */
package com.nikhilsaraf.distributed_primes.android;

import com.firebase.client.Firebase;

/**
 * @author nikhilsaraf
 *
 */
public class NetworkSynchronizer {
	private final String username;
	private static final String baseUrl = "https://nikhilsaraf-primedistributedworkernode.firebaseio.com";
	private static final String primeList = "/primes";
	private static final String lastFullyCheckedValue = "/lastFullyCheckedValue";
	private static final String userPoints = "/userPoints";
	
	private final Firebase firebaseRef;
	
	private static NetworkSynchronizer singleton = null;
	
	private NetworkSynchronizer(String username) {
		firebaseRef = new Firebase(baseUrl);
		this.username = username;
	}
	
	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	public NetworkSynchronizer get(String username) {
		if (singleton == null) {
			synchronized(singleton) {
				if (singleton == null) {
					// set singleton
					singleton = new NetworkSynchronizer(username);
				}
			}
		}
		
		return singleton;
	}
	
	/**
	 * 
	 * @param prime
	 * @return total points for the user
	 */
	public long savePrime(Long prime) {
		
	}
//	
//	/**
//	 * 
//	 * @param value
//	 * @return total points for the user
//	 */
//	public long submitLastFullyCheckedNonPrimeValue(Long value) {
//		
//	}
	
	/**
	 * 
	 * @return total points for the user
	 */
	public long getPoints() {
//		getP
	}
	
	/**
	 * 
	 * @return url where points are saved for this user
	 */
	public String getUserPointsUrl() {
		return baseUrl + userPoints + "/" + username;
	}
	
	
}
