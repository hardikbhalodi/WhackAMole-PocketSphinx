package com.welkinlan.whackamole;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;

import com.welkinlan.whackamole.R;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity implements RecognitionListener {
	private static final String PS_TAG = "PocketSphinx";
	
	private static SpeechRecognizer recognizer;
	private static final String WORD_SEARCH = "words";
	//game
	static private GraphicsMole im;
	static private int currentMolePos = -1;
	private int scoreCurr = 0;
	private int lifeCurr = 5;
	private Handler step;
	private Handler Update;
	private MoleGame mg;
	static private boolean isMole = true;
	static private Mole mole = new Mole();
	static private double prob=0.7;
    //UI
	TextView scoreTview;
	TextView lifeTview;
	GridView gw;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_screen_loader);
		
		new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
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
                	Intent backToMenu = new Intent(GameActivity.this, Menu.class);
            		startActivity(backToMenu);
                } else { 	
            		startGame();
                }
            }
        }.execute();
		
	}

	private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);
        // Create grammar-based searches.
        File wordsGrammar = new File(modelsDir, "grammar/words.gram");
        recognizer.addGrammarSearch(WORD_SEARCH, wordsGrammar);
    }
	
	private void startGame(){
		//initialize recognizer
        recognizer.startListening(WORD_SEARCH);
        Log.v("text", "start");
		// set up UI
        setContentView(R.layout.startgame);	
        scoreTview = (TextView) findViewById(R.id.Score);
		lifeTview = (TextView) findViewById(R.id.Life);
		gw = (GridView) findViewById(R.id.gridview);
		//set up game
		step = new ChangeImage();
		Update = new Handler();
		im = new GraphicsMole(this);
		gw.setAdapter(im);
		
		mg = new MoleGame(step);
		mg.start();
	}
	
	public Activity getActivity(){
		return this.getActivity();
	}

	// ---------------------------
	private static class ChangeImage extends Handler {
		
		Hole hole = new Hole();
		private int oldPosition = -1;

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			
			currentMolePos = bundle.getInt("newPosition");
			if (Math.random() < prob) {
				im.setItem(currentMolePos, mole.getMole());
				isMole = true;
			} else {
				im.setItem(currentMolePos, mole.getButterfly());
				isMole = false;
			}

			if (oldPosition != -1 && currentMolePos != oldPosition) {
				im.setItem(oldPosition, hole.getHole());
			}

			oldPosition = currentMolePos;
			im.notifyDataSetChanged();
			recognizer.startListening(WORD_SEARCH);
		}
	}
	
	public void setProbability(double prob){
		this.prob = prob;
	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		Log.v(PS_TAG, "onBeginningOfSpeech()");
	}

	/* for speech recognition */
	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		Log.v(PS_TAG, "onEndOfSpeech()");
		recognizer.stop();
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		Log.v(PS_TAG, "onPartialResult()");
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		Log.v(PS_TAG, "onResult");
		if (hypothesis != null) {
			final String text = hypothesis.getHypstr();
			Log.v(PS_TAG, text);
			Update.post(new Runnable() {
				@Override
				public void run() {
					if (isMole == true) {
						scoreCurr = scoreCurr + 1;
						scoreTview.setText("Score: " + scoreCurr);
						scoreTview.refreshDrawableState();
						
					} else {
						lifeCurr = lifeCurr - 1;
						lifeTview.setText("Life: " + lifeCurr);
						lifeTview.refreshDrawableState();
						if (lifeCurr == 0) {
							mg.stopThread();
							recognizer.stop();
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



/* ---------------------------------------------- DEPRECATED ---------------------------------------------------------------- 
// gw onClickEvent handler in OnCreate()

gw.setOnItemClickListener(new OnItemClickListener() {
	public void onItemClick(AdapterView<?> parent, final View v,
			int position, long id) {
		if (currentMolePos == position) {
			Update.post(new Runnable() {
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
