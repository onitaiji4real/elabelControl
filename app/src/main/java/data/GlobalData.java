package data;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

//MakerID尚未加入當識別值，目前APP若有回寫會帶入0
//盤點資料以及盤點所產生的收入支出只能用總數回傳，目前無法考慮各批號的數量，若是此種方式傳回的資料其lotnumber會帶入"APP"，系統端後續再去決定如何相容
public class GlobalData extends Application {
    private Context context;

    ConnectivityManager connectivityManager;//實作連線的實例
    WifiManager wifiManager;


    NetworkInfo activeNetworkInfo;
    boolean isConnected;

    WifiInfo wifiInfo;
    String connectedSSID;
    String targetSSID;

    boolean isConnectedToTargetNetwork;

    public GlobalData() {
    }

    public GlobalData(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        this.isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        this.wifiInfo = wifiManager.getConnectionInfo();
        this.connectedSSID = wifiInfo.getSSID();
        this.targetSSID = "BAIGUO";

//        if (this.connectedSSID == this.targetSSID){
//            isConnectedToTargetNetwork = true;
//        }else{
//            isConnectedToTargetNetwork = false;
//        }
        this.isConnectedToTargetNetwork = connectedSSID.equals(this.targetSSID);

        // 打印变量值到日志
        Log.d("GlobalData", "context: " + context);
        Log.d("GlobalData", "connectivityManager: " + connectivityManager);
        Log.d("GlobalData", "wifiManager: " + wifiManager);
        Log.d("GlobalData", "activeNetworkInfo: " + activeNetworkInfo);
        Log.d("GlobalData", "isConnected: " + isConnected);
        Log.d("GlobalData", "wifiInfo: " + wifiInfo);
        Log.d("GlobalData", "connectedSSID: " + connectedSSID);
        Log.d("GlobalData", "targetSSID: " + targetSSID);
        Log.d("GlobalData", "isConnectedToTargetNetwork: " + isConnectedToTargetNetwork);
//        Log.e("GlobalData", "context: " + context);
//        Log.e("GlobalData", "connectivityManager: " + connectivityManager);
//        Log.e("GlobalData", "wifiManager: " + wifiManager);
//        Log.e("GlobalData", "activeNetworkInfo: " + activeNetworkInfo);
//        Log.e("GlobalData", "isConnected: " + isConnected);
//        Log.e("GlobalData", "wifiInfo: " + wifiInfo);
//        Log.e("GlobalData", "connectedSSID: " + connectedSSID);
//        Log.e("GlobalData", "targetSSID: " + targetSSID);
//        Log.e("GlobalData", "isConnectedToTargetNetwork: " + isConnectedToTargetNetwork);

    }

    public String Server = "http://192.168.5.77:8080/";
    public String AIMSServer = "http://192.168.5.137:9003/";

    //public String PHP_SERVER = "http://10.0.0.11:8080/pda_submit.php?";
    //public String PHP_SERVER = "http://192.168.5.41/pda_submit.php?";
    public String PHP_SERVER = "http://192.168.5.130:8080/pda_submit.php?";


    public String AIMS_SERVER = "192.168.219.100";
    public String AIMS_BLINK_URL = String.format("http://%s:9003/labels/contents/led", AIMS_SERVER);

    private String LoginUserID;
    private String LoginUserName;
    private List<User> users;
    public List<Drugstore> Drugstores;
    private List<Druginfo> Druginfos;
    private List<Inventory> Inventorys;

    //修改 變數値
    public void setLoginUserID(String LoginUserID) {
        this.LoginUserID = LoginUserID;
    }

    public void setLoginUserName(String LoginUserName) {
        this.LoginUserName = LoginUserName;
    }

    public void setDrugstores(List<Drugstore> Drugstores) {
        this.Drugstores = Drugstores;
    }

    //取得 變數值
    public String getLoginUserID() {
        return LoginUserID;
    }

    public String getLoginUserName() {
        return LoginUserName;
    }

    public List<Drugstore> getDrugstores() {
        return Drugstores;
    }

    public String getPHP_SERVER() {
        return PHP_SERVER;
    }

    public String getAIMS_BLINK_URL() {
        return AIMS_BLINK_URL;
    }

    public boolean getNetworkConnectStatus() {
        return isConnected;
    }
}
