package com.example.admin.terminaluart;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SerialInputOutputManager.Listener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    MQTTHelper mqttHelper;
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    Spinner baudrate, databit, parity, stopbit;
    ArrayAdapter<String> baudrateSequence, databitSequence, paritySequence, stopbitSequence;
    ImageButton btnSend;
    EditText message;
    TextView txtMessage;
    String dataReceived = "";
    UsbSerialPort port;
    public int baudrateParameter = 9600;
    public int databitParamenter = 5;
    public int parityParameter = 0;
    public int stopbitParameter = 1;
    boolean availableDevice = false;

    private Thread readThread;

    private boolean mRunning;

    boolean dataTEMP = false;
    boolean dataLIGHT = false;
    String valueTEMP = "";
    String valueLIGHT = "";
    //private UsbHidDevice device = null;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("testttt", "Step 1");
        startMQTT();
        Log.e("testttt", "Step 2");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //*********************************************************************************************
        baudrate = (Spinner) navigationView.getMenu().findItem(R.id.nav_baudrate).getActionView();
        databit = (Spinner) navigationView.getMenu().findItem(R.id.nav_databit).getActionView();
        parity = (Spinner) navigationView.getMenu().findItem(R.id.nav_parity).getActionView();
        stopbit = (Spinner) navigationView.getMenu().findItem(R.id.nav_stopbit).getActionView();

        btnSend = findViewById(R.id.btnSend);
        txtMessage = findViewById(R.id.txtMessage);
        message = findViewById(R.id.message);


        baudrate.setOnItemSelectedListener(this);
        databit.setOnItemSelectedListener(this);
        parity.setOnItemSelectedListener(this);
        stopbit.setOnItemSelectedListener(this);
        btnSend.setOnClickListener(this);
        //BAUDRATE
        List<String> baudrateSeq = new ArrayList<String>();
        baudrateSeq.add("9600");
        baudrateSeq.add("38400");
        baudrateSeq.add("57600");
        baudrateSeq.add("115200");


        baudrateSequence =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, baudrateSeq);
        baudrateSequence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        baudrate.setAdapter(baudrateSequence);
        //DATABIT
        List<String> databitSeq = new ArrayList<String>();
        databitSeq.add("5");
        databitSeq.add("6");
        databitSeq.add("7");
        databitSeq.add("8");


        databitSequence =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, databitSeq);
        databitSequence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        databit.setAdapter(databitSequence);
        //PARITY
        List<String> paritySeq = new ArrayList<String>();
        paritySeq.add("None");
        paritySeq.add("Odd");
        paritySeq.add("Even");

        Log.e("testtt", "Step 1");
        paritySequence =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, paritySeq);
        paritySequence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parity.setAdapter(paritySequence);
        //STOPBIT
        List<String> stopbitSeq = new ArrayList<String>();
        stopbitSeq.add("1");
        stopbitSeq.add("1.5");
        stopbitSeq.add("2");

        Log.e("testtt", "Step 2");
        stopbitSequence =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stopbitSeq);
        stopbitSequence.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stopbit.setAdapter(stopbitSequence);
        ///////////////////////////////////////////

        ///////////////////////////////////////////
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);


        if (availableDrivers.isEmpty()) {
            Log.d("UART", "UART is not available");
            txtMessage.append("No devices found \n");
            Log.e("testtt", "Step 4");
            availableDevice = false;

        }else {
            Log.d("UART", "UART is available");
            txtMessage.append("Connected" + "\n");
            availableDevice = true;
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {

                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
            } else {
                port = driver.getPorts().get(0);
                try {
                    port.open(connection);
                    port.setParameters(9600, 5, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    //Log.e("testtt", "Step 3");

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);
                } catch (Exception e) {
                    Log.e("testtt", "Step 3");
                }

            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend && !message.getText().equals("")){
            sendMessageUART(message.getText().toString());
            message.setText("");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinBaudrate){
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Baudrate: " + item, Toast.LENGTH_LONG).show();
            switch (position){
                case 0:
                    baudrateParameter = 9600;
                    break;
                case 1:
                    baudrateParameter = 38400;
                    break;
                case 2:
                    baudrateParameter = 57600;
                    break;
                case 3:
                    baudrateParameter = 115200;
                    break;
                default: break;
            }

        }

        if (parent.getId() == R.id.spinDatabit){
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Data bit: " + item, Toast.LENGTH_LONG).show();
            switch (position){
                case 0:
                    databitParamenter = 5;
                    break;
                case 1:
                    databitParamenter = 6;
                    break;
                case 2:
                    databitParamenter = 7;
                    break;
                case 3:
                    databitParamenter = 8;
                    break;
                default: break;
            }
        }

        if (parent.getId() == R.id.spinParity){
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Parity: " + item, Toast.LENGTH_LONG).show();
            switch (position){
                case 0:
                    parityParameter = 0;
                    break;
                case 1:
                    parityParameter = 1;
                    break;
                case 2:
                    parityParameter = 2;
                    break;
                default: break;
            }
        }

        if (parent.getId() == R.id.spinStopbit) {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(parent.getContext(), "Stop bit: " + item, Toast.LENGTH_LONG).show();
            switch (position) {
                case 0:
                    stopbitParameter = 1;
                    break;
                case 1:
                    stopbitParameter = 3;
                    break;
                case 2:
                    stopbitParameter = 2;
                    break;
                default:
                    break;
            }
        }
        if (availableDevice) {
            try {
                port.setParameters(baudrateParameter, databitParamenter, stopbitParameter, parityParameter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onNewData(final byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataReceived = dataReceived + new String(data);
                getDataReciedved();
            }
        });
    }

    @Override
    public void onRunError(Exception e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            port.close();
        }catch (Exception e) {}
    }

    private void getDataReciedved(){
        int startPos = 0;
        int endPos = 0;
        try {
            startPos = dataReceived.indexOf("$");
            endPos = dataReceived.indexOf("#");
            String message = dataReceived.substring(startPos + 1, endPos);
            txtMessage.append("Message received: " + message + "\n");
            String sensor;
            String port;
            String value;
            if (endPos > startPos) {
                Pattern p = Pattern.compile("(.*),(\\d+),(\\d+)");
                Matcher m = p.matcher(message);
                if (m.find()) {
                    sensor = m.group(1);
                    port = m.group(2);
                    value = m.group(3);
                    switch (sensor){
                        case "LIGHT":
                            txtMessage.append("Light density: " + value + "\n");
                            dataLIGHT = true;
                            valueLIGHT = value;
                            break;
                        case "TEMP":
                            txtMessage.append("Temperature: " + value + "\n");
                            dataTEMP = true;
                            valueTEMP = value;
                            break;
                        default:break;
                    }
                    if (dataTEMP && dataLIGHT){
                        dataTEMP = false;
                        dataLIGHT = false;
                        sendDataToThingSpeak(valueTEMP, valueLIGHT);
                    }

                }
                dataReceived = dataReceived.substring(endPos + 1);
            }
        }
        catch (Exception e){}
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
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//MQTT & THINGSPEAK PROCESS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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
                Log.e("Mqtt-check", topic);
                Log.e("Mqtt-check", mqttMessage.toString());
                sendMessageUART(mqttMessage.toString()+"#");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }


    void sendDataToThingSpeak(String temp, String light){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String apiURL = "https://api.thingspeak.com/update?api_key=DRXEWSI2KWA6H0D6&field1=" + temp + "&field2=" + light;
        Request request = builder.url(apiURL).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });
    }

    private void publishControlSignal(String signal){
        MqttMessage message = new MqttMessage();
        message.setPayload(signal.getBytes());
        try {
            mqttHelper.mqttAndroidClient.publish(mqttHelper.subscriptionTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



}
