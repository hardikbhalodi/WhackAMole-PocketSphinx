package com.welkinlan.whackamole;

import java.io.File;
import java.io.IOException;

import com.welkinlan.whackamole.R;

import edu.cmu.pocketsphinx.Assets;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;
import android.widget.Toast;

public class ScreenLoaderActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_loader);
		
		//app = ((Globals) getApplicationContext());
		
		new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(ScreenLoaderActivity.this);
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
			    	System.exit(0); 
                } else {
                    Intent goToMenu = new Intent(ScreenLoaderActivity.this, Menu.class);
            		startActivity(goToMenu);
                }
            }
        }.execute();
	}
	
	private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        Globals.recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();

        // Create keyword-activation search.
        File wordCorpus = new File(modelsDir, "words/mole.txt");
        Globals.recognizer.addKeywordSearch(Globals.wordSearch, wordCorpus);
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
