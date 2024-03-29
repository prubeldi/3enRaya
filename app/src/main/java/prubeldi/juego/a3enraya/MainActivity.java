package prubeldi.juego.a3enraya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
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
    private final List<String> doneBoxes = new ArrayList<>();

    // player unique ID
    private String playerUniqueId = "0";

    //getting firebase database reference from URL
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://enraya-5bf81-default-rtdb.europe-west1.firebasedatabase.app/");

    //true when opponent will be found to play the game
    private boolean opponentFound = false;

    //unique id of opponent
    private String opponentUniqueId = "0";

    // values must be matching or waiting. When a user create a new connection/room and he is waiting for other to join then the value will be waiting.
    private String status = "matching";


    //player turn
    private String playerTurn = "";

    private String connectionId = "";


    ValueEventListener turnsEventListener,wonEventListener;

    private final  String[]boxesSelectedBy = {"","","","","","","","","",};



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

                        for (DataSnapshot connections : snapshot.getChildren()){

                            //getting connection unique id
                            String conId =connections.getKey();

                            //2 player are required to play the game
                            //if getPlayerCount is 1 it means other player is waiting for a opponent to play the game
                            //else if getPlayerCount is 2 it means this connection has completed with 2 player
                            int getPlayersCount = (int)connections.getChildrenCount();

                            //afthe create a new connection waiting for othe to joing
                            if (status.equals("Waiting")){

                                //if getPlayerCount is 2 it means other player joined th match
                                if (getPlayersCount == 2){
                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);

                                    boolean playerFound = false;
                                    for (DataSnapshot players : connections.getChildren()){
                                        String getPlayerUniqueId = players.getKey();
                                        if (getPlayerUniqueId.equals(playerUniqueId)){
                                            playerFound = true;
                                        }
                                        else if (playerFound) {
                                            String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            player2TV.setText(getOpponentPlayerName);

                                            connectionId = conId;
                                            opponentFound = true;
                                                //adding turns listener and won lister to the database reference
                                            databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                            databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);
                                          //hide progress dialog
                                            if (progressDialog.isShowing()){
                                                progressDialog.dismiss();
                                            }
                                            databaseReference.child("connections").removeEventListener(this);
                                        }
                                    }

                                }

                            }
                            else {
                                if(getPlayersCount == 1){
                                    connections.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                                    for (DataSnapshot players : connections.getChildren()){
                                        String getOpponentName = players.child("player_name").getValue(String.class);
                                        opponentUniqueId = players.getKey();


                                        playerTurn =  opponentUniqueId;

                                        applyPlayerTurn(playerTurn);

                                        player2TV.setText(getOpponentName);

                                        connectionId = conId;
                                        opponentFound = true;

                                        databaseReference.child("turns").child(connectionId).addValueEventListener(turnsEventListener);
                                        databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);


                                        //hide progress dialog
                                        if (progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                        }
                                        databaseReference.child("connections").removeEventListener(this);

                                        break;
                                    }
                                }
                            }
                        }
                        if (!opponentFound && !status.equals("waiting")){
                            //generating unique id for the connection
                            String connectionsUniqueId = String.valueOf(System.currentTimeMillis());

                            //adding first player to the connection and waiting for other to complete the connection and play the gam
                            snapshot.child(connectionsUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                            status= "Waiting";
                        }
                    }

                    // if there is no connection available in the firebase then create a new connection.
                    // it is like creating a room and waitin for other players to join the room
                    else {

                        //generating unique id for the connection
                        String connectionsUniqueId = String.valueOf(System.currentTimeMillis());

                        //adding first player to the connection and waiting for other to complete the connection and play the gam
                        snapshot.child(connectionsUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                        status= "Waiting";
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (dataSnapshot.getChildrenCount()== 2){
                        final int getBoxPosition = Integer.parseInt(dataSnapshot.child("box position").getValue(String.class));

                        final String getPlayerId = dataSnapshot.child("player_id").getValue(String.class);


                        if(!doneBoxes.contains(String.valueOf(getBoxPosition))){
                            doneBoxes.add(String.valueOf(getBoxPosition));

                            if(getBoxPosition == 1){
                                selectBox(image1,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 2) {
                                selectBox(image2,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 3) {
                                selectBox(image3,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 4) {
                                selectBox(image4,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 5) {
                                selectBox(image5,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 6) {
                                selectBox(image6,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 7) {
                                selectBox(image7,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 8) {
                                selectBox(image8,getBoxPosition,getPlayerId);
                            }
                            else if (getBoxPosition == 9) {
                                selectBox(image9,getBoxPosition,getPlayerId);
                            }
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             if (snapshot.hasChild("player_id")){
                 String getWinPlayerId = snapshot.child("player_id").getValue(String.class);
                 final  WinDialog winDialog;
                 if(getWinPlayerId.equals(playerUniqueId)){
                    winDialog = new WinDialog(MainActivity.this,"you won this game");

                 }else {
                     winDialog = new WinDialog(MainActivity.this,"yon lost the game");
                 }
                 winDialog.setCancelable(false);
                 winDialog.show();



                 databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                 databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                image1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("1")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("1");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("2")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("2");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("3")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("3");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("4")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("4");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("5")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("5");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("6")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("6");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image7.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("7")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("7");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });

                image8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("8")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("8");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });
                image9.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!doneBoxes.contains("9")&& playerTurn.equals(playerUniqueId)){
                            ((ImageView)v).setImageResource(R.drawable.cross_icon);

                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("box_position").setValue("9");
                            databaseReference.child("turns").child(connectionId).child(String.valueOf(doneBoxes.size()+1)).child("player_id").setValue(playerUniqueId);

                            playerTurn = opponentUniqueId;

                        }
                    }
                });


            }
        };
    }
    private void applyPlayerTurn(String playerUniqueId2){
        if (playerUniqueId2.equals(playerUniqueId)){
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
        else {
            player2Layout.setBackgroundResource(R.drawable.round_back_dark_blue_stroke);
            player1Layout.setBackgroundResource(R.drawable.round_back_dark_blue_20);
        }
    }

    private void selectBox(ImageView imageView,int selectedBoxPosition,String selectedByPlayer){

     boxesSelectedBy[selectedBoxPosition - 1] = selectedByPlayer;
     if (selectedByPlayer.equals(playerUniqueId)){
         imageView.setImageResource(R.drawable.cross_icon);
         playerTurn = opponentUniqueId;
     }
     else{
         imageView.setImageResource(R.drawable.zero_icon);
         playerTurn = playerUniqueId;
     }

       applyPlayerTurn(playerTurn);

     if(checkPlayerWin(selectedByPlayer)){
         databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
     }

     if (doneBoxes.size() == 9){
      final WinDialog winDIalog = new WinDialog(MainActivity.this, "It is a Draw");
      winDIalog.setCancelable(false);
      winDIalog.show();
     }
    }

    private boolean checkPlayerWin(String playerId){
      boolean  isPlayerwon = false;
      for (int i = 0; i< combinacionesList.size();i++){
          final  int[] combination = combinacionesList.get(i);


          if (boxesSelectedBy[combination[0]].equals(playerId) && boxesSelectedBy[combination[1]].equals(playerId) &&
                  boxesSelectedBy[combination[2]].equals(playerId)){
              isPlayerwon = true;
          }
      }
      return isPlayerwon;
    }
}