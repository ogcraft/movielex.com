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

import android.os.Handler;
import android.os.Message;
import android.os.PowerManager; 
import android.os.SystemClock;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
//import android.media.MediaPlayer.OnCompletionListener; 
//import android.media.MediaPlayer.OnErrorListener; 
//import android.media.MediaPlayer.OnPreparedListener;
//import android.media.MediaPlayer.OnSeekCompleteListener;
//import android.media.MediaPlayer;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.AutomaticGainControl;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.MediaList;


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
	private static Amatch instance;
    private MFApplication gs; 
	private String TAG = "Amatch";

    //////// Handlers //////////
    
    Handler found_display_view_handler = null;
    //Handler seekbar_handler = null;
    //Handler progress_display_view_handler = null;
    
    ////////////////////////////
    public boolean isFpkeysLoaded = false;
    public boolean isMatching = false;
    public boolean isMediaPlayerReady = false;
    public boolean isMediaPlayerPlaying = false;
    
    public Context mContext = null;
    private String data_root_path = "";
    private String track_keys_fn = data_root_path;
    private String translation_fn = data_root_path;
    //private static double SEC_PER_KEY = 0.011609977324263039;
    private static int testcount1 = 0;
    private double found_sec = 0;
    private int currentPlayingTime_ms = 0;
    private int translationMaxDuration_ms = 0;
    private long recording_start_ms = 0;
    private long recording_end_ms = 0;
    private long matching_start_ms = 0;
    private long matching_end_ms = 0;
    private long seek_start_ms = 0;
    private long seek_end_ms = 0;
    private long prepare_start_ms = 0;
    private long prepare_end_ms = 0;

    LibVLC mLibVLC;
    //private MediaPlayer mp = null;
    private RecorderThread recorder_thread;
    
    Handler player_thread_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {              
                Bundle bundle = msg.getData();
                int i = bundle.getInt("FoundIndex");
                long time_to_match_ms = bundle.getLong("time_to_match_ms");
                Log.d(TAG,"FoundIndex: n = " + i);
                
                isMatching = false;

                matching_end_ms = System.currentTimeMillis();

                found_sec = i * amatch_interface.SEC_PER_KEY;
                Log.d(TAG,"found_sec: " + found_sec);
                
                // add delay from algorithm
                //found_sec = (amatch_interface.delay_per_sec()+1) * found_sec;
                //found_sec = found_sec + amatch_interface.num_sec_to_record();

                Log.d(TAG,"Starting playing from " + found_sec + " sec");
                
                recording_end_ms = System.currentTimeMillis();
                long recording_time_ms = recording_end_ms - recording_start_ms;

                Log.d(TAG,String.format("**** recording_time_ms: %d (%d - %d)", 
                                    recording_time_ms, recording_end_ms, recording_start_ms));
                long calculated_ms = -1;
                i = 100;
                if( i > 10 ) {
                    calculated_ms = 25795; //(long)found_sec*1000 + recording_time_ms + 500; 
                }
                if( i > 10 && 
                    (calculated_ms < translationMaxDuration_ms)) {
                    play_translation(translation_fn, (long) calculated_ms);
                } else {
                    play_translation(translation_fn, (long) calculated_ms);
                    //play_recorded();
                }
                Message msg1 = found_display_view_handler.obtainMessage();
                Bundle bundle1 = new Bundle();
                bundle1.putLong("found_ms", calculated_ms);
                bundle1.putLong("search_duration_ms", recording_time_ms);
                msg1.setData(bundle1);
                found_display_view_handler.sendMessage(msg1);
            }
        }; 

	public static Amatch initInstance(MFApplication app, Context context) {
		if (instance == null) {
			instance = new Amatch(app, context);
		}
		return instance;
	}

    private Amatch(MFApplication app, Context context) {
        gs = app;
        mContext = context;
        TAG = gs.getTAG();
        Log.d(TAG,"Amatch() constructor");
    }   
       
    public void Release(){
    	Log.d(TAG, "Amatch.Release()");
    	mLibVLC.stop();
        if(recorder_thread != null){
            recorder_thread.finish();
            recorder_thread = null;
        }
    }
    
    public long load_fpkeys(String fn) {
        long n = amatch_interface.read_track_fpkeys(fn);
        if(n>10) {
            isFpkeysLoaded = true;
        }
        Log.d(TAG,"Amatch.load_fpkeys(): Loaded " + n + " fpkeys");
        return n;
    }

void  createMediaPlayerForTranslation(String translation_fn) {
        Log.d(TAG,"Amatch.createMediaPlayerForTranslation: " + translation_fn); 
        isMediaPlayerPlaying = false;
        if (mLibVLC == null) { 
            Log.d(TAG,"Amatch.createMediaPlayerForTranslation: Initializing mLibVLC");
            try {
                mLibVLC = LibVLC.getInstance();
                mLibVLC.init(mContext);
            } catch(LibVlcException e) {
                Toast.makeText(mContext,
                    "Error initializing the libVLC multimedia framework!",
                    Toast.LENGTH_LONG).show();
                return;
            }
        } 
        else  
        {   Log.d(TAG,"Amatch.createMediaPlayerForTranslation: Reseting mLibVLC");
            isMediaPlayerReady = false;
            mLibVLC.restart(mContext); 
        }
        //mLibVLC.getMediaList().insert(0, LibVLC.PathToURI(translation_fn));
        //mLibVLC.playIndex(0);
        //translationMaxDuration_ms = (int) mLibVLC.getTime();
        //Log.d(TAG,String.format("createMediaPlayerForTranslation(): Max Duration: ",translationMaxDuration_ms));
        isMediaPlayerReady = true; 

    }

//    @Override
//    public void onSeekComplete(MediaPlayer mp) {
//        seek_end_ms = System.currentTimeMillis();
//        Log.d(TAG, String.format("onSeekComplete() seek to: %d took: %d ms", 
//            mp.getCurrentPosition(), seek_end_ms - seek_start_ms));
//        Log.d(TAG, String.format("From search_start_ms till seek_end ms: %d", seek_end_ms-matching_start_ms));
//        mp.setVolume(1.0f, 1.0f); // we can be loud 
//        isMediaPlayerPlaying = true;
//        mp.start();
//        translationMaxDuration_ms = mp.getDuration();
//        Log.d(TAG,String.format("onSeekComplete() translationMaxDuration_ms: %d", translationMaxDuration_ms));
//        
//    }

    public long getTranslationMaxDuration() {
        return translationMaxDuration_ms;
    }

    public void  play_translation(String fn, long from_ms){
        // Play translation
        try {
            Log.d(TAG,String.format("play_translation from %d ms", from_ms));
            //if(from_ms >= translationMaxDuration_ms)
            //{
            //    Log.d(TAG, String.format("play_translation(), requested time %d is more than Max Duration: %d",
            //        from_ms, translationMaxDuration_ms));
            //    Toast.makeText(gs.getApplicationContext(), "Translation position out of bounds", Toast.LENGTH_SHORT).show();
            //}
            
            // Move song to particular second
            //mLibVLC.playMRL(fn);
            seek_start_ms = System.currentTimeMillis();

            //mLibVLC.getPrimaryMediaList().insert(0, LibVLC.PathToURI(fn));
            //mLibVLC.playIndex(0);
            
            mLibVLC.playMRL(LibVLC.PathToURI(fn));
            
            translationMaxDuration_ms = (int) mLibVLC.getLength();
            Log.d(TAG,String.format("play_translation(): Max Duration: %d",translationMaxDuration_ms));

            //Log.d(TAG, "play_translation(): Seeking to " + from_ms);
            //mLibVLC.setTime(from_ms); // position in milliseconds
            Log.d(TAG,"After seek getTime: " + mLibVLC.getTime());
            
            mLibVLC.play();
            
            Log.d(TAG,"mLibVLC.play()");

            isMediaPlayerPlaying = mLibVLC.isPlaying();
            Log.d(TAG,"play_translation(): isMediaPlayerPlaying : " + isMediaPlayerPlaying);
        
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stop_plaing_translation() {
        if(isMediaPlayerPlaying) 
        {
            isMediaPlayerPlaying = false;
            mLibVLC.pause();
        }
    }

    public long getCurrentTranslationPosition() {
        return mLibVLC.getTime();
    }

    public void  play_recorded(){
        Log.d(TAG,"Amatch.play_recorded()");
        //try
        //{
            int sz = amatch_interface.get_recorded_samples_size();
            Log.d(TAG,"sz: " + sz);
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, amatch_interface.get_sample_rate(), 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 4096, AudioTrack.MODE_STREAM);
            
            audioTrack.play();
            
            float recs[] = new float[sz];
            
            amatch_interface.get_recorded_samples(recs);
            
            short samples[] = new short[sz];
            Log.d(TAG, "-----------------\n");
            for( int i = 0; i < sz; i++ ) {
                samples[i] = (short)(recs[i]*Short.MAX_VALUE);
            }

            audioTrack.write(samples, 0, sz);
            
            audioTrack.stop();
            audioTrack.release();
        //}
        //catch (IOException e)
        //{
           
        //}

    }

    public void do_matching() {
        Log.d(TAG,"Start searching");
        matching_start_ms = System.currentTimeMillis();
        testcount1 = testcount1 + 1;
        //int found_index = 86 * 1000 * (testcount1 % 5); //match_sample();
        int found_index = match_sample();
        long index_found_ms = System.currentTimeMillis();
        long time_to_match_ms = index_found_ms - matching_start_ms; 
        Log.d(TAG,"found_index: " + found_index + " ms took: " + time_to_match_ms);
        Message msg = player_thread_handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt("FoundIndex", found_index);
        bundle.putLong("time_to_match_ms", time_to_match_ms);
        msg.setData(bundle);
        player_thread_handler.sendMessage(msg);
    }

    private int match_sample() {
        Log.d(TAG,"Generating FPKEYS from recorded...");         
        amatch_interface.generate_fp_keys_from_in();
        Log.d(TAG,"START MATCHING...");
        int found_index = amatch_interface.match_sample();
        return found_index;
    
    }

    public void start_recording_thread() {  
        isMatching = true; 
        recorder_thread = new RecorderThread();
        recorder_thread.start();
    }

    //////////////////////////////////// Recorder RecorderThread ////////////////////
    public class RecorderThread extends Thread {
            AudioRecord record;
            int SR = amatch_interface.SR;
            int minBytes;
            long baseTimeMs;
            boolean isRunning = false;
            boolean isPaused1 = false;
           
            public RecorderThread() {
                prepare_to_record();
            }
            
            public void prepare_to_record() {
                isRunning = true;
                minBytes = AudioRecord.getMinBufferSize(SR /*sampleRate*/, 
                      AudioFormat.CHANNEL_IN_MONO,
                      AudioFormat.ENCODING_PCM_16BIT);
                Log.d(TAG, " Recomended min buffer: " + minBytes); 
                record =  new AudioRecord( AudioSource.MIC,SR,
                  AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,  minBytes*10);
                Log.d(TAG, "Buffer size: " + minBytes + " (" + record.getSampleRate() + "=" + SR + ")");
            }
            
            public void do_recording_fixed(int nsamples) {
                amatch_interface.clear_recorded_samples();
                recording_start_ms = System.currentTimeMillis();
                short[] audioSamples = new short[minBytes];
                Log.d(TAG,"Start recording by AudioTrack");
                record.startRecording();

                while(!Thread.currentThread().isInterrupted()){
                    int b = record.read(audioSamples,0, minBytes);
                    amatch_interface.put_recorded_samples(audioSamples, b);
                    if(amatch_interface.get_recorded_samples_size() >= nsamples) {
                        break;
                    }
                }
                Log.i(TAG, "Releasing Audio");
                record.stop();
                record.release();
                record = null;
                //start_matching_thread();
                do_matching();
            }

            @Override
            public void run() {
                do_recording_fixed(amatch_interface.num_samples_to_record());    
            }
            public void finish() {
              isRunning=false;
              interrupt();
            }
     };
}
