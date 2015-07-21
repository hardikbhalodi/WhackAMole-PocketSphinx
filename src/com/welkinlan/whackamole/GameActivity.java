/*
 * 
 */
package com.welkinlan.whackamole;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.welkinlan.util.FileHelper;
import com.welkinlan.util.TurnAnimation;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class GameActivity.
 */
public class GameActivity extends Activity implements RecognitionListener {

	/** The Constant PS_TAG. */
	private static final String PS_TAG = "PocketSphinx";

	/** The Constant GA_TAG. */
	private static final String GA_TAG = "GameActivity";

	/** The Time for the child to speak */
	private static long timeForSpeak = 3000, timeForBomb = 800;

	/** The lock. */
	private static Object lock = new Object();

	/** The recognizer. */
	private static SpeechRecognizer recognizer;

	/** The Constant WORD_SEARCH. */
	private static final String WORD_SEARCH = "words";

	/** The recognized text. */
	private static String recognizedText;

	/** The star num. */
	private static int starNum = 0;

	/** The finish num. */
	private int finishNum = 10;

	/** The step handler. */
	private static Handler gameHandler, updateHandler, clickHandler;
	
	/** The operation to continue game thread. */
	private static Runnable continueGameThread;

	/** The MoleGame thread to generate random positions. */
	private static MoleGameThread gameThread;

	/** The star gv. */
	//UI
	static GridView starGV;

	/** The image gv. */
	static GridView imageGV;

	/** The golden star. */
	private static Integer goldenStar = R.drawable.golden_star;

	/** The default star. */
	private static Integer defaultStar = R.drawable.default_star;

	/** The hole image. */
	static int holeImage = R.drawable.question;

	/** The current view. */
	private static View currentView;

	/** The alpha animation. */
	//Variables for animation
	private Animation alphaAnimation;

	/** The scale animation. */
	private Animation scaleAnimation;

	/** The set. */
	private static AnimationSet set;

	/** The flash animation. */
	Animation flashAnimation;

	/** The language model file. */
	//file
	File languageModelFile;

	/** The image map. */
	private static HashMap<String, Bitmap> imageMap;

	/** The image names. */
	private static String[] imageNames;
	
	/** The current word index. */
	static int currentWordIndex = -1;
	
	/** The current image position. */
	static int currentImagePosition = -1;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		languageModelFile = (File) getIntent().getExtras().get("file");
		Log.v(GA_TAG, languageModelFile.getName());
		setContentView(R.layout.loading);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();  // Always call the superclass method first
		if (recognizer!=null){
			recognizer.stop();
		}	
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();  // Always call the superclass method first
		if (recognizer==null){
			setupRecognizerTask();
		}
		else{
			recognizer.stop();
			recognizer.startListening(WORD_SEARCH);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();  // Always call the superclass method first
		recognizer = null;
		gameThread.stopThread();
	}


	/**
	 * Setup recognizer task.
	 */
	public void setupRecognizerTask(){
		new AsyncTask<Void, Void, Exception>() {
			@Override
			protected Exception doInBackground(Void... params) {
				try {
					//load images
					imageMap = FileHelper.getImages(languageModelFile, getApplicationContext());
					Object[] keys = imageMap.keySet().toArray();
					imageNames = new String[keys.length];
					for (int i = 0; i < keys.length; i++){
						imageNames[i] = keys[i].toString();
					}
					//set up recognizer
					Assets assets = new Assets(GameActivity.this);
					File assetDir = assets.syncAssets();
					setupRecognizer(assetDir);
				} catch (IOException e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				if (result != null) {
					Toast.makeText(getApplicationContext(), "Failed to init recognizer " + result,
							Toast.LENGTH_LONG).show();
					finish();  
					Intent backToMenu = new Intent(GameActivity.this, MenuActivity.class);
					startActivity(backToMenu);
				} else { 	
					startGame();
				}
			}
		}.execute();

	}

	/**
	 * Prepare the game UI and Handlers
	 */
	private void startGame(){
		// set up UI
		setContentView(R.layout.activity_game);	
		imageGV = (GridView) findViewById(R.id.game_view);
		starGV = (GridView) findViewById(R.id.starGV);
		//image grid view
		imageGV.setAdapter(new ImageAdapter(this));
		imageGV.setEnabled(false);
		//star grid view
		starGV.setSelector(new ColorDrawable(Color.TRANSPARENT));
		starGV.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
		starGV.setAdapter(new StarAdapter(this));
		starGV.setEnabled(false);
		setAnimation();

		//set up game handlers
		updateHandler = new Handler(); //update the UI based on the recognition result
		clickHandler = new Handler(); //handle the events after the image is clicked
		gameHandler = new GameHandler(); //handle the new position generated from MoleGame thread
		gameThread = new MoleGameThread(gameHandler, imageNames.length);
		gameThread.start();
		
		continueGameThread = new Runnable() {
			public void run() {
				recognizer.stop();
				currentView.setOnClickListener(null);
				gameThread.setState("RUNNING"); //gameThread thread state => running
			}
		};

	}

	/**
	 * Sets the up recognizer.
	 *
	 * @param assetsDir the new up recognizer
	 */
	private void setupRecognizer(File assetsDir) {
		File modelsDir = new File(assetsDir, "models");
		recognizer = defaultSetup()
				.setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
				.setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
				.setRawLogDir(assetsDir).setKeywordThreshold(1e-15f) //threshold (larger = more accurate, fewer false alarms)
				.getRecognizer();
		recognizer.addListener(this);

		// Create grammar-based searches.
		// File wordsGrammar = new File(modelsDir, "grammar/words.gram");
		// recognizer.addGrammarSearch(WORD_SEARCH, wordsGrammar);

		// Create keyword-based searches.
		//File wordsKeyWord = new File(modelsDir, "words/kwords.txt");
		File wordsKeyWord = languageModelFile;
		recognizer.addKeywordSearch(WORD_SEARCH, wordsKeyWord);
	}

	/**
	 * Sets the animation.
	 */
	public void setAnimation() {
		scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		alphaAnimation = new AlphaAnimation(0.1f, 11f);
		set = new AnimationSet(true);
		set.addAnimation(scaleAnimation);
		set.addAnimation(alphaAnimation);
		set.setDuration(200);
	}


	/**
	 * Gets the activity.
	 *
	 * @return the activity
	 */
	public Activity getActivity(){
		return this.getActivity();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:	
			recognizer.stop();
			gameThread.stopThread();
			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * The Class GameHandler for driving the game handler
	 */
	private static class GameHandler extends Handler {

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			ImageView image;
			TextView text;
			//stop the previous recognition
			if (recognizer!=null) recognizer.stop();
			//restore the previous one
			if (currentImagePosition != -1){
				currentView.setOnClickListener(null);
				image = (ImageView) currentView.findViewById(R.id.image);
				text = (TextView) currentView.findViewById(R.id.text);
				image.setImageResource(holeImage);
				text.setVisibility(View.GONE);
				ImageView checkImage = (ImageView) currentView.findViewById(R.id.check);
				checkImage.setVisibility(View.INVISIBLE);
				ImageView crossImage = (ImageView) currentView.findViewById(R.id.cross);
				crossImage.setVisibility(View.INVISIBLE);
			}
			//generate random word (including an extra bomb)
			currentWordIndex = (int) (Math.random() * (imageNames.length + 1));
			//update the current one
			synchronized (lock) {
				currentImagePosition = bundle.getInt("newPosition");
				currentView = imageGV.getChildAt(currentImagePosition);
				applyTurnRotation(0, 180);
			}
		}
	}


	/**
	 * Apply turn rotation.
	 *
	 * @param position the position
	 * @param start the start
	 * @param end the end
	 */
	// For the turn image animation
	private static void applyTurnRotation(float start, float end) {
		final float centerX = currentView.getWidth() / 2.0f;
		final float centerY = currentView.getHeight() / 2.0f;

		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final TurnAnimation rotation = new TurnAnimation(start, end, centerX,
				centerY, 310.0f, true);
		rotation.setDuration(180);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView());
		currentView.startAnimation(rotation);
	}

	/**
	 * This class listens for the end of the first half of the animation. It
	 * then posts a new action that effectively swaps the views when the
	 * container is rotated 90 degrees and thus invisible.
	 */
	private static final class DisplayNextView implements Animation.AnimationListener {

		/* (non-Javadoc)
		 * @see android.view.animation.Animation.AnimationListener#onAnimationStart(android.view.animation.Animation)
		 */
		public void onAnimationStart(Animation animation) {
		}

		/* (non-Javadoc)
		 * @see android.view.animation.Animation.AnimationListener#onAnimationEnd(android.view.animation.Animation)
		 */
		public void onAnimationEnd(Animation animation) {
			currentView.post(new SwapViews());
		}

		/* (non-Javadoc)
		 * @see android.view.animation.Animation.AnimationListener#onAnimationRepeat(android.view.animation.Animation)
		 */
		public void onAnimationRepeat(Animation animation) {
		}
	}

	/**
	 * This class is responsible for swapping the views and start the second
	 * half of the animation. Then add OnClickListener
	 */
	@SuppressLint("DefaultLocale")
	private static final class SwapViews implements Runnable {
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			ImageView image = (ImageView) currentView.findViewById(R.id.image);
			TextView text = (TextView) currentView.findViewById(R.id.text);

			if (currentWordIndex < imageNames.length) {
				//show image
				image.setImageBitmap(imageMap.get(imageNames[currentWordIndex]));
				text.setVisibility(View.VISIBLE);
				text.setText(imageNames[currentWordIndex].toLowerCase());
			} else {
				//show bomb
				image.setImageResource(R.drawable.bomb);
			}

			final float centerX = currentView.getWidth() / 2.0f;
			final float centerY = currentView.getHeight() / 2.0f;
			TurnAnimation rotation;
			rotation = new TurnAnimation(180, 0, centerX, centerY, 310.0f,
					false);
			rotation.setDuration(180);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new DecelerateInterpolator());
			currentView.startAnimation(rotation);
			//add OnClick listener after the image shows
			currentView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					//set the gameThread thread state to stop
					gameThread.setState("STOPPED");

					//if it is the bomb
					if (currentWordIndex == imageNames.length) {
						updateHandler.post(new Runnable() {
							@Override
							public void run() {
								ImageView crossImage = (ImageView) currentView.findViewById(R.id.cross);
								crossImage.setVisibility(View.VISIBLE);
								crossImage.startAnimation(set);
								//remove a star
								if (starNum > 0) {
									starNum--;
								}
								ImageView thisStar = (ImageView) starGV
										.getChildAt(starNum);
								thisStar.setImageResource(defaultStar);	
								//continue game thread
								clickHandler.postDelayed(continueGameThread, timeForBomb);
								
							}
						});
					} else {
						//run the recognition thread for timeForSpeak seconds
						recognizer.startListening(WORD_SEARCH);
						clickHandler.postDelayed(continueGameThread, timeForSpeak);
					}
				}

			});
		}
	}

	
	/**
	 * The Class ImageAdapter.
	 */
	static class ImageAdapter extends BaseAdapter {

		/** The m context. */
		private Context mContext;

		/**
		 * Instantiates a new image adapter.
		 *
		 * @param context the context
		 */
		public ImageAdapter(Context context) {
			this.mContext = context;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return 10;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return imageNames[position];
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.text_image_layout, null);
			}
			return convertView;
		}
	}

	/**
	 * The Class StarAdapter.
	 */
	static class StarAdapter extends BaseAdapter {

		/** The m context. */
		private Context mContext;

		/**
		 * Instantiates a new star adapter.
		 *
		 * @param context the context
		 */
		public StarAdapter(Context context) {
			this.mContext = context;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return 10;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView star;
			if (convertView == null) {
				star = new ImageView(mContext);
				star.setLayoutParams(new GridView.LayoutParams(80, 80));
				star.setScaleType(ImageView.ScaleType.CENTER_CROP);
				star.setPadding(2, 2, 2, 2);
				star.setImageResource(defaultStar);
			} else {
				star = (ImageView) convertView;
			}
			return star;
		}
	}


	
	/* for speech recognition */

	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onBeginningOfSpeech()
	 */
	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onBeginningOfSpeech()");
	}

	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onEndOfSpeech()
	 */
	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onEndOfSpeech()");
	}


	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onPartialResult(edu.cmu.pocketsphinx.Hypothesis)
	 */
	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		if (hypothesis != null) {
			synchronized (lock) {
				recognizedText = hypothesis.getHypstr().toLowerCase().trim();
				//update UI
				updateHandler.post(new Runnable() {
					@Override
					public void run() {
						String curWord = imageNames[currentWordIndex].toLowerCase().trim();
						//Log.v(PS_TAG, recognizedText+" vs. "+curWord+" :"+curWord.equals(recognizedText));
						ImageView checkImage = (ImageView) currentView.findViewById(R.id.check);
						//if correct
						if (recognizedText.contains(curWord)) {
							//show check mark for correct utterance
							checkImage.setVisibility(View.VISIBLE);
							checkImage.startAnimation(set);
							//add a star
							ImageView thisStar = (ImageView) starGV
									.getChildAt(starNum);
							thisStar.setImageResource(goldenStar);	
							starNum++;
							if (starNum == finishNum) {
								gameThread.stopThread();
								Intent gameOverIntent = new Intent(GameActivity.this, GameOverActivity.class);
								startActivity(gameOverIntent);
								finish();
							}
							//continue the game thread
							gameThread.setState("RUNNING");
							//remove the callback
							clickHandler.removeCallbacks(continueGameThread);
						} 
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onResult(edu.cmu.pocketsphinx.Hypothesis)
	 */
	@Override
	public void onResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onResult");
	}

	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onError(java.lang.Exception)
	 */
	@Override
	public void onError(Exception arg0) {
		// TODO Auto-generated method stub
		recognizer.removeListener(this);
	}

	/* (non-Javadoc)
	 * @see edu.cmu.pocketsphinx.RecognitionListener#onTimeout()
	 */
	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG,"onTimeout()");
		recognizer.removeListener(this);
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Dialog dialog = new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.btn_default)
			.setTitle("Do you want to quit?")
			.setPositiveButton("Quit",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					/*
									android.os.Process
											.killProcess(android.os.Process
													.myPid());
									System.exit(0);
					 */
					finish();  
					System.exit(0); 
				}

			})
			.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			}).create();
			dialog.show();
		}
		return false;
	}

}
