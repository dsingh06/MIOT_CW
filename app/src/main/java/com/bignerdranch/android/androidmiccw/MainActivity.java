package com.bignerdranch.android.androidmiccw;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static java.lang.Math.log10;
import static java.lang.Math.round;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 31;
    private Button sampleBut;
    private SeekBar frequency;
    private SeekBar duration;
    private MediaRecorder recorder = null;
    private TextView readingTV;
    private TextView timeTV;
    private TextView frequencyTV;
    private String mFileName = null;
    private int samplingTime = 1;
    private int totalTime = 1;
    private int howManyTimes;
    private List<Integer> samplesArray = new ArrayList<>();

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        readingTV = findViewById(R.id.reading);
        timeTV = findViewById(R.id.timeDuration);
        frequencyTV = findViewById(R.id.timeFrequency);


        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audio31.3gp";

        sampleBut = findViewById(R.id.sample);
        sampleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                samplesArray.clear();
                howManyTimes = totalTime / samplingTime;
                if (howManyTimes==0) howManyTimes=1;
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
                if (samplingTime>totalTime) duration.setProgress(samplingTime);
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
                if (samplingTime>totalTime){
                    duration.setProgress(samplingTime);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        initialiseRecorder();

    }


    private void startSampling(){
        howManyTimes--;
        initialiseRecorder();
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.i("startSampling: ", "prepare() failed");
        }
        sampleBut.setText(R.string.Sampling);
        readingTV.setText(".....");
        recorder.start();
        recorder.getMaxAmplitude();
    }

    private void stopSampling(){
        int value = (int) round(20 * log10(recorder.getMaxAmplitude() / 32767.0));
        recorder.stop();
        recorder.release();
        recorder = null;
        sampleBut.setText(R.string.startSampling);
        samplesArray.add(value);
        String display = "";
        for (int j: samplesArray){
            display+=j+"dB ";
        }
        readingTV.setText(display);
        if (howManyTimes>0) startSampling();
//        recordSQL();
    }

    private void recordSQL(){
        //TODO - Eileen to provide object of Location class with parameters
    }

    private void initialiseRecorder(){
        if (recorder == null){
            recorder = new MediaRecorder();
            recorder.setMaxDuration(samplingTime*1000);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(mFileName);
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
}