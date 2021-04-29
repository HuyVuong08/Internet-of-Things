import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //    private TextInputEditText txtInput;
//    private Button btnSend;
    private GraphView graphTemperature, graphLightLevel;
    DataPoint[] dataRes;

    public class DataPoint implements DataPointInterface {
        public int idx = 0;
        public int val = 0;

        public DataPoint(int idx, int val){
            this.idx = idx;
            this.val = val;
        }

        @Override
        public double getX() {
            return idx;
        }

        @Override
        public double getY() {
            return val;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                Random rand = new Random();
                Log.i("tag", String.format("random: %s", rand.nextInt(30 + 1) + 20));
            }
        },0,5000);

        startMQTT();

        graphTemperature = findViewById(R.id.graphTemperature);
//        graphLightLevel = findViewById(R.id.graphLightLevel);

//        txtInput = findViewById(R.id.txtInput);
//        btnSend = findViewById(R.id.btnSend);
        dataRes = new DataPoint[5];
        dataRes[0] = new DataPoint(0, 30);
        dataRes[1] = new DataPoint(1, 31);
        dataRes[2] = new DataPoint(2, 31);
        dataRes[3] = new DataPoint(3, 29);
        dataRes[4] = new DataPoint(4, 29);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataRes);

        graphTemperature.addSeries(series);
//
//        LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>(dataPointTemp);
//
//        showDataOnGraph(seriesTemp, graphTemperature);
    }

//    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
//        if(graph.getSeries().size() > 0){
//            graph.getSeries().remove(0);
//        }
//        graph.addSeries(series);
//        series.setDrawDataPoints(true);
//        series.setDataPointsRadius(10);
//    }

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