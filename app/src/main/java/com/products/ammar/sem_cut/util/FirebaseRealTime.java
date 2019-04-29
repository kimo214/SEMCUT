package com.products.ammar.sem_cut.util;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRealTime implements IAppRealTime {

    private final DatabaseReference root;
    private OnDataChange callbacks;

    private boolean firstTimeFetch = true;
    private ValueEventListener suggestValueEventListener;

    public FirebaseRealTime(final OnDataChange callbacks) {
        this.callbacks = callbacks;

        root = FirebaseDatabase.getInstance().getReference();

        root.child("rpm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer rpm = dataSnapshot.getValue(Integer.class);
                if (rpm == null) return;
                callbacks.onRpmChange(rpm);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.child("speed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer speed = dataSnapshot.getValue(Integer.class);
                if (speed == null) return;
                callbacks.onSpeedChange(speed);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Float latitude = dataSnapshot.child("latitude").getValue(float.class);
                Float longitude = dataSnapshot.child("longitude").getValue(float.class);
                if (latitude == null || longitude == null)
                    return;
                callbacks.onLocationChange(new LatLng(latitude, longitude));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isRunning = dataSnapshot.getValue(Boolean.class);
                if (isRunning == null) return;
                callbacks.onStatusChange(isRunning);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        suggestValueEventListener = root.child("suggestChange").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (firstTimeFetch) {
                    firstTimeFetch = false;
                    return;
                }
                Integer value = dataSnapshot.getValue(Integer.class);
                if (value == null) return;
                callbacks.onSuggestChange(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void setRpm(int value) {
        root.child("rpm").setValue(value);
    }

    @Override
    public void setSpeed(int value) {
        root.child("speed").setValue(value);
    }

    @Override
    public void setLocation(LatLng location) {
        root.child("location").child("latitude").setValue(location.latitude);
        root.child("location").child("longitude").setValue(location.longitude);

    }

    @Override
    public void setStatus(boolean status) {
        root.child("status").setValue(status);
    }

    @Override
    public void setSuggestValue(int value) {
        firstTimeFetch = true;
        root.child("suggestChange").setValue(value);
    }
}
