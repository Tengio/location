package com.tengio.location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

import static com.tengio.location.LocationPermissionUtil.hasAccessToLocation;
import static com.tengio.location.LocationPermissionUtil.isLocationPermissionRequestCode;
import static com.tengio.location.LocationPermissionUtil.requestLocationPermission;

public class GoogleLocationClient implements GoogleApiClient.ConnectionCallbacks,
                                             GoogleApiClient.OnConnectionFailedListener,
                                             com.google.android.gms.location.LocationListener,
                                             LocationClient {

    private float thresholdMeter;
    private long interval;
    private long fastestInterval;

    private final FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private LocationListener locationListener;
    private boolean alreadyQueryingLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;

    @Override
    public void register(final LocationListener listener, Fragment fragment) {
        this.locationListener = listener;
        if (hasAccessToLocation(fragment)) {
            registerListener(fragment.getActivity());
            return;
        }
        if (FragmentCompat.shouldShowRequestPermissionRationale(fragment, Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationListener.onShowRequestPermissionRationale();
        } else {
            requestLocationPermission(fragment);
        }
    }

    @Override
    public void register(final LocationListener listener, Activity activity) {
        this.locationListener = listener;
        if (hasAccessToLocation(activity)) {
            registerListener(activity);
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationListener.onShowRequestPermissionRationale();
        } else {
            requestLocationPermission(activity);
        }
    }

    private void registerListener(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationListener.onProviderDisabled();
            return;
        }
        buildLocationRequestAndGoogleConnection(activity);
    }

    private void buildLocationRequestAndGoogleConnection(Context context) {
        buildLocationRequest();
        buildGoogleApiClient(context);
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
    }

    private void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void unregister() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            locationProvider.removeLocationUpdates(googleApiClient, this);
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
            float distance = newLocation.distanceTo(lastLocation);
            if (distance > thresholdMeter) {
                notifyLocation(newLocation);
            }
        } else {
            notifyLocation(newLocation);
        }
        unregister();
    }

    @SuppressWarnings("MissingPermission")
    private void queryLocation() {
        lastLocation = locationProvider.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            notifyLocation(lastLocation);
        }
        locationProvider.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void notifyLocation(Location location) {
        if (locationListener != null && location != null) {
            locationListener.onLocationChanged(location.getLatitude(), location.getLongitude());
        }
    }

    @Override
    public void onRequestPermissionResult(Context context, int requestCode, int[] grantResults) {
        if (isLocationPermissionRequestCode(requestCode)) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationListener.onPermissionAccepted();
            buildLocationRequestAndGoogleConnection(context);
        } else {
            locationListener.onPermissionDenied();
        }
    }

    public static class Builder {

        private float thresholdMeter = 250;
        private long interval = 500;
        private long fastestInterval = 200;

        private Builder() {

        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder withDistance(float thresholdMeter) {
            this.thresholdMeter = thresholdMeter;
            return this;
        }

        public Builder withInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder withFastestInterval(long fastestInterval) {
            this.fastestInterval = fastestInterval;
            return this;
        }

        public LocationClient build() {
            GoogleLocationClient client = new GoogleLocationClient();
            client.thresholdMeter = this.thresholdMeter;
            client.interval = this.interval;
            client.fastestInterval = this.fastestInterval;
            return client;
        }
    }
}
