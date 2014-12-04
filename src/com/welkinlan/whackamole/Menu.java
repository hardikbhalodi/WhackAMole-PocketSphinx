package com.welkinlan.whackamole;



import com.welkinlan.whackamole.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Menu extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button StartGameButton = (Button)findViewById(R.id.StartGame);
        StartGameButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		Intent StartGameIntent = new Intent(Menu.this,GameActivity.class);
        		startActivity(StartGameIntent);
        		finish();
        	
        	}
        });
        
        Button HelpButton = (Button)findViewById(R.id.Help);
        HelpButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		Intent HelpIntent = new Intent(Menu.this,Help.class);
        		startActivity(HelpIntent);
        	}
        });
      
        
        Button CreditsButton = (Button)findViewById(R.id.Credits);
        CreditsButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		Intent CreditsIntent= new Intent(Menu.this,Credits.class);
        		startActivity(CreditsIntent);
        	}
        });
    }
    
    public Activity getActivity(){
    	
    	return this.getActivity();
    }
}