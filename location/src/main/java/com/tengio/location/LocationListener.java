package com.tengio.location;

import java.io.Serializable;

public interface LocationListener extends Serializable {

    void onProviderDisabled();

    void onConnectionFailed();

    void onPermissionDenied();

    void onShowRequestPermissionRationale();

    void onLocationChanged(double latitude, double longitude);

    void onPermissionAccepted();
}
