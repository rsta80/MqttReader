package es.ugr.mqttreader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.macroyau.thingspeakandroid.ThingSpeakChannel;
import com.macroyau.thingspeakandroid.ThingSpeakLineChart;
import com.macroyau.thingspeakandroid.ThingSpeakService;
import com.macroyau.thingspeakandroid.model.ChannelFeed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ThingSpeakActivity extends AppCompatActivity {

    private ThingSpeakChannel tsChannel;
    private ThingSpeakService tsService;
    private ThingSpeakLineChart tsChart;
    private LineChartView chartView;
    private TextView txtChannel, txtField, txtLastValue;
    private String readApiKey = "Y74ZH5MMFTU8EANY",
            channel = "422677",
            field = "2";
    private Button btn, btn2;


    Intent mServiceIntent;
    private GetLastValueService mSensorService;

    Context ctx;

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thing_speak_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ThingSpeak Client");
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        channel = prefs.getString("chn", null);
        field = prefs.getString("fld", null);
        readApiKey = prefs.getString("apik", null);
        if (channel == null && field == null && readApiKey == null) {
            readApiKey = "Y74ZH5MMFTU8EANY";
            channel = "422677";
            field = "2";
        }

        ctx = this;
        mSensorService = new GetLastValueService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());

        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }

        //Subtitle
        txtChannel = findViewById(R.id.txt_channel);
        txtField = findViewById(R.id.txt_field);
        btn = findViewById(R.id.btn_parameters);
        btn2 = findViewById(R.id.btn_broker);

        btn.setOnClickListener(new View.OnClickListener() {

                                   @Override
                                   public void onClick(View v) {
                                       Intent intent = new Intent(ThingSpeakActivity.this, NewChannel.class);
                                       startActivity(intent);
                                   }
                               });

        txtChannel.setText("Canal: " + channel);
        txtField.setText("Campo: " + field);

        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThingSpeakActivity.this, BrockerActivity.class);
                startActivity(intent);
            }
        });


        // Connect to ThinkSpeak Channel
        tsChannel = new ThingSpeakChannel(Integer.parseInt(channel), readApiKey);
        // Set listener for Channel feed update events
        tsChannel.setChannelFeedUpdateListener(new ThingSpeakChannel.ChannelFeedUpdateListener() {
            @Override
            public void onChannelFeedUpdated(long channelId, String channelName, ChannelFeed channelFeed) {
                Date lastUpdate = channelFeed.getChannel().getUpdatedAt();
                DateFormat df = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
                Toast.makeText(ThingSpeakActivity.this,
                        "Ultima actualización:\n" + df.format(lastUpdate),
                        Toast.LENGTH_LONG).show();
            }
        });
        // Fetch the specific Channel feed
        tsChannel.loadChannelFeed();

        // Create a Calendar object dated 5 minutes ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);

        // Configure LineChartView
        chartView = findViewById(R.id.graphic_chart);
        chartView.setZoomEnabled(false);
        chartView.setValueSelectionEnabled(true);

        // Create a line chart from Field Id of ThinkSpeak Channel
        tsChart = new ThingSpeakLineChart(Integer.parseInt(channel),
                Integer.parseInt(field), readApiKey);

        // Get 200 entries at maximum
        tsChart.setNumberOfEntries(10);
        // Set value axis labels on 10-unit interval
        tsChart.setValueAxisLabelInterval(10);
        // Set date axis labels on 5-minute interval
        tsChart.setDateAxisLabelInterval(1);
        // Show the line as a cubic spline
        tsChart.useSpline(true);
        // Set the line color
        tsChart.setLineColor(Color.parseColor("#D32F2F"));
        // Set the axis color
        tsChart.setAxisColor(Color.parseColor("#455a64"));
        // Set the starting date (5 minutes ago) for the default viewport of the chart
        tsChart.setChartStartDate(calendar.getTime());

        setRepeatingAsyncTask(tsChart);

    }

    private void setRepeatingAsyncTask(final ThingSpeakLineChart tsChart) {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            final GetLastValue mLastValue = new GetLastValue();
                            // Set listener for chart data update
                            tsChart.setListener(new ThingSpeakLineChart.ChartDataUpdateListener() {
                                @Override
                                public void onChartDataUpdated(long channelId, int fieldId, String title, LineChartData lineChartData, Viewport maxViewport, Viewport initialViewport) {
                                    // Set chart data to the LineChartView
                                    chartView.setLineChartData(lineChartData);
                                    // Set scrolling bounds of the chart
                                    chartView.setMaximumViewport(maxViewport);
                                    // Set the initial chart bounds
                                    chartView.setCurrentViewport(initialViewport);
                                    // Update last value entry
                                    try {
                                        Double distancia = mLastValue.execute(channel, field, readApiKey).get();
                                        if (distancia < 50) {
                                            // Create an explicit intent for an Activity in your app
                                            Log.i("ThingSpeak","Distancia muy corta");
                                            Toast.makeText(ThingSpeakActivity.this,
                                                    "Distancia muy corta:\n"+ distancia,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    Log.i("Loop", "Pase por aqui");
                                }
                            });
                            // Load chart data asynchronously
                            tsChart.loadChartData();
                        } catch (Exception e) {
                            Log.e("HttpRequest", Log.getStackTraceString(e));
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 10 * 1000);  // interval of one minute

    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    private class GetLastValue extends AsyncTask<String, Void, Double> {
        @Override
        protected Double doInBackground(String... args) {
            // we use the OkHttp library from https://github.com/square/okhttp
            txtLastValue = findViewById(R.id.txt_last_value);
            Double lastValue = 0.0;
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                if (args[2] != null) {
                    url = new URL("https://api.thingspeak.com/channels/" + args[0] + "/fields/" + args[1] + "/last?key=" + args[2]);
                } else {
                    url = new URL("https://api.thingspeak.com/channels/" + args[0] + "/fields/" + args[1] + "/last");
                }
                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream content = (InputStream) urlConnection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = in.readLine()) != null) {
                    lastValue = Double.parseDouble(line);
                }

                return (lastValue);

            } catch (Exception e) {
                Log.e("HttpRequest", Log.getStackTraceString(e));
                return 0.0;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Double result) {
            txtLastValue.setText(result.toString());
            Log.i("HttpRequest", result.toString());
        }
    }

}


