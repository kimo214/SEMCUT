package com.products.ammar.sem_cut;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.products.ammar.sem_cut.App.Constants;
import com.products.ammar.sem_cut.util.FirebaseRealTime;
import com.products.ammar.sem_cut.util.IAppRealTime;

public class PaddocksMainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, View.OnLongClickListener {

    private Button changeStatusView;
    private boolean mIsRunning;

    private FirebaseRealTime db;
    private LatLng currLocation;

    private GoogleMap mMap;
    private Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paddocks);
        changeStatusView = findViewById(R.id.changeStatus);
        changeStatusView.setOnClickListener(this);
        changeStatusView.setOnLongClickListener(this);


        db = new FirebaseRealTime(new IAppRealTime.OnDataChange() {
            @Override
            public void onRpmChange(int newValue) {
                TextView currView = findViewById(R.id.rpm);
                updateOneView(currView, newValue);
            }

            @Override
            public void onSpeedChange(int newValue) {
                TextView currView = findViewById(R.id.speed);
                updateOneView(currView, newValue);
            }

            @Override
            public void onLocationChange(LatLng location) {
                currLocation = new LatLng(location.latitude, location.longitude);
                driverMarker.setPosition(currLocation);
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation));
            }

            @Override
            public void onStatusChange(boolean isRunning) {
                if (isRunning) {
                    changeStatusView.setText(R.string.suggest_close);
                    mIsRunning = true;
                } else {
                    changeStatusView.setText(R.string.suggest_open);
                    mIsRunning = false;
                }
            }

            @Override
            public void onSuggestChange(int suggestValue) {
                // nothing to to here as paddocks is the man who suggest
                Toast.makeText(PaddocksMainActivity.this, "suggested value updated... wait for driver to accept", Toast.LENGTH_SHORT).show();
            }
        });

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.paddocksActivity_map)).getMapAsync(this);
    }

    private <T> void updateOneView(TextView view, T data) {
        view.setText(data.toString());
    }

    @Override
    public void onClick(View view) {
        if (mIsRunning) {
//            db.setStatus(false);
            db.setSuggestValue(0);
} else {
//            db.setStatus(true);
        db.setSuggestValue(1);
        }
        }

@Override
public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        driverMarker = mMap.addMarker(new MarkerOptions().position(Constants.TRACK_START_LOCATION).title("driver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.TRACK_START_LOCATION));
        }

@Override
public boolean onLongClick(View view) {
        if (mIsRunning) {
            db.setStatus(false);
        } else {
            db.setStatus(true);
        }
        return true;
    }
}