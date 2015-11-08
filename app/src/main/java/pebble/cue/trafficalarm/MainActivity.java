package pebble.cue.trafficalarm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;


import java.util.UUID;


import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

public class MainActivity extends ActionBarActivity {
    // map embedded in the map fragment
    private Map map = null;

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;
    private PebbleKit.PebbleDataReceiver mReceiver;
    private Handler mHandler = new Handler();
    private final static UUID COMPESS_UUID = UUID.fromString("2d0d78a2-6af6-4890-a1b4-0f568611383f");
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button launchButton = (Button)findViewById(R.id.launch_button);
        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(49.196261, -123.004773, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel(
                            (map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment" + error.toString());

                }
            }
        });


        launchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final Context context = getApplicationContext();

                boolean isConnected = PebbleKit.isWatchConnected(context);

                if(isConnected) {
                    // Launch the sports app
                    PebbleKit.startAppOnPebble(context, COMPESS_UUID);

                    // Send data 5s after launch
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            PebbleDictionary outgoing = new PebbleDictionary();
                            int test = 300;
                            outgoing.addString(0,"Bnup");
                            outgoing.addUint16(1,(short)test);
                            PebbleKit.sendDataToPebble(getApplicationContext(), COMPESS_UUID, outgoing);
                            Log.v("Info","Message Sent");

                        }

                    }, 2000L);

                    Toast.makeText(context, "Launching...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
                }

            }

        });
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
    protected void onResume() {
        super.onResume();

        // Construct output String
        StringBuilder builder = new StringBuilder();
        builder.append("Pebble Info\n\n");

        // Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);
        builder.append("Watch connected: " + (isConnected ? "true" : "false")).append("\n");

        // What is the firmware version?
        PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
        builder.append("Firmware version: ");
        builder.append(info.getMajor()).append(".");
        builder.append(info.getMinor()).append("\n");

        // Is AppMessage supported?
        boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
        builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));

        TextView textView = (TextView)findViewById(R.id.text_view);
        textView.setText(builder.toString());


        PebbleKit.registerReceivedDataHandler(this, mReceiver);
    }
    @Override
    protected void onPause() {
        super.onPause();

        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }
}
