package com.welkinlan.whackamole;
import java.io.File;

import com.welkinlan.util.FileHelper;
import com.welkinlan.util.Globals;
import com.welkinlan.whackamole.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MenuActivity extends Activity {
	File file;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		final Button startGameButton = (Button)findViewById(R.id.start_game);
		startGameButton.setEnabled(false);
		final ListView fileListView = (ListView)findViewById(R.id.file_list);
		
		//get all files
		FileHelper fh = new FileHelper();
		final File[] files = fh.getAllFiles();
		final String[] fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getName();
		}
		
		fileListView.setAdapter(new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_list_item_single_choice,fileNames));
		fileListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		fileListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				file = files[pos];
				Globals.file = file;
				startGameButton.setEnabled(true);
			}
		});
		
		startGameButton.setOnClickListener(new OnClickListener() {   	
			public void onClick(View v) {
				Intent startGameIntent = new Intent(MenuActivity.this,GameActivity.class);
				startGameIntent.putExtra("file", file); 
				startActivity(startGameIntent);
				finish();
			}
		});

	}
}