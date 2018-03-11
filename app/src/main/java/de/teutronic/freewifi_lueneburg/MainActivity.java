package de.teutronic.freewifi_lueneburg;

import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.util.GeoPoint;

import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBhelper;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private SensorManager sensorManager;
    private FreeWIFI_DBhelper freeWIFI_DBhelper;
    private GeoPoint actGeoPt = new GeoPoint(53.25000,10.41000);
    private GeoPoint nextAPGeoPt = new GeoPoint(53.247135,10.409009);
    private Sensor sensor_orientation;
    private float handyAzimut=0;
    private float angleToAP =0;
    private DirectionView dirView;
//    private View dirView;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        /*SQL Datenbank*/
        freeWIFI_DBhelper = new FreeWIFI_DBhelper (this);

        /*Sensoren*/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor_orientation == null){
            Toast.makeText(this, "no orientation sensor", Toast.LENGTH_LONG).show();
        }

        /*Richtungsanzeige*/
        dirView = findViewById(R.id.direction);
 //      dirView = new DirectionView(this);
        if (dirView == null) {
            Toast.makeText(this, "no dirview :-(", Toast.LENGTH_LONG).show();
        } else {
            //setContentView(dirView);
           int widthSpec = View.MeasureSpec.makeMeasureSpec (ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
            int heightSpec = View.MeasureSpec.makeMeasureSpec (400, View.MeasureSpec.UNSPECIFIED);
            dirView.measure(widthSpec, heightSpec);
            dirView.layout(0, 0, dirView.getMeasuredWidth(), dirView.getMeasuredHeight());
            canvas = new Canvas();
            canvas.translate(dirView.getMeasuredWidth(),dirView.getMeasuredHeight());
        }

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
        handyAzimut = - event.values[0];  /*0: Azimut 1: Polar    das Minuszeichen ist wichtig!*/
        angleToAP = getAngleToAP();
        if (dirView != null) {
            dirView.setWinkel(angleToAP);
            dirView.draw(canvas);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensor_orientation != null){
            sensorManager.registerListener( this ,sensor_orientation, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this, "no orientation sensor", Toast.LENGTH_LONG).show();
        }


    }
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private float getAngleToAP(){

       return handyAzimut;
    }

    public void setActGeoPt (Location location) {
        actGeoPt = new GeoPoint(location);

    }
}
