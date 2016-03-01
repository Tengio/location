# locations

Version
-------

[ ![Download](https://api.bintray.com/packages/tengioltd/maven/locations/images/download.svg) ](https://bintray.com/tengioltd/maven/cpn/_latestVersion)

Current version uses Google Play Services 8.4.0.

Version will follows google play services version so that it is going to be even easier to implement.


HOW TO
======

Dependencies
------------

```
dependencies {
    ...
    compile 'com.tengio:locations:latest_version'
}
```

By adding locations library dependency you will automatically get the following dependencies:

```
com.google.android.gms:play-services-location:8.4.0
com.android.support:support-v13:23.1.1
```

Gradle Plugins
--------------

In the root build.gradle file add: 
```
buildscript {
...
    dependencies {
        ...
        classpath 'com.google.gms:google-services:2.0.0-beta6'
    }
}
```
In the app build.gradle at the very bottom of the file (this it is probably just a temporary bug in google play 
services plugin) place:
```
apply plugin: 'com.google.gms.google-services'
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
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    googleLocationService = new GoogleLocationService.Builder()
            .setDistance( 250 )
            .setInterval( 500 )
            .setFastestInterval( 200 ).build();
}

@Override
protected void onResume() {
    ...
    googleLocationService.register( this, this );
}

@Override
protected void onPause() {
    ...
    googleLocationService.unregister();
}
```

The library asks for location permission to Marshmallow or greater devices. 
You have to pass the result to the library from you activity or fragment:
```    
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    ...
    googleLocationService.onRequestPermissionResult(this, requestCode, grantResults);
}
```
Your class need to implement the Location Listener:
```
public class MainActivity extends AppCompatActivity implements GoogleLocationService.LocationListener {
```
It permit you to have the Listener methods implemented in your class:
```
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
```
