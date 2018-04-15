package com.bignerdranch.android.androidmiccw;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.iid.InstanceID;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.*;
import java.util.concurrent.TimeUnit;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static java.lang.Math.log10;
import static java.lang.Math.round;

/**
 * App's main activity contains buttons for setting sampling frequency and duration of recordings
 * and start/stop button.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_SIGN_IN = 42;
    static final int RESULT_FAIL = 0;
    static final int RESULT_SUCCESS = 1;
    private final int REQUEST_PERMISSION_CODE = 31;

    private Button sampleBut;
    private SeekBar frequency;
    private SeekBar duration;
    private MediaRecorder recorder = null;
    private TextView readingTV;
    private TextView timeTV;
    private TextView latitude;
    private TextView longitude;
    private TextView frequencyTV;
    private String mFileName = null;
    private int sampleKickoffFrequency = 1;
    private int sampleDuration = 1;
    private List<Sample> sampleArray = new ArrayList<>();
    private Sample sample;
    private String uid;
    private Location mLocation;
    private boolean isContinuouslyRecording = false;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    // Requesting permission to RECORD_AUDIO and access location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    // Authentication item in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivityForResult(intent, REQUEST_SIGN_IN);
        return true;
    }

    // Result of AuthActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGN_IN && resultCode == RESULT_SUCCESS) {
            uid = data.getStringExtra("User ID");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.my_toolbar));
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);

        readingTV = findViewById(R.id.reading_units);
        timeTV = findViewById(R.id.sampling_duration_units);
        frequencyTV = findViewById(R.id.sampling_frequency_units);
        latitude = findViewById(R.id.lat_units);
        longitude = findViewById(R.id.lon_units);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audio31.3gp";

        sampleBut = findViewById(R.id.sample_but);
        sampleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isContinuouslyRecording) {
                    Log.i(TAG, "Clicked stop");
                    stopSampling();
                } else {
                    Log.i(TAG, "Clicked start");
                    sampleBut.setText(R.string.stop_sampling);
                    duration.setClickable(false);
                    frequency.setClickable(false);
                    startSampling();
                }
            }
        });

        // Two time-related variables:
        // sampleDuration (how long a recording is taken for)
        // sampleKickoffFrequency (time between start times of consecutive samples)
        duration = findViewById(R.id.duration);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i==0) i=1;
                sampleDuration = i;
                timeTV.setText(sampleDuration +"s");
                if (sampleKickoffFrequency < sampleDuration)
                    duration.setProgress(sampleKickoffFrequency);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        frequency = findViewById(R.id.freq);
        frequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i==0) i=1;
                sampleKickoffFrequency = i;
                frequencyTV.setText(sampleKickoffFrequency +"s");
                if (sampleKickoffFrequency < sampleDuration){
                    duration.setProgress(sampleKickoffFrequency);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Starts a sampling session of one or more samples.
     */
    private void startSampling() {
        Log.i(TAG, "startSampling");
        isContinuouslyRecording = true;
        sampleBut.setText(R.string.stop_sampling);
        readingTV.setText(".....");
        startSample();
    }

    /**
     * Starts a sample recording.
     */
    private void startSample() {
        Log.i(TAG, "startSample");
        //instantiate a new sample
        sample = new Sample();
        sample.setIid(InstanceID.getInstance(this).getId());
        sample.setUid(uid);
        initialiseRecorder();
        Log.i(TAG, "Recorder initialised with " + sampleDuration + " s max duration of recording ");
        try {
            recorder.prepare();
            if (mLocation != null) {
                sample.setLatitude(mLocation.getLatitude());
                sample.setLongitude(mLocation.getLongitude());
            }
            sample.setStartTime(new Date().getTime());
            recorder.start();
            recorder.getMaxAmplitude();
            Log.i(TAG, "recorder started");
        } catch (IOException e) {
            Log.i(TAG, "prepare() failed");
        }
    }

    /**
     * Called when the recording duration has elapsed for a single sample.
     * Queues up the next sample if isContinuouslyRecording is true.
     */
    private void onSampleComplete() {
        recorder.stop();
        double amplitude = recorder.getMaxAmplitude();
        Log.i(TAG, "onSampleComplete, recorded amplitude" + amplitude);
        double doublevalue = (20 * log10((0.00002 + (amplitude * ((0.6325-0.00002) / 32767.0)))/0.00002));
        Log.i(TAG, "onSampleComplete, conversion to double" + doublevalue);
        int value = (int) round(doublevalue);
        Log.i(TAG, "onSampleComplete, conversion to decibels" + value);
        recorder.reset();
        recorder.release();
        recorder = null;
        long stopTime = new Date().getTime();
        sample.setStopTime(stopTime);
        sample.setMaxDecibels(value);
        readingTV.setText(""+value+"dB");
        sampleArray.add(sample);
        if (isContinuouslyRecording) {
            //Since the callback is on the UI thread, using an AsyncTask to sleep on a background thread
            (new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    if (sampleKickoffFrequency > sampleDuration) {
                        try {
                            Log.i(TAG, "Waiting " + (sampleKickoffFrequency - sampleDuration) + " seconds");
                            TimeUnit.SECONDS.sleep((sampleKickoffFrequency - sampleDuration));
                        } catch (InterruptedException e) {
                            Log.d("Interrupted Exception", "Was sleeping until next recording");
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (isContinuouslyRecording) {
                        startSample();
                    }
                }
            }).execute();
        }
    }

    /**
     * Stops sampling session.
     * If a sample is in progress when this is called, the recorder is reset and sample discarded.
     */
    private void stopSampling() {
        Log.i(TAG, "stopSampling");
        isContinuouslyRecording = false;
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sync");
        for (Sample s: sampleArray) {
            myRef.child(s.getIid()).child(String.valueOf(s.getStartTime())).setValue(s);
        }
        Log.i("How many times","****** "+sampleArray.size());
        duration.setClickable(true);
        frequency.setClickable(true);
        sampleBut.setText(R.string.start_sampling);
    }

    private void initialiseRecorder() {
        Log.i(TAG, "initialiseRecorder");
        if (recorder == null){
            recorder = new MediaRecorder();
            recorder.setMaxDuration(sampleDuration*1000);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            try{
                recorder.setOutputFile(mFileName);
            } catch (IllegalStateException e){
                Log.i("Exception caught","File already set");
            }
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            Log.i(TAG,"recorder about to setOnInfoListener");
            recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        onSampleComplete();
                        Log.i(TAG,"recorder sample done, max dB:" + sample.getMaxDecibels());
                    }
                }
            });
        }
    }

    /**
     * Updates the latitude and longitude displayed on screen.
     * Sets the latitude and longitude for any sample recording.
     */
    public void update(Location location) {
        mLocation = location;
        if (location != null) {
            latitude.setText("" + location.getLatitude());
            longitude.setText("" + location.getLongitude());
        }
    }
}