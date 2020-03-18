// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;
import com.microsoft.azure.spatialanchors.CloudSpatialAnchor;
import com.microsoft.azure.spatialanchors.PlatformLocationProvider;
import com.microsoft.azure.spatialanchors.NearDeviceCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CoarseRelocActivity extends FragmentActivity
        implements AnchorPlacementListener, AnchorCreationListener, AnchorDiscoveryListener {
    private AzureSpatialAnchorsManager cloudAnchorManager;
    private PlatformLocationProvider locationProvider;

    private ArFragment arFragment;
    private ArSceneView sceneView;
    private ActionSelectionFragment actionSelectionFragment;
    private static final int REQUEST_CODE_ALL_SENSORS = 1;
    private Scene sceneForCamera;

    private List<AnchorVisual> placedAnchorVisuals = new ArrayList<>();
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ALL_SENSORS) {
            if (!SensorPermissionsHelper.hasAllRequiredPermissionGranted(this)) {
                Toast.makeText(
                        this,
                        "Location permission is needed to run this demo",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
            } else if (locationProvider != null) {
                SensorPermissionsHelper.enableAllowedSensors(this, locationProvider);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coarse_reloc);
        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        sceneView = arFragment.getArSceneView();

        Scene scene = sceneView.getScene();
        sceneForCamera = scene;
        scene.addOnUpdateListener(frameTime -> {
            if (cloudAnchorManager != null) {
                // Pass frames to Azure Spatial Anchors for processing.
                cloudAnchorManager.update(sceneView.getArFrame());
            }

        });

        actionSelectionFragment = new ActionSelectionFragment();
        FragmentHelper.replaceFragment(this, actionSelectionFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ArFragment of Sceneform automatically requests the camera permission before creating the AR session,
        // so we don't need to request the camera permission explicitly.
        // This will cause onResume to be called again after the user responds to the permission request.
        if (!SceneformHelper.hasCameraPermission(this)) {
            return;
        }

        if (sceneView != null && sceneView.getSession() == null) {
            if (!SceneformHelper.trySetupSessionForSceneView(this, sceneView)) {
                finish();
                return;
            }
        }

        if ((AzureSpatialAnchorsManager.SpatialAnchorsAccountId == null || AzureSpatialAnchorsManager.SpatialAnchorsAccountId.equals("Set me"))
                || (AzureSpatialAnchorsManager.SpatialAnchorsAccountKey == null|| AzureSpatialAnchorsManager.SpatialAnchorsAccountKey.equals("Set me"))) {
            Toast.makeText(this, "\"Set SpatialAnchorsAccountId and SpatialAnchorsAccountKey in AzureSpatialAnchorsManager.java\"", Toast.LENGTH_LONG)
                    .show();

            finish();
        }

        SensorPermissionsHelper.requestMissingPermissions(this, REQUEST_CODE_ALL_SENSORS);

        locationProvider = new PlatformLocationProvider();

        SensorPermissionsHelper.enableAllowedSensors(this, locationProvider);

        cloudAnchorManager = new AzureSpatialAnchorsManager(sceneView.getSession());
        cloudAnchorManager.setLocationProvider(locationProvider);
        cloudAnchorManager.start();

    }

    @Override
    protected void onPause() {


        if (cloudAnchorManager != null) {
            cloudAnchorManager.stop();
            cloudAnchorManager = null;
        }
        locationProvider = null;

        super.onPause();
    }

    public void onAddAnchorClicked(View view) {
        AnchorPlacementFragment placementFragment = new AnchorPlacementFragment();
        placementFragment.setListener(this);
        FragmentHelper.pushFragment(this, placementFragment);
    }

    @Override
    public void onAnchorPlaced(AnchorVisual placedAnchor) {
        AnchorCreationFragment creationFragment = new AnchorCreationFragment();
        creationFragment.setListener(this);
        creationFragment.setCloudAnchorManager(cloudAnchorManager);
        creationFragment.setPlacement(placedAnchor);
        FragmentHelper.backToPreviousFragment(this);
        FragmentHelper.pushFragment(this, creationFragment);
    }

    @Override
    public void onAnchorCreated(AnchorVisual createdAnchor) {
        createdAnchor.setShape(AnchorVisual.Shape.Cube);
        FragmentHelper.backToPreviousFragment(this);
    }

    @Override
    public void onAnchorCreationFailed(AnchorVisual placedAnchor, String errorMessage) {
        placedAnchor.destroy();
        FragmentHelper.backToPreviousFragment(this);
        runOnUiThread(() -> {
            String toastMessage = "Failed to save anchor: " + errorMessage;
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        });
    }

    public void onStartWatcherClicked(View view) {
        WatcherFragment watcherFragment = new WatcherFragment();
        watcherFragment.setCloudAnchorManager(cloudAnchorManager);
        watcherFragment.setListener(this);
        FragmentHelper.pushFragment(this, watcherFragment);
    }

    public void onDeleteAllNearbyAnchors(View view) {
        NearDeviceCriteria criteria = new NearDeviceCriteria();
        criteria.setDistanceInMeters(5.f);

        actionSelectionFragment.disableDeleteButton();

        cloudAnchorManager.enumerateNearbyAnchors(criteria)
                .thenAccept(this::confirmAnchorDeletion)
                .exceptionally(ex -> {
                    runOnUiThread(() ->
                    {
                        ex.printStackTrace();

                        Toast.makeText(this, "Failed to enumerate anchors, check log",
                                Toast.LENGTH_LONG).show();

                        actionSelectionFragment.enableDeleteButton();
                    });
                    return null;
                });
    }

    public void onAnchorMoved(View view){
        Session session = arFragment.getArSceneView().getSession();
        for(AnchorVisual av : placedAnchorVisuals){

            if(av!=null){
                Pose previousPose = av.getLocalAnchor().getPose().compose(Pose.makeTranslation(10, 0, 0));
                //av.destroy();
                //av = null;



                /*LayoutInflater layoutInflater = getLayoutInflater();
                View myLayout = layoutInflater.inflate(R.layout.planetinfocard, null, false);

                AnchorVisual visual = new AnchorVisual(arFragment, av.getCloudAnchor(), this);
                visual.setScene(sceneForCamera);
                visual.setView(myLayout);
                Map<String, String> properties = av.getCloudAnchor().getAppProperties();
                if (properties.containsKey("Shape")) {
                    try {
                        AnchorVisual.Shape savedShape = AnchorVisual.Shape.valueOf(properties.get("Shape"));
                        visual.setShape(savedShape);
                    } catch (IllegalArgumentException ex) {
                        // Invalid shape property, keep default shape
                    }
                }
                if (properties.containsKey("ComponentType")) {
                    try {
                        ComponentType ct = ComponentType.valueOf(properties.get("ComponentType"));
                        visual.setComponentType(ct);
                        Log.v("Selected component", visual.getComponentType().toString());
                    } catch (IllegalArgumentException ex) {
                        // Invalid shape property, keep default shape
                    }
                }
                visual.setMovable(true);
                av.setContext(this);
                visual.render(arFragment);


            */









                Anchor localAnchor = session.createAnchor(previousPose);
                if(localAnchor!=null){
                    av = new AnchorVisual(arFragment, localAnchor);
                    av.setContext(arFragment.getContext());
                    av.setMovable(true);
                    av.setShape(AnchorVisual.Shape.Cube);
                    av.render(arFragment);
                }
                else{
                    Log.i("LocalAnchor","Je suis null");
                }

            }



        }
    }

    public void onDeleteSelectedAnchor(View view) {
        NearDeviceCriteria criteria = new NearDeviceCriteria();
        criteria.setDistanceInMeters(5.f);


        cloudAnchorManager.enumerateNearbyAnchors(criteria)
                .thenAccept(this::deleteAnchor)
                .exceptionally(ex -> {
                    runOnUiThread(() ->
                    {
                        ex.printStackTrace();

                    });
                    return null;
                });
    }

    @Override
    public void onAnchorDiscovered(CloudSpatialAnchor cloudAnchor) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View myLayout = layoutInflater.inflate(R.layout.planetinfocard, null, false);

        AnchorVisual visual = new AnchorVisual(arFragment, cloudAnchor, this);
        visual.setScene(sceneForCamera);
        visual.setView(myLayout);
        Map<String, String> properties = cloudAnchor.getAppProperties();
        if (properties.containsKey("Shape")) {
            try {
                AnchorVisual.Shape savedShape = AnchorVisual.Shape.valueOf(properties.get("Shape"));
                visual.setShape(savedShape);
            } catch (IllegalArgumentException ex) {
                // Invalid shape property, keep default shape
            }
        }
        if (properties.containsKey("ComponentType")) {
            try {
                ComponentType ct = ComponentType.valueOf(properties.get("ComponentType"));
                visual.setComponentType(ct);
                Log.v("Selected component", visual.getComponentType().toString());
            } catch (IllegalArgumentException ex) {
                // Invalid shape property, keep default shape
            }
        }
        visual.setMovable(true);
        placedAnchorVisuals.add(visual);
        visual.render(arFragment);


    }

    public void onBackClicked(View view) {
        if (!FragmentHelper.backToPreviousFragment(this)) {
            finish();
        }
    }

    private void confirmAnchorDeletion(List<CloudSpatialAnchor> anchors) {
        // There might be hundreds of anchors found near device. Deleting them without
        // confirmation might be disruptive.
        // Keep it safe while show casing API usage.
        runOnUiThread(() ->
        {
            actionSelectionFragment.enableDeleteButton();

            new AlertDialog.Builder(this)
                .setTitle("Confirm deletion")
                .setMessage(anchors.size() == 0
                        ? "No anchors found to delete."
                        : "About to delete " + anchors.size() + " nearby anchors. Are you sure?")
                .setPositiveButton(android.R.string.yes, (d, b) -> deleteAnchors(anchors))
                .setNegativeButton(android.R.string.no, null).show();
        });
    }

    private void deleteAnchors(List<CloudSpatialAnchor> anchors) {
        for (CloudSpatialAnchor anchor : anchors) {
            cloudAnchorManager.deleteAnchorAsync(anchor);
        }
    }

    private void deleteAnchor(List<CloudSpatialAnchor> anchors) {
        for (CloudSpatialAnchor anchor : anchors) {
            if(AnchorVisual.selectedId.equals(anchor.getIdentifier())){
                cloudAnchorManager.deleteAnchorAsync(anchor);
                AnchorVisual.selectedId="";
//                Intent mStartActivity = new Intent(this, CoarseRelocActivity.class);
//                int mPendingIntentId = 123456;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//                System.exit(0);
//                Toast.makeText(this,"Restart your app. Selected anchor will disappear",Toast.LENGTH_LONG).show();

            }
        }

    }
}
