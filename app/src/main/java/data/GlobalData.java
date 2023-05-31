package data;

import android.app.Application;

import java.util.List;

//MakerID尚未加入當識別值，目前APP若有回寫會帶入0
//盤點資料以及盤點所產生的收入支出只能用總數回傳，目前無法考慮各批號的數量，若是此種方式傳回的資料其lotnumber會帶入"APP"，系統端後續再去決定如何相容
public class GlobalData extends Application {
    public String Server = "http://192.168.5.77:8080/";
    public String AIMSServer = "http://192.168.5.42:8003/";
    private String LoginUserID;
    private String LoginUserName;
    private List<User> users;
    public List<Drugstore> Drugstores;
    private List<Druginfo> Druginfos;
    private List<Inventory> Inventorys;

    //修改 變數値
    public void setLoginUserID(String LoginUserID){
        this.LoginUserID = LoginUserID;
    }
    public void setLoginUserName(String LoginUserName){
        this.LoginUserName = LoginUserName;
    }
    public void setDrugstores(List<Drugstore> Drugstores){
        this.Drugstores = Drugstores;
    }

    //取得 變數值
    public String getLoginUserID() {
        return LoginUserID;
    }
    public String getLoginUserName(){
        return LoginUserName;
    }
    public List<Drugstore> getDrugstores(){
        return Drugstores;
    }
}