package com.hello.simple_calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.hello.simple_calculator.MainActivity.OPERATOR.ADD;
import static com.hello.simple_calculator.MainActivity.OPERATOR.DIV;
import static com.hello.simple_calculator.MainActivity.OPERATOR.MUL;
import static com.hello.simple_calculator.MainActivity.OPERATOR.SUB;
import static com.hello.simple_calculator.MainActivity.STATE.FIRST_OP;
import static com.hello.simple_calculator.MainActivity.STATE.RESET_SECOND_OP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public enum STATE {
        FIRST_OP
    }

    public enum OPERATOR {
        NONE, ADD, SUB, MUL, DIV, MOD
    }

    STATE state = STATE.FIRST_OP;
    double first_op = 0.0, second_op = 0.0; // first_op: store first value of calculator, second_op: store second value of calculator
    OPERATOR operator = OPERATOR.NONE;

    TextView txtOut;
    EditText num1, num2;

    Button btnAdd, btnSub, btnDiv, btnMul;

    public void calculateAndPrint () {
        switch (operator) {
            case ADD:
                first_op += second_op;
                break;
            case SUB:
                first_op -= second_op;
                break;
            case MUL:
                first_op *= second_op;
                break;
            case DIV:
                first_op /= second_op;
                break;
            case MOD:
                first_op %= second_op;
                break;
            default:
                break;
        }

        if (first_op == (int) first_op)
        {
            txtOut.setText((int) first_op + "");
        }
        else
        {
            txtOut.setText(first_op + "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtOut = findViewById(R.id.txtOut);
        num1   = findViewById(R.id.num1);
        num2   = findViewById(R.id.num2);

        // tao lien ket voi button
        // button
        btnAdd = findViewById(R.id.btnAdd); btnAdd.setOnClickListener(this);
        btnSub = findViewById(R.id.btnSub); btnSub.setOnClickListener(this);
        btnMul = findViewById(R.id.btnMul); btnMul.setOnClickListener(this);
        btnDiv = findViewById(R.id.btnDiv); btnDiv.setOnClickListener(this);
    }

    // set state for program
    @Override
    public void onClick(View v) {
        switch (state){
            case FIRST_OP:
                if(v.getId() == R.id.btnAdd)
                { //button Add
                    first_op = Double.parseDouble(num1.getText().toString());
                    second_op = Double.parseDouble(num2.getText().toString());
                    operator = ADD;
                    calculateAndPrint();
                    state = FIRST_OP;
                }
                else if(v.getId() == R.id.btnSub)
                { //button Sub
                    first_op = Double.parseDouble(num1.getText().toString());
                    second_op = Double.parseDouble(num2.getText().toString());
                    operator = SUB;
                    calculateAndPrint();
                    state = FIRST_OP;
                }
                else if(v.getId() == R.id.btnMul)
                { //button Mul
                    first_op = Double.parseDouble(num1.getText().toString());
                    second_op = Double.parseDouble(num2.getText().toString());
                    operator = MUL;
                    calculateAndPrint();
                    state = FIRST_OP;
                }
                else if(v.getId() == R.id.btnDiv)
                { //button Div
                    first_op = Double.parseDouble(num1.getText().toString());
                    second_op = Double.parseDouble(num2.getText().toString());
                    operator = DIV;
                    calculateAndPrint();
                    state = FIRST_OP;
                }
                break;
            default:
                break;
        }
    }
}