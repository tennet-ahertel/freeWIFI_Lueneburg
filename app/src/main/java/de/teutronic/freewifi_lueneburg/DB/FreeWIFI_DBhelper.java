package de.teutronic.freewifi_lueneburg.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Andreas Hertel on 08.03.2018.
 */

public class FreeWIFI_DBhelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "freeWIFI_LG_lokal.db";
    private static final int CURRENT_VERSION = 1;
    private FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();

    public FreeWIFI_DBhelper(Context context) {
        super(context , DATABASE_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(freeWIFI_DBobj.createTable());
        } catch (Exception exp){
            Log.e("freeWIFI", exp.getLocalizedMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
