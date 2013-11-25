/**
 * 
 */
package com.nikhilsaraf.distributed_primes.android;

import java.util.logging.Logger;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * @author nikhilsaraf
 *
 */
public class NetworkSynchronizer {
	private static final Logger logger = Logger.getLogger(PrimeWorkerMain.class.getSimpleName() + "_Tag");
	
	private final String username;
	private static final String baseUrl = "https://nikhilsaraf-primedistributedworkernode.firebaseio.com";
	private static final String childLevel1_primeList = "primes";
//	private static final String childLevel1_lastFullyCheckedValue = "lastFullyCheckedValue";
	private static final String childLevel1_userPointsRoot = "userPoints";
	
	private final AbstractPrimeGenerator primeGenerator;
	private final PrimeWorkerMain initializingWorker;
	
	private final Firebase firebaseRef;
	private final Firebase primeListRef;
//	private final Firebase lastFullyCheckedValueRef;
	private final Firebase userPointsRef;
	
	private static NetworkSynchronizer singleton = null;
	
	private NetworkSynchronizer(String username, AbstractPrimeGenerator primeGenerator, PrimeWorkerMain initializingWorker) {
		this.username = username;
		this.firebaseRef = new Firebase(baseUrl);
		this.primeGenerator = primeGenerator;
		this.initializingWorker = initializingWorker;
		this.primeListRef = firebaseRef.child(childLevel1_primeList);
//		this.lastFullyCheckedValueRef = firebaseRef.child(childLevel1_lastFullyCheckedValue);
		// note that this is a double call to .child
		this.userPointsRef = firebaseRef.child(childLevel1_userPointsRoot).child(username);
		
		this.primeListRef.addChildEventListener(new PrimeListChildEventListener());
//		this.userPointsRef.addChildEventListener(new PointsChildEventListener());
		
		// use a value listener as opposed to a completion listener because we want this to serve the
		// purpose of initial updates and also on adding new points
		this.userPointsRef.addValueEventListener(new ValueEventListener() {
		    @Override
		    public void onDataChange(DataSnapshot snapshot) {
		    	final Long newTotalPoints = snapshot.getValue(Long.class);
		    	logger.info("You earned points! New total: " + newTotalPoints);
		    	// set points locally
		    	singleton.initializingWorker.setPoints(newTotalPoints);
		    }

			@Override
			public void onCancelled(FirebaseError arg0) {}
		});
	}
	
	class PrimeListChildEventListener implements ChildEventListener {
		@Override
		public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
			final Long newPrime = snapshot.getValue(Long.class);
			final Long nValueOfPrime = Long.valueOf(snapshot.getName());
			primeGenerator.appendPrimeFromDistributedHelper(nValueOfPrime, newPrime);
	    }
		
		@Override
		public void onCancelled(FirebaseError arg0) {}
		@Override
		public void onChildChanged(DataSnapshot arg0, String arg1) {}
		@Override
		public void onChildMoved(DataSnapshot arg0, String arg1) {}
		@Override
		public void onChildRemoved(DataSnapshot arg0) {}
	}
	
//	class PointsChildEventListener implements ChildEventListener {
//		@Override
//	    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {}
//		@Override
//		public void onCancelled(FirebaseError arg0) {}
//		
//		@Override
//		public void onChildChanged(DataSnapshot snapshot, String arg1) {
//			synchronized (initializingWorker.getPoints()) {
//				final Long newPoints = snapshot.getValue(Long.class);
//				initializingWorker.setPoints(newPoints);
//			}
//		}
//		
//		@Override
//		public void onChildMoved(DataSnapshot arg0, String arg1) {}
//		@Override
//		public void onChildRemoved(DataSnapshot arg0) {}
//	}
	
	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	public static NetworkSynchronizer get(String username, AbstractPrimeGenerator primeGenerator, PrimeWorkerMain initializingWorker) {
		if (singleton == null) {
			synchronized(singleton) {
				if (singleton == null) {
					// set singleton
					singleton = new NetworkSynchronizer(username, primeGenerator, initializingWorker);
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
	public static void savePrime(final int nValueOfPrime, Long prime) {
		if (singleton == null) {
			throw new IllegalStateException("Network synchrnizer not initialized!");
		}
		
		singleton.primeListRef.child(String.valueOf(nValueOfPrime)).setValue(prime, new CompletionListener() {
			public void onComplete(FirebaseError error, Firebase arg1) {
				if (error != null) {
		            logger.info("Someone beat you to it! No points for you. errorMessage: " + error.getMessage());
		        } else {
		        	final long newTotalPoints = singleton.initializingWorker.getPoints() + (nValueOfPrime * nValueOfPrime);
		        	// update points on server
		        	singleton.userPointsRef.setValue(newTotalPoints);
		        }
			}
		});
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
}
