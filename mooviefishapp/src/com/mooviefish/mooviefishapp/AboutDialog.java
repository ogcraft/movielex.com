package com.mooviefish.mooviefishapp;
import com.mooviefish.mooviefishapp.MFApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog extends Dialog{

	private static Context mContext = null;
	public MFApplication gs;
	
	public AboutDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about);
		
		String info_text = String.format( 
			"<h3>MoovieFish Application</h3>App ver: %s<br>Amatch ver: %s<br><br>Copyright 2014<br><b>www.mooviefish.com</b><br><br>",
			gs.appVersion, gs.amatchVersion);

		TextView tv = (TextView)findViewById(R.id.legal_text);
		tv.setText(readRawTextFile(R.raw.legal));
		tv = (TextView)findViewById(R.id.info_text);
		//tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));
		tv.setText(Html.fromHtml(info_text));
		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
	}
	
	public static String readRawTextFile(int id) {
		InputStream inputStream = mContext.getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try {
        	while (( line = buf.readLine()) != null) text.append(line);
         } catch (IOException e) {
            return null;
         }
         return text.toString();
     }

}
