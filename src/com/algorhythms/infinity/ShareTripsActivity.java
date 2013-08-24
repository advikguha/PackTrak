package com.algorhythms.infinity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by demouser on 6/13/13.
 */
public class ShareTripsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_sharing_view);
    }

    public void shareTrips(View view) {
        Exporter e = new Exporter(this);
        e.export(1);
    }
}