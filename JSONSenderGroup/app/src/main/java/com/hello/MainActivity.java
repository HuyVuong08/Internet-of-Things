package com.hello;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.hello.MainActivity.Data_Type.ID;
import static com.hello.MainActivity.Data_Type.Light;
import static com.hello.MainActivity.Data_Type.None;
import static com.hello.MainActivity.Data_Type.Temperature;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int data_id, json_id;
    float data_temperature, data_light, json_temperature, json_light;

    enum Data_Type {ID, Temperature, Light, None};

    Data_Type data_type;

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
            else if (!id.getText().toString().equals("")
                    && !temperature.getText().toString().equals("")
                    && !light.getText().toString().equals("")) {
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

                String json_string = String.format("{\"id\":%s,\"temperature\":%s,\"light\":%s}",
                        data_id, data_temperature, data_light);

                data = gson.fromJson(json_string, Data.class);

                Log.d("JSON-Send", String.format("%s",data));

                if(mqttHelper != null) {
                    mqttHelper.connectToPublish(String.format("%s", data));
                }
            }

            String json = String.format("%s", data);

            JsonReader jsonReader = new JsonReader(new StringReader(json));
            jsonReader.setLenient(true);

            try
            {
                while (jsonReader.hasNext())
                {
                    JsonToken nextToken = jsonReader.peek();

                    if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {

                        jsonReader.beginObject();

                    } else if (JsonToken.NAME.equals(nextToken)) {

                        String name = jsonReader.nextName();
                        if (name.equals("ID")) {
                            data_type = ID;
                        }
                        else if (name.equals("Temperature")) {
                            data_type = Temperature;
                        }
                        else if (name.equals("Light")) {
                            data_type = Light;
                        }
                        else {
                            data_type = None;
                        }

                    } else if (JsonToken.STRING.equals(nextToken)) {

                        String value = jsonReader.nextString();
                        switch (data_type) {

                            case ID:
                                json_id = (int) Float.parseFloat(value);
                                data_type = None;
                                Log.d("JSON-Receive" ,String.format("ID: %s", json_id));
                                break;

                            case Temperature:
                                json_temperature = Float.parseFloat(value);
                                data_type = None;
                                Log.d("JSON-Receive" ,String.format("Temperature: %s", json_temperature));
                                break;

                            case Light:
                                json_light = Float.parseFloat(value);
                                data_type = None;
                                Log.d("JSON-Receive" ,String.format("Light: %s", json_light));
                                break;

                            default:
                                break;
                        }

                    } else if (JsonToken.NULL.equals(nextToken)) {

                        jsonReader.nextNull();

                    } else if (JsonToken.END_OBJECT.equals(nextToken)) {

                        jsonReader.endObject();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    jsonReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
            return new StringBuilder().append("{\"feeds\":{\"temperature").append(id).append("\":\"")
                    .append(temperature).append("\",\"light").append(id).append("\":\"")
                    .append(light).append("\"}}").toString();
//            .append("\"},\"location\": {\"id\"").append(id)

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
//                String json = String.format("%s", mqttMessage);
//
//                JsonReader jsonReader = new JsonReader(new StringReader(json));
//                jsonReader.setLenient(true);
//
//                try {
//                    while (jsonReader.hasNext()) {
//                        JsonToken nextToken = jsonReader.peek();
//
//                        if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
//
//                            jsonReader.beginObject();
//
//                        } else if (JsonToken.NAME.equals(nextToken)) {
//
//                            String name = jsonReader.nextName();
//                            if (name.equals("ID")) {
//                                data_type = ID;
//                            } else if (name.equals("Temperature")) {
//                                data_type = Temperature;
//                            } else if (name.equals("Light")) {
//                                data_type = Light;
//                            } else {
//                                data_type = None;
//                            }
//
//                        } else if (JsonToken.STRING.equals(nextToken)) {
//
//                            String value = jsonReader.nextString();
//                            switch (data_type) {
//
//                                case ID:
//                                    json_id = (int) Float.parseFloat(value);
//                                    data_type = None;
//                                    Log.d("JSON-Receive", String.format("ID: %s", json_id));
//                                    break;
//
//                                case Temperature:
//                                    json_temperature = Float.parseFloat(value);
//                                    data_type = None;
//                                    Log.d("JSON-Receive", String.format("Temperature: %s", json_temperature));
//                                    break;
//
//                                case Light:
//                                    json_light = Float.parseFloat(value);
//                                    data_type = None;
//                                    Log.d("JSON-Receive", String.format("Light: %s", json_light));
//                                    break;
//
//                                default:
//                                    break;
//                            }
//
//                        } else if (JsonToken.NULL.equals(nextToken)) {
//
//                            jsonReader.nextNull();
//
//                        } else if (JsonToken.END_OBJECT.equals(nextToken)) {
//
//                            jsonReader.endObject();
//
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        jsonReader.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

//                String data = mqttMessage.toString();
//                if(mqttMessage.toString().contains(STOP_SIGNAL)){
//                    data = "-1";
//                }
//                dataRes.add(new DataPoint(dataRes.size(), Integer.parseInt(data)));
//                DataPoint[] dataTemp = new DataPoint[0];
//                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataRes.toArray(dataTemp));
//
//                graphSensor1.addSeries(series);
//                graphSensor2.addSeries(series);
//
        }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}

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
