package de.teutronic.freewifi_lueneburg.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas Hertel on 08.03.2018.
 */

public class FreeWIFI_DBResolver {
    private final Boolean SHOW_SQL = true;

    private static SQLiteDatabase freeWIFI_DB;
    private FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();
    private ContentValues contentValues = new ContentValues();

    /*
    * constructor, der das static DB versorgt*/
    public FreeWIFI_DBResolver(SQLiteDatabase DBParam) {
        freeWIFI_DB =DBParam;
    }

    public List<FreeWIFI_DBobj> getFreeWIFIList() {
        Cursor cursor = null;
        try {
            cursor = getCursorSQL("select * from "+FreeWIFI_DBobj.GPS_TABLE_NAME+";");
            List<FreeWIFI_DBobj> freeWIFI_DBobjList = new ArrayList<>();
            while (cursor.moveToNext()) {
                FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();
                freeWIFI_DBobj.setSsid(cursor.getString(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_SSID_NAME)));
                freeWIFI_DBobj.setLogitude(cursor.getString(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_LONGITUDE)));
                freeWIFI_DBobj.setLatitude(cursor.getString(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_LATITUDE)));
                freeWIFI_DBobj.setPraise(cursor.getString(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_PRAISE)));
                freeWIFI_DBobj.setLink(cursor.getString(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_LINK)));
                freeWIFI_DBobj.setLastupdate(cursor.getLong(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_LASTUPDATE)));
                if (cursor.getInt(cursor.getColumnIndex(FreeWIFI_DBobj.GPS_OFFLINE)) == 1) {
                    freeWIFI_DBobj.setOffline(true);
                } else {
                    freeWIFI_DBobj.setOffline(false);
                }
                freeWIFI_DBobjList.add(freeWIFI_DBobj);
            }
            return freeWIFI_DBobjList;

        } catch (Exception exp) {
            Log.e("geht nicht", exp.getLocalizedMessage());
            return null;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
    private Cursor getCursorSQL (String sqlStatement) {
        return freeWIFI_DB.rawQuery(sqlStatement, null);
    }

    public boolean checkAndInsertNewNode (FreeWIFI_DBobj freeWIFI_dBobj) {
        ContentValues contentValues= new ContentValues();
        try {
            contentValues.put(FreeWIFI_DBobj.GPS_NODE_NAME, freeWIFI_dBobj.getName());
            contentValues.put(FreeWIFI_DBobj.GPS_LONGITUDE, freeWIFI_dBobj.getLogitude());
            contentValues.put(FreeWIFI_DBobj.GPS_LATITUDE, freeWIFI_dBobj.getLatitude());
            contentValues.put(FreeWIFI_DBobj.GPS_OFFLINE, freeWIFI_dBobj.getOffline());
            contentValues.put(FreeWIFI_DBobj.GPS_MAPID, freeWIFI_dBobj.getMapid());
            if (freeWIFI_DB.update(FreeWIFI_DBobj.GPS_TABLE_NAME,contentValues,FreeWIFI_DBobj.GPS_MAPID+" = '"+freeWIFI_dBobj.getMapid()+"'", null) == 1) {
                Log.v("freeWIFI", freeWIFI_dBobj.getName()+" updated !");
            } else {
                //neuer Eintrag
                String praise = "";
                String name = freeWIFI_dBobj.getName();
                if ((name.indexOf("ff") == -1) && (name.indexOf("FF") == -1) && (name.indexOf("freifunk") == -1)) {
                    praise = name;
                }
                contentValues.put(FreeWIFI_DBobj.GPS_PRAISE, praise);
                if (freeWIFI_DB.insert(FreeWIFI_DBobj.GPS_TABLE_NAME, " ", contentValues) > 0) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Log.e("freeWIFI", ex.getLocalizedMessage());
        }

        return false;
    }

}
