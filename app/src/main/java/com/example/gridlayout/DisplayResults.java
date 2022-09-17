package com.example.gridlayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayResults extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        Bundle bundle = getIntent().getExtras();
        boolean win = bundle.getBoolean("win");
        int clock = bundle.getInt("clock");
        Log.d("test",String.valueOf(win));
        TextView text = (TextView) findViewById(R.id.results);
        if (win==false){
            text.setText("Used "+String.valueOf(clock)+" seconds.\nYou lost.");
        }
        else{
            text.setText("Used "+String.valueOf(clock)+" seconds.\nYou Won!.");
        }
        Button btn = (Button)findViewById(R.id.reset);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisplayResults.this, MainActivity.class));
            }
        });
    }
}
