package de.teutronic.freewifi_lueneburg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBResolver;
import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBhelper;
import de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBobj;

/**
 * Created by Andreas Hertel on 19.03.2018.
 */

public class OpenStreetMapInterface {
    private Activity mainActivity;
    private Context context;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private ItemizedOverlayWithFocus<OverlayItem> fflgOverlay;
    private GeoPoint geoPoints[];
    private FreeWIFI_DBhelper freeWIFI_DBhelper;
    private FreeWIFI_DBResolver resolver;

    public void init(Activity activity) {
        mainActivity = activity;
        context = mainActivity.getBaseContext();
        freeWIFI_DBhelper = new FreeWIFI_DBhelper (context);
        resolver = new FreeWIFI_DBResolver(freeWIFI_DBhelper.getReadableDatabase());

        mapView = mainActivity.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        centermap2pos();
        appPos2osm();
        mapView.getOverlays().add(fflgOverlay);
    }

    public void centermap2pos() {
        mapView.getController().setZoom(19);
        geoPoints=MainActivity.getGeoPts();
        mapView.getController().setCenter(geoPoints[0]);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

    }

    public void invalidate() {
        mapView.invalidate();
    }

    public void zeigePosition() {
        List<Location> weg = PositionService.weg;
        geoPoints=MainActivity.getGeoPts();
        mapView.getOverlayManager().clear();

        if ( MainActivity.showway  && !weg.isEmpty() && (weg.size()>1) ) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            Polyline line = new Polyline();
            line.setColor(Color.LTGRAY);
            for(int i=0; i<weg.size(); i++) {
                geoPoints.add(new GeoPoint(weg.get(i)));
            }
            line.setPoints(geoPoints);
            mapView.getOverlayManager().add(line);
        }

        if (geoPoints[3] != null) {
            List<GeoPoint> geoPoints3 = new ArrayList<>();
            Polyline line2AP3 = new Polyline();
            line2AP3.setColor(Color.WHITE);
            geoPoints3.add(geoPoints[0]);
            geoPoints3.add(geoPoints[3]);
            line2AP3.setPoints(geoPoints3);
            mapView.getOverlayManager().add(line2AP3);
        }
        if (geoPoints[2] != null) {
            List<GeoPoint> geoPoints2 = new ArrayList<>();
            Polyline line2AP2 = new Polyline();
            line2AP2.setColor(Color.YELLOW);
            geoPoints2.add(geoPoints[0]);
            geoPoints2.add(geoPoints[2]);
            line2AP2.setPoints(geoPoints2);
            mapView.getOverlayManager().add(line2AP2);
        }
        List<GeoPoint> geoPointlist = new ArrayList<>();
        Polyline line2AP = new Polyline();
        line2AP.setColor(Color.GREEN);
        geoPointlist.add(geoPoints[0]);
        geoPointlist.add(geoPoints[1]);
        line2AP.setPoints(geoPointlist);
        mapView.getOverlayManager().add(line2AP);

        mapView.getOverlays().add(myLocationOverlay);
        mapView.getOverlays().add(fflgOverlay);
    }

    public void appPos2osm() {
        //aus Datenbank-Liste OSM items erzeugen
        List<FreeWIFI_DBobj> freeWIFIList;
        ArrayList<OverlayItem> fflgitems = new ArrayList<OverlayItem>();
        Drawable fflgMarker = context.getResources().getDrawable(R.drawable.freifunk);
        Drawable fflgMarkerOff = context.getResources().getDrawable(R.drawable.freifunk_);
        freeWIFIList = resolver.getFreeWIFIList();
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
        fflgOverlay = new ItemizedOverlayWithFocus<OverlayItem>(context,fflgitems,
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
}
