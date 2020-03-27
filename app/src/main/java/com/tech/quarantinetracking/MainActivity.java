package com.tech.quarantinetracking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Wherebouts.instance().onChange(null);

        Wherebouts.instance().onChange(new Workable<GPSPoint>() {
            @Override
            public void work(GPSPoint gpsPoint) {
                Log.d("sos", String.valueOf(gpsPoint.getLatitude()));

            }
        });

        new Workable<GPSPoint>() {
            @Override
            public void work(GPSPoint gpsPoint) {
                // draw something in the UI with this new data
            }
        };
    }


}
