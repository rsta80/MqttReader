package es.ugr.mqttreader.mqtt;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 使用EventBus分发事件
 *
 * @author LichFaker on 16/3/25.
 * @Email lichfaker@gmail.com
 */
public class MqttCallbackBus implements MqttCallback {

    public String r="";

    @Override
    public void connectionLost(Throwable cause) {
        Log.e("Broker",cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log.d("Broker",topic + "====" + message.toString());
        r=message.toString();

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
