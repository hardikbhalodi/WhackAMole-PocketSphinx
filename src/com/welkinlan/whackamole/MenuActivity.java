package com.welkinlan.whackamole;
import java.io.File;
import java.util.List;

import com.welkinlan.util.FileHelper;
import com.welkinlan.util.Globals;
import com.welkinlan.whackamole.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class MenuActivity extends Activity {
    File file;
    File[] files;
    Button startGameButton;
    ListView fileListView;
    
    private TextView selectedTextView  = null;
    private RadioButton selectedRadioButton  = null;
    int selectedIndex = -1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startGameButton = (Button)findViewById(R.id.start);
        startGameButton.setEnabled(false);
        fileListView = (ListView)findViewById(R.id.file_list);

        //get all files
        FileHelper fh = new FileHelper();
        files = fh.getAllFiles();
        final String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }
        
        if (fileNames.length == 0) {
        	TextView helper = (TextView)findViewById(R.id.helper);
        	helper.setVisibility(View.VISIBLE);
        } else {
            ListAdapter listAdapter = new ListAdapter(MenuActivity.this , R.layout.file_list_item, fileNames);
            fileListView.setAdapter(listAdapter);
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

    public void onClickListItem(View v) {
        int newIndex = ((ViewGroup) v.getParent()).indexOfChild(v);
        if (newIndex != selectedIndex) {
        	if (selectedIndex != -1) {
        		// uncheck previous checked button. 
                selectedTextView.setBackgroundColor(0x00ffffff); //set transparent bg
                selectedRadioButton.setChecked(false);
        	}
        	
        	selectedIndex = newIndex;
    		
        	// select current
            selectedTextView = (TextView) v.findViewById(R.id.textView);
            selectedTextView.setBackgroundColor(0xff009900);//set green bg
            selectedRadioButton = (RadioButton) v.findViewById(R.id.radioButton);
            selectedRadioButton.setChecked(true);
            
            //update file
            file = files[selectedIndex];
            Globals.file = file;
            startGameButton.setEnabled(true);
        } 
    }

    private class ListAdapter extends BaseAdapter {
        private Context mContext;
        private int id;
        private String[] items ;

        public ListAdapter(Context context, int textViewResourceId, String[] list) 
        {     
            mContext = context;
            id = textViewResourceId;
            items = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View mView = convertView;
            
            if(mView == null){
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = vi.inflate(id, null);
            }

            TextView text = (TextView) mView.findViewById(R.id.textView);

            if(items[position] != null )
            {
                text.setTextColor(Color.WHITE);
                text.setText(items[position]);
            }

            return mView;
        }

    }
}