package com.hello.huygay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//package com.example.timer;

//import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    EditText edt1, edt2, edt3;
    Button Plus,Min,Mul,Div;

    float num1 = 0, num2 = 0;
    int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edt1 = findViewById(R.id.edit1);
        edt2 = findViewById(R.id.edit2);
        edt3 = findViewById(R.id.edit3);
        Plus = findViewById(R.id.btnPlus);
        Min = findViewById(R.id.btnMin);
        Mul = findViewById(R.id.btnMul);
        Div = findViewById(R.id.btnDiv);

        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = parseInt(edt1.getText().toString()) + parseInt(edt2.getText().toString());
                String s=Float.toString(num1);
                edt3.setText(s);
            }
        });

        Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = parseInt(edt1.getText().toString()) - parseInt(edt2.getText().toString());
                String s=Float.toString(num1);
                edt3.setText(s);
            }
        });

        Mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = parseInt(edt1.getText().toString()) * parseInt(edt2.getText().toString());
                String s=Float.toString(num1);
                edt3.setText(s);
            }
        });

        Div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = parseInt(edt1.getText().toString()) / parseInt(edt2.getText().toString());
                String s=Float.toString(num1);
                edt3.setText(s);
            }
        });
    }
}
