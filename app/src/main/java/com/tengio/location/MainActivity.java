package com.tengio.location;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Location";

    private LocationClient locationClient = GoogleLocationClient.Builder.newInstance().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationClient.register(new LocationListener() {
            @Override
            public void onProviderDisabled() {
                Log.d(TAG, "GPS Disabled");
            }

            @Override
            public void onConnectionFailed() {
                Log.e(TAG, "Error retrieving GPS signal");
            }

            @Override
            public void onShowRequestPermissionRationale() {
                Log.d(TAG, "GPS Permission missing, inform the user");
            }

            @Override
            public void onPermissionDenied() {
                Log.d(TAG, "GPS Permission denied");
            }

            @Override
            public void onLocationChanged(double latitude, double longitude) {
                Log.i(TAG, "Latitude: " + latitude + "\nLongitude: " + longitude);
            }
        }, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationClient.unregister();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationClient.onRequestPermissionResult(this, requestCode, grantResults);
    }
}
