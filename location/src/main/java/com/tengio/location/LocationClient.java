package com.tengio.location;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

public interface LocationClient {

    void register(LocationListener listener, Fragment fragment);

    void register(LocationListener listener, Activity activity);

    void unregister();

    void onRequestPermissionResult(Context context, int requestCode, int[] grantResults);
}
