package com.products.ammar.sem_cut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * This is the activity for user to choose his role
 */
public class ChooserActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
    }

    public void driverClick(View view) {
        Intent intent = new Intent(this, DriverActivity.class);
        startActivity(intent);
    }

    public void paddocksClick(View view) {
        Intent intent = new Intent(this, PaddocksMainActivity.class);
        startActivity(intent);
    }
}
