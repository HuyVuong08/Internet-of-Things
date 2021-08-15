package com.example.hello.spinner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.example.hello.spinner.MainActivity.Data_Type.Light1;
import static com.example.hello.spinner.MainActivity.Data_Type.Node1_Control;
import static com.example.hello.spinner.MainActivity.Data_Type.Node2_Control;
import static com.example.hello.spinner.MainActivity.Data_Type.None;
import static com.example.hello.spinner.MainActivity.Data_Type.Temperature1;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] Sampling_Rate = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    int sampling_rate;
    String json_node1_control, json_node2_control;
    float json_temperature, json_light;

    boolean node1_power = true, node2_power = true;

    enum Data_Type {Node1_Control, Node2_Control, Temperature1, Temperature2, Light1, Light2, None}

    Data_Type data_type;

    String json_string;

    Gson gson = new Gson();
    Data data;

    GraphView Sensor_Node_1_Temperature_Graph, Sensor_Node_1_Light_Level_Graph;
    ArrayList<DataPoint> Sensor_Node_1_Data_Temperature_Results = new ArrayList<>();
    ArrayList<DataPoint> Sensor_Node_1_Data_Light_Level_Results = new ArrayList<>();
    DataPoint[] dataTemp;
    LineGraphSeries<DataPoint> series;

    private EditText editText;
    private ImageView micButton;
    boolean isOpenMic = false;
    String STOP_SIGNAL = "STOP";

    Button bntNode1_Control, bntNode2_Control;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        Spinner spin = findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the Sampling_Rate list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,Sampling_Rate);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_spinner_style,Sampling_Rate) {

            public View getView(int position, View convertView,ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);

                return v;

            }

            public View getDropDownView(int position, View convertView,ViewGroup parent) {

                View v = super.getDropDownView(position, convertView,parent);

                ((TextView) v).setGravity(Gravity.CENTER);

                return v;

            }

        };
//        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
//        spin.setAdapter(aa);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        startMQTT();

    }

    //Performing action onItemSelected and onNothing selected
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(),Sampling_Rate[position] , Toast.LENGTH_LONG).show();

        sampling_rate = Integer.parseInt(Sampling_Rate[position]);

        Log.d("CONTROL", String.format("Sampling Rate: %s", sampling_rate));

//        if (bntNode1_Control.getText().toString().equalsIgnoreCase("NODE1 OFF") == true) {
//
//            bntNode1_Control.setText("NODE1 ON");
        json_string = String.format("{\"id\":\"node1\",\"power\":\"" + sampling_rate + "\"}");
//
//        } else if (bntNode1_Control.getText().toString().equalsIgnoreCase("NODE1 ON") == true) {
//
//            bntNode1_Control.setText("NODE1 OFF");
//            json_string = String.format("{\"id\":\"node1\",\"power\":\"ON\"}");
//
//        }
//
        data = gson.fromJson(json_string, Data.class);
//
        Log.d("JSON-Send", String.format("%s",data));
        if(mqttHelper != null) {

            mqttHelper.connectToPublish(String.format("%s", data));

        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
                            } else if (name.equals("node2")) {
                                data_type = Node2_Control;
                            } else {
                                data_type = None;
                            }

                        } else if (JsonToken.STRING.equals(nextToken)) {

                            String value = jsonReader.nextString();
                            switch (data_type) {

                                case Node1_Control:

                                    json_node1_control = String.valueOf(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node 1 Control: %s", json_node1_control));
                                    if (json_node1_control.equalsIgnoreCase("ON") == true) {

                                        bntNode1_Control.setText("NODE1 OFF");

                                    } else if (json_node1_control.equalsIgnoreCase("OFF") == true) {

                                        bntNode1_Control.setText("NODE1 ON");
                                    }
                                    break;

                                case Temperature1:

                                    json_temperature = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node1 Temperature: %s", json_temperature));

//                                    cars.remove(0);
                                    Sensor_Node_1_Data_Temperature_Results.set(Sensor_Node_1_Data_Temperature_Results.size() - 1,new DataPoint(Sensor_Node_1_Data_Temperature_Results.size() - 1, (int)json_temperature));
                                    Sensor_Node_1_Data_Temperature_Results.add(new DataPoint(Sensor_Node_1_Data_Temperature_Results.size(), (int)json_temperature));
                                    dataTemp = new DataPoint[Sensor_Node_1_Data_Temperature_Results.size()];
                                    series = new LineGraphSeries<>(Sensor_Node_1_Data_Temperature_Results.toArray(dataTemp));
                                    showDataOnGraph(series, Sensor_Node_1_Temperature_Graph);
                                    Log.d("GRAPH", String.format("Node1 Temperature: %s", Sensor_Node_1_Data_Temperature_Results));
                                    break;

                                case Light1:

                                    json_light = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node1 Light Level: %s", json_light));

                                    Sensor_Node_1_Data_Light_Level_Results.set(Sensor_Node_1_Data_Light_Level_Results.size() - 1,new DataPoint(Sensor_Node_1_Data_Light_Level_Results.size() - 1, (int)json_light));
                                    Sensor_Node_1_Data_Light_Level_Results.add(new DataPoint(Sensor_Node_1_Data_Light_Level_Results.size(), (int)json_light));
                                    dataTemp = new DataPoint[Sensor_Node_1_Data_Light_Level_Results.size()];
                                    series = new LineGraphSeries<>(Sensor_Node_1_Data_Light_Level_Results.toArray(dataTemp));
                                    showDataOnGraph(series, Sensor_Node_1_Light_Level_Graph);
                                    Log.d("GRAPH", String.format("Node1 Light Level: %s", Sensor_Node_1_Data_Light_Level_Results));
                                    break;

                                case Node2_Control:

                                    json_node2_control = String.valueOf(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node 2 Control: %s", json_node2_control));
                                    if (json_node2_control.equalsIgnoreCase("ON") == true) {

                                        bntNode2_Control.setText("NODE2 OFF");

                                    } else if (json_node2_control.equalsIgnoreCase("OFF") == true) {

                                        bntNode2_Control.setText("NODE2 ON");
                                    }
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

//                String data = mqttMessage.toString();
//                if(mqttMessage.toString().contains(STOP_SIGNAL)){
//                    data = "-1";
//                }
//                dataRes.add(new DataPoint(dataRes.size(), Integer.parseInt(String.valueOf(json_light))));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
        if(graph.getSeries().size() > 0){
            graph.getSeries().remove(0);
        }
        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().scrollToEnd();
        graph.addSeries(series);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
    }
}