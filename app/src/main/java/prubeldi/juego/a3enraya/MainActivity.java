package prubeldi.juego.a3enraya;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting PlanerName from PlayerName.class file
        final String getPlayerName = getIntent().getStringExtra("playerName");
    }
}