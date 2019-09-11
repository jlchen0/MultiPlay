package com.jchen.multiplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.types.Empty;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

public class LobbyActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "*******************";
    private static final String REDIRECT_URI = "https://google.com";
    private static final String DEV = "Dev";
    private SpotifyAppRemote mSpotifyAppRemote;

    private String lobbyName;

    Handler syncHandler = new Handler();
    Runnable sync = new Runnable(){
        @Override
        public void run(){
            new herokuAppSync(LobbyActivity.this).execute(lobbyName);
            syncHandler.postDelayed(this, 5000);
            Log.d("Runnable", "Syncing with Heroku.....");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        lobbyName = intent.getStringExtra(MainActivity.LOBBY);

        TextView welcome = (TextView) findViewById(R.id.Welcome);
        Typeface ubuntuBold = Typeface.createFromAsset(getAssets(),  "fonts/Ubuntu-Bold.ttf");
        welcome.setTypeface(ubuntuBold);

        TextView lobby = (TextView) findViewById(R.id.Lobby);
        lobby.setTypeface(ubuntuBold);
        lobby.setText(lobbyName);

        TextView addSongsAt = (TextView) findViewById(R.id.addSongsAt);
        addSongsAt.setTypeface(ubuntuBold);

        String url = "multi-play.herokuapp.com/lobby/" + lobbyName;
        TextView urlTV = (TextView) findViewById(R.id.link);
        urlTV.setTypeface(ubuntuBold);
        urlTV.setText(url);

        ImageButton cp = (ImageButton) findViewById(R.id.copy);
        cp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", url);
                clipboard.setPrimaryClip(clip);

                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(getApplicationContext(), "Copied to clipboard!", duration);
                toast.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener(){
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote){
                        mSpotifyAppRemote  = spotifyAppRemote;
                        Log.d("MainActivity", "Connected");
                        connected();
                    }
                    @Override
                    public void onFailure(Throwable throwable){
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
        // Continue to refresh and look for songs to add
        Log.d("MainActivity", "Sync");
        syncHandler.post(sync);
    }

    private void connected() {
        // Playing from playlist:

        // Initialize the songs already added to the queue
        new herokuAppSync(this).execute(lobbyName);
        /*
        // Logging info about the current song playing

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                }); */
    }

    private static class herokuAppSync extends AsyncTask<String, Void, String[]> {
        String lobbyName;
        private WeakReference<LobbyActivity> activityWeakReference;

        herokuAppSync(LobbyActivity context){
            activityWeakReference = new WeakReference<>(context);

        }


        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String...url){
            LobbyActivity activity = activityWeakReference.get();
            String[] songs = null;
            lobbyName = url[0];
            try{
                InputStream in = new URL("https://multi-play.herokuapp.com/songs/" + lobbyName).openStream();
                JSONArray jsonArr = new JSONArray(IOUtils.toString(in, "UTF-8"));
                songs = new String[jsonArr.length()];
                for (int i = 0; i < jsonArr.length(); i++){
                    Log.d("ReadQueue", "Reading track.");
                    songs[i] = jsonArr.getString(i);
                }
                // Log.d("ReadQueue", IOUtils.toString(in, "UTF-8"));
                in.close();
            } catch(Exception e) {
                e.printStackTrace();
            }

            for (String song: songs){
                CallResult<Empty> callResult = activity.mSpotifyAppRemote.getPlayerApi()
                        .queue(song);
                Result<Empty> result = callResult.await();
                if (result.isSuccessful()){
                    Log.d("ADD_QUEUE", "Added " + song + " to queue.");
                    try {
                        InputStream in = new URL("https://multi-play.herokuapp.com/poll/" + lobbyName).openStream();
                        Log.d("poll", "Polled");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return songs;
        }
        @Override
        protected void onPostExecute(String[] songs){
            super.onPostExecute(songs);

        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        syncHandler.removeCallbacks(sync);
    }
    @Override
    protected void onResume(){
        super.onResume();
        syncHandler.post(sync);

    }
    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}
