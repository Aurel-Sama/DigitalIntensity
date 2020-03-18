// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.microsoft.azure.spatialanchors.CloudSpatialAnchor;


class AnchorVisual {

    enum Shape{
        Cube

    }

    private final AnchorNode anchorNode;
    private TransformableNode transformableNode;
    private CloudSpatialAnchor cloudAnchor;
    private Context context;
    private Shape shape = Shape.Cube;
    private View view;
    private ComponentType componentType = ComponentType.FAN;
    private Scene scene;
    private Vector3 previousPosition;
    private Quaternion previousRotation;



    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public static String selectedId="";

    public AnchorVisual(ArFragment arFragment, Anchor localAnchor) {
        anchorNode = new AnchorNode(localAnchor);
        transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.getScaleController().setEnabled(false);
        transformableNode.getTranslationController().setEnabled(false);
        transformableNode.getRotationController().setEnabled(false);
        transformableNode.setParent(this.anchorNode);
    }


    public AnchorVisual(ArFragment arFragment, CloudSpatialAnchor cloudAnchor, Context context) {
        this(arFragment, cloudAnchor.getLocalAnchor());
        setCloudAnchor(cloudAnchor);
        this.context=context;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        if (this.shape != shape) {
            this.shape = shape;
            MainThreadContext.runOnUiThread(this::recreateRenderableOnUiThread);
        }
    }


    public AnchorNode getAnchorNode() {
        return this.anchorNode;
    }

    public CloudSpatialAnchor getCloudAnchor() {
        return this.cloudAnchor;
    }

    public Anchor getLocalAnchor() {
        return this.anchorNode.getAnchor();
    }

    public void render(ArFragment arFragment) {
        MainThreadContext.runOnUiThread(() -> {
            recreateRenderableOnUiThread();
            anchorNode.setParent(arFragment.getArSceneView().getScene());
        });
    }

    public void setCloudAnchor(CloudSpatialAnchor cloudAnchor) {
        this.cloudAnchor = cloudAnchor;
    }

    public void setMovable(boolean movable) {
        MainThreadContext.runOnUiThread(() -> {
            transformableNode.getTranslationController().setEnabled(movable);
            transformableNode.getRotationController().setEnabled(movable);
        });
    }

    public void destroy() {
        MainThreadContext.runOnUiThread(() -> {
            anchorNode.setRenderable(null);
            anchorNode.setParent(null);
            Anchor localAnchor =  anchorNode.getAnchor();
            if (localAnchor != null) {
                anchorNode.setAnchor(null);
                localAnchor.detach();
            }
        });
    }

    private void displayShape(){
        switch (shape) {
            case Cube:
                ModelRenderable.builder()
                        .setSource(context, Uri.parse("arrow.sfb"))
                        .build()
                        .thenAccept(render -> {
                            transformableNode.setLocalScale(new Vector3(5f,5f,5f));
                            transformableNode.setRenderable(render);
                        })
                        .exceptionally(
                                throwable -> {
                                    Log.e("3DRender", "Unable to load Renderable.", throwable);
                                    return null;
                                });
                break;

            default:
                throw new IllegalStateException("Invalid shape");
        }

    }

    private void displayText(){
        Typeface typeface = ResourcesCompat.getFont(context, R.font.aldrich);

        Button valuesBtn = view.findViewById(R.id.values_button);
        switch (componentType){
            case FAN:
                valuesBtn.setOnClickListener(view1 -> {
                    FanFragment fanFragment = new FanFragment();
                    FragmentHelper.isAlreadyOpen((FragmentActivity)context);
                    FragmentHelper.pushFragment((FragmentActivity)context, fanFragment);
                });
                break;
            case PUMP:
                valuesBtn.setOnClickListener(view1 -> {
                    PumpFragment pumpFragment = new PumpFragment();
                    FragmentHelper.isAlreadyOpen((FragmentActivity)context);
                    FragmentHelper.pushFragment((FragmentActivity)context, pumpFragment);
                });
                break;
            case VALVE:
                valuesBtn.setOnClickListener(view1 -> {
                    ValveFragment valveFragment = new ValveFragment();
                    FragmentHelper.isAlreadyOpen((FragmentActivity)context);
                    FragmentHelper.pushFragment((FragmentActivity)context, valveFragment);
                });
                break;
            case SENSOR:
                valuesBtn.setOnClickListener(view1 -> {
                    SensorFragment sensorFragment = new SensorFragment();
                    FragmentHelper.isAlreadyOpen((FragmentActivity)context);
                    FragmentHelper.pushFragment((FragmentActivity)context, sensorFragment);
                });
                break;
            default :
                break;
        }


        TextView componentNameTV = view.findViewById(R.id.component_name);
        TextView componentTypeTV = view.findViewById(R.id.component_type);
        valuesBtn.setTypeface(typeface);
        componentNameTV.setTypeface(typeface);
        componentTypeTV.setTypeface(typeface);
        componentNameTV.setText(cloudAnchor.getAppProperties().get("Message"));
        componentTypeTV.setText(componentType.toString());



        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept(renderable -> {
                    transformableNode.setLocalScale(new Vector3(0.30f,0.30f,0.30f));
                    transformableNode.setRenderable(renderable);

                });



    }

    private void recreateRenderableOnUiThread() {
        displayShape();
        if(previousPosition==null){
            previousPosition = transformableNode.getWorldPosition();

        }
        if(previousRotation==null){
            previousRotation= transformableNode.getWorldRotation();
        }

        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            selectedId = cloudAnchor.getIdentifier();

            if(transformableNode.getRenderable() instanceof ViewRenderable) {
                transformableNode.setWorldPosition(previousPosition);
                transformableNode.setWorldRotation(previousRotation);
                displayShape();
            }
            else if(transformableNode.getRenderable() instanceof ModelRenderable){
                Vector3 cameraPosition = scene.getCamera().getWorldPosition();
                Vector3 objectPosition = transformableNode.getWorldPosition();
                Vector3 direction = Vector3.subtract(cameraPosition, objectPosition);
                Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
                transformableNode.setWorldRotation(lookRotation);
                displayText();

            }

        });

    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
