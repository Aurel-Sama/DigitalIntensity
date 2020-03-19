// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.microsoft.digitalintensity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;

public class AnchorPlacementFragment extends Fragment {
    private ArFragment arFragment;
    private AnchorVisual visual = null;
    private AnchorPlacementListener listener;

    private TextView hintText;
    private Button confirmPlacementButton;
    private Spinner dropdownComponent;
    private SeekBar translateSK;
    private HitResult savedHitResult;


    private float translation =0f;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.coarse_reloc_anchor_placement, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = (FragmentActivity)context;
        arFragment = (ArFragment)activity.getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmPlacementButton = view.findViewById(R.id.confirm_placement);
        hintText = view.findViewById(R.id.hint_text);
        dropdownComponent = view.findViewById(R.id.dropdown_component);
        translateSK = view.findViewById(R.id.seekBar);
    }

    @Override
    public void onStart() {
        super.onStart();

        confirmPlacementButton.setEnabled(false);
        arFragment.setOnTapArPlaneListener(this::onTapArPlaneListener);
        confirmPlacementButton.setOnClickListener(this::onConfirmPlacementClicked);
        ArrayAdapter<ComponentType> adapter = new ArrayAdapter<>(arFragment.getContext(), android.R.layout.simple_spinner_dropdown_item, ComponentType.values());
        dropdownComponent.setAdapter(adapter);
        dropdownComponent.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(visual!=null){
                    visual.destroy();
                    visual = null;

                    Anchor localAnchor = savedHitResult.getTrackable().createAnchor(
                            savedHitResult.getHitPose().compose(Pose.makeTranslation(0, translation, 0)));
                    visual = new AnchorVisual(arFragment, localAnchor);
                    visual.setContext(arFragment.getContext());
                    visual.setMovable(true);
                    visual.setShape(getSelectedShape());
                    visual.setComponentType((ComponentType) dropdownComponent.getSelectedItem());
                    visual.setAnchor(dropdownComponent.getSelectedItem().toString().toLowerCase()+".sfb");
                    visual.render(arFragment);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        translateSK.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                translation=translateSK.getProgress()/10f;
                if(visual!=null){
                    visual.destroy();
                    visual = null;

                    Anchor localAnchor = savedHitResult.getTrackable().createAnchor(
                            savedHitResult.getHitPose().compose(Pose.makeTranslation(0, translation, 0)));
                    visual = new AnchorVisual(arFragment, localAnchor);
                    visual.setContext(arFragment.getContext());
                    visual.setMovable(true);
                    visual.setShape(getSelectedShape());
                    visual.render(arFragment);

                }

            }
        });

    }

    public void setListener(AnchorPlacementListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStop() {
        arFragment.setOnTapArPlaneListener(null);

        if (visual != null) {
            visual.destroy();
            visual = null;
        }

        super.onStop();
    }


    private void onTapArPlaneListener(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (visual != null) {
            visual.destroy();
            visual = null;
        }
        savedHitResult = hitResult;
        Anchor localAnchor = hitResult.getTrackable().createAnchor(
                hitResult.getHitPose().compose(Pose.makeTranslation(0, translation, 0)));
        //Without translation on axis Y
        //Anchor localAnchor = hitResult.createAnchor();


        visual = new AnchorVisual(arFragment, localAnchor);
        visual.setContext(arFragment.getContext());
        visual.setMovable(true);
        visual.setShape(getSelectedShape());
        visual.setComponentType((ComponentType) dropdownComponent.getSelectedItem());
        visual.setAnchor(dropdownComponent.getSelectedItem().toString().toLowerCase()+".sfb");
        visual.render(arFragment);


        hintText.setText(R.string.hint_adjust_anchor);

        confirmPlacementButton.setEnabled(true);
    }

    private void onShapeSelected(RadioGroup radioGroup, int selectedId) {
        if (visual == null) {
            return;
        }

        visual.setShape(getSelectedShape());
    }

    private AnchorVisual.Shape getSelectedShape() {
            return AnchorVisual.Shape.Cube;
    }

    private void onConfirmPlacementClicked(View view) {
        if (visual != null) {
            visual.setComponentType((ComponentType) dropdownComponent.getSelectedItem());
            AnchorVisual placedAnchor = visual;
            visual = null;
            placedAnchor.setMovable(false);
            if (listener != null) {
                listener.onAnchorPlaced(placedAnchor);
            }
        }
    }
}
