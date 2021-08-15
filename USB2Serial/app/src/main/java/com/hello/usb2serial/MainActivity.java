package com.hello.usb2serial;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hello.usb2serial.MainActivity.Data_Type.Light1;
import static com.hello.usb2serial.MainActivity.Data_Type.Light2;
import static com.hello.usb2serial.MainActivity.Data_Type.Node1_Control;
import static com.hello.usb2serial.MainActivity.Data_Type.Node2_Control;
import static com.hello.usb2serial.MainActivity.Data_Type.None;
import static com.hello.usb2serial.MainActivity.Data_Type.Temperature1;
import static com.hello.usb2serial.MainActivity.Data_Type.Temperature2;

public class MainActivity extends Activity implements View.OnClickListener, SerialInputOutputManager.Listener {

    final String TAG = "MAIN_TAG";
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    String dataReceived = "";
//    String valueTEMP = "";
//    String valueLIGHT = "";

    boolean availableDevice = false;

    Button btnSend;
    TextView txtMessage;
    EditText message;

    int data_id;
    float data_temperature, data_light, json_temperature, json_light;
    String json_node1_control, json_node2_control;

    enum Data_Type {Node1_Control, Node2_Control, Temperature1, Temperature2, Light1, Light2, None}

    Data_Type data_type;

    Gson gson = new Gson();
    Data data;

    UsbSerialPort port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        txtMessage = findViewById(R.id.txtMessage);
        message = findViewById(R.id.message);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        Display_Log_On_Text_View();

        startMQTT();
        openUART();
    }

    void Display_Log_On_Text_View(){
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append("\n");
            }

            txtMessage = findViewById(R.id.txtMessage);
            txtMessage.setText(log.toString());

        } catch (IOException e) {

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend && !message.getText().equals("")){
            sendMessageUART(message.getText().toString());
            message.setText("");
        }
        //dataReceived = "#N1:33.4,40.4$";
    }

    void Send_Data_In_JSON_Format(String Sensor_Node_id, String Temperature, String Light_Level) {

        data_id = Integer.parseInt(Sensor_Node_id);
        data_temperature = Float.parseFloat(Temperature);
        data_light = Float.parseFloat(Light_Level);

        Log.d("Data", String.format("ID: %s", data_id));
        Log.d("Data", String.format("Temperature: %s", data_temperature));
        Log.d("Data", String.format("Light:  %s", data_light));

        String json_string = String.format("{\"id\":%s,\"temperature\":%s,\"light\":%s}",
                data_id, data_temperature, data_light);

        data = gson.fromJson(json_string, Data.class);

        Log.d("JSON-Send", String.format("%s", data));

        if (mqttHelper != null) {
            mqttHelper.connectToPublish(String.format("%s", data));
        }
        txtMessage.append("JSON-Send: " + data + "\n");
    }

    private void openUART(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "UART is not available");
            txtMessage.append("Device not found \n");
            availableDevice = false;

        }else {
            Log.d(TAG, "UART is available");
            txtMessage.append("Device connected" + "\n");
            availableDevice = true;
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {

                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);

                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));


                return;
            } else {

                port = driver.getPorts().get(0);
                try {
                    port.open(connection);
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);
                    Log.d(TAG, "UART is openned");

                } catch (Exception e) {
                    Log.d(TAG, "There is error");
                }
            }
        }

    }

    @Override
    public void onNewData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            dataReceived = dataReceived + new String(data);
            getDataReceived();
            }
        });
    }

    private void getDataReceived(){
        int startPos, endPos;

        Display_Log_On_Text_View();

        try {
            startPos = dataReceived.indexOf("#");
            endPos = dataReceived.indexOf("$");
            String message = dataReceived.substring(startPos + 1, endPos);
            txtMessage.append("Message received: " + message + "\n");
            String sensor_node_id;
            String temperature;
            String light_level;
            if (endPos > startPos) {
                Pattern p = Pattern.compile("N(\\d):(.+),(.+)");
                Matcher m = p.matcher(message);
                if (m.find()) {
                    sensor_node_id = m.group(1);
                    temperature = m.group(2);
                    light_level = m.group(3);

                    txtMessage.append("Sensor Node " + sensor_node_id + "\n");
                    txtMessage.append("Temperature: " + temperature + "\n");
                    txtMessage.append("Light Intensity: " + light_level + "\n");

                    Send_Data_In_JSON_Format(sensor_node_id, temperature, light_level);

//                    valueTEMP = temperature;
//                    valueLIGHT = light_level;
//                    if (dataTEMP && dataLIGHT){
//                        dataTEMP = false;
//                        dataLIGHT = false;
//                        sendDataToThingSpeak(valueTEMP, valueLIGHT);
//                    }

                }
                dataReceived = dataReceived.substring(endPos + 1);
            }
        }
        catch (Exception e){}
    }

    @Override
    public void onRunError(Exception e) {

    }

    private void sendMessageUART(String message){
        try {
            if (availableDevice) {
                port.write((message).getBytes(), 1000);
                txtMessage.append("Message sent: " + message + "\n");
            }
            else txtMessage.append("No target device \n");
        } catch (IOException e) {
            e.printStackTrace();
            txtMessage.append("Message failed: "+message+"\n");
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

//        @Override
//        public String toString() {
//            return new StringBuilder().append("{\"feeds\":{\"Sensor-Node-").append(id).append("-Temperature\":\"")
//                    .append(temperature).append("\",\"Sensor-Node-").append(id).append("-Light-Level\":\"")
//                    .append(light).append("\"}}").toString();
//        }

        @Override
        public String toString() {
            return new StringBuilder().append("{\"feeds\":{\"Temperature-Sensor-Node-").append(id).append("\":\"")
                    .append(temperature).append("\",\"Light-Level-Sensor-Node-").append(id).append("\":\"")
                    .append(light).append("\"}}").toString();
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

                Display_Log_On_Text_View();

                try {
                    while (jsonReader.hasNext()) {
                        JsonToken nextToken = jsonReader.peek();

                        if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {

                            jsonReader.beginObject();

                        } else if (JsonToken.NAME.equals(nextToken)) {

                            String name = jsonReader.nextName();
                            if (name.equals("control-sensor-node-1")) {
                                data_type = Node1_Control;
                            } else if (name.equals("temperature1")) {
                                data_type = Temperature1;
                            } else if (name.equals("light1")) {
                                data_type = Light1;
                            } else if (name.equals("control-sensor-node-2")) {
                                data_type = Node2_Control;
                            } else if (name.equals("temperature2")) {
                                data_type = Temperature2;
                            } else if (name.equals("light2")) {
                                data_type = Light2;
                            } else {
                                data_type = None;
                            }

                        } else if (JsonToken.STRING.equals(nextToken)) {

                            String value = jsonReader.nextString();
                            switch (data_type) {

                                case Node1_Control:
                                    json_node1_control = "#GW:N1," + value + "$";
                                    sendMessageUART(json_node1_control);
                                    txtMessage.append("UART-Send: " + json_node1_control + "\n");
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Sensor Node 1 Control: %s", value));
                                    Log.d("UART-Send", String.format("Message: %s", json_node1_control));
                                    break;

                                case Temperature1:
                                    json_temperature = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Temperature 1: %s", json_temperature));
                                    break;

                                case Light1:
                                    json_light = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Light level 1: %s", json_light));
                                    break;

                                case Node2_Control:
                                    json_node2_control = "#GW:N2," + value + "$";
                                    sendMessageUART(json_node2_control);
                                    txtMessage.append("UART-Send: " + json_node2_control + "\n");
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Sensor Node 2 Control: %s", value));
                                    Log.d("UART-Send", String.format("Message: %s", json_node2_control));
                                    break;

                                case Temperature2:
                                    json_temperature = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Temperature 2: %s", json_temperature));
                                    break;

                                case Light2:
                                    json_light = Float.parseFloat(value);
                                    data_type = None;
                                    Log.d("JSON-Receive", String.format("Light level 2: %s", json_light));
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
