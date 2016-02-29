package com.tengio.location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class GoogleLocationService implements GoogleApiClient.ConnectionCallbacks,
                                              GoogleApiClient.OnConnectionFailedListener,
                                              com.google.android.gms.location.LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2839;
    private float threshold_meter;
    private long intervals_ms;
    private long fast_intervals_ms;

    private final FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private LocationListener locationListener;
    private boolean alreadyQueryingLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;

    public void register(final LocationListener listener, Fragment fragment) {
        if (!hasAccessToLocation( fragment.getActivity() )) {
            requestLocationPermission( fragment );
            return;
        }
        registerListener( listener, fragment.getActivity() );
    }

    public void register(final LocationListener listener, Activity activity) {
        if (!hasAccessToLocation( activity )) {
            requestLocationPermission( activity );
            return;
        }
        registerListener( listener, activity );
    }

    private void registerListener(LocationListener listener, Activity activity) {
        this.locationListener = listener;
        LocationManager locationManager = (LocationManager) activity.getSystemService( Context.LOCATION_SERVICE );
        if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            locationListener.onProviderDisabled();
            return;
        }

        buildLocationRequestAndGoogleConnection( activity );
    }

    private void buildLocationRequestAndGoogleConnection(Context context) {
        buildLocationRequest();
        buildGoogleApiClient( context );
    }

    public void unregister() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            locationProvider.removeLocationUpdates( googleApiClient, this );
            alreadyQueryingLocation = false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (alreadyQueryingLocation) {
            return;
        }
        alreadyQueryingLocation = true;
        if (googleApiClient.isConnected()) {
            queryLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (locationListener != null) {
            locationListener.onConnectionFailed();
        }
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        if (lastLocation != null) {
            float distance = newLocation.distanceTo( lastLocation );
            if (distance > threshold_meter) {
                notifyLocation( newLocation );
            }
        } else {
            notifyLocation( newLocation );
        }
        unregister();
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority( LocationRequest.PRIORITY_LOW_POWER );
        locationRequest.setInterval( intervals_ms );
        locationRequest.setFastestInterval( fast_intervals_ms );
    }

    private void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder( context )
                .addApi( LocationServices.API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void queryLocation() {
        lastLocation = locationProvider.getLastLocation( googleApiClient );
        if (lastLocation != null) {
            notifyLocation( lastLocation );
        }
        locationProvider.requestLocationUpdates( googleApiClient, locationRequest, this );
    }

    private void notifyLocation(Location location) {
        notifyLocation( new LatLng( location.getLatitude(), location.getLongitude() ) );
    }

    private void notifyLocation(LatLng latLng) {
        if (locationListener != null) {
            locationListener.onLocationChanged( latLng );
        }
    }

    private boolean hasAccessToLocation(Context context) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION );
    }

    protected void requestLocationPermission(Object object) {
        if (object instanceof Fragment) {
            FragmentCompat
                    .requestPermissions( (Fragment) object, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                         LOCATION_PERMISSION_REQUEST_CODE );
        } else {
            ActivityCompat
                    .requestPermissions( (Activity) object, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                         LOCATION_PERMISSION_REQUEST_CODE );
        }
    }

    public void onRequestPermissionResult(Context context, int requestCode, int[] grantResults) {
        if (requestCode != GoogleLocationService.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            buildLocationRequestAndGoogleConnection( context );
        }
    }

    public interface LocationListener {

        void onProviderDisabled();

        void onConnectionFailed();

        void onLocationChanged(LatLng latLng);
    }

    public static class Builder {

        private float threshold_meter = 250;
        private long intervals_ms = 500;
        private long fast_intervals_ms = 200;

        public Builder threaholdMeter(float threshold_meter) {
            this.threshold_meter = threshold_meter;
            return this;
        }

        public Builder intervalsMeters(int intervals_ms) {
            this.intervals_ms = intervals_ms;
            return this;
        }

        public Builder fastIntervalsMeters(int fast_intervals_ms) {
            this.fast_intervals_ms = fast_intervals_ms;
            return this;
        }

        public GoogleLocationService build() {
            return new GoogleLocationService( this );
        }
    }

    private GoogleLocationService(Builder b) {
        this.threshold_meter = b.threshold_meter;
        this.intervals_ms = b.intervals_ms;
        this.fast_intervals_ms = b.fast_intervals_ms;
    }
}
