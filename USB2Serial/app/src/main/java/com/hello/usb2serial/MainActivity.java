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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements View.OnClickListener, SerialInputOutputManager.Listener {

    final String TAG = "MAIN_TAG";
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    String dataReceived = "";
    String valueTEMP = "";
    String valueLIGHT = "";

    boolean availableDevice = false;
    boolean dataTEMP = false;
    boolean dataLIGHT = false;

    Button btnSend;
    TextView txtMessage;
    EditText message;

    UsbSerialPort port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtMessage = findViewById(R.id.txtMessage);
        message = findViewById(R.id.message);
        btnSend = findViewById(R.id.btnSend); btnSend.setOnClickListener(this);

        openUART();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend && !message.getText().equals("")){
            sendMessageUART(message.getText().toString());
            message.setText("");
        }
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
                getDataReciedved();
            }
        });
    }

    private void getDataReciedved(){
        int startPos = 0;
        int endPos = 0;
        try {
            startPos = dataReceived.indexOf("#");
            endPos = dataReceived.indexOf("$");
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
                            txtMessage.append("Light Intensity: " + value + "\n");
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
//                        sendDataToThingSpeak(valueTEMP, valueLIGHT);
                    }

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
}
