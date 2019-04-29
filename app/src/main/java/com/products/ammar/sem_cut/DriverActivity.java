package com.products.ammar.sem_cut;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.products.ammar.sem_cut.util.BlutoothHelper;
import com.products.ammar.sem_cut.util.FirebaseRealTime;
import com.products.ammar.sem_cut.util.IAppRealTime;
import com.products.ammar.sem_cut.util.IBlutoothHelper;

import java.io.UnsupportedEncodingException;

public class DriverActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private Button changeStatusView;
    private boolean mIsRunning;
    private boolean mIsMapVisible;

    private BlutoothHelper bHelper;
    private LatLng currLocation;
    private SupportMapFragment mapView;
    private GoogleMap mMap;
    private Marker driverMarker;

    private FirebaseRealTime db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        changeStatusView = findViewById(R.id.changeStatus);
        changeStatusView.setOnClickListener(this);
        mIsMapVisible = true;

        handleDbChange();

        bHelper = new BlutoothHelper(this);
        bHelper.setOnReceiveData(new IBlutoothHelper.OnReceiveDataListener() {
            @Override
            public void receive(byte[] data) {

                String str = null; // for UTF-8 encoding
                try {
                    str = new String(data, "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
                int k=1;
                if(str.charAt(1)>'9' ||str.charAt(1)<'0' ){
                    k=2;
                }
                int rpm=(Character.getNumericValue(str.charAt(k))*1000)+(Character.getNumericValue(str.charAt(k+1))*100);
                rpm+=(Character.getNumericValue(str.charAt(k+2))*10)+Character.getNumericValue(str.charAt(k+3));
                int speed=(Character.getNumericValue(str.charAt(k+4))*1000)+(Character.getNumericValue(str.charAt(k+5))*100);
                speed+=(Character.getNumericValue(str.charAt(k+6))*10)+Character.getNumericValue(str.charAt(k+7));

                if(rpm>1000 && rpm<9000){
                    db.setRpm(rpm);
                }
                if(speed<100){
                    db.setSpeed(speed);
                }
                Log.e("BLUE", str);
                /*int rpm=0,speed=0;
                if(str.charAt(1) == 'a'){
                    rpm=(Character.getNumericValue(str.charAt(2))*1000)+(Character.getNumericValue(str.charAt(3))*100);
                    rpm+=(Character.getNumericValue(str.charAt(4))*10)+Character.getNumericValue(str.charAt(5));
                }

                if(str.charAt(1) == 'b'){
                    speed=(Character.getNumericValue(str.charAt(2))*100)+(Character.getNumericValue(str.charAt(3))*10);
                    speed+=(Character.getNumericValue(str.charAt(4))*1);
                }

                if(rpm>1000 && rpm<9000){
                    db.setRpm(rpm);
                }
                db.setSpeed(speed);*/
            }
        });
        bHelper.start();

        // TODO: Remove this line after testing
        bHelper.send("5".getBytes());

        handleLocationChange();

        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverActivity_map);
        mapView.getMapAsync(this);
    }

    private void handleDbChange() {
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
//                Toast.makeText(DriverActivity.this, "Location change db", Toast.LENGTH_SHORT).show();
//                currLocation = new LatLng(location.latitude, location.longitude);
//                driverMarker.setPosition(currLocation);
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation));
            }

            @Override
            public void onStatusChange(boolean isRunning) {
                if (isRunning) {
                    changeStatusView.setText(R.string.close);
                    mIsRunning = true;
                } else {
                    changeStatusView.setText(R.string.open);
                    mIsRunning = false;
                }
            }

            @Override
                    public void onSuggestChange(int suggestValue) {
                        showAlertDialog(suggestValue);
                    }
                });
    }

    private void showAlertDialog(final int suggestValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String closeOpen = suggestValue == 1 ? "open" : "close";
        builder.setMessage("Paddocks wants to " + closeOpen + " the motor").setCancelable(false)
                .setPositiveButton("Accept change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DriverActivity.this.onClick(null);

                    }
                })
                .setNegativeButton("Keep status", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        db.setSuggestValue((suggestValue + 1) % 2); // flip the suggested value
                    }
                }).show();
    }

    private void handleLocationChange() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                currLocation = new LatLng(latitude, longitude);
                db.setLocation(currLocation);
                // update instead when db change
                driverMarker.setPosition(currLocation);

                // TODO: Remove this line if you want, this line make the marker of the driver located at the center of the map
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(DriverActivity.this, "GPS enabled", Toast.LENGTH_SHORT).show();
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(DriverActivity.this, "GPS disabled", Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "gps permission denied", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationManager == null) {
            Toast.makeText(this, "location manger not found", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private <T> void updateOneView(TextView view, T data) {
        view.setText(data.toString());
    }

    @Override
    public void onClick(View view) {
        // note: here we are going to close the motor before text views updated
        if (mIsRunning) {
            db.setStatus(false);
            // TODO: send to arduino here to close the motor
//            bHelper.send("close the motor byte code".getBytes());
        } else {
            db.setStatus(true);
            // TODO: // // // open // //
//            bHelper.send("open the motor byte code".getBytes());
        }
        // mIsRunning = !mIsRunning; // we are doing it in the db listener
    }

    @Deprecated
    public void onShowHideMapClick(View view) {
        if (mIsMapVisible) {
            ((Button) view).setText("hide");
            mapView.getView().setVisibility(View.VISIBLE);
        } else {
            ((Button) view).setText("show");
            mapView.getView().setVisibility(View.GONE);
        }
        mIsMapVisible = !mIsMapVisible;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        driverMarker = mMap.addMarker(new MarkerOptions().position(Constants.TRACK_START_LOCATION).title("driver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.TRACK_START_LOCATION));
    }
}
