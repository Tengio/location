# LOCATION

Version
-------

[ ![Download](https://api.bintray.com/packages/tengioltd/maven/location/images/download.svg) ](https://bintray.com/tengioltd/maven/location/_latestVersion)

Current version uses Google Play Services 9.8.0.

Version will follows google play services version so that it is going to be even easier to implement.


HOW TO
======

Dependencies
------------

```
dependencies {
    ...
    compile('com.tengio:location:latest_version') {
        transitive = true;    
    }
}
```

By adding locations library dependency you will automatically get the following dependencies:

```
com.google.android.gms:play-services-location:8.4.0
com.android.support:support-v13:23.1.1
```


Android manifest
----------------

You need to add permission :
```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```


Activity or Fragment
-------------

```
private LocationClient locationClient = GoogleLocationClient.Builder.newInstance().build();

@Override
protected void onResume() {
    ...
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
    ...
    locationClient.unregister();
}
```

The library asks for location permission to Marshmallow or greater devices. 
You have to pass the result to the library from your activity or fragment:

```    
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    ...
    locationClient.onRequestPermissionResult(this, requestCode, grantResults);
}
```


Library updates
---------------

We use bintray to deploy changes to jcenter. To deploy a new version make sure to define BINTRAY_USER and BINTRAY_KEY variables. Then run:

```
gradle bintrayUpload
```