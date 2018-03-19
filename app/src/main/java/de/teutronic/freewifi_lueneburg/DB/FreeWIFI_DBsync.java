package de.teutronic.freewifi_lueneburg.DB;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.teutronic.freewifi_lueneburg.Connectivity;
import de.teutronic.freewifi_lueneburg.OpenStreetMapInterface;

/**
 * Created by Andreas Hertel on 18.03.2018.
 */

public class FreeWIFI_DBsync {
    private Activity mainActivity;
    private Context context = null;
    private Handler handler = new Handler();
    private FreeWIFI_DBhelper freeWIFI_DBhelper;
    private int delaycount=0;

    public void init(Activity activity) {
        mainActivity = activity;
        context = mainActivity.getBaseContext();
        freeWIFI_DBhelper = new FreeWIFI_DBhelper (context);
        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int delaymillisec = 60*1000; //eine Minute
            if (delaycount <= 0) {
                if (( context != null) && Connectivity.isConnected(context)) {
                    //WLAN ist da, nächste Abfrage erst wieder in einer Stunde vorsehen
                    delaycount=60;
                    syncSQLiteMySQLDB();
                } else {
                    //kein WLAN , probiere es in ein paar Minuten nochmal
                    delaycount=3;
                }
            } else {
                delaycount-= 1;
            }
            handler.postDelayed(this, delaymillisec);
        }
    };

    // Method to Sync MySQL to SQLite DB
    public void syncSQLiteMySQLDB() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        // Show ProgressBar
//        prgDialog.show();
        // Make Http call to getusers.php
//        client.post("<a class='vglnk' href='http://192.168.2.4:9000/mysqlsqlitesync/getusers.php' rel='nofollow'><span>http</span><span>://</span><span>192</span><span>.</span><span>168</span><span>.</span><span>2</span><span>.</span><span>4</span><span>:</span><span>9000</span><span>/</span><span>mysqlsqlitesync</span><span>/</span><span>getusers</span><span>.</span><span>php</span></a>", params, new AsyncHttpResponseHandler() {
        RequestHandle post = client.post("http://gw4.freifunk-lueneburg.de/meshviewer/data/nodelist.json", null, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                // Hide ProgressBar
                //              prgDialog.hide();
                Toast.makeText(context, "got map infos", Toast.LENGTH_SHORT).show();
                // Update SQLite DB with response sent by getusers.php
                updateSQLite(response);
                OpenStreetMapInterface openStreetMapInterface = new OpenStreetMapInterface();
                openStreetMapInterface.init(mainActivity);
                openStreetMapInterface.appPos2osm();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                if (( context != null) && Connectivity.isConnectedWifi(context)) {
                    //WLAN ist da, aber trotzdem Fehler ? probiere es gleich nochmal
                    delaycount = 0;
                } else {
                    delaycount = 3;
                }

                if (statusCode == 404) {
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                    //da nützt dann vermutlich auch ständiges Abfragen nichts ...
                    delaycount = 10;
                } else if (statusCode == 500) {
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Unexpected Error occcured! ", Toast.LENGTH_LONG).show();
                    //[Most common Error: Device might not be connected to Internet]
                }
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
    public void updateSQLite(byte[] response){
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        String responseString = new String(response);
        FreeWIFI_DBResolver resolver = new FreeWIFI_DBResolver(freeWIFI_DBhelper.getWritableDatabase());
        try {
            JSONObject obj2 = new JSONObject(responseString);
            // Extract JSON array from the response
            JSONArray arr = obj2.getJSONArray("nodes");
            // If no of array elements is not zero
            if(arr.length() != 0){
                // Loop through each array element, get JSON object which has userid and username
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
                    if (! obj.has("position")) continue;
                    JSONObject pos = obj.getJSONObject("position");
                    Log.d("map","name="+obj.get("name")+" lon="+pos.get("long")+" lat="+pos.get("lat"));
                    FreeWIFI_DBobj freeWIFI_DBobj = new FreeWIFI_DBobj();
                    freeWIFI_DBobj.setName(obj.get("name").toString());
                    freeWIFI_DBobj.setMapid(obj.get("id").toString());
                    freeWIFI_DBobj.setLogitude(pos.get("long").toString());
                    freeWIFI_DBobj.setLatitude(pos.get("lat").toString());
                    JSONObject stat = obj.getJSONObject("status");
                    if (stat.get("online").toString() == "false")
                        freeWIFI_DBobj.setOffline(true);
                    else
                        freeWIFI_DBobj.setOffline(false);
                    resolver.checkAndInsertNewNode(freeWIFI_DBobj);
                }
                Log.v("freeWIFI", "new data from "+arr.length()+" nodes imported");
                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                //updateMySQLSyncSts(gson.toJson(usersynclist));
                // Reload the Main Activity
                //reloadActivity();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Method to inform remote MySQL DB about completion of Sync activity
 /*   public void updateMySQLSyncSts(String json) {
        System.out.println(json);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("syncsts", json);
        // Make Http call to updatesyncsts.php with JSON parameter which has Sync statuses of Users
        client.post("<a class='vglnk' href='http://192.168.2.4:9000/mysqlsqlitesync/updatesyncsts.php' rel='nofollow'><span>http</span><span>://</span><span>192</span><span>.</span><span>168</span><span>.</span><span>2</span><span>.</span><span>4</span><span>:</span><span>9000</span><span>/</span><span>mysqlsqlitesync</span><span>/</span><span>updatesyncsts</span><span>.</span><span>php</span></a>", params, new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(String response) {
            Toast.makeText(getApplicationContext(),	"MySQL DB has been informed about Sync activity", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(int statusCode, Throwable error, String content) {
            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
        }
    });
}*/

}
