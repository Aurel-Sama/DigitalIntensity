// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentHelper {
    public static boolean backToPreviousFragment(@Nullable FragmentActivity activity) {
        if (activity == null) {
            return false;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return false;
        }

        fragmentManager.popBackStack();
        return true;
    }

    public static void replaceFragment(FragmentActivity activity, Fragment fragment) {
        switchToFragment(activity, fragment, false);
    }

    public static void isAlreadyOpen(FragmentActivity activity){
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        for(Fragment fragment : fragmentManager.getFragments()){
            if(fragment instanceof FanFragment || fragment instanceof ValveFragment || fragment instanceof SensorFragment || fragment instanceof PumpFragment){
                backToPreviousFragment(activity);
            }

        }
    }

    public static void pushFragment(FragmentActivity activity, Fragment fragment) {
        switchToFragment(activity, fragment, true);
    }

    private static void switchToFragment(FragmentActivity activity, Fragment fragment, boolean preserveCurrent) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ux_frame, fragment);
        if (preserveCurrent) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
