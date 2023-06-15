package com.example.elabelcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.opencsv.CSVReader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.Drugstore;
import data.GlobalData;
import data.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentActivity extends AppCompatActivity {
    private AFragment aFragment;
    private BFragment bFragment;
    private CFragment cFragment;
    private DFragment dFragment;
    private EFragment eFragment;
    Button btnLogout, btnDownloadData;
    GlobalData globaldata;
    List<Drugstore> Drugstores;
    String download_url = "http://192.168.5.49/download_data.php?";
    boolean getFin;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface VolleyCallback {
        void onSuccess();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        globaldata = (GlobalData) getApplicationContext();

        getSupportActionBar().hide(); //隱藏標題列

        Drugstores = new ArrayList<>();

        aFragment = new AFragment();
        //透過getSupportFragmentManager()，獲取FragmentManager並使用beginTransaction()開啟一個事務。最後將Fragmnet加入容器內的方法，可以使用add。
        getSupportFragmentManager().beginTransaction().add(R.id.fl_container, aFragment, "A").commit();

        TabLayout guiTabs;
        guiTabs = findViewById(R.id.tabLayout);

        guiTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getSupportFragmentManager().executePendingTransactions(); // 確保前一個交易已完成
                switch (tab.getPosition()) {
                    case 0:
                        if (aFragment == null)
                            aFragment = new AFragment();
                        //這裡要做到替換的效果，所以我們用replace做到取代。
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_container, aFragment, "A").commitAllowingStateLoss();
                        break;
                    case 1:
                        if (bFragment == null)
                            bFragment = new BFragment();
                        CSVReadDrugStore();
                        //這裡要做到替換的效果，所以我們用replace做到取代。
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_container, bFragment, "B").commitAllowingStateLoss();

                        break;
                    case 2:
                        if (cFragment == null)
                            cFragment = new CFragment();
                        //這裡要做到替換的效果，所以我們用replace做到取代。
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_container, cFragment, "C").commitAllowingStateLoss();
                        break;

                    case 3:
                        if (dFragment == null)
                            dFragment = new DFragment();
                        //這裡要做到替換的效果，所以我們用replace做到取代。
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_container, dFragment, "D").commitAllowingStateLoss();
                        break;

                    case 4:
                        if (eFragment == null)
                            eFragment = new EFragment();
                        //這裡要做到替換的效果，所以我們用replace做到取代。
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fl_container, eFragment, "E").commitAllowingStateLoss();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        guiTabs.newTab().select();
        btnLogout = findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(onLogout);

        // btnDownloadData = (Button)findViewById(R.id.btnDownloadData);
        // btnDownloadData.setOnClickListener(onDownload);

    }

    private View.OnClickListener onLogout = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            globaldata.setLoginUserID("");
            globaldata.setLoginUserName("");
            startActivity(new Intent(view.getContext(), Login.class));

        }
    };

    public void CSVReadDrugStore() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/drugstore.csv"));
            String[] nextLine;

            int i = 0;
            String[] record = null;
            while ((record = reader.readNext()) != null) {
                Drugstore drugstore = new Drugstore();
                drugstore.setStoreID(record[0]);
                drugstore.setAreaNo(record[1]);
                drugstore.setBlockNo(record[2]);
                drugstore.setBlockType(record[3]);
                drugstore.setDrugCode(record[4]);
                drugstore.setMakerID(record[5]);
                drugstore.setElabelNumber(record[6]);
                drugstore.setTemPt_Kind(record[7]);
                drugstore.setSafeStock(record[8]);
                drugstore.setTotalQty(record[9]);
                drugstore.setSetTime(record[10]);
                drugstore.setSetUserID(record[11]);
                drugstore.setInvQtyTime(record[12]);
                drugstore.setInvQtyUserID(record[13]);
                drugstore.setLotNumber(record[14]);
                drugstore.setEffectDate(record[15]);
                drugstore.setStockQty(record[16]);
                drugstore.setUpdateUserID(record[17]);
                drugstore.setUpdateTime(record[18]);
                Drugstores.add(drugstore);
                Log.d("drugstore", record[6]);
            }
            reader.close();
            bFragment.Drugstores = Drugstores;
        } catch (IOException e) {
            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
            e.printStackTrace();
        }
    }

    public void hideKeyboard() {
        View view = getWindow().getCurrentFocus();
        if (view == null) {
            View decorView = getWindow().getDecorView();
            View focusView = decorView.findViewWithTag("keyboardTagView");
            if (focusView == null) {
                view = new EditText(getWindow().getContext());
                view.setTag("keyboardTagView");
                ((ViewGroup) decorView).addView(view, 0, 0);
            } else {
                view = focusView;
            }
            view.requestFocus();
        }
        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public void putRequestWithHeaderAndBody(String url, String jsonstr) {


        RequestBody body = RequestBody.create(jsonstr, JSON);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .put(body) //PUT
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                // GetFin=true;
            }
        });
    }

    public void getRequestWithHeaderAndBody(String url, FragmentActivity.VolleyCallback callback) {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                System.out.println("發生錯誤");
                System.out.println(e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                // GetFin=true;
                callback.onSuccess();
                if (response.isSuccessful()) {
                    System.out.println("有結果");

                    // ReadElabelData(response.body().string());
                    //adapter.notifyDataSetChanged();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //return response from here to update any UI
                        }
                    });
                }
            }
        });
    }
}
