package com.ivy.radiogroupnest;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    private boolean isFirst=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RadioGroup rg= (RadioGroup) this.findViewById(R.id.rg);
        new RadioGroupUtils(rg).supportNest();
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRadioButton(rg);
            }
        });
    }

    private void addRadioButton(RadioGroup rg) {
        if (isFirst) {
            isFirst=false;
            RadioButton radioButton = new RadioButton(this);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height, 1));
            radioButton.setText("1层");
            rg.addView(radioButton);

            RadioButton radioButton2 = new RadioButton(this);
            radioButton2.setLayoutParams(new RadioGroup.LayoutParams(0, height, 1));
            radioButton2.setText("2层");
            ((ViewGroup) rg.getChildAt(2)).addView(radioButton2);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new RadioGroup.LayoutParams(0, height, 2));
            linearLayout.setBackgroundColor(Color.GREEN);
            RadioButton radioButton3 = new RadioButton(this);
            radioButton3.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            radioButton3.setText("3层");
            radioButton3.setMinWidth(0);
            linearLayout.addView(radioButton3);
            ((ViewGroup) rg.getChildAt(2)).addView(linearLayout);
        }else{
            RadioButton radioButton4 = new RadioButton(this);
            radioButton4.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            radioButton4.setText("三层");
            ((ViewGroup)((ViewGroup) rg.getChildAt(2)).getChildAt(3)).addView(radioButton4);
        }
    }
}
