package de.teutronic.freewifi_lueneburg;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.ContentValues;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.ViewGroup;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBhelper;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private SensorManager sensorManager;
    private FreeWIFI_DBhelper freeWIFI_DBhelper;
    private volatile GeoPoint actGeoPt = new GeoPoint(53.24774,10.41125);
    private volatile GeoPoint nextAPGeoPt = new GeoPoint(53.247135,10.409009);
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

        /*SQL Datenbank*/
        freeWIFI_DBhelper = new FreeWIFI_DBhelper (this);

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
        Log.d("freeWIFI","angle (actGeoPt/nextAPGeoPt):"+Float.toString(angle));
        //Log.d("freeWIFI","handyAzimut:"+Float.toString(handyAzimut));
        return handyAzimut + angle;
    }
    private float getDistanceToAP(){
        float dist = (float) actGeoPt.distanceTo(nextAPGeoPt);
        Log.d("freeWIFI","distance (actGeoPt/nextAPGeoPt):"+Float.toString(dist));
        //Log.d("freeWIFI","handyAzimut:"+Float.toString(handyAzimut));
        return dist;
    }

    public void setActGeoPt (Location location) {
        actGeoPt = new GeoPoint(location);
        Log.d("freeWIFI","actGeoPt (lat/lon):"+Double.toString(actGeoPt.getLatitude())+"/"+Double.toString(actGeoPt.getLongitude()));
        //wo ist der naechstgelegene AP ?
        //nextAPGeoPt = ?
        Log.d("freeWIFI","nextAPGeoPt (lat/lon):"+Double.toString(nextAPGeoPt.getLatitude())+"/"+Double.toString(nextAPGeoPt.getLongitude()));
    }

    private class PositionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            zeigePosition();
        }
    }

    private void zeigePosition() {
        List<Location> weg = PositionService.weg;
        if(!weg.isEmpty()) {
            mapView.getOverlayManager().clear();
            //Log.d("freeWIFI","weg.size()="+Integer.toString(weg.size()));
            if(weg.size()>1) {
                List<GeoPoint> geoPoints = new ArrayList<>();
                Polyline line = new Polyline();
                line.setColor(Color.GREEN);
                for(int i=0; i<weg.size(); i++) {
                    geoPoints.add(new GeoPoint(weg.get(i)));
                }
                line.setPoints(geoPoints);
                mapView.getOverlayManager().add(line);
            }
            List<GeoPoint> geoPoints = new ArrayList<>();
            Polyline line2AP = new Polyline();
            line2AP.setColor(Color.RED);
            geoPoints.add(actGeoPt.clone());
            geoPoints.add(nextAPGeoPt.clone());
            line2AP.setPoints(geoPoints);
            mapView.getOverlayManager().add(line2AP);
            mapView.getOverlays().add(myLocationOverlay);
        } else {
            Log.d("freeWIFI","weg.isEmpty()");
        }
    }
}
