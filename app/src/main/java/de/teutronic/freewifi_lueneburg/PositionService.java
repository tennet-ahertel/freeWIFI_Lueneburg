package de.teutronic.freewifi_lueneburg;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 06.03.18.
 */
public class PositionService extends Service implements LocationListener {

    private LocationManager locationManager;
    public static List<Location> weg = new ArrayList<Location>();
    public static Handler updateHandler;

    public static MainActivity mainActivity = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //checkPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,2,this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,    1000,2,this);
        weg.clear();
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        weg.clear();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        weg.add(location);
        if (mainActivity != null)
            mainActivity.setActGeoPt(location);
        if(updateHandler!=null) {
            updateHandler.sendEmptyMessage(1);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public static void deleteWeg() {
        weg.clear();
    }

}
