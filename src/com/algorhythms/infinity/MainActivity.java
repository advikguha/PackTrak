package com.algorhythms.infinity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by demouser on 6/13/13.
 */
public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view
        setContentView(R.layout.activity_main);

        /*Button btnView = (Button) findViewById(R.id.home_button1);
        Button export = (Button) findViewById(R.id.home_button2);

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                Intent intent = new Intent(MainActivity.this, TripsViewActivity.class);
                startActivity(intent);
            }
        });*/
    }

    public void goToTripsViewActivity(View view){
        Intent intent = new Intent(MainActivity.this, TripsViewActivity.class);
        startActivity(intent);
    }

    public void goToTripsExportActivity(View view){
        Intent intent = new Intent(MainActivity.this, ShareTripsActivity.class);
        startActivity(intent);
    }
}