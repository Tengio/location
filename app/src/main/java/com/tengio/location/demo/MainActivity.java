package com.tengio.location.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.tengio.location.GoogleLocationClient;
import com.tengio.location.LocationClient;
import com.tengio.location.LocationListener;

public class MainActivity extends Activity {

    public static final String TAG = "Location";

    private LocationClient locationClient = GoogleLocationClient.Builder.newInstance().build();
    private TextView debugMessages;
    private TextView latitude;
    private TextView longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugMessages = findViewById(R.id.debug_messages);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        locationClient.register(new LocationListener() {
            @Override
            public void onProviderDisabled() {
                debugMessages.setText("GPS Disabled");
                latitude.setText("N/A");
                longitude.setText("N/A");
            }

            @Override
            public void onConnectionFailed() {
                debugMessages.setText("Error retrieving GPS signal");
                latitude.setText("N/A");
                longitude.setText("N/A");
            }

            @Override
            public void onShowRequestPermissionRationale() {
                debugMessages.setText("GPS Permission missing, inform the user");
                latitude.setText("N/A");
                longitude.setText("N/A");
            }

            @Override
            public void onPermissionDenied() {
                debugMessages.setText("GPS Permission denied");
                latitude.setText("N/A");
                longitude.setText("N/A");
            }

            @Override
            public void onLocationChanged(double lat, double lon) {
                Log.i(TAG, "Latitude: " + latitude + "\nLongitude: " + longitude);
                latitude.setText("" + lat);
                longitude.setText("" + lon);
                debugMessages.setText(null);
            }

            @Override
            public void onPermissionAccepted() {
                debugMessages.setText("GPS Permission accepted");
                latitude.setText("N/A");
                longitude.setText("N/A");
            }
        }, this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        locationClient.unregister(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationClient.onRequestPermissionResult( requestCode, grantResults);
    }
}
