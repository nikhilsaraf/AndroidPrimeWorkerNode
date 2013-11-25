package com.nikhilsaraf.distributed_primes.android;

import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.nikhilsaraf.distributed_primes.android.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class PrimeWorkerMain extends Activity implements PrimeGeneratorDelegate {
	private static final Logger logger = Logger.getLogger(PrimeWorkerMain.class.getSimpleName() + "_Tag");
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    
    private static final AtomicBoolean dialogOpen = new AtomicBoolean(false);

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    
    private AbstractPrimeGenerator primeGenerator;
    private ExecutorService primeExecutor;
    
    private TableLayout tableView;
    private Button buttonFindFirstNPrimes;
    private Button buttonFindNextPrime;
    private TextView textViewPoints;
    
    private LinkedHashSet<Long> uiVisiblePrimes;
    private Long pointsAccummulated;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prime_worker_main);

        /* While it is more computationally efficient to use the sieve, it is more taxing on memory.
         * Also, the sieve needs us to define the maximum number to check for primes up front (because we cannot
         * go up to infinite). This is quite limiting when you consider the limited memory on a mobile device,
         * the variation in hardware etc. Not to mention the extra work needed to be done when you find that
         * maximum prime.
         * 
         * For that reason, I decided to not use the sieve, but use a modified version of the divide till sqrt algorithm.
         * From the perspective and goal of this app, this allows us a few things that outweigh the alternative option:
         * 1. minimal state data needed to be maintain to persist data (and also transfer knowledge in the case of distributed case)
         * 2. minimal memory overhead
         * Disadvantage: Computationally slower
         * 
         * Also, I was running into memory limits on the simulator so had to decide against the sieve after I had implemented it.
         *  
         */
        
        this.uiVisiblePrimes = new LinkedHashSet<Long>(128);
        this.pointsAccummulated = 0L;
        
//        // once loaded content view, we want to initiate sieve
//        this.primeGenerator = new SievePrimeGenerator();
        this.primeGenerator = new DividingPrimeGenerator();
        primeExecutor = Executors.newSingleThreadExecutor();

        // initialize network synchronizer (need to do this before we add any click listeners so that all updates are appropriately
        // passed on)
        NetworkSynchronizer.get("test1", primeGenerator, this);
        
        tableView = (TableLayout) findViewById(R.id.tableView);
        buttonFindFirstNPrimes = (Button) findViewById(R.id.button_find_first_n_primes);
        buttonFindNextPrime = (Button) findViewById(R.id.button_find_next_prime);
        textViewPoints = (TextView) findViewById(R.id.textView_points);
        // TODO implement cancel button
        
        resetTableView();
        initializeTextViewPoints();
        
        // add click listeners for the buttons
        buttonFindFirstNPrimes.setOnClickListener(findFirstNPrimesClickListener);
        buttonFindNextPrime.setOnClickListener(findNextPrimeClickListener);
        
        
        /* ************************************************************************************ */
        /* ************* CODE TO PERFORM AUTO-HIDING OF SCREEN (Autogenerated) **************** */
        /* ************************************************************************************ */
        
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, tableView, HIDER_FLAGS);
        mSystemUiHider.setup();

        // Set up the user interaction to manually show or hide the system UI.
        tableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        buttonFindFirstNPrimes.setOnTouchListener(mDelayHideTouchListener);
        buttonFindNextPrime.setOnTouchListener(mDelayHideTouchListener);

        /* ************************************************************************************ */
        /* ************* / CODE TO PERFORM AUTO-HIDING OF SCREEN (Autogenerated) ************** */
        /* ************************************************************************************ */
    }
    
    public Long getPoints() {
    	return pointsAccummulated;
    }
    
    public void setPoints(final Long newTotalPoints) {
    	if (newTotalPoints > pointsAccummulated) {
    		// update UI
    		textViewPoints.setText("You earned +" + (newTotalPoints - pointsAccummulated) + " points for calculating a prime!");

    		// update new points data structure
    		pointsAccummulated = newTotalPoints;
    		
    		// enqueue a action to update the points later on after a bit
    		final Runnable textTimer = new Runnable() {
    			@Override
    			public void run() {
    				// set to pointsAccummulated rather than newTotalPoints because this may get executed a few times so we want the latest value, not a blur
    				textViewPoints.setText("Total Contribution Points: " + pointsAccummulated);
    			}
    		};
    		
    		final Handler textUpdateHandler = new Handler();
    		// runnable will execute after 1 second
    		textUpdateHandler.postDelayed(textTimer, 1000);
    	}
    }
    
    @Override
    public void onReceivePrime(Long prime) {
    	addPrimeToTable(prime);
    }

	private void addPrimeToTable(Long prime) {
		if (!this.uiVisiblePrimes.contains(prime)) {
    		logger.info("Received prime from prime generator thread: " + prime + " (was added to UI)");
    		
    		final int number = this.uiVisiblePrimes.size() + 1;	// +1 because we are adding it after this
    		this.uiVisiblePrimes.add(prime);
    		
    		// update UI with prime
    		final TableRow tr = new TableRow(this);
    		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		tr.setLayoutParams(lp);

    		// SR. No
    		final TextView textViewNumber = new TextView(this);
    		textViewNumber.setLayoutParams(lp);
    		textViewNumber.setBackgroundColor(Color.WHITE);
    		textViewNumber.setText(String.valueOf(number) + ".");
    		tr.addView(textViewNumber);
    		
    		// prime
    		final TextView textViewPrime = new TextView(this);
    		textViewPrime.setLayoutParams(lp);
    		textViewPrime.setBackgroundColor(Color.WHITE);
    		textViewPrime.setText(String.valueOf(prime));
    		tr.addView(textViewPrime);
    		
    		insertIntoTableView(tr);
    	} else {
    		logger.info("Received prime from prime generator thread: " + prime + " (was NOT added to UI because it existed)");
    	}
	}
    
    private void cancelGeneratingPrimes() {
    	primeGenerator.hardCancel();
    }

	private void insertIntoTableView(final TableRow tr) {
		// add to top leaving header
		synchronized (tableView) {
			tableView.addView(tr, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}
    
    /* ************************************************************************************ */
    /* ************************ CLICK LISTENERS FOR THE BUTTONS *************************** */
    /* ************************************************************************************ */
    
    /**
     * ClickListener for findFirstNPrimes button
     */
    View.OnClickListener findFirstNPrimesClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// do only if not already running
			if (primeGenerator.isRunning()) {
				alertGeneratorRunning();
			} else {
				getInputN();
			}
		}
	};
	
	private void getInputN() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("Enter N:");
		alertDialogBuilder.setTitle("Find first N Primes");
		final EditText edit = new EditText(this);
		edit.setText("20");
		alertDialogBuilder.setView(edit);

		// set dialog message
		alertDialogBuilder
				.setCancelable(true)
				.setPositiveButton("Done",
					  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								// edit text
						    	Integer numberOfPrimesToFind = Integer.parseInt(edit.getText().toString());
							    
								primeGenerator.setIsRunning();
								
								// we want to start from scratch 
								clearUITable();
								findPrimes(0, numberOfPrimesToFind);
								dialogOpen.set(false);
						    }
					  })
				.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
						    	dialogOpen.set(false);
						    	dialog.cancel();
						    }
					  });

		// create alert dialog
		if (!dialogOpen.get()) {
			alertDialogBuilder.create().show();
			dialogOpen.set(true);
		}
	}
	
	private void clearUITable() {
		uiVisiblePrimes.clear();
		resetTableView();
	}

	private void resetTableView() {
		synchronized (tableView) {
			tableView.removeAllViews();
			
			// update UI with prime
			final TableRow tr = new TableRow(this);
			final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			tr.setLayoutParams(lp);
			
			final TextView textViewNumber = new TextView(this);
			textViewNumber.setLayoutParams(lp);
			textViewNumber.setBackgroundColor(Color.WHITE);
			textViewNumber.setText("N value");
			tr.addView(textViewNumber);
			
			// prime
			final TextView textViewPrime = new TextView(this);
			textViewPrime.setLayoutParams(lp);
			textViewPrime.setBackgroundColor(Color.WHITE);
			textViewPrime.setText("Nth Prime Number");
			tr.addView(textViewPrime);
			
			// insert at top
			tableView.addView(tr, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}
	
//	private void addAllPrimesToTable() {
//		for (Long prime : primeGenerator.getPrimesFound()) {
//			addPrimeToTable(prime);
//		}
//	}
	
	private void initializeTextViewPoints() {
		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		textViewPoints.setLayoutParams(lp);
		textViewPoints.setBackgroundColor(Color.WHITE);
		textViewPoints.setText("Total Contribution Points: " + 0);
	}
	
    /**
     * ClickListener for findNextPrime button
     */
    View.OnClickListener findNextPrimeClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// only add if not already running
			if (primeGenerator.isRunning()) {
				alertGeneratorRunning();
			} else {
				primeGenerator.setIsRunning();
				// find 1 more prime
				int currentPrimesInView = uiVisiblePrimes.size();
				findPrimes(currentPrimesInView, currentPrimesInView + 1);
			}
		}
	};
	
	private void findPrimes(final int startingIndex, final long numberOfPrimesToFind) {
		for (int i = startingIndex; i < numberOfPrimesToFind && i < primeGenerator.getPrimesFound().size(); i++) {
			onReceivePrime(primeGenerator.getPrimesFound().get(i));
		}
		
		if (numberOfPrimesToFind > primeGenerator.getPrimesFound().size()) {
			enqueuePrimeGeneration(numberOfPrimesToFind - primeGenerator.getPrimesFound().size());
		} else {
			// very important, because the job in the queue will not mark it complete as it will never be enqueued
			primeGenerator.markQueueEmpty();
		}
	}
	
	private void enqueuePrimeGeneration(final long N) {
		// submit N jobs
		AsyncTask<Object, Integer, Object> task = new AsyncTask<Object, Integer, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				for (int i = 0; i < N; i++) {
//					logger.info("Generating a new prime in Executor Service thread");
					primeGenerator.generateNextPrime();
					publishProgress(Integer.valueOf(i));
				}
				return null;
			}
			
			@Override
	        protected void onProgressUpdate(Integer... values) {
				// TODO - this condition is not needed, but the simulator was giving an IOB Exception, no time to debug
				// this now, if improving upon this app then maybe should tackle this
				if (primeGenerator.getPrimesFound().size() > values[0]) {
					// add last prime to view
					onReceivePrime(primeGenerator.getPrimesFound().get(values[0]));
				}
	        }
			
			@Override
		    protected void onPostExecute(Object result) {
		        super.onPostExecute(result);

		        // reload full table so that 
//		        clearUITable();
//		        addAllPrimesToTable();
		        
		        // only add those that have not been added
		        // this will take care of anything happening in the background during distributed processing help
		        for (long i = primeGenerator.getPrimesFound().size() - uiVisiblePrimes.size(); i < primeGenerator.getPrimesFound().size(); i++) {
		        	onReceivePrime(primeGenerator.getPrimesFound().get((int) i));
		        }
		        primeGenerator.markQueueEmpty();
		    }
		};
		
		// need min api level of 11 to use executeOnExecutor
		task.executeOnExecutor(primeExecutor, new Object[]{}); //.execute(new Object[]{});
	}

	private void alertGeneratorRunning() {
		logger.info("showed alert because the prime generator is running and we cannot start a new generating action");
		
		// TODO - need to give a popup saying taht ew are already running and suggest the option of canceling the run from the top
		// buttons
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("Currently processing primes...");
		alertDialogBuilder.setTitle("Oh No!");

		// set dialog message
		alertDialogBuilder
				.setCancelable(true)
				.setPositiveButton("Continue",
					  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int id) {
						    	logger.info("continued prime generation after dialog");
						    	dialogOpen.set(false);
						    	dialog.cancel();
						    }
					  })
				.setNegativeButton("Stop Current Run",
					  new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog,int id) {
						    	logger.info("canceled prime generation after dialog");
						    	cancelGeneratingPrimes();
						    	// TODO need to send lastFullyChecked value to server
						    	dialogOpen.set(false);
						    	dialog.cancel();
						    }
					  });

		// create alert dialog
		if (!dialogOpen.get()) {
			alertDialogBuilder.create().show();
			dialogOpen.set(true);
		}
	}
    
    /* ************************************************************************************ */
    /* *********************** / TOUCH LISTENERS FOR THE BUTTONS ************************** */
    /* ************************************************************************************ */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
        	if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
