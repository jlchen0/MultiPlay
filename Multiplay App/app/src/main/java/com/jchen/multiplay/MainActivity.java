package com.jchen.multiplay;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jchen.multiplay.RandomString;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.Empty;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity{


    public static final String LOBBY = "com.jchen.multiplay.NAME";

    private String lobbyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface ubuntuBold = Typeface.createFromAsset(getAssets(),  "fonts/Ubuntu-Bold.ttf");

        Button createButton = (Button)  findViewById(R.id.createNew);
        TextView createButtonTV = (TextView) findViewById(R.id.createNew);

        createButtonTV.setTypeface(ubuntuBold);
        createButton.setText("Create New Lobby");
        // add onclick method
        createButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                // createButton.setBackgroundColor(Color.parseColor("#403866"));
                RandomString rs = new RandomString(5);
                lobbyName = rs.generate();
                // create new lobby with name newLobbyName
                Log.d("onCreate", lobbyName);
                Intent changeActivity = new Intent(v.getContext(), LobbyActivity.class);
                changeActivity.putExtra(LOBBY, lobbyName);
                startActivity(changeActivity);
            }
        });


        Button joinButton = (Button) findViewById(R.id.joinExisting);
        TextView joinButtonTV = (TextView) findViewById(R.id.joinExisting);
        joinButtonTV.setTypeface(ubuntuBold);
        joinButton.setText("Join Existing Lobby");

        TextInputEditText input = (TextInputEditText) findViewById(R.id.inputLobby);
        TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.textInputLayout);
        Button submitButton = (Button) findViewById(R.id.join);
        TextView submitButtonTV = (TextView) findViewById(R.id.join);
        joinButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                inputLayout.setTypeface(ubuntuBold);

                submitButton.setTypeface(ubuntuBold);
                submitButton.setText("Join!");
                submitButton.setVisibility(View.VISIBLE);

                input.setVisibility(View.VISIBLE);
                input.setHint("Lobby Name");

                Log.d("Join", "Clicked!");
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lobbyName = input.getText().toString();
                Log.d("Submit", lobbyName);
                Intent changeActivity = new Intent(view.getContext(), LobbyActivity.class);
                changeActivity.putExtra(LOBBY, lobbyName);
                startActivity(changeActivity);
                // fire intent to get new activity
            }
        });

    }
}