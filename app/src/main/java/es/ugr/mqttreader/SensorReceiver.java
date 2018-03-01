package es.ugr.mqttreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by rastap on 1/03/18.
 */

public class SensorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");

        context.startService(new Intent(context, GetLastValueService.class));
    }
}
