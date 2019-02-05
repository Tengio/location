package com.tengio.location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

import static com.tengio.location.LocationPermissionUtil.hasAccessToLocation;
import static com.tengio.location.LocationPermissionUtil.isLocationPermissionRequestCode;
import static com.tengio.location.LocationPermissionUtil.requestLocationPermission;

public class GoogleLocationClient implements OnSuccessListener<Location>,
                                             LocationClient {

    private FusedLocationProviderClient locationProvider ;
    private float thresholdMeter;
    private long interval;
    private long fastestInterval;
    private LocationListener locationListener;
    private boolean alreadyQueryingLocation;
    private Location lastLocation;

    private final LocationCallback callback = new LocationCallback() {

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null || locationResult.getLocations().size() == 0) {
                return;
            }
            Location location = locationResult.getLocations().get(0);
            if (lastLocation != null) {
                float distance = location.distanceTo(lastLocation);
                if (distance > thresholdMeter) {
                    notifyLocation(location);
                }
            } else {
                notifyLocation(location);
            }
            unregister();
        }
    };



    @Override
    public void register(final LocationListener listener, Fragment fragment) {
        if (fragment.getActivity() == null) {
            return;
        }
        this.locationProvider = LocationServices.getFusedLocationProviderClient(fragment.getActivity());
        this.locationListener = listener;
        if (hasAccessToLocation(fragment) && fragment.getActivity() != null) {
            registerListener(fragment.getActivity());
            return;
        }
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationListener.onShowRequestPermissionRationale();
        } else {
            requestLocationPermission(fragment);
        }
    }


    @Override
    public void register(final LocationListener listener, Activity activity) {
        this.locationProvider = LocationServices.getFusedLocationProviderClient(activity);
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
        queryLocation();
    }


    @Override
    public void unregister() {
        locationProvider.removeLocationUpdates(callback);
        alreadyQueryingLocation = false;
    }

    @SuppressWarnings("MissingPermission")
    private void queryLocation() {
        if (alreadyQueryingLocation) {
            return;
        }
        locationProvider.getLastLocation().addOnSuccessListener(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        locationProvider.requestLocationUpdates(locationRequest, callback, null);
        alreadyQueryingLocation = true;
    }

    private void notifyLocation(Location location) {
        alreadyQueryingLocation = false;
        lastLocation = location;
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
            queryLocation();
        } else {
            locationListener.onPermissionDenied();
        }
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            notifyLocation(lastLocation);
        }
    }

    public static final class Builder {

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
