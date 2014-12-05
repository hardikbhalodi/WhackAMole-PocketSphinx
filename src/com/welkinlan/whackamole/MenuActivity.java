package com.welkinlan.whackamole;



import com.welkinlan.whackamole.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MenuActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button StartGameButton = (Button)findViewById(R.id.StartGame);
        StartGameButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		Intent StartGameIntent = new Intent(MenuActivity.this,GameActivity.class);
        		startActivity(StartGameIntent);
        		finish();
        	
        	}
        });
        
    }
    
    public Activity getActivity(){
    	
    	return this.getActivity();
    }
}