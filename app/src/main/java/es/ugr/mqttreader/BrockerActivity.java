package es.ugr.mqttreader;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import es.ugr.mqttreader.mqtt.MqttManager;

public class BrockerActivity extends AppCompatActivity {

    public static final String URL = "tcp://m11.cloudmqtt.com:11409";

    private String userName = "qpnhccpj";

    private String password = "4yl31SjMrEpN";

    private String clientId = "clientId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brocker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        boolean b = MqttManager.getInstance().creatConnect(URL, userName, password, clientId);
        Log.i("broker","isConnected: " + b);

        setRepeatingAsyncTask();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            MqttManager.getInstance().subscribe("/telemeter/out", 2);
                        } catch (Exception e) {
                            Log.e("HttpRequest", Log.getStackTraceString(e));
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 10 * 1000);  // interval of one minute

    }

}
