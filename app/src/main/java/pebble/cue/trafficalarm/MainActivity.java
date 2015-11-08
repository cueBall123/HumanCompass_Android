package pebble.cue.trafficalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.here.android.mpa.common.PositioningManager.LocationMethod;
import com.here.android.mpa.common.PositioningManager.LocationStatus;
import com.here.android.mpa.common.PositioningManager.OnPositionChangedListener;
import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

import java.util.EnumSet;
import java.util.List;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

public class MainActivity extends ActionBarActivity  {
    // TextView for displaying the current map scheme
    private TextView textViewResult = null;
    private static LocationCoord Dest = null;
    private static LocationCoord Src = null;
   // private boolean paused = false;
    // MapRoute for this activity
    private MapRoute mapRoute = null;
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
        textViewResult = (TextView) findViewById(R.id.title);
        textViewResult.setText(R.string.textview_routecoordinates_2waypoints);

           // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(
                R.id.mapfragment);

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(
                    OnEngineInitListener.Error error)
            {
                if (error == OnEngineInitListener.Error.NONE) {
                    // Register positioning listener
                    mapFragment.getMapGesture().addOnGestureListener(new MyOnGestureListener());
                    PositioningManager PM = PositioningManager.getInstance();
                    PM.addListener(
                            new WeakReference<>(positionListener));
                    PM.start(LocationMethod.GPS_NETWORK);
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(49.196261, -123.004773, 0.0),
                            Map.Animation.NONE);

                    map.getPositionIndicator().setVisible(true);

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
                            int bearing = ((int) Math.floor(CalculateGPSDistance.Distance(Src,Dest).Bearing)+90)%360;

                            outgoing.addString(0,"Compass");
                            outgoing.addUint16(1,(short)bearing);
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
    // Define positioning listener
    private OnPositionChangedListener positionListener = new
            OnPositionChangedListener() {

                public void onPositionUpdated(LocationMethod method,
                                              GeoPosition position, boolean isMapMatched) {
                    // set the center only when the app is in the foreground
                    // to reduce CPU consumption

                        map.setCenter(position.getCoordinate(),
                                Map.Animation.BOW);

                    Src = new LocationCoord(position.getCoordinate().getLatitude(),position.getCoordinate().getLongitude());
                    map.setZoomLevel(
                            (map.getMaxZoomLevel() + map.getMinZoomLevel()));
                }

                public void onPositionFixChanged(LocationMethod method,
                                                 LocationStatus status) {
                }
            };
    private RouteManager.Listener routeManagerListener =
            new RouteManager.Listener()
            {
                public void onCalculateRouteFinished(RouteManager.Error errorCode,
                                                     List<RouteResult> result) {

                    if (errorCode == RouteManager.Error.NONE &&
                            result.get(0).getRoute() != null) {

                        // create a map route object and place it on the map
                        mapRoute = new MapRoute(result.get(0).getRoute());
                        map.addMapObject(mapRoute);


                        // Get the bounding box containing the route and zoom in
                        GeoBoundingBox gbb = result.get(0).getRoute().getBoundingBox();
                        map.zoomTo(gbb, Map.Animation.NONE,
                                Map.MOVE_PRESERVE_ORIENTATION);

                        textViewResult.setText(
                                String.format("Route calculated with %d maneuvers.",
                                        result.get(0).getRoute().getManeuvers().size()));
                    } else {
                        textViewResult.setText(
                                String.format("Route calculation failed: %s",
                                        errorCode.toString()));
                    }
                }

                public void onProgress(int percentage) {
                    textViewResult.setText(
                            String.format("... %d percent done ...", percentage));
                }
            };
    // Functionality for taps of the "Get Directions" button
    public void getDirections(View view) {
        // 1. clear previous results
        textViewResult.setText("");
        if (map != null && mapRoute != null) {
            map.removeMapObject(mapRoute);
            mapRoute = null;
        }

        // 2. Initialize RouteManager
        RouteManager routeManager = new RouteManager();

        // 3. Select routing options via RoutingMode
        RoutePlan routePlan = new RoutePlan();

        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);


        // 4. Select Waypoints for your routes
        // START: Nokia, Burnaby
        routePlan.addWaypoint(new GeoCoordinate(Src.Lat,Src.Longt));
        ;
        // END: Airport, YVR
        routePlan.addWaypoint(new GeoCoordinate(Dest.Lat,Dest.Longt));

        // 5. Retrieve Routing information via RouteManagerListener
        RouteManager.Error error =
                routeManager.calculateRoute(routePlan, routeManagerListener);

        if (error != RouteManager.Error.NONE) {
            Toast.makeText(getApplicationContext(),
                    "Route calculation failed with: " + error.toString(),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    };

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


    private class MyOnGestureListener implements MapGesture.OnGestureListener {
        @Override
        public void onPanStart() {

        }

        @Override
        public void onPanEnd() {

        }

        @Override
        public void onMultiFingerManipulationStart() {

        }

        @Override
        public void onMultiFingerManipulationEnd() {

        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> viewObjects) {
            return false;
        }

        @Override
        public boolean onTapEvent(PointF pointF) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF pointF) {

            return false;
        }

        @Override
        public void onPinchLocked() {

        }

        @Override
        public boolean onPinchZoomEvent(float v, PointF pointF) {
            return false;
        }

        @Override
        public void onRotateLocked() {

        }

        @Override
        public boolean onRotateEvent(float v) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float v) {
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF pointF) {
            Dest = new LocationCoord(map.pixelToGeo(pointF).getLatitude(),map.pixelToGeo(pointF).getLongitude());
/*
            Image myImage = new Image();
            try {
                myImage.setImageResource(R.drawable.trackingdot);

            //Toast.makeText(getApplicationContext(), "Launching...", Toast.LENGTH_SHORT).show();

            MapMarker mm = new MapMarker(new GeoCoordinate(Dest.Lat,Dest.Longt),myImage);
            mm.setAnchorPoint(pointF);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            return false;
        }

        @Override
        public void onLongPressRelease() {

        }

        @Override
        public boolean onTwoFingerTapEvent(PointF pointF) {
            return false;
        }
    }

}
