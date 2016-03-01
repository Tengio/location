package com.tengio.geolocation;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tengio.location.GoogleLocationService;

public class MainActivity extends AppCompatActivity implements GoogleLocationService.LocationListener {

    public static final String TAG = "Geolocation";

    private GoogleLocationService googleLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        googleLocationService = new GoogleLocationService.Builder()
                .setDistance( 250 )
                .setInterval( 500 )
                .setFastestInterval( 200 ).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleLocationService.register( this, this );
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleLocationService.unregister();
    }

    @Override
    public void onProviderDisabled() {
        Log.d( TAG, "GPS Disabled" );
    }

    @Override
    public void onConnectionFailed() {
        Log.e( TAG, "Error retrieving GPS signal" );
    }

    @Override
    public void onLocationChanged(LatLng latLng) {
        Log.i( TAG, "Latitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude);
    }

    @Override
    public void shouldShowRequestPermissionRationale() {
        Log.d( TAG, "GPS Permission missing, inform the user" );
    }

    @Override
    public void onPermissionDenied() {
        Log.d( TAG, "GPS Permission denied" );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        googleLocationService.onRequestPermissionResult(this, requestCode, grantResults);
    }
}
