package com.ivankelber.multiballcontroller;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Socket socket;
    private String webClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText clientIdEditText = (EditText) findViewById(R.id.client_id_edit_text);
        clientIdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    if(socket != null) {
                        socket.connect();
                        handled = true;
                    }
                }
                return handled;
            }
        });
        setButtonHandlers();

        try {
            socket = IO.socket("https://cryptic-gorge-96821.herokuapp.com");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("SOCKET CONNECTED?","I THINK SO");
                }
            }).on("init", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("word", clientIdEditText.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("JSON EXCPETION", e.getStackTrace().toString());
                    }
                    socket.emit("new controller", obj);
                }
            }).on("web client disconnect", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        Log.d("WEB CLIENT DISCONNECTED","passed: " + obj.get("id").toString());
                        Log.d("WEB CLIENT DISCONNECTED","stored: " + webClientId);
                        Log.d("WEB CLIENT DISCONNECTED","My ID: " + socket.id());
                        if(obj.get("id").toString().equals(webClientId)) {

                            socket.disconnect();
                            webClientId = "";
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on("web client connected", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        webClientId = ((JSONObject) args[0]).get("id").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("DISCONNECT","disconnect");
                }
            });


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ivankelber.multiballcontroller/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ivankelber.multiballcontroller/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    public void setButtonHandlers() {
        int[] button_ids = {R.id.left_button,R.id.right_button,R.id.up_button,R.id.down_button};
        for(final int id : button_ids) {
            Button b = (Button) findViewById(id);
            b.setOnTouchListener(new RepeatListener(1000 / 60, 1000 / 60, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   emitKeyDown(id);
                }
            }, new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    emitKeyUp(id);
                    return false;
                }
            }));

        }
    }

    public void emitKeyDown(int id) {
        switch(id) {
            case R.id.left_button:
                Log.d("BUTTON PRESSED","Left");
//                socket.emit("controller left");
                break;
            case R.id.right_button:
                Log.d("BUTTON PRESSED","Right");

                break;
            case R.id.up_button:
                Log.d("BUTTON PRESSED","Up");

                break;
            case R.id.down_button:
                Log.d("BUTTON PRESSED","Down");
                break;
        }    }

    public void emitKeyUp(int id) {
        switch(id) {
            case R.id.left_button:
                Log.d("BUTTON RELEASED","Left");
//                socket.emit("controller left");
                break;
            case R.id.right_button:
                Log.d("BUTTON RELEASED","Right");

                break;
            case R.id.up_button:
                Log.d("BUTTON RELEASED","Up");

                break;
            case R.id.down_button:
                Log.d("BUTTON RELEASED","Down");
                break;
        }    }
}
