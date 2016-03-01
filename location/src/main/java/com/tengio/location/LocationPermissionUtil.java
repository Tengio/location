package com.tengio.location;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public final class LocationPermissionUtil {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 89;

    private LocationPermissionUtil() {

    }

    public static boolean hasAccessToLocation(Activity activity) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasAccessToLocation(Fragment fragment) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                fragment.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static void requestLocationPermission(Fragment fragment) {
        FragmentCompat.requestPermissions(fragment,
                                          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                          LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                                          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                          LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static boolean isLocationPermissionRequestCode(int requestCode) {
        return requestCode != LOCATION_PERMISSION_REQUEST_CODE;
    }
}
