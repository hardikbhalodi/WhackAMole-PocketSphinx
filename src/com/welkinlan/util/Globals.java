package com.welkinlan.util;

import java.io.File;

import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.app.Application;

public class Globals extends Application
{
	static SpeechRecognizer recognizer;
	static String wordSearch = "word";
	public static File file;
	
}
