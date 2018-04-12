package com.bignerdranch.android.androidmiccw;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
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

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static java.lang.Math.log10;
import static java.lang.Math.round;


public class MainActivity extends AppCompatActivity {
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
    private int samplingTime = 1;
    private int totalTime = 1;
    private int howManyTimes;
    private List<Integer> soundArray = new ArrayList<>();
    private List<Sample> sampleArray = new ArrayList<>();
    private Sample sample;
    private String uid;
    private Location mLocation;

    // Requesting permission to RECORD_AUDIO and access location
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToAccessFineLocation = false;
    private boolean permissionToAccessCoarseLocation = false;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToAccessFineLocation  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                permissionToAccessCoarseLocation  = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
        initialiseRecorder();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivityForResult(intent, REQUEST_SIGN_IN);
        return true;
    }

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

        readingTV = findViewById(R.id.reading);
        timeTV = findViewById(R.id.timeDuration);
        frequencyTV = findViewById(R.id.timeFrequency);
        latitude = findViewById(R.id.lat);
        longitude = findViewById(R.id.lon);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audio31.3gp";

        sampleBut = findViewById(R.id.sample);
        sampleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sampleBut.setClickable(false);
                duration.setClickable(false);
                frequency.setClickable(false);
                soundArray.clear();
                howManyTimes = totalTime / samplingTime;
                if (howManyTimes == 0) howManyTimes = 1;
                update(mLocation);
                startSampling();
            }
        });
        duration = findViewById(R.id.duration);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i==0) i=1;
                totalTime = i;
                timeTV.setText(totalTime+"s");
                if (samplingTime<totalTime)
                    duration.setProgress(samplingTime);
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
                samplingTime = i;
                frequencyTV.setText(samplingTime+"s");
                if (samplingTime<totalTime){
                    duration.setProgress(samplingTime);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startSampling(){
        //instantiate a new sample
        sample = new Sample();
        sample.setIid(InstanceID.getInstance(this).getId());
        sample.setUid(uid);
        howManyTimes--;
        initialiseRecorder();
        try {
            recorder.prepare();
            sampleBut.setText(R.string.Sampling);
            readingTV.setText(".....");
            recorder.start();
            sample.setStartTime(new Date().getTime());
            if (mLocation != null) {
                sample.setLatitude(mLocation.getLatitude());
                sample.setLongitude(mLocation.getLongitude());
            }
            recorder.getMaxAmplitude();
        } catch (IOException e) {
            Log.i("startSampling: ", "prepare() failed");
        } catch (IllegalStateException e){
            Log.i("startSampling: ", "prepare() failed");
        }
    }

    private void stopSampling(){
        int value = (int) round(20 * log10(recorder.getMaxAmplitude() / 32767.0));
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        sample.setStopTime(new Date().getTime());
        sampleBut.setText(R.string.startSampling);
        soundArray.add(value);
        sample.setMaxDecibels(value);
        int display=-500;
        for (int j: soundArray){
            if (j>display)display = j;
        }
        readingTV.setText(""+display+"dB");
        sampleArray.add(sample);
        if (howManyTimes > 0) {
            startSampling();
        } else {
            recordSQL();
        }
    }

    //triggered upon stopping sampling
    private void recordSQL(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sync");
        for (Sample s: sampleArray) {
            myRef.child(s.getIid()).child(String.valueOf(s.getStartTime())).setValue(s);
        }
        Log.i("How many times","****** "+howManyTimes );
        sampleBut.setClickable(true);
        duration.setClickable(true);
        frequency.setClickable(true);
    }

    private void initialiseRecorder(){
        if (recorder == null){
            recorder = new MediaRecorder();
            recorder.setMaxDuration(samplingTime*1000);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            try{
                recorder.setOutputFile(mFileName);
            } catch (IllegalStateException e){
                Log.i("Exception caught","File already set");
            }
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what==MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                        stopSampling();
                    }
                }
            });
        }
    }

    public void update(Location location) {
        mLocation = location;
        latitude.setText(""+location.getLatitude());
        longitude.setText(""+location.getLongitude());
    }
}