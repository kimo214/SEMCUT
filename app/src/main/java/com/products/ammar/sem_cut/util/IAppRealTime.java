package com.products.ammar.sem_cut.util;

import com.google.android.gms.maps.model.LatLng;

public interface IAppRealTime {

    void setRpm(int value);
    void setSpeed(int value);
    void setLocation(LatLng location);
    void setStatus(boolean status);
    void setSuggestValue(int value);
    interface OnDataChange{
        void onRpmChange(int newValue);
        void onSpeedChange(int newValue);
        void onLocationChange(LatLng location);
        void onStatusChange(boolean isRunning);

        /**
         * called when paddocks suggest to change the motor status
         * @param suggestValue may be 0 or 1 depending on what he suggest
         */
        void onSuggestChange(int suggestValue);
    }
}