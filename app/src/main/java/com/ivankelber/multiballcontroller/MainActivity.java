package com.ivankelber.multiballcontroller;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
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
                        hideSoftKeyboard(MainActivity.this);
                        toggleConnectionInterface(true);
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
        for(int id : button_ids) {
            Button b = (Button) findViewById(id);
            b.setOnTouchListener(new RepeatListener(400,100, new HoldListener() {

                @Override
                public void onRelease(View v) {
                    emitKeyUp(v);
                }

                @Override
                public void onHeld(View v) {
                    emitKeyDown(v);
                }
            }));

        }
    }

    public void emitKeyDown(View v) {
        JSONObject obj = new JSONObject();
        int key = 0;

        switch(v.getId()) {
            case R.id.left_button:
                Log.d("BUTTON PRESSED","Left");
                key = 37;
                break;
            case R.id.up_button:
                Log.d("BUTTON PRESSED","Up");
                key = 38;
                break;
            case R.id.right_button:
                Log.d("BUTTON PRESSED","Right");
                key = 39;
                break;
            case R.id.down_button:
                Log.d("BUTTON PRESSED","Down");
                key = 40;
                break;
        }
        try {
            obj.put("keyCode",key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(socket.connected()) {
            socket.emit("key down", obj);
        }
    }

    public void emitKeyUp(View v) {
        JSONObject obj = new JSONObject();
        int key = 0;

        switch(v.getId()) {
            case R.id.left_button:
                Log.d("BUTTON RELEASED","Left");
                key = 37;
                break;
            case R.id.up_button:
                Log.d("BUTTON RELEASED","Up");
                key = 38;
                break;
            case R.id.right_button:
                Log.d("BUTTON RELEASED","Right");
                key = 39;
                break;
            case R.id.down_button:
                Log.d("BUTTON RELEASED","Down");
                key = 40;
                break;
        }
        try {
            obj.put("keyCode",key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(socket.connected()) {
            socket.emit("key up",obj);
        }
    }

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void toggleConnectionInterface(boolean connected) {
        EditText et = (EditText) findViewById(R.id.client_id_edit_text);
        Button disconnect = (Button) findViewById(R.id.disconnect_button);
        if(connected) {
            et.setVisibility(View.GONE);
            disconnect.setVisibility(View.VISIBLE);
        } else {
            et.setVisibility(View.VISIBLE);
            disconnect.setVisibility(View.GONE);
        }
    }

    public void onDisconnect(View v) {
        toggleConnectionInterface(false);
        socket.disconnect();
        webClientId = "";

    }
}
