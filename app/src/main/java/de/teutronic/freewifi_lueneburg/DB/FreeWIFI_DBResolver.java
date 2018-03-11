package de.teutronic.freewifi_lueneburg.DB;

import android.content.ContentValues;
import android.database.ContentObservable;
import android.database.sqlite.SQLiteDatabase;

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
    public FreeWIFI_DBResolver(SQLiteDatabase firstDBParam) {
        freeWIFI_DB =firstDBParam;
    }

}
