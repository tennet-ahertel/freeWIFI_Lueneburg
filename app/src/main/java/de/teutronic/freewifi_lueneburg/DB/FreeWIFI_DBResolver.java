package de.teutronic.freewifi_lueneburg.DB;

import android.content.ContentValues;
import android.database.ContentObservable;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.teutronic.freewifi_lueneburg.R;

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

}
