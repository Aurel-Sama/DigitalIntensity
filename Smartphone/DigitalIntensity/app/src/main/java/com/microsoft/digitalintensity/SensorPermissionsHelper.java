// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;

import com.microsoft.azure.spatialanchors.PlatformLocationProvider;
import com.microsoft.azure.spatialanchors.SensorCapabilities;

public class SensorPermissionsHelper {


    public static boolean requestMissingPermissions(Activity activity, int requestCode) {
        if (hasAllPermissionGranted(activity)) {
            return true;
        }

        activity.requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE},
                requestCode);

        return hasAllRequiredPermissionGranted(activity);
    }

    public static void enableAllowedSensors(Context context, PlatformLocationProvider locationProvider) {
        // Retrieve permissions granted by the user
        boolean hasFineLocationPermission = hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasAccessCoarseLocationPermission = hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean hasAnyLocationPermission = hasFineLocationPermission || hasAccessCoarseLocationPermission;
        boolean hasAccessWifiStatePermission = hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
        boolean hasChangeWifiStatePermission = hasPermission(context, Manifest.permission.CHANGE_WIFI_STATE);

        // Try to turn on Wi-Fi, if allowed
        boolean isWifiAllowed = hasAnyLocationPermission
                && hasAccessWifiStatePermission
                && hasChangeWifiStatePermission;
        boolean isWifiOn = isWifiAllowed && tryTurnOnWifi(context);


        // Configure location provider to use the allowed sensors
        SensorCapabilities sensors = locationProvider.getSensors();
        sensors.setGeoLocationEnabled(hasAnyLocationPermission);
        sensors.setWifiEnabled(isWifiOn);
    }

    public static boolean hasAllRequiredPermissionGranted(Context context) {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private static boolean hasAllPermissionGranted(Context context) {
        return hasAllRequiredPermissionGranted(context)
                && hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
                && hasPermission(context, Manifest.permission.CHANGE_WIFI_STATE);
    }

    private static boolean hasPermission(Context context, String manifestPermission) {
        return ActivityCompat.checkSelfPermission(context, manifestPermission) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean tryTurnOnWifi(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        return wifiManager.isWifiEnabled();
    }


}
