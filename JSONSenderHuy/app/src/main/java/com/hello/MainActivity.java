package com.hello;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int data_id;
    float data_temperature, data_light;

    EditText id, temperature, light;
    Button btnSubmit;

    Gson gson = new Gson();
    Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = findViewById(R.id.id);
        temperature = findViewById(R.id.temperature);
        light = findViewById(R.id.light);

        btnSubmit = findViewById(R.id.btnSubmit); btnSubmit.setOnClickListener(this);

//        new Timer().scheduleAtFixedRate(new TimerTask(){
//            @Override
//            public void run(){
//                Random rand = new Random();
//                int randnum = rand.nextInt(30+1) + 20;
//                Log.i("tag", String.format("random: %s", randnum));
//
//                if(mqttHelper != null)
//                    mqttHelper.connectToPublish(String.format("%s", randnum));
//            }
//        },0,10000);

        startMQTT();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnSubmit) {
            if (id.getText().toString().equals("")) {
                id.requestFocus();
            }
            else if (temperature.getText().toString().equals("")) {
                temperature.requestFocus();
            }
            else if (light.getText().toString().equals("")) {
                light.requestFocus();
            }
            else if (!id.getText().toString().equals("") && !temperature.getText().toString().equals("") && !light.getText().toString().equals("")) {
                data_id = Integer.parseInt(id.getText().toString());
                data_temperature = Float.parseFloat(temperature.getText().toString());
                data_light = Float.parseFloat(light.getText().toString());

                id.getText().clear();
                temperature.getText().clear();
                light.getText().clear();

                //Reset cursor to the first EditText
                id.requestFocus();

                Log.d( "Data", String.format("ID: %s", data_id));
                Log.d( "Data", String.format("Temperature: %s", data_temperature));
                Log.d( "Data", String.format("Light:  %s", data_light));

                String json_string = String.format("{\"id\":%s, \"temperature\":%s, \"light\":%s}", data_id, data_temperature, data_light);

                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON", String.format("%s",data));

                if(mqttHelper != null) {
                    mqttHelper.connectToPublish(String.format("%s", data));
                }
            }
        }
    }

    class Data {

        private final String id;
        private final String temperature;
        private final String light;

        public Data(String id, String temperature, String light) {
            this.id = id;
            this.temperature = temperature;
            this.light = light;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("JSON { ").append("\"ID\":\"")
                    .append(id).append("\", \"Temperature\":\"")
                    .append(temperature).append("\", \"Light\":\"")
                    .append(light).append("\" }").toString();
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
                Log.d("MQTT", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
