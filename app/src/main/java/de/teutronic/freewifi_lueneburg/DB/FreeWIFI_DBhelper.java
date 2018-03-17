package de.teutronic.freewifi_lueneburg.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import static de.teutronic.freewifi_lueneburg.DB.FreeWIFI_DBobj.CURRENT_VERSION;

/**
 * Created by Andreas Hertel on 08.03.2018.
 */

public class FreeWIFI_DBhelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "freeWIFI_LG_lokal.db";
    private FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();

    public FreeWIFI_DBhelper(Context context) {
        super(context , DATABASE_NAME, null, CURRENT_VERSION);
 //       context.deleteFile(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<String> sql_cmds = freeWIFI_DBobj.createTable();
        for (String sql_cmd :sql_cmds) {
            try {
                db.execSQL(sql_cmd);
            } catch (Exception exp){
                Log.e("freeWIFI", exp.getLocalizedMessage());
                Log.e("freeWIFI", "sql:"+sql_cmd);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("freeWIFI", "DB Upgrade:"+Integer.toString(oldVersion)+" -> " +Integer.toString(newVersion));
        db.execSQL("Drop table if exists "+freeWIFI_DBobj.GPS_TABLE_NAME);
        db.execSQL("Drop table if exists "+freeWIFI_DBobj.SYSTEMCONFIG_TABLE_NAME);
        List<String> sql_cmds = freeWIFI_DBobj.createTable();
        for (String sql_cmd :sql_cmds) {
            try {
                db.execSQL(sql_cmd);
            } catch (Exception exp){
                Log.e("freeWIFI", exp.getLocalizedMessage());
                Log.e("freeWIFI", "sql:"+sql_cmd);
            }
        }
    }


}
