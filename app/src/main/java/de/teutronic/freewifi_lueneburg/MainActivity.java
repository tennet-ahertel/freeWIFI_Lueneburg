package de.teutronic.freewifi_lueneburg;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBResolver;
import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBhelper;
import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBobj;
import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBsync;


public class MainActivity  extends AppCompatActivity implements SensorEventListener  {
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private SensorManager sensorManager;
    private FreeWIFI_DBhelper freeWIFI_DBhelper;
    private volatile GeoPoint actGeoPt = new GeoPoint(53.24774,10.41125);
    private volatile GeoPoint nextAPGeoPt = new GeoPoint(53.247135,10.409009);
    private volatile GeoPoint nextAPGeoPt2=null;
    private volatile GeoPoint nextAPGeoPt3=null;
    private Sensor sensor_accelerometer;
    private Sensor sensor_magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;

    private float handyAzimut=0;
    private float angleToAP =0;
    private DirectionView dirView;
//    private View dirView;
    private Canvas canvas;
    private PositionHandler positionHandler = new PositionHandler();
    private boolean magnetometer_availiable = true;
    private boolean showway = false;
    private List<FreeWIFI_DBobj> freeWIFIList;
    private ItemizedOverlayWithFocus<OverlayItem> fflgOverlay;
    private FreeWIFI_DBsync freeWIFI_DBsync = new FreeWIFI_DBsync();

    // Progress Dialog Object
    ProgressDialog prgDialog;
    HashMap<String, String> queryValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("freeWIFI","started");
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        /*Sensoren*/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor_magnetometer == null){
            Toast.makeText(this, "no magnetometer sensor", Toast.LENGTH_SHORT).show();
            magnetometer_availiable = false;
        } else {
            sensor_accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (sensor_accelerometer == null){
                Toast.makeText(this, "no accelerometer sensor", Toast.LENGTH_SHORT).show();
            }
        }

        /*Richtungsanzeige*/
        dirView = findViewById(R.id.direction);
        if (dirView == null) {
            Toast.makeText(this, "no dirview element found", Toast.LENGTH_LONG).show();
        } else {
            if (magnetometer_availiable) {
                int widthSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.UNSPECIFIED);
                dirView.measure(widthSpec, heightSpec);
                dirView.layout(0, 0, dirView.getMeasuredWidth(), dirView.getMeasuredHeight());
                canvas = new Canvas();
                canvas.translate(dirView.getMeasuredWidth(), dirView.getMeasuredHeight());
            } else {
                dirView.setVisibility(View.INVISIBLE);
            }
        }

        /*Position */
        startService(new Intent(this, PositionService.class));
        PositionService.updateHandler = positionHandler;
        PositionService.mainActivity = this;


        /*SQL Datenbank*/
        /* test stuff
        File file = new File("/data/data/de.teutronic.freewifi_lueneburg/databases/"+FreeWIFI_DBhelper.DATABASE_NAME);
        if (file.delete()) {
            Toast.makeText(this, "database deleted", Toast.LENGTH_LONG).show();
        }*/

        freeWIFI_DBhelper = new FreeWIFI_DBhelper (this);

        FreeWIFI_DBResolver resolver = new FreeWIFI_DBResolver(freeWIFI_DBhelper.getWritableDatabase());
        FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();

        // WLAN ?
        if (Connectivity.isConnectedWifi(this)){
            NetworkInfo info =Connectivity.getNetworkInfo(this);
            String ssid =info.getExtraInfo();
            if (ssid.indexOf("lueneburg.freifunk.net") != -1) {
                Toast.makeText(this, "trying database update", Toast.LENGTH_LONG).show();
            }
        }
        freeWIFI_DBsync.init(this);


        //freeWIFI_DBobj.setSsid("First");
        //  freeWIFI_DBobj.setCreationDate("02.03.2018");
        //  resolver.insertNewStuff(freeWIFI_DBobj);
        freeWIFIList = resolver.getFreeWIFIList();
        for (FreeWIFI_DBobj freeWIFI_DBobj2 : freeWIFIList) {
            Log.v("DB:", "ssid="+freeWIFI_DBobj2.getSsid()+" Lon="+freeWIFI_DBobj2.getLogitude()+" Lat="+freeWIFI_DBobj2.getLatitude()+" praise="+freeWIFI_DBobj2.getPraise()+" offline="+Boolean.toString(freeWIFI_DBobj2.getOffline()));
        }


        /*OSM*/
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(19);
        mapView.getController().setCenter(actGeoPt);
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();


        //aus Datenbank-Liste OSM items erzeugen
        ArrayList<OverlayItem> fflgitems = new ArrayList<OverlayItem>();
        Drawable fflgMarker = this.getResources().getDrawable(R.drawable.freifunk);
        Drawable fflgMarkerOff = this.getResources().getDrawable(R.drawable.freifunk_);
        for (FreeWIFI_DBobj freeWIFI_DBobj2 : freeWIFIList) {
            GeoPoint apGeoPt = new GeoPoint(Float.parseFloat(freeWIFI_DBobj2.getLatitude()),Float.parseFloat(freeWIFI_DBobj2.getLogitude()));
            OverlayItem olItem = new OverlayItem(freeWIFI_DBobj2.getPraise(), freeWIFI_DBobj2.getLink(), apGeoPt);
            if (freeWIFI_DBobj2.getOffline()) {
                olItem.setMarker(fflgMarkerOff);
            } else {
                olItem.setMarker(fflgMarker);
            }
            fflgitems.add(olItem);
        }
        //the overlay
        fflgOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this,fflgitems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Log.i("Touch","you just tap the hotspot");
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                });
        fflgOverlay.setFocusItemsOnTap(true);
        mapView.getOverlays().add(fflgOverlay);



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
        if (id == R.id.action_center) {
            mapView.getController().setZoom(19);
            mapView.getController().setCenter(actGeoPt);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
            return true;
        }
        if (id == R.id.action_showway) {
            if (showway) {
                showway = false ;
                item.setTitle(R.string.action_showway);
            } else {
                showway = true;
                item.setTitle(R.string.action_showway_alt);
            }
            zeigePosition();
            mapView.invalidate();
            return true;
        }
        if (id == R.id.action_delway) {
            PositionService.deleteWeg();
            if (showway) {
                zeigePosition();
                mapView.invalidate();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean success =false;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                handyAzimut = (float)( Math.toDegrees ((double) orientation[0])); // orientation contains: azimut, pitch and roll
                //Log.d("freeWIFI","handyAzimut :"+Float.toString(handyAzimut));
            }
        }

        angleToAP = getAngleToAP();
        if (dirView != null) {
            dirView.setWinkel(- angleToAP); //das Minuszeichen ist wichtig ; Azimutwinkel dreht rechtsrum; Graphik linksrum
            dirView.draw(canvas);
        }
        float dist = getDistanceToAP();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensor_magnetometer != null){
            sensorManager.registerListener(this, sensor_magnetometer, SensorManager.SENSOR_DELAY_UI);
            if (sensor_accelerometer != null){
                sensorManager.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_UI);
            } else {
               // Toast.makeText(this, "no accelerometer sensor", Toast.LENGTH_LONG).show();
            }
        } else {
           // Toast.makeText(this, "no magnetometer sensor", Toast.LENGTH_LONG).show();
        }
        zeigePosition();
        PositionService.updateHandler = positionHandler;
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private float getAngleToAP(){
        float angle = (float) actGeoPt.bearingTo(nextAPGeoPt);
        if (angle > 180) angle = -(360 -angle);
        //Log.d("freeWIFI","angle (actGeoPt/nextAPGeoPt):"+Float.toString(angle));
        //Log.d("freeWIFI","handyAzimut:"+Float.toString(handyAzimut));
        return handyAzimut + angle;
    }
    private float getDistanceToAP(){
        float dist = (float) actGeoPt.distanceTo(nextAPGeoPt);
        //Log.d("freeWIFI","distance (actGeoPt/nextAPGeoPt):"+Float.toString(dist));
        //Log.d("freeWIFI","handyAzimut:"+Float.toString(handyAzimut));
        return dist;
    }

    public void setActGeoPt (Location location) {
        actGeoPt = new GeoPoint(location);
        float distact,distlow=1000000,distlow2=1000001,distlow3=1000002;;;
        Log.d("freeWIFI","actGeoPt (lat/lon):"+Double.toString(actGeoPt.getLatitude())+"/"+Double.toString(actGeoPt.getLongitude()));
        //wo sind die drei naechstgelegenen AP ?
        for (FreeWIFI_DBobj freeWIFI_DBobj2 : freeWIFIList) {
            GeoPoint apGeoPt = new GeoPoint(Float.parseFloat(freeWIFI_DBobj2.getLatitude()),Float.parseFloat(freeWIFI_DBobj2.getLogitude()));
            distact = (float) actGeoPt.distanceTo(apGeoPt);
            if ((distact <distlow) && (!freeWIFI_DBobj2.getOffline())){
                distlow=distact;
                nextAPGeoPt = apGeoPt;
            }
        }
        Log.d("freeWIFI","nextAPGeoPt (meter):"+Float.toString(distlow));
        for (FreeWIFI_DBobj freeWIFI_DBobj2 : freeWIFIList) {
            GeoPoint apGeoPt = new GeoPoint(Float.parseFloat(freeWIFI_DBobj2.getLatitude()),Float.parseFloat(freeWIFI_DBobj2.getLogitude()));
            distact = (float) actGeoPt.distanceTo(apGeoPt);
            if ((distact >distlow) &&  (distact <distlow2) && (!freeWIFI_DBobj2.getOffline())){
                distlow2=distact;
                nextAPGeoPt2 = apGeoPt;
            }
        }
        Log.d("freeWIFI","nextAPGeoPt2 (meter):"+Float.toString(distlow2));
        for (FreeWIFI_DBobj freeWIFI_DBobj2 : freeWIFIList) {
            GeoPoint apGeoPt = new GeoPoint(Float.parseFloat(freeWIFI_DBobj2.getLatitude()),Float.parseFloat(freeWIFI_DBobj2.getLogitude()));
            distact = (float) actGeoPt.distanceTo(apGeoPt);
            if ((distact >distlow2) &&  (distact <distlow3) && (!freeWIFI_DBobj2.getOffline())){
                distlow3=distact;
                nextAPGeoPt3 = apGeoPt;
            }
        }
        Log.d("freeWIFI","nextAPGeoPt3 (meter):"+Float.toString(distlow3));
    }

    private class PositionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            zeigePosition();
        }
    }

    private void zeigePosition() {
        List<Location> weg = PositionService.weg;
        mapView.getOverlayManager().clear();

        if ( showway  && !weg.isEmpty() && (weg.size()>1) ) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            Polyline line = new Polyline();
            line.setColor(Color.LTGRAY);
            for(int i=0; i<weg.size(); i++) {
                geoPoints.add(new GeoPoint(weg.get(i)));
            }
            line.setPoints(geoPoints);
            mapView.getOverlayManager().add(line);
        }

        if (nextAPGeoPt3 != null) {
            List<GeoPoint> geoPoints3 = new ArrayList<>();
            Polyline line2AP3 = new Polyline();
            line2AP3.setColor(Color.WHITE);
            geoPoints3.add(actGeoPt.clone());
            geoPoints3.add(nextAPGeoPt3.clone());
            line2AP3.setPoints(geoPoints3);
            mapView.getOverlayManager().add(line2AP3);
        }
        if (nextAPGeoPt2 != null) {
            List<GeoPoint> geoPoints2 = new ArrayList<>();
            Polyline line2AP2 = new Polyline();
            line2AP2.setColor(Color.YELLOW);
            geoPoints2.add(actGeoPt.clone());
            geoPoints2.add(nextAPGeoPt2.clone());
            line2AP2.setPoints(geoPoints2);
            mapView.getOverlayManager().add(line2AP2);
        }
        List<GeoPoint> geoPoints = new ArrayList<>();
        Polyline line2AP = new Polyline();
        line2AP.setColor(Color.GREEN);
        geoPoints.add(actGeoPt.clone());
        geoPoints.add(nextAPGeoPt.clone());
        line2AP.setPoints(geoPoints);
        mapView.getOverlayManager().add(line2AP);

        mapView.getOverlays().add(myLocationOverlay);
        mapView.getOverlays().add(fflgOverlay);
    }

}
