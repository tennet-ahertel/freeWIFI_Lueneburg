package de.teutronic.freewifi_lueneburg.DB;

import java.sql.Date;

/**
 * Created by Andreas Hertel on 08.03.2018.
 */

public class FreeWIFI_DBobj {
    public static final String GPS_TABLE_NAME ="GPS_Tbl";
    public static final String GPS_RECORD_ID  ="id";
    public static final String GPS_SSID_NAME  ="SSID";
    public static final String GPS_LATITUDE   ="latitude";
    public static final String GPS_LONGITUDE  ="logitude";
    public static final String GPS_LASTUPDATE ="lastupdate";


    private String ssid;
    private String latitude;
    private String logitude;
    private Date lastupdate;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Date getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String createTable () {
        StringBuilder stb = new StringBuilder();
        stb.append("CREATE TABLE IF NOT EXISTS ");
        stb.append(GPS_TABLE_NAME);
        stb.append(" ( ");
        stb.append(GPS_RECORD_ID);
        stb.append(" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " );
        stb.append(GPS_SSID_NAME);
        stb.append(" TEXT not null, ");
        stb.append(GPS_LATITUDE);
        stb.append(" TEXT not null, ");
        stb.append(GPS_LONGITUDE);
        stb.append(" TEXT not null, ");
        stb.append(GPS_LASTUPDATE);
        stb.append(" TEXT not null ");
        stb.append("  )");

        return stb.toString();
    }
}
