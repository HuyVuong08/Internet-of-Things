package com.hello.increamental_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static com.hello.increamental_app.MainActivity.OPERATOR.ADD;
import static com.hello.increamental_app.MainActivity.OPERATOR.NONE;
import static com.hello.increamental_app.MainActivity.STATE.FIRST_OP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public enum STATE {
        FIRST_OP, RESET_SECOND_OP, SECOND_OP
    }

    public enum OPERATOR {
        NONE, ADD, SUB, MUL, DIV, MOD
    }

    STATE state = FIRST_OP;
    double first_op = 0.0, second_op = 0.0; // first_op: store first value of calculator, second_op: store second value of calculator
    OPERATOR operator = NONE;

    TextView txtOut;

    Button btnAdd;

    boolean isBntEqual = false;

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

        // tao lien ket voi button
        // button
        btnAdd = findViewById(R.id.btnAdd); btnAdd.setOnClickListener(this);
    }

    // set state for program
    @Override
    public void onClick(View v) {
        switch (state){
            case FIRST_OP:
                if(v.getId() == R.id.btnAdd)
                { //button Add
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    second_op = 1;
                    operator = ADD;
                    calculateAndPrint();
                    operator = ADD;
                    state = FIRST_OP;
                }
                break;
            default:
                break;
        }
    }
}