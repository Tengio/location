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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 89;
    private float thresholdMeter;
    private long interval;
    private long fastestInterval;

    private final FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private LocationListener locationListener;
    private boolean alreadyQueryingLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;

    public void register(final LocationListener listener, Fragment fragment) {
        this.locationListener = listener;
        if (!hasAccessToLocation( fragment.getActivity() )) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( fragment.getActivity(),
                                                                     Manifest.permission.ACCESS_FINE_LOCATION )) {
                locationListener.shouldShowRequestPermissionRationale();
            } else {
                requestLocationPermission( fragment );
            }
            return;
        }
        registerListener( fragment.getActivity() );
    }

    public void register(final LocationListener listener, Activity activity) {
        this.locationListener = listener;
        if (!hasAccessToLocation( activity )) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( activity,
                                                                     Manifest.permission.ACCESS_FINE_LOCATION )) {
                locationListener.shouldShowRequestPermissionRationale();
            } else {
                requestLocationPermission( activity );
            }
            return;
        }
        registerListener( activity );
    }

    private void registerListener(Activity activity) {
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

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority( LocationRequest.PRIORITY_LOW_POWER );
        locationRequest.setInterval( interval );
        locationRequest.setFastestInterval( fastestInterval );
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
            if (distance > thresholdMeter) {
                notifyLocation( newLocation );
            }
        } else {
            notifyLocation( newLocation );
        }
        unregister();
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

    public boolean hasAccessToLocation(Context context) {
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
        return;
    }

    public interface LocationListener {

        void onProviderDisabled();

        void onConnectionFailed();

        void onLocationChanged(LatLng latLng);

        void shouldShowRequestPermissionRationale();
    }

    public static class Builder {

        private float thresholdMeter = 250;
        private long interval = 500;
        private long fastestInterval = 200;

        public Builder setDistance(float thresholdMeter) {
            this.thresholdMeter = thresholdMeter;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setFastestInterval(long fastestInterval) {
            this.fastestInterval = fastestInterval;
            return this;
        }

        public GoogleLocationService build() {
            return new GoogleLocationService( this );
        }
    }

    private GoogleLocationService(Builder b) {
        this.thresholdMeter = b.thresholdMeter;
        this.interval = b.interval;
        this.fastestInterval = b.fastestInterval;
    }
}
