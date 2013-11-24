///**
// * 
// */
//package com.nikhilsaraf.distributed_primes.android;
//
///**
// * @author nikhilsaraf
// *
// */
//public class NetworkSynchronizer {
//	private static final String baseUrl = null;
//	
//	public NetworkSynchronizer() {
//	}
//	
//	/**
//	 * 
//	 * @param prime
//	 * @return total points for the user
//	 */
//	public long submitPrime(Long prime) {
//		
//	}
//	
//	/**
//	 * 
//	 * @param value
//	 * @return total points for the user
//	 */
//	public long submitLastFullyCheckedNonPrimeValue(Long value) {
//		
//	}
//	
//	/**
//	 * 
//	 * @return total points for the user
//	 */
//	public long getPoints() {
//		HttpResponse response = null;
//		try {        
//		        HttpClient client = new DefaultHttpClient();
//		        HttpGet request = new HttpGet();
//		        request.setURI(new URI("https://www.googleapis.com/shopping/search/v1/public/products/?key={my_key}&country=&q=t-shirts&alt=json&rankByrelevancy="));
//		        response = client.execute(request);
//		    } catch (URISyntaxException e) {
//		        e.printStackTrace();
//		    } catch (ClientProtocolException e) {
//		        // TODO Auto-generated catch block
//		        e.printStackTrace();
//		    } catch (IOException e) {
//		        // TODO Auto-generated catch block
//		        e.printStackTrace();
//		    }   
//		    return response;
//		}
//	}
//}
