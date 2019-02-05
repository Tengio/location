package com.tengio.location;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public interface LocationClient {

    void register(@NonNull LocationListener listener, @NonNull Fragment fragment, @Nullable Bundle savedState);

    void register(@NonNull LocationListener listener, @NonNull Activity activity, @Nullable Bundle savedState);

    void unregister(@Nullable Bundle savedState);

    void onRequestPermissionResult(int requestCode, int[] grantResults);
}
