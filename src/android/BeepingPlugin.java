package com.aitoraznar.beeping;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BeepingPlugin extends CordovaPlugin {
    private static final String LOG_TAG = "BeepingPlugin";
    protected final static String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int LISTEN_BEEPING = 0;
    public static String CALLBACK_BEEPING_RECEIVED = "BEEPING_RECEIVED";
    //public static final int SAVE_TO_ALBUM_SEC = 1;

    private Intent beepingIntent;
    private String isDebugging = "true";
    public CallbackContext callbackContext;
    private boolean isServiceBound = false;



    /**
     * Gets the application context from cordova's main activity.
     *
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

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true); // Keep callback
            callbackContext.sendPluginResult(pluginResult);
            //callbackContext.success();

            return true;
        } else if ("stopBeepingListen".equals(action)) {
            stopBeepingListenService();
            callbackContext.success();

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

    // Used to (un)bind the service to with the activity
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // Nothing to do here
            Log.i(LOG_TAG, "SERVICE CONNECTED TO MAIN ACTIVITY");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here
            Log.i(LOG_TAG, "SERVICE DISCONNECTED");
        }
    };

    private BroadcastReceiver beepingUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (debug()) {
                Log.d(LOG_TAG, "Location Received, ready for callback");
            }
            if (callbackContext != null) {

                if (debug()) {
                    Toast.makeText(context, "We received a beeping", Toast.LENGTH_SHORT).show();
                }

                final Bundle b = intent.getExtras();
                final String errorString = b.getString("error");

                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        PluginResult pluginResult;

                        if (b == null) {
                            String unkownError = "An Unkown Error has occurred, there was no Location attached";
                            pluginResult = new PluginResult(PluginResult.Status.ERROR, unkownError);

                        } else if (errorString != null) {
                            Log.d(LOG_TAG, "ERROR " + errorString);
                            pluginResult = new PluginResult(PluginResult.Status.ERROR, errorString);

                        } else {
                            JSONObject data = bundleToJSON(intent.getExtras());
                            pluginResult = new PluginResult(PluginResult.Status.OK, data);
                        }

                        if (pluginResult != null) {
                            pluginResult.setKeepCallback(true);
                            callbackContext.sendPluginResult(pluginResult);
                        }
                    }
                });
            } else {
                if (debug()) {
                    Toast.makeText(context, "We received a location update but locationUpdate was null", Toast.LENGTH_SHORT).show();
                }
                Log.w(LOG_TAG, "WARNING LOCATION UPDATE CALLBACK IS NULL, PLEASE RUN REGISTER LOCATION UPDATES");
            }
        }
    };

    private void startBeepingListenService() {
        Activity activity = this.cordova.getActivity();

        beepingIntent = new Intent(activity, BeepingService.class);
        beepingIntent.setAction("startBeepingListen");
        //activity.startService(beepingIntent);

        // Register BroadcastReceiver
        isServiceBound = bindServiceToWebview(activity, beepingIntent);
    }

    private void stopBeepingListenService() {
        Activity activity = this.cordova.getActivity();

        beepingIntent.setAction("stopBeepingListen");

        isServiceBound = unbindServiceFromWebview(activity, beepingIntent);
    }

    public Boolean debug() {
        if (Boolean.parseBoolean(isDebugging)) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean bindServiceToWebview(Context context, Intent intent) {
        Boolean didBind = false;

        try {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            context.startService(intent);

            webView.getContext().registerReceiver(beepingUpdateReceiver, new IntentFilter(CALLBACK_BEEPING_RECEIVED));

            didBind = true;
        } catch(Exception e) {
            Log.e(LOG_TAG, "ERROR BINDING SERVICE" + e);
        }

        return didBind;
    }

    private Boolean unbindServiceFromWebview(Context context, Intent intent) {
        Boolean didUnbind = false;

        try {
            context.unbindService(serviceConnection);
            context.stopService(intent);

            webView.getContext().unregisterReceiver(beepingUpdateReceiver);

            didUnbind = true;
        } catch(Exception e) {
            Log.e(LOG_TAG, "ERROR UNBINDING SERVICE" + e);
        }

        return didUnbind;
    }

    private JSONObject bundleToJSON(Bundle b) {
        JSONObject data = new JSONObject();
        try {
            data.put("type", b.getString("type"));
            data.put("data", b.getString("data"));
            data.put("url", b.getString("url"));
            data.put("title", b.getString("title"));
            data.put("brand", b.getString("brand"));
            data.put("imgSrc", b.getString("imgSrc"));
            data.put("ogType", b.getString("ogType"));
            data.put("_id", b.getString("_id"));
            data.put("avatar", b.getString("avatar"));
            data.put("init", b.getInt("init"));
            data.put("final", b.getInt("final"));
            data.put("createdAt", b.getString("createdAt"));
            data.put("updatedAt", b.getString("updatedAt"));
        } catch(JSONException e) {
            Log.d(LOG_TAG, "ERROR CREATING JSON" + e);
        }

        return data;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Activity activity = this.cordova.getActivity();

        if (isServiceBound) {
            activity.stopService(beepingIntent);
            unbindServiceFromWebview(activity, beepingIntent);
        }

        // Deallocating memory
        //beeping.dealloc();
    }

}
