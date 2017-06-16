package com.aitoraznar.beeping;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import com.beeping.AndroidBeepingCore.*;


public class BeepingPlugin extends CordovaPlugin implements BeepEventListener {
    private static final String LOG_TAG = "BeepingPlugin";
    BeepingCore beeping;

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
        if ("startBeepingListen".equals(action)) {
            startBeepingListen(callbackContext);
            return true;
        }

        return false;
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
        Log.d("APP", "onBeepingEvent: " + beep.toString());
    }


    private String getAppId() {
        return "8vLYXZrfqtoWPF6rYBt8KYpeFqeju9SJ";
		//return preferences.getString("APP_ID", "");
	}

    private void startBeepingListen(CallbackContext callbackContext) {
        Log.v(LOG_TAG, "startBeepingListen - enter");
        String appId = "";

        appId = getAppId();
        Log.v(LOG_TAG, "execute: appId=" + appId);

        Context context = getApplicationContext();

        Log.v(LOG_TAG, "startBeepingListen - start");
        beeping = new BeepingCore(appId, this);
        Log.v(LOG_TAG, "startBeepingListen - listen");
        beeping.startBeepingListen();

        callbackContext.success();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        // Stop listening for beeps
        beeping.stopBeepingListen();

        // Deallocating memory
        //beeping.dealloc();
    }

}
