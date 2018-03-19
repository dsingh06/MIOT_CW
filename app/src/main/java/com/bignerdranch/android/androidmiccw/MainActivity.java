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

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static java.lang.Math.log10;
import static java.lang.Math.round;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 31;
    private Button sampleBut;
    private SeekBar duration;
    private MediaRecorder recorder = null;
    private TextView readingTV;
    private TextView time;
    private String mFileName = null;
    private int samplingTime = 1;

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
        time = findViewById(R.id.time);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audio31.3gp";

        sampleBut = findViewById(R.id.sample);
        sampleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startSampling();
            }
        });
        duration = findViewById(R.id.duration);
        duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i==0) i=1;
                samplingTime = i;
                time.setText(samplingTime+"s");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        initialiseRecorder();

    }


    private void startSampling(){
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
        readingTV.setText("" + round(20 * log10(recorder.getMaxAmplitude() / 32767.0)) + "dB");
        recorder.stop();
        recorder.release();
        recorder = null;
        sampleBut.setText(R.string.startSampling);
//        recordSQL();
    }

    private void recordSQL(){
        //TODO - Eileen to provide object with parameters
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