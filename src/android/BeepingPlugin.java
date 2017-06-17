package com.aitoraznar.beeping;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import com.beeping.AndroidBeepingCore.*;


public class BeepingPlugin extends CordovaPlugin {
    private static final String LOG_TAG = "BeepingPlugin";
    protected final static String[] permissions = { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int LISTEN_BEEPING = 0;
    //public static final int SAVE_TO_ALBUM_SEC = 1;

    public CallbackContext callbackContext;

    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    protected void pluginInitialize() {
        final Context context = getApplicationContext();
        final Bundle extras = this.cordova.getActivity().getIntent().getExtras();


        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Log.d(LOG_TAG, "Starting Beeping plugin");
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if ("startBeepingListen".equals(action)) {
            //startBeepingListen(callbackContext);

            prepareForListenBeeping();

            return true;
        }

        return false;
    }

    /**
     * Check for permissions
     */
    public void prepareForListenBeeping() {
        boolean recordAudioPermission = PermissionHelper.hasPermission(this, Manifest.permission.RECORD_AUDIO);
        boolean writeExternalStoragePermission = PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // check the package info to determine if the permission is present.

        if (!recordAudioPermission) {
            recordAudioPermission = true;
            try {
                PackageManager packageManager = this.cordova.getActivity().getPackageManager();
                String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                if (permissionsInPackage != null) {
                    for (String permission : permissionsInPackage) {
                        if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
                            recordAudioPermission = false;
                            break;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                // We are requesting the info for our package, so this should
                // never be caught
            }
        }

        if (recordAudioPermission && writeExternalStoragePermission) {
            startBeepingListenService();
        } else if (writeExternalStoragePermission && !recordAudioPermission) {
            PermissionHelper.requestPermission(this, LISTEN_BEEPING, Manifest.permission.RECORD_AUDIO);
        } else if (!writeExternalStoragePermission && recordAudioPermission) {
            PermissionHelper.requestPermission(this, LISTEN_BEEPING, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            PermissionHelper.requestPermissions(this, LISTEN_BEEPING, permissions);
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case LISTEN_BEEPING:
                startBeepingListenService();
                break;
        }
    }

    private void startBeepingListenService() {
        final Context context = getApplicationContext();
        Intent intent = new Intent(context, BeepingService.class);
        intent.setAction("startBeepingListen");
        context.startService(intent);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        // Stop listening for beeps
        //beeping.stopBeepingListen();

        // Deallocating memory
        //beeping.dealloc();
    }

}
