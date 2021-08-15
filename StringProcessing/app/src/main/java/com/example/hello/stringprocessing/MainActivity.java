package com.example.hello.stringprocessing;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView Text;
    Gson gson = new Gson();
    Data data;
    String json_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Text = findViewById(R.id.Text);

        startMQTT();

        ArrayList<String> speech;
        speech = new ArrayList<String>();
        speech.add("stop the sensor node 1");

        if (speech.get(0).toUpperCase().endsWith("node 1".toUpperCase())) {

            if (speech.get(0).toUpperCase().startsWith("start".toUpperCase())) {

                json_string = String.format("{\"id\":\"node1\",\"power\":\"ON\"}");
                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON-Send", String.format("%s", data));

                if(mqttHelper != null) {

                    mqttHelper.connectToPublish(String.format("%s", data));

                }

            } else if (speech.get(0).toUpperCase().startsWith("stop".toUpperCase())) {

                json_string = String.format("{\"id\":\"node1\",\"power\":\"OFF\"}");
                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON-Send", String.format("%s", data));

                if(mqttHelper != null) {

                    mqttHelper.connectToPublish(String.format("%s", data));

                }
            }

        } else if (speech.get(0).toUpperCase().endsWith("node 2".toUpperCase())) {

            if (speech.get(0).toUpperCase().startsWith("start".toUpperCase())) {

                json_string = String.format("{\"id\":\"node2\",\"power\":\"ON\"}");
                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON-Send", String.format("%s", data));

                if(mqttHelper != null) {

                    mqttHelper.connectToPublish(String.format("%s", data));

                }

            } else if (speech.get(0).toUpperCase().startsWith("stop".toUpperCase())) {

                json_string = String.format("{\"id\":\"node2\",\"power\":\"OFF\"}");
                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON-Send", String.format("%s", data));

                if(mqttHelper != null) {

                    mqttHelper.connectToPublish(String.format("%s", data));

                }
            }
        }
    }

    class Data {

        private final String id;
        private final String power;

        public Data(String id, String power) {
            this.id = id;
            this.power = power;
        }

        @Override
        public String toString() {

            return new StringBuilder().append("{\"feeds\":{\"").append(id).append("\":\"")
                    .append(power).append("\"},\"location\":\"21\"}").toString();

        }
    }

    MQTTHelper mqttHelper;
    private void startMQTT(){

        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                Log.d("MQTT-Receive", mqttMessage.toString());

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
