package com.welkinlan.whackamole;

import com.welkinlan.whackamole.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameOverActivity extends Activity {
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameover);    
        Button RestartGameButton = (Button)findViewById(R.id.restart);
        RestartGameButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent RestartGameIntent = new Intent(GameOverActivity.this,GameActivity.class);
        		startActivity(RestartGameIntent);
        	}
        });
        
        Button QuitButton = (Button)findViewById(R.id.quit);
        QuitButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		finish();  
		    	System.exit(0); 
        	}
        });
              
    }
    
}