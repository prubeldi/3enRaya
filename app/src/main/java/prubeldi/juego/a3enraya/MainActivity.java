package prubeldi.juego.a3enraya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout player1Layout, player2Layout;
    private ImageView image1, image2, image3, image4, image5, image6, image7, image8, image9;
    private TextView player1TV, player2TV;

    //winning combinations
    private final List<int[]> combinacionesList = new ArrayList<>();

    // player unique ID
    private String playerUniqueId = "0";

    //getting firebase database reference from URL
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://enraya-5bf81-default-rtdb.europe-west1.firebasedatabase.app/");

    //true when opponent will be found to play the game
    private boolean opponentFound = false;

    //unique id of opponent
    private String opponentUniqueId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player1Layout = findViewById(R.id.player1Layout);
        player2Layout = findViewById(R.id.player2Layout);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        image5 = findViewById(R.id.image5);
        image6 = findViewById(R.id.image6);
        image7 = findViewById(R.id.image7);
        image8 = findViewById(R.id.image8);
        image9 = findViewById(R.id.image9);

        player1TV = findViewById(R.id.player1TV);
        player2TV = findViewById(R.id.player2TV);

        //getting PlanerName from PlayerName.class file
        final String getPlayerName = getIntent().getStringExtra("playerName");


        //generating winnin combinations
        combinacionesList.add(new  int[]{0,1,2});
        combinacionesList.add(new  int[]{3,4,5});
        combinacionesList.add(new  int[]{6,7,8});
        combinacionesList.add(new  int[]{0,3,6});
        combinacionesList.add(new  int[]{1,4,7});
        combinacionesList.add(new  int[]{2,5,8});
        combinacionesList.add(new  int[]{2,4,6});
        combinacionesList.add(new  int[]{0,4,8});


        // showing progess dialog whiole waiting for opponent
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for Opponent");
        progressDialog.show();

        // generate player unique id. Player will bi identified by this id

        playerUniqueId = String.valueOf(System.currentTimeMillis());

        //setting player name to the textViiew
        player1TV.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check if opponent found or not. If not then look the opponent
                if (opponentFound){

                    //Checking if there are other in the firebase realtime
                    if (snapshot.hasChildren()){

                        // Checking all connections if other user are also witing for a user to play th match

                        for (DataSnapshot connections : shapshot.getChildren()){

                            //getting connection unique id
                            long conId = Long.parseLong(connections.getKey());

                            //2 player are required to play the game
                            //if getPlayerCount is 1 it means other player is waiting for a opponent to play the game
                            //else if getPlayerCount is 2 it means this connection has completed with 2 player
                            int getPlayersCount = (int)connections.getChildren();
                        }
                    }

                    else {

                        String connectionsUniqueId = String.valueOf(System.currentTimeMillis());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}