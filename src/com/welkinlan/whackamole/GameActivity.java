package com.welkinlan.whackamole;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

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
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

public class GameActivity extends Activity implements RecognitionListener {
	private static final String PS_TAG = "PocketSphinx";
	private static final String GA_TAG = "GameActivity";

	private static Object lock = new Object();
	private static SpeechRecognizer recognizer;
	private static final String WORD_SEARCH = "words";
	private static String recognizedText;
	//game
	private int scoreCurr = 0;
	private int lifeCurr = 5;
	private static Handler stepHandler;
	private static Handler updateHandler;
	private MoleGame mg;
	//UI
	TextView scoreTview;
	TextView lifeTview;
	static GridView gw;
	//for image adapter
	static int currentImagePosition = -1;
	static int holeImage = R.drawable.question;
	private static View currentView, previousView;

	//Variables for animation
	private Animation alphaAnimation;
	private Animation scaleAnimation;
	private AnimationSet set;
	Animation flashAnimation;
	
	//file
	File languageModelFile;
	private static HashMap<String, Bitmap> imageMap;
	private static String[] imageNames;
	static int currentWordIndex = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		languageModelFile = (File) getIntent().getExtras().get("file");
		Log.v(GA_TAG, languageModelFile.getName());
		setContentView(R.layout.loading);
	}
	
	@Override
	public void onPause() {
		super.onPause();  // Always call the superclass method first
		if (recognizer!=null){
	    	recognizer.stop();
		}	
	}
	
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
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first
	    recognizer = null;
	    mg.stopThread();
	}

	
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
						//Log.v(GA_TAG, imageNames[i]);
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

	private void startGame(){
		// set up UI
		setContentView(R.layout.activity_game);	
		scoreTview = (TextView) findViewById(R.id.Score);
		lifeTview = (TextView) findViewById(R.id.Life);
		gw = (GridView) findViewById(R.id.game_view);
		//set up game
		stepHandler = new ChangeImage(this);
		updateHandler = new Handler();
		gw.setAdapter(new ImageAdapter(this));
		gw.setEnabled(false);
		setAnimation();

		mg = new MoleGame(stepHandler, imageNames.length);
		mg.start();

	}


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
	
	public void setAnimation() {
//		flashAnimation = new AlphaAnimation(1, 0);
//	    flashAnimation.setDuration(400);
//	    flashAnimation.setInterpolator(new LinearInterpolator());
//	    flashAnimation.setRepeatCount(Animation.INFINITE);
//	    flashAnimation.setRepeatMode(Animation.REVERSE); 
		scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		alphaAnimation = new AlphaAnimation(0.1f, 11f);
		set = new AnimationSet(true);
		set.addAnimation(scaleAnimation);
		set.addAnimation(alphaAnimation);
		set.setDuration(200);
		//set.setFillEnabled(true);
		//set.setFillAfter(true);
	}


	public Activity getActivity(){
		return this.getActivity();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:	
				recognizer.stop();
				mg.stopThread();
				int pid = android.os.Process.myPid();
	    		android.os.Process.killProcess(pid);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	// new image handler
	private static class ChangeImage extends Handler {
		Context context;
		
		public ChangeImage(Context pContext){
			context = pContext;
		}
		
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			ImageView image;
			TextView text;
			//stop the previous recognition
			if (recognizer!=null) recognizer.stop();
			//restore the previous one
			if (currentImagePosition!=-1){
				previousView = gw.getChildAt(currentImagePosition);
				image = (ImageView) previousView.findViewById(R.id.icon);
				text = (TextView) previousView.findViewById(R.id.text);
				image.setImageResource(holeImage);
				text.setVisibility(View.GONE);
				ImageView checkImage = (ImageView) currentView.findViewById(R.id.check);
				checkImage.setVisibility(View.INVISIBLE);
				ImageView crossImage = (ImageView) currentView.findViewById(R.id.cross);
				crossImage.setVisibility(View.INVISIBLE);
			}
			//generate random word
			currentWordIndex = (int) (Math.random() * imageNames.length);
			//update the current one
			synchronized (lock) {
				currentImagePosition = bundle.getInt("newPosition");
				currentView = gw.getChildAt(currentImagePosition);
				applyTurnRotation(currentWordIndex, 0, 180, context);
			}
			//start listening the current one
			recognizer.startListening(WORD_SEARCH);
			
			/* without animation
			//show the mole
			image = (ImageView) currentView.findViewById(R.id.icon);
			text = (TextView) currentView.findViewById(R.id.text);
			text.setVisibility(View.VISIBLE);

			InputStream in = null;
			try {
				in = context.getAssets().open("img/" + imageNames[currentImagePosition]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final Bitmap bitmap = BitmapFactory.decodeStream(in);
			// Display image and text
			image.setImageBitmap(bitmap);
			text.setText(imageNames[currentImagePosition].split("\\.")[0].toString().toLowerCase());
			*/
		}
	}
	

	static class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return imageNames.length < 6 ? imageNames.length : 6;
		}

		@Override
		public Object getItem(int position) {
			return imageNames[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.text_image_layout, null);
			}
			return convertView;
		}
	}
	

	// For the turn image animation
	private static void applyTurnRotation(int position, float start, float end, Context context) {
		final float centerX = currentView.getWidth() / 2.0f;
		final float centerY = currentView.getHeight() / 2.0f;

		// Create a new 3D rotation with the supplied parameter
		// The animation listener is used to trigger the next animation
		final TurnAnimation rotation = new TurnAnimation(start, end, centerX,
				centerY, 310.0f, true);
		rotation.setDuration(180);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new DisplayNextView(position, context));
		currentView.startAnimation(rotation);
	}

	/**
	 * This class listens for the end of the first half of the animation. It
	 * then posts a new action that effectively swaps the views when the
	 * container is rotated 90 degrees and thus invisible.
	 */
	private static final class DisplayNextView implements Animation.AnimationListener {
		private final int mPosition;
		private final Context mContext;
		
		private DisplayNextView(int position, Context context) {
			mPosition = position;
			mContext = context;
		}

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
			currentView.post(new SwapViews(mPosition, mContext));
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}

	/**
	 * This class is responsible for swapping the views and start the second
	 * half of the animation.
	 */
	@SuppressLint("DefaultLocale")
	private static final class SwapViews implements Runnable {
		private final int mPosition;
		private final Context mContext;

		public SwapViews(int position, Context context) {
			mPosition = position;
			mContext = context;
		}

		@SuppressLint("DefaultLocale")
		public void run() {
			ImageView image = (ImageView) currentView.findViewById(R.id.icon);
			TextView text = (TextView) currentView.findViewById(R.id.text);
			text.setVisibility(View.VISIBLE);

			image.setImageBitmap(imageMap.get(imageNames[mPosition]));
			text.setText(imageNames[mPosition].toLowerCase());

			final float centerX = currentView.getWidth() / 2.0f;
			final float centerY = currentView.getHeight() / 2.0f;
			TurnAnimation rotation;
			rotation = new TurnAnimation(180, 0, centerX, centerY, 310.0f,
					false);
			rotation.setDuration(180);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new DecelerateInterpolator());
			currentView.startAnimation(rotation);
		}
	}
	

	/* for speech recognition */
	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onBeginningOfSpeech()");
	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onEndOfSpeech()");
	}


	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		if (hypothesis != null) {
			synchronized (lock) {
				recognizedText = hypothesis.getHypstr().toLowerCase().trim();
				recognizer.stop();
				//update UI
				updateHandler.post(new Runnable() {
					@Override
					public void run() {
						String curWord = imageNames[currentWordIndex].toLowerCase().trim();
						//Log.v(PS_TAG, recognizedText+" vs. "+curWord+" :"+curWord.equals(recognizedText));
						ImageView crossImage = (ImageView) currentView.findViewById(R.id.cross);
						ImageView checkImage = (ImageView) currentView.findViewById(R.id.check);
						//if correct
						if (recognizedText.contains(curWord)) {
							scoreCurr++;
							scoreTview.setText("Score: " + scoreCurr);
							scoreTview.refreshDrawableState();
							//show check mark for correct utterance
							crossImage.setVisibility(View.INVISIBLE);
							checkImage.setVisibility(View.VISIBLE);
							checkImage.startAnimation(set);
						} else {
							//change game data
							//show check mark for correct utterance
							checkImage.setVisibility(View.INVISIBLE);
							crossImage.setVisibility(View.VISIBLE);
							crossImage.startAnimation(set);
							lifeCurr--;
							lifeTview.setText("Life: " + lifeCurr);
							lifeTview.refreshDrawableState();
							if (lifeCurr == 0) {
								mg.stopThread();
								Intent gameOverIntent = new Intent(GameActivity.this, GameOverActivity.class);
								startActivity(gameOverIntent);
								finish();									
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG, "onResult");
	}

	@Override
	public void onError(Exception arg0) {
		// TODO Auto-generated method stub
		recognizer.removeListener(this);
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		//Log.v(PS_TAG,"onTimeout()");
		recognizer.removeListener(this);
	}
	


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



/* ---------------------------------------------- DEPRECATED ---------------------------------------------------------------- 
// gw onClickEvent handler in OnCreate()

gw.setOnItemClickListener(new OnItemClickListener() {
	public void onItemClick(AdapterView<?> parent, final View v,
			int position, long id) {
		if (currentMolePos == position) {
			updateHandler.post(new Runnable() {
				@Override
				public void run() {
					if (isMole == true) {
						scoreCurr = scoreCurr + 1;
						scoreTview.setText("Score: " + scoreCurr);
						scoreTview.refreshDrawableState();

						// adaptive difficulty 
						if(scoreCurr==20){
							setProbability(0.60);
							mg.setTimeToWait(700);

						}
						else if(scoreCurr==50){
							setProbability(0.45);
							mg.setTimeToWait(500);
						}

					} else {
						lifeCurr = lifeCurr - 1;
						lifeTview.setText("Life: " + lifeCurr);
						lifeTview.refreshDrawableState();
						if (lifeCurr == 0) {
							mg.stopThread();

							Intent gameOverIntent = new Intent(
									Slt.this, GameOver.class);
							startActivity(gameOverIntent);
							finish();

						}
					}

				}
			});
		}
	}
});

 */
