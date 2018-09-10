package baqui88.game;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import baqui88.widget.FButton;

public class HomeScreen extends AppCompatActivity {
    FButton playGameButton, quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        playGameButton = (FButton) findViewById(R.id.playGame_bt);
        quitButton = (FButton) findViewById(R.id.quit_bt);

        //PlayGame button - it will take you to the MainGameActivity
        playGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, MainGameActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //Quit button - This will quitButton the game
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Typeface - this is for fonts style
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/shablagooital.ttf");
        playGameButton.setTypeface(typeface);
        quitButton.setTypeface(typeface);

    }
}
