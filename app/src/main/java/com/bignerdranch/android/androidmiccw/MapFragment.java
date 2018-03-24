package com.bignerdranch.android.androidmiccw;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MapFragment extends Fragment {
    private static final int REQUEST_ERROR = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
    @Override           //  TO CHECK IF UPDATED PLAY SERVICES IS INSTALLED
    public void onResume() {    // CODE COPIED FROM PREVIOUS IN LAB EXERCISE
        super.onResume();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(getContext());///////// made change here from 'this' to 'getcontext()'

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability
                    .getErrorDialog(getActivity(), errorCode, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // Leave if services are unavailable.
                                    getActivity().finish(); ///////// made a change here from finish to getactivity.finish
                                }
                            });
            errorDialog.show();
        }
    }
}

