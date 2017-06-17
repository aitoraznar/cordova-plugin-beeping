package com.aitoraznar.beeping;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.beeping.AndroidBeepingCore.BeepEventListener;
import com.beeping.AndroidBeepingCore.BeepingCore;

import org.apache.cordova.CallbackContext;
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
    private static final String ACTION_BAZ = "com.aitoraznar.beeping.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.aitoraznar.beeping.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.aitoraznar.beeping.extra.PARAM2";

    public BeepingService() {
        super("BeepingService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startBeepingListen(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BeepingService.class);
        intent.setAction(ACTION_START_BEEPING_LISTEN);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, BeepingService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_BEEPING_LISTEN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                startBeepingListen();
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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
    }


    private String getAppId() {
        return "8vLYXZrfqtoWPF6rYBt8KYpeFqeju9SJ";
        //return preferences.getString("APP_ID", "");
    }

    private void startBeepingListen() {
        Log.v(LOG_TAG, "startBeepingListen - enter");
        String appId = "";

        appId = getAppId();
        Log.v(LOG_TAG, "execute: appId=" + appId);

        Log.v(LOG_TAG, "startBeepingListen - start");
        beeping = new BeepingCore(appId, this);
        Log.v(LOG_TAG, "startBeepingListen - listen");
        beeping.startBeepingListen();
    }
}
