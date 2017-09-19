package com.aitoraznar.beeping;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.beeping.AndroidBeepingCore.BeepEventListener;
import com.beeping.AndroidBeepingCore.BeepingCore;
import com.flybuy.cordova.location.Constants;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BeepingService extends IntentService implements BeepEventListener {
    private static final String LOG_TAG = "BeepingPlugin";
    BeepingCore beeping;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_START_BEEPING_LISTEN = "startBeepingListen";
    private static final String ACTION_STOP_BEEPING_LISTEN = "stopBeepingListen";
    private static final String ACTION_BAZ = "com.aitoraznar.beeping.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.aitoraznar.beeping.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.aitoraznar.beeping.extra.PARAM2";

    public BeepingService() {
        super("BeepingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_BEEPING_LISTEN.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                startBeepingListen();
            } else if (ACTION_STOP_BEEPING_LISTEN.equals(action)) {
                stopBeepingListen();
            }
        }
    }

    /**
     Sample Response Beep JSONObject ()
     {
     "type": "url",
     "data": "https://www.youtube.com/watch?v=DiTECkLZ8HM",
     "url": "https://www.youtube.com/watch?v=DiTECkLZ8HM",
     "title": "SPIDER-MAN: HOMECOMING - Official Trailer #2 (HD)",
     "brand": "YouTube",
     "imgSrc": "https://i.ytimg.com/vi/DiTECkLZ8HM/maxresdefault.jpg",
     "ogType": "video",
     "_id": "C9bPJJNtij6JAhKeS",
     "avatar": "https://s3.amazonaws.com/beepingfiles/images/beeping_avatar.png",
     "init": 0,
     "final": 10000,
     "createdAt": "2017-03-22T19:18:45.178Z",
     "updatedAt": "2017-03-22T19:18:45.180Z"
     }
     */
    @Override
    public void onBeepResponseEvent(JSONObject beep) {
        Log.d(LOG_TAG, "onBeepingEvent: " + beep.toString());

        Intent intent = new Intent(BeepingPlugin.CALLBACK_BEEPING_RECEIVED);
        intent.putExtras(createBeepingBundle(beep));
        getApplicationContext().sendBroadcast(intent);
    }


    private String getAppId() {
        return "8vLYXZrfqtoWPF6rYBt8KYpeFqeju9SJ";
        //return preferences.getString("APP_ID", "");
    }

    private void startBeepingListen() {
        Log.v(LOG_TAG, "startBeepingListen - enter");

        try {
            String appId = "";

            appId = getAppId();
            Log.v(LOG_TAG, "execute: appId=" + appId);

            Log.v(LOG_TAG, "startBeepingListen - start");
            beeping = new BeepingCore(appId, this);
            Log.v(LOG_TAG, "startBeepingListen - listen");
            beeping.startBeepingListen();
        } catch (Exception e) {
            // Throw error to cordova
            Log.v(LOG_TAG, "startBeepingListen - ERROR: " + e.getMessage());
        }

    }

    private void stopBeepingListen() {
        Log.v(LOG_TAG, "stopBeepingListen - enter");

        try {
            Log.v(LOG_TAG, "stopBeepingListen - stop");
            beeping.stopBeepingListen();
        } catch (Exception e) {
            // Throw error to cordova
            Log.v(LOG_TAG, "stopBeepingListen - ERROR: " + e.getMessage());
        }

    }

    private Bundle createBeepingBundle(JSONObject beep) {
        Bundle bundle = new Bundle();

        try {
            bundle.putString("type", beep.getString("type"));
            bundle.putString("data", beep.getString("data"));
            bundle.putString("url", beep.getString("url"));
            bundle.putString("title", beep.getString("title"));
            bundle.putString("brand", beep.getString("brand"));
            bundle.putString("imgSrc", beep.getString("imgSrc"));
            bundle.putString("ogType", beep.getString("ogType"));
            bundle.putString("_id", beep.getString("_id"));
            bundle.putString("avatar", beep.getString("avatar"));
            bundle.putInt("init", beep.getInt("init"));
            bundle.putInt("final", beep.getInt("final"));
            bundle.putString("createdAt", beep.getString("createdAt"));
            bundle.putString("updatedAt", beep.getString("updatedAt"));
        } catch(JSONException e) {
            Log.d(LOG_TAG, "ERROR CREATING createBeepingBundle JSON" + e);
        }

        return bundle;
    }
}
