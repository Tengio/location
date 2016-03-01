package com.tengio.location;

public interface LocationListener {

    void onProviderDisabled();

    void onConnectionFailed();

    void onPermissionDenied();

    void onShowRequestPermissionRationale();

    void onLocationChanged(double latitude, double longitude);
}
