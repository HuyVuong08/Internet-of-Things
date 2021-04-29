package com.hello.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static com.hello.calculator.MainActivity.OPERATOR.ADD;
import static com.hello.calculator.MainActivity.OPERATOR.DIV;
import static com.hello.calculator.MainActivity.OPERATOR.MOD;
import static com.hello.calculator.MainActivity.OPERATOR.MUL;
import static com.hello.calculator.MainActivity.OPERATOR.NONE;
import static com.hello.calculator.MainActivity.OPERATOR.SUB;
import static com.hello.calculator.MainActivity.STATE.FIRST_OP;
import static com.hello.calculator.MainActivity.STATE.RESET_SECOND_OP;
import static com.hello.calculator.MainActivity.STATE.SECOND_OP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public enum STATE {
        FIRST_OP, RESET_SECOND_OP, SECOND_OP
    }

    public enum OPERATOR {
        NONE,ADD, SUB, MUL, DIV, MOD
    }

    STATE state = FIRST_OP;
    double first_op = 0.0, second_op = 0.0; // first_op: store first value of calculator, second_op: store second value of calculator
    OPERATOR operator = NONE;

    TextView txtOut;

    Button btn9, btn8, btn7, btn6, btn5, btn4, btn3, btn2, btn1, btn0;

    Button btnAdd, btnSub, btnDiv, btnMul, btnDel, btnAC, btnEqual, btnMod, btnDec;

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
        btn9 = findViewById(R.id.btn9); btn9.setOnClickListener(this);
        btn8 = findViewById(R.id.btn8); btn8.setOnClickListener(this);
        btn7 = findViewById(R.id.btn7); btn7.setOnClickListener(this);
        btn6 = findViewById(R.id.btn6); btn6.setOnClickListener(this);
        btn5 = findViewById(R.id.btn5); btn5.setOnClickListener(this);
        btn4 = findViewById(R.id.btn4); btn4.setOnClickListener(this);
        btn3 = findViewById(R.id.btn3); btn3.setOnClickListener(this);
        btn2 = findViewById(R.id.btn2); btn2.setOnClickListener(this);
        btn1 = findViewById(R.id.btn1); btn1.setOnClickListener(this);
        btn0 = findViewById(R.id.btn0); btn0.setOnClickListener(this);

        btnAC = findViewById(R.id.btnAC); btnAC.setOnClickListener(this);
        btnDel = findViewById(R.id.btnDel); btnDel.setOnClickListener(this);
        btnAdd = findViewById(R.id.btnAdd); btnAdd.setOnClickListener(this);
        btnSub = findViewById(R.id.btnSub); btnSub.setOnClickListener(this);
        btnMul = findViewById(R.id.btnMul); btnMul.setOnClickListener(this);
        btnDiv = findViewById(R.id.btnDiv); btnDiv.setOnClickListener(this);
        btnMod = findViewById(R.id.btnMod); btnMod.setOnClickListener(this);
        btnEqual = findViewById(R.id.btnEqual); btnEqual.setOnClickListener(this);
        btnDec = findViewById(R.id.btnDec); btnDec.setOnClickListener(this);

    }

    // set state for program
    @Override
    public void onClick(View v) {
        switch (state){
            case FIRST_OP:
                if( v.getId() == R.id.btn0 || v.getId() == R.id.btn1 ||
                        v.getId() == R.id.btn2 || v.getId() == R.id.btn3 ||
                        v.getId() == R.id.btn4 || v.getId() == R.id.btn5 ||
                        v.getId() == R.id.btn6 || v.getId() == R.id.btn7 ||
                        v.getId() == R.id.btn8 || v.getId() == R.id.btn9)
                { //button Num
                    String displayNumber = ((Button)v).getText().toString();
                    if(txtOut.getText().toString().equals("0") == true){
                        txtOut.setText(displayNumber);
                        isBntEqual = false;
                    }
                    else
                    {
                        txtOut.setText(txtOut.getText() + displayNumber);
                    }
                }
                else if (v.getId() == R.id.btnDec)
                { //button Decimal
                    if(txtOut.getText().toString().contains(".") == false) {
                        txtOut.setText(txtOut.getText() + ".");
                    }
                }
                else if (v.getId() == R.id.btnAC)
                { //button AC
                    state = FIRST_OP;
                    first_op = 0;
                    second_op = 0;
                    operator = NONE;
                    isBntEqual = false;
                    txtOut.setText("0");
                }
                else if (v.getId() == R.id.btnDel)
                { //button Del
                    if (txtOut.length() > 1)
                    {
                        txtOut.setText(txtOut.getText().toString().substring(0, txtOut.length() - 1));
                    }
                    else if (txtOut.length() == 1)
                    {
                        txtOut.setText("0");
                    }
                }
                else if (v.getId() == R.id.btnEqual)
                { //button Equal
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = NONE;
                    isBntEqual = true;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnAdd)
                { //button Add
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = ADD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnSub)
                { //button Sub
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = SUB;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMul)
                { //button Mul
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = MUL;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnDiv)
                { //button Div
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = DIV;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMod)
                { //button Mod
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    operator = MOD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                break;
            case RESET_SECOND_OP:
                if( v.getId() == R.id.btn0 || v.getId() == R.id.btn1 ||
                        v.getId() == R.id.btn2 || v.getId() == R.id.btn3 ||
                        v.getId() == R.id.btn4 || v.getId() == R.id.btn5 ||
                        v.getId() == R.id.btn6 || v.getId() == R.id.btn7 ||
                        v.getId() == R.id.btn8 || v.getId() == R.id.btn9)
                { //button Num
                    String displayNumber = ((Button)v).getText().toString();
                    txtOut.setText(displayNumber);
                    if (isBntEqual == true)
                    {
                        state = FIRST_OP;
                        isBntEqual = false;
                    }
                    else
                    {
                        state = SECOND_OP;
                    }
                }
                else if (v.getId() == R.id.btnAC)
                { //button AC
                    state = FIRST_OP;
                    first_op = 0;
                    second_op = 0;
                    operator = NONE;
                    isBntEqual = false;
                    txtOut.setText("0");
                }
                else if (v.getId() == R.id.btnEqual)
                { //button Equal
                    first_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    isBntEqual = true;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnAdd)
                { //button Add
                    operator = ADD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnSub)
                { //button Sub
                    operator = SUB;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMul)
                { //button Mul
                    operator = MUL;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnDiv)
                { //button Div
                    operator = DIV;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMod)
                { //button Mod
                    operator = MOD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                break;
            case SECOND_OP:

                if( v.getId() == R.id.btn0 || v.getId() == R.id.btn1 ||
                        v.getId() == R.id.btn2 || v.getId() == R.id.btn3 ||
                        v.getId() == R.id.btn4 || v.getId() == R.id.btn5 ||
                        v.getId() == R.id.btn6 || v.getId() == R.id.btn7 ||
                        v.getId() == R.id.btn8 || v.getId() == R.id.btn9)
                { //button Num
                    String displayNumber = ((Button)v).getText().toString();
                    txtOut.setText(txtOut.getText() + displayNumber);
                }
                else if (v.getId() == R.id.btnDec)
                { //button Decimal
                    if(txtOut.getText().toString().contains(".") == false) {
                        txtOut.setText(txtOut.getText() + ".");
                    }
                }
                else if (v.getId() == R.id.btnAC)
                { //button AC
                    state = FIRST_OP;
                    first_op = 0;
                    second_op = 0;
                    operator = NONE;
                    txtOut.setText("0");
                }
                else if (v.getId() == R.id.btnDel)
                { //button Del
                    if (txtOut.length() > 1)
                    {
                        txtOut.setText(txtOut.getText().toString().substring(0, txtOut.length() - 1));
                    }
                    else if (txtOut.length() == 1)
                    {
                        txtOut.setText("0");
                    }
                }
                else if (v.getId() == R.id.btnEqual)
                { //button Equal
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    isBntEqual = true;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnAdd)
                { //button Add
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    operator = ADD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnSub)
                { //button Sub
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    operator = SUB;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMul)
                { //button Mul
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    operator = MUL;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnDiv)
                { //button Div
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    operator = DIV;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                else if(v.getId() == R.id.btnMod)
                { //button Mod
                    second_op = Double.parseDouble(txtOut.getText().toString());
                    calculateAndPrint();
                    operator = MOD;
                    isBntEqual = false;
                    state = RESET_SECOND_OP;
                }
                break;
            default:
                break;
        }
    }
}