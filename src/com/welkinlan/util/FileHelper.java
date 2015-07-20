package com.welkinlan.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.UserDictionary.Words;
import android.util.Log;
import android.widget.ListView;

public class FileHelper {
	private final static String FH_TAG = "FileHelper";
	
	private File fileDir;
	private final String dirName = "/Exercises/";
	public FileHelper() {
		setUpFolder();
	}

	public void setUpFolder() {
		fileDir = new File(Environment.getExternalStorageDirectory()+ dirName);
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
		} 
		else {
			return;
		}
	}

	public File[] getAllFiles(){
		return fileDir.listFiles();
	}

	public static HashMap<String, Bitmap> getImages(File file, Context context){
		HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));  
			String imageName;   
			while ((imageName = br.readLine()) != null) {
				//change first letter to capital to match image names
				StringBuilder sb = new StringBuilder(imageName.toLowerCase().trim());
				char c = sb.charAt(0);
				sb.setCharAt(0, Character.toUpperCase(c));
				imageName = sb.toString().trim();
				Log.v(FH_TAG, imageName);
				//read image
				InputStream in = null;
				try {		
					in = context.getAssets().open("img/"+imageName+".png");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				final Bitmap bitmap = BitmapFactory.decodeStream(in);
				imageMap.put(imageName, bitmap);
			}
			br.close() ;
		}catch (IOException e) {
			e.printStackTrace();           
		}
		return imageMap;
		
	}
	
}
