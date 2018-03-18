package de.teutronic.freewifi_lueneburg.DB;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Andreas Hertel on 08.03.2018.
 */

public class FreeWIFI_DBobj {
    public static final int CURRENT_VERSION = 1;

    public static final String GPS_TABLE_NAME ="GPS_Tbl";
    public static final String GPS_RECORD_ID  ="id";
    public static final String GPS_SSID_NAME  ="ssid";
    public static final String GPS_LATITUDE   ="latitude";
    public static final String GPS_LONGITUDE  ="logitude";
    public static final String GPS_LASTUPDATE ="lastupdate";
    public static final String GPS_PRAISE     ="praise";
    public static final String GPS_LINK       ="link";
    public static final String GPS_OFFLINE    ="offline";
    public static final String GPS_MAPID      ="mapid";

    public static final String SYSTEMCONFIG_TABLE_NAME ="System_Configuration";

    private String ssid;
    private String latitude;
    private String logitude;
    private Date lastupdate;
    private String praise;
    private String link;
    private Boolean offline;
    private String mapid;

    public String getSsid() {
        return ssid;
    }
    public String getLatitude() {
        return latitude;
    }
    public String getLogitude() {
        return logitude;
    }
    public String getPraise() {
        return praise;
    }
    public String getLink() {
        if (link == null) return "";
        return link;
    }
    public Boolean getOffline() {
        return offline;
    }
    public String getMapid() {
        return mapid;
    }


    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public void setLogitude(String logitude) {
        this.logitude = logitude;
    }
    public void setPraise(String praise) {
        this.praise = praise;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setOffline(Boolean offline) {
        this.offline = offline;
    }
    public void setMapid(String mapid) {
        this.mapid = mapid;
    }

    public Date getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(long lastupdate) {
        Date dateLU = new Date();
     //   dateLU.getTime(lastupdate);
        this.lastupdate = dateLU;
    }

    public List<String> createTable () {
        StringBuilder stb = new StringBuilder();
        List<String> r =new ArrayList<>();
        int i=0;
        stb.append("CREATE TABLE '"+GPS_TABLE_NAME+"' (");
        stb.append("    "+GPS_RECORD_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");
        stb.append("    "+GPS_MAPID+     " TEXT ,");
        stb.append("    "+GPS_SSID_NAME+ " TEXT NOT NULL DEFAULT 'lueneburg.freifunk.net',");
        stb.append("    "+GPS_LATITUDE+  " TEXT NOT NULL DEFAULT '',");
        stb.append("    "+GPS_LONGITUDE+ " TEXT NOT NULL DEFAULT '',");
        stb.append("    "+GPS_LASTUPDATE+" DATETIME NOT NULL DEFAULT '946728000',");
        stb.append("    "+GPS_PRAISE+    " TEXT NOT NULL DEFAULT '',");
        stb.append("    "+GPS_LINK+      " TEXT ,");
        stb.append("    "+GPS_OFFLINE+   " INTEGER NOT NULL DEFAULT 0");
        stb.append(")");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.247135','10.409009','leuphana');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.247467','10.41233','Ev.Familien-Bildungsstätte');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.25029' ,'10.3991' ,'Landkreis-Geb.5');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.30757' ,'10.28674','Gemeinde-Radbruch');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.1722'  ,'10.8164' ,'Gemeinde-Nahrendorf');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+") VALUES ('53.22515' ,'10.53676','Gemeinde-Barendorf');");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+GPS_TABLE_NAME+"' ("+GPS_LATITUDE+","+GPS_LONGITUDE+","+GPS_PRAISE+","+GPS_LINK+","+GPS_OFFLINE+") VALUES ('53.24245' ,'10.40674','Stadtlichter','www.stadtlichter.com',1);");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("CREATE TABLE '"+SYSTEMCONFIG_TABLE_NAME+"' (");
        stb.append("    'Setting' VARCHAR(255)  UNIQUE NOT NULL,");
        stb.append("    'Value'   VARCHAR(255)  NOT NULL");
        stb.append(");");
        r.add(stb.toString());
        stb = new StringBuilder();
        stb.append("INSERT INTO '"+SYSTEMCONFIG_TABLE_NAME+"' VALUES ('DatabaseRevision','"+CURRENT_VERSION+"');");
        r.add(stb.toString());

        return r;
    }
}
