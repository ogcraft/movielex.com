/*
Amatch.java:
Copyright (c) 2014, Oleg Galbert
All rights reserved.
*/

package com.mooviefish.mooviefishapp;
import amatch_generated.amatch_interface;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.util.Log;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Amatch {
    private MFApplication gs; 
	private String TAG = "Amatch";
	private boolean isEngineInitialized = false;
	private boolean isRecordedEnough = false;
	
    private static double SEC_PER_KEY = 0.011609977324263039;
	
    private double found_sec = 0;
	private double currentPlayingTime_ms = 0;
	private double finalTime_ms = 0;
	private long recording_start_ms = 0;
	private long recording_end_ms = 0;
	
    // Media Player
    private  MediaPlayer mp;
    
    Thread load_fpkeys_thread;
	Thread match_thread;
	Thread player_thread;
	Thread recorder_thread;
	
    public Amatch(MFApplication app) {
        gs = app;
        TAG = gs.getTAG();
        Log.d(TAG,"Amatch() constructor");
        mp = new MediaPlayer();
    }   
       
    public void Release(){
    	Log.d(TAG, "Amatch.Release()");
    	recorder_thread.interrupt();
    	isEngineInitialized = false;
    	mp.stop();
    	load_fpkeys_thread = null;
    }
}
