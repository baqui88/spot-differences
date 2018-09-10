package baqui88.game;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import baqui88.widget.FButton;

public class Time_Up extends AppCompatActivity {
    FButton newGameButton;
    TextView timeUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_up);

        newGameButton = (FButton)findViewById(R.id.new_game_bt);
        timeUpText = (TextView)findViewById(R.id.timeUpText);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/shablagooital.ttf");
        timeUpText.setTypeface(typeface);
        newGameButton.setTypeface(typeface);

        //play again button onclick listener
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Time_Up.this,MainGameActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
