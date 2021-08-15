package com.hello;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.hello.MainActivity.Data_Type.Light1;
import static com.hello.MainActivity.Data_Type.Light2;
import static com.hello.MainActivity.Data_Type.Node1_Control;
import static com.hello.MainActivity.Data_Type.Node2_Control;
import static com.hello.MainActivity.Data_Type.None;
import static com.hello.MainActivity.Data_Type.Temperature1;
import static com.hello.MainActivity.Data_Type.Temperature2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    String[] Sampling_Rate = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    int sampling_rate;

    String json_node1_control, json_node2_control;
    float json_temperature, json_light;

    boolean node1_power = true, node2_power = true;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    enum Data_Type {Node1_Control, Node2_Control, Temperature1, Temperature2, Light1, Light2, None}

    Data_Type data_type;

    String json_string;

    Gson gson = new Gson();
    Data data;

    GraphView Sensor_Node_1_Temperature_Graph, Sensor_Node_1_Light_Level_Graph;
    ArrayList<DataPoint> Sensor_Node_1_Data_Temperature_Results = new ArrayList<>();
    ArrayList<DataPoint> Sensor_Node_1_Data_Light_Level_Results = new ArrayList<>();
    GraphView Sensor_Node_2_Temperature_Graph, Sensor_Node_2_Light_Level_Graph;
    ArrayList<DataPoint> Sensor_Node_2_Data_Temperature_Results = new ArrayList<>();
    ArrayList<DataPoint> Sensor_Node_2_Data_Light_Level_Results = new ArrayList<>();
    DataPoint[] dataTemp;
    LineGraphSeries<DataPoint> series;

    private EditText editText;
    private ImageView micButton;
    boolean isOpenMic = false;
    String STOP_SIGNAL = "STOP";

    Button bntNode1_Control, bntNode2_Control;
    Spinner spin1, spin2;

////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////        ONCREATE         /////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Sensor_Node_1_Temperature_Graph = findViewById(R.id.graphN1Temperature);
        Sensor_Node_1_Light_Level_Graph = findViewById(R.id.graphN1LightLevel);

        Sensor_Node_2_Temperature_Graph = findViewById(R.id.graphN2Temperature);
        Sensor_Node_2_Light_Level_Graph = findViewById(R.id.graphN2LightLevel);


        Sensor_Node_1_Data_Temperature_Results.add(new DataPoint(0,0));
        Sensor_Node_1_Data_Light_Level_Results.add(new DataPoint(0,0));

        Sensor_Node_2_Data_Temperature_Results.add(new DataPoint(0,0));
        Sensor_Node_2_Data_Light_Level_Results.add(new DataPoint(0,0));

        bntNode1_Control = findViewById(R.id.btnNode1);
        bntNode1_Control.setOnClickListener(this);
        bntNode2_Control = findViewById(R.id.btnNode2);
        bntNode2_Control.setOnClickListener(this);

        //Getting the instance of Spinner and applying OnItemSelectedListener on it
        spin1 = findViewById(R.id.spinner1);
        spin1.setOnItemSelectedListener(this);
        spin2 = findViewById(R.id.spinner2);
        spin2.setOnItemSelectedListener(this);

        //Creating the ArrayAdapter instance having the Sampling_Rate list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,Sampling_Rate);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter bb = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,Sampling_Rate);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Setting the ArrayAdapter data on the Spinner
        spin1.setAdapter(aa);
        spin1.setOnItemSelectedListener(new Spinner1());
        spin2.setAdapter(bb);
        spin2.setOnItemSelectedListener((new Spinner2()));

        startMQTT();

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////        SPEECH RECOGNITION CODE FROM MEDIUM & THOAI         ////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("speech/", "BEGIN!");
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                isOpenMic = false;
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> speech = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                assert speech != null;
                Log.d("speech", speech.get(0));

                if(mqttHelper != null && speech.get(0).toUpperCase().startsWith("sensor node 1".toUpperCase())) {
                    mqttHelper.connectToPublish(String.format("%s", STOP_SIGNAL));
                    editText.setText(String.format("Command: %s", speech.get(0)));
                } else {
                    editText.setText(String.format("Unknown: %s", speech.get(0)));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton = findViewById(R.id.button);
        editText = findViewById(R.id.text);

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOpenMic = !isOpenMic;

                if (!isOpenMic){
                    editText.setText("");
                    editText.setHint("");
                    micButton.setImageResource(R.drawable.ic_mic_black_off);
                    speechRecognizer.stopListening();
                } else {
                    micButton.setImageResource(R.drawable.ic_mic_black_24dp);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnNode1) {

            node1_power = !node1_power;
            Log.d("POWER", String.format("NODE1: %s", node1_power));

            if (bntNode1_Control.getText().toString().equalsIgnoreCase("NODE1 OFF") == true) {

                bntNode1_Control.setText("NODE1 ON");
                json_string = String.format("{\"id\":\"Control-Sensor-Node-1\",\"power\":\"ON\"}");
                bntNode1_Control.setBackgroundResource(R.drawable.on_button_border);

            } else if (bntNode1_Control.getText().toString().equalsIgnoreCase("NODE1 ON") == true) {

                bntNode1_Control.setText("NODE1 OFF");
                json_string = String.format("{\"id\":\"Control-Sensor-Node-1\",\"power\":\"OFF\"}");
                bntNode1_Control.setBackgroundResource(R.drawable.off_button_border);

            }

            data = gson.fromJson(json_string, Data.class);

            Log.d("JSON-Send", String.format("%s",data));
            if(mqttHelper != null) {

                mqttHelper.connectToPublish(String.format("%s", data));

            }
        }

        else if(v.getId()==R.id.btnNode2) {

            node2_power = !node2_power;
            Log.d("POWER", String.format("NODE2: %s", node2_power));

            if (bntNode2_Control.getText().toString().equalsIgnoreCase("NODE2 OFF") == true) {

                bntNode2_Control.setText("NODE2 ON");
                json_string = String.format("{\"id\":\"Control-Sensor-Node-2\",\"power\":\"ON\"}");
                bntNode2_Control.setBackgroundResource(R.drawable.on_button_border);


            } else if (bntNode2_Control.getText().toString().equalsIgnoreCase("NODE2 ON") == true) {

                bntNode2_Control.setText("NODE2 OFF");
                json_string = String.format("{\"id\":\"Control-Sensor-Node-2\",\"power\":\"OFF\"}");
                bntNode2_Control.setBackgroundResource(R.drawable.off_button_border);

            }

            data = gson.fromJson(json_string, Data.class);

            Log.d("JSON-Send", String.format("%s",data));
            if(mqttHelper != null) {

                mqttHelper.connectToPublish(String.format("%s", data));

            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////        SPINNER         /////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    class Spinner1 implements AdapterView.OnItemSelectedListener
    {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {

            Toast.makeText(v.getContext(),"Sensor Node 1: " + Sampling_Rate[position] + " Samples/Min", Toast.LENGTH_LONG).show();
            sampling_rate = Integer.parseInt(Sampling_Rate[position]);

            json_string = String.format("{\"id\":\"Control-Sensor-Node-1\",\"power\":\"" + sampling_rate + "\"}");

            data = gson.fromJson(json_string, Data.class);

            Log.d("JSON-Send", String.format("%s",data));
            if(mqttHelper != null) {

                mqttHelper.connectToPublish(String.format("%s", data));

            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class Spinner2 implements AdapterView.OnItemSelectedListener
    {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
        {
            Toast.makeText(v.getContext(),"Sensor Node 2: " + Sampling_Rate[position] + " Samples/Min", Toast.LENGTH_LONG).show();
            sampling_rate = Integer.parseInt(Sampling_Rate[position]);

            json_string = String.format("{\"id\":\"Control-Sensor-Node-2\",\"power\":\"" + sampling_rate + "\"}");

            data = gson.fromJson(json_string, Data.class);

            Log.d("JSON-Send", String.format("%s",data));
            if(mqttHelper != null) {

                mqttHelper.connectToPublish(String.format("%s", data));

            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//        sampling_rate = Integer.parseInt(Sampling_Rate[position]);
//
//        if(view.getId() == R.id.spinner1) {
//
//            Toast.makeText(this,"Node 1: " + Sampling_Rate[position] + "Sample/Min", Toast.LENGTH_LONG).show();
//            json_string = String.format("{\"id\":\"Control-Sensor-Node-1\",\"power\":\"" + sampling_rate + "\"}");
//
//        } else if(view.getId() == R.id.spinner2) {
//
//            Toast.makeText(this,"Node 2: " + Sampling_Rate[position] + "Sample/Min", Toast.LENGTH_LONG).show();
//            json_string = String.format("{\"id\":\"Control-Sensor-Node-2\",\"power\":\"" + sampling_rate + "\"}");
//
//        }
//
//        data = gson.fromJson(json_string, Data.class);
//
//        Log.d("JSON-Send", String.format("%s",data));
//        if(mqttHelper != null) {
//
//            mqttHelper.connectToPublish(String.format("%s", data));
//
//        }
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//
//    }

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////        PARSE DATA TO JSON FORMAT               ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

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

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////        ADAFRUIT MQTT SERVER               //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

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
                            if (name.equals("control-sensor-node-1")) {
                                data_type = Node1_Control;
                            } else if (name.equals("sensor-node-1-temperature")) {
                                data_type = Temperature1;
                            } else if (name.equals("sensor-node-1-light-level")) {
                                data_type = Light1;
                            } else if (name.equals("control-sensor-node-2")) {
                                data_type = Node2_Control;
                            } else if (name.equals("sensor-node-2-temperature")) {
                                data_type = Temperature2;
                            } else if (name.equals("sensor-node-2-light-level")) {
                                data_type = Light2;
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

                                        bntNode1_Control.setText("NODE1 ON");
                                        bntNode1_Control.setBackgroundResource(R.drawable.on_button_border);

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 1 POWER ON").setContentText("SENSOR NODE 1 POWER ON").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(1, notify);

                                    } else if (json_node1_control.equalsIgnoreCase("OFF") == true) {

                                        bntNode1_Control.setText("NODE1 OFF");
                                        bntNode1_Control.setBackgroundResource(R.drawable.off_button_border);

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 1 POWER OFF").setContentText("SENSOR NODE 1 POWER OFF").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(1, notify);

                                    } else if (Integer.parseInt(json_node1_control) <= 10 && Integer.parseInt(json_node1_control) >= 0 ) {

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 1 SAMPLING RATE " + Integer.parseInt(json_node1_control) + " SPM").setContentText("Sensor Node 1 Sampling Rate " + Integer.parseInt(json_node1_control) + " Samples/Min").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(3, notify);
                                        spin1.setSelection(Integer.parseInt(json_node1_control) - 1);

                                    }


//                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                                    // notificationId is a unique int for each notification that you must define
//                                    notificationManager.notify(1, builder.build());

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

                                        bntNode2_Control.setText("NODE2 ON");
                                        bntNode2_Control.setBackgroundResource(R.drawable.on_button_border);

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 2 POWER ON").setContentText("SENSOR NODE 2 POWER ON").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(2, notify);

                                    } else if (json_node2_control.equalsIgnoreCase("OFF") == true) {

                                        bntNode2_Control.setText("NODE2 OFF");
                                        bntNode2_Control.setBackgroundResource(R.drawable.off_button_border);

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 2 POWER OFF").setContentText("SENSOR NODE 2 POWER OFF").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(2, notify);

                                    } else if (Integer.parseInt(json_node2_control) <= 10 && Integer.parseInt(json_node2_control) >= 0 ) {

                                        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                        Notification notify=new Notification.Builder
                                                (getApplicationContext()).setContentTitle("NODE 2 SAMPLING RATE " + Integer.parseInt(json_node2_control) + " SPM").setContentText("Sensor Node 2 Sampling Rate " + Integer.parseInt(json_node2_control) + " Samples/Min").
                                                setSmallIcon(R.drawable.notification_icon).build();
                                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                        notif.notify(4, notify);
                                        spin2.setSelection(Integer.parseInt(json_node2_control) - 1);

                                    }
                                    break;

                                case Temperature2:

                                    json_temperature = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node2 Temperature: %s", json_temperature));

//                                    cars.remove(0);
                                    Sensor_Node_2_Data_Temperature_Results.set(Sensor_Node_2_Data_Temperature_Results.size() - 1,new DataPoint(Sensor_Node_2_Data_Temperature_Results.size() - 1, (int)json_temperature));
                                    Sensor_Node_2_Data_Temperature_Results.add(new DataPoint(Sensor_Node_2_Data_Temperature_Results.size(), (int)json_temperature));
                                    dataTemp = new DataPoint[Sensor_Node_2_Data_Temperature_Results.size()];
                                    series = new LineGraphSeries<>(Sensor_Node_2_Data_Temperature_Results.toArray(dataTemp));
                                    showDataOnGraph(series, Sensor_Node_2_Temperature_Graph);
                                    Log.d("GRAPH", String.format("Node1 Temperature: %s", Sensor_Node_2_Data_Temperature_Results));
                                    break;

                                case Light2:

                                    json_light = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Node2 Light Level: %s", json_light));

                                    Sensor_Node_2_Data_Light_Level_Results.set(Sensor_Node_2_Data_Light_Level_Results.size() - 1,new DataPoint(Sensor_Node_2_Data_Light_Level_Results.size() - 1, (int)json_light));
                                    Sensor_Node_2_Data_Light_Level_Results.add(new DataPoint(Sensor_Node_2_Data_Light_Level_Results.size(), (int)json_light));
                                    dataTemp = new DataPoint[Sensor_Node_2_Data_Light_Level_Results.size()];
                                    series = new LineGraphSeries<>(Sensor_Node_2_Data_Light_Level_Results.toArray(dataTemp));
                                    showDataOnGraph(series, Sensor_Node_2_Light_Level_Graph);
                                    Log.d("GRAPH", String.format("Node2 Light Level: %s", Sensor_Node_2_Data_Light_Level_Results));
                                    break;

                                case None:
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

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////        NOTIFICATION               //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////        SHOW DATA ON GRAPH               //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
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

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////        SPEECH RECOGNITION CODE FROM MEDIUM & THOAI         ////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    @Override

    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
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