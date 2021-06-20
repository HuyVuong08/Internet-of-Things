package com.example.hello.livegraph;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static com.example.hello.livegraph.MainActivity.Data_Type.Light1;
import static com.example.hello.livegraph.MainActivity.Data_Type.Node1_Control;
import static com.example.hello.livegraph.MainActivity.Data_Type.None;
import static com.example.hello.livegraph.MainActivity.Data_Type.Temperature1;

public class MainActivity extends AppCompatActivity {

    GraphView graphTemperature;
    ArrayList<DataPoint> Data_Temperature_Result = new ArrayList<>();

    int json_node1_control, json_node2_control;
    float json_temperature, json_light;

    boolean node1_power = true, node2_power = true;

    enum Data_Type {Node1_Control, Node2_Control, Temperature1, Temperature2, Light1, Light2, None}

    Data_Type data_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graphTemperature = findViewById(R.id.graphTemperature);

//        Data_Temperature_Result.add(new DataPoint(0, 30));
//        Data_Temperature_Result.add(new DataPoint(1, 31));
//        Data_Temperature_Result.add(new DataPoint(2, 31));
//        Data_Temperature_Result.add(new DataPoint(3, 29));
//        Data_Temperature_Result.add(new DataPoint(4, 29));

        DataPoint[] dataTemp = new DataPoint[Data_Temperature_Result.size()];
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(Data_Temperature_Result.toArray(dataTemp));

        showDataOnGraph(series, graphTemperature);

        startMQTT();
    }

    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
        if(graph.getSeries().size() > 0){
            graph.getSeries().remove(0);
        }
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.addSeries(series);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
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

                String data = mqttMessage.toString();
//                if(mqttMessage.toString().contains(STOP_SIGNAL)){
//                    data = "-1";
//                }

                Log.d("MQTT-Receive", mqttMessage.toString());
                String json = String.format("%s", mqttMessage);

                JsonReader jsonReader = new JsonReader(new StringReader(json));
                jsonReader.setLenient(true);

                try {
                    while (jsonReader.hasNext()) {
                        JsonToken nextToken = jsonReader.peek();

                        if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {

                            jsonReader.beginObject();

                        } else if (JsonToken.NAME.equals(nextToken)) {

                            String name = jsonReader.nextName();
                            if (name.equals("node1")) {
                                data_type = Node1_Control;
                            } else if (name.equals("temperature1")) {
                                data_type = Temperature1;
                            } else if (name.equals("light1")) {
                                data_type = Light1;
                            } else {
                                data_type = None;
                            }

                        } else if (JsonToken.STRING.equals(nextToken)) {

                            String value = jsonReader.nextString();
                            switch (data_type) {

                                case Node1_Control:

                                    json_node1_control = (int) Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node1 Control: %s", json_node1_control));
//                                    bntNode1_Control.setText();
                                    break;

                                case Temperature1:

                                    json_temperature = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Temperature 1: %s", json_temperature));

//                                    Data_Temperature_Result.add(new DataPoint(Data_Temperature_Result.size(), Integer.parseInt(data)));
                                    Data_Temperature_Result.add(new DataPoint(Data_Temperature_Result.size(), (int)json_temperature));
                                    DataPoint[] dataTemp = new DataPoint[Data_Temperature_Result.size()];
                                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(Data_Temperature_Result.toArray(dataTemp));

                                    showDataOnGraph(series, graphTemperature);
//                                    dataResTemp.add(new DataPoint(dataResTemp.size(), (int) json_temperature));
//                                    DataPoint[] dataTemp = new DataPoint[0];
//                                    LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>(dataResTemp.toArray(dataTemp));
//                                    Sensor_Node_1_Temperature_Graph.addSeries(seriesTemp);
//                                    Log.d("Graph", String.format("Temperature: %s", json_temperature));
                                    break;

                                case Light1:

                                    json_light = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Light level 1: %s", json_light));

//                                    dataResLight.add(new DataPoint(dataResLight.size(), (int) json_light));
//                                    DataPoint[] dataLight = new DataPoint[0];
//                                    LineGraphSeries<DataPoint> seriesLight = new LineGraphSeries<>(dataResLight.toArray(dataLight));
//                                    Sensor_Node_1_Light_Graph.addSeries(seriesLight);
//                                    Log.d("Graph", String.format("Light Level: %s", json_light));
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

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
