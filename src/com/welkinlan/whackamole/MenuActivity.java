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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startGameButton = (Button)findViewById(R.id.start);
        startGameButton.setEnabled(false);
        final ListView fileListView = (ListView)findViewById(R.id.file_list);

        //get all files
        FileHelper fh = new FileHelper();
        files = fh.getAllFiles();
        final String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }

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

    private RadioButton listRadioButton = null;
    int listIndex = -1;

    public void onClickRadioButton(View v) {
        View vMain = ((View) v.getParent());
        // getParent() must be added 'n' times, 
        // where 'n' is the number of RadioButtons' nested parents
        // in your case is one.

        // uncheck previous checked button. 
        if (listRadioButton != null) listRadioButton.setChecked(false);
        // assign to the variable the new one
        listRadioButton = (RadioButton) v;
        // find if the new one is checked or not, and set "listIndex"
        if (listRadioButton.isChecked()) {
            listIndex = ((ViewGroup) vMain.getParent()).indexOfChild(vMain);
            file = files[listIndex];
            Globals.file = file;
            startGameButton.setEnabled(true);
        } else {
            listRadioButton = null;
            listIndex = -1;
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