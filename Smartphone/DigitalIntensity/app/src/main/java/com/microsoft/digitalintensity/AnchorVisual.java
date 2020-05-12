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
    private ComponentType componentType = ComponentType.HEART;
    private Scene scene;
    private Vector3 previousPosition;
    private Quaternion previousRotation;

    private String anchor;

    public void setAnchor (String anchor) {
        this.anchor = anchor;
    }

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
                if(this.anchor.equals("smile.sfb")){
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse(this.anchor))
                            .build()
                            .thenAccept(render -> {
                                transformableNode.setLocalScale(new Vector3(0.2f,0.2f,0.2f));
                                transformableNode.setRenderable(render);
                            })
                            .exceptionally(
                                    throwable -> {
                                        Log.e("3DRender", "Unable to load Renderable.", throwable);
                                        return null;
                                    });
                    break;
                }
                else if(this.anchor.equals("sad.sfb")){
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse(this.anchor))
                            .build()
                            .thenAccept(render -> {
                                transformableNode.setLocalScale(new Vector3(0.02f,0.02f,0.02f));
                                transformableNode.setRenderable(render);
                            })
                            .exceptionally(
                                    throwable -> {
                                        Log.e("3DRender", "Unable to load Renderable.", throwable);
                                        return null;
                                    });
                    break;
                }
                else {
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse(this.anchor))
                            .build()
                            .thenAccept(render -> {
                                transformableNode.setLocalScale(new Vector3(5f, 5f, 5f));
                                transformableNode.setRenderable(render);
                            })
                            .exceptionally(
                                    throwable -> {
                                        Log.e("3DRender", "Unable to load Renderable.", throwable);
                                        return null;
                                    });
                    break;
                }

            default:
                throw new IllegalStateException("Invalid shape");
        }

    }

    private void recreateRenderableOnUiThread() {
        anchor = this.getComponentType().toString().toLowerCase() + ".sfb";
        displayShape();
        if(previousPosition==null){
            previousPosition = transformableNode.getWorldPosition();

        }
        if(previousRotation==null){
            previousRotation= transformableNode.getWorldRotation();
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
