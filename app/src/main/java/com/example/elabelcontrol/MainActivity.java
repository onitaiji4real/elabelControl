package com.example.elabelcontrol;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity<textView> extends AppCompatActivity {
    EditText edtElabelNumber,edtDrugCode,edtDrugEnglish,edtNumBox,edtNumRow,edtNumPill;
    Button btnSumit,btnLight,btnGetStockQty;
    Boolean GetFin;
    Spinner SpPage;
    GridView gridLotnumber;
    CustomGrid adapter;
    String PageDrugCode;
    String PageDrugEnglish;
    String PageLotnumber1;
    String PageStockQty1;
    String PageLotnumber2;
    String PageStockQty2;
    String PageEffectDate1;
    String PageEffectDate2;
    String ElabelID,ElabelName,ElabelArticleID,ElabelDrugName,ElabelDrugCode2,ElabelDrugName2;

    boolean getFin;

    private String[] LotNumberArray ;
    private String[] StockQtyArray ;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface VolleyCallback{
        void onSuccess();
    }


    @SuppressLint("MissingInflatedId")
    @Override
    //String[] LotNumberArray = [];
    //String[] StockQtyArray;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtElabelNumber = findViewById(R.id.edtElabelNumber);
        btnGetStockQty = findViewById(R.id.btnGetDrugStore);
        btnGetStockQty.setOnClickListener(onGetStockQty);

        edtDrugCode = findViewById(R.id.edtDrugCode);
        edtDrugEnglish = findViewById(R.id.edtDrugEnglish);

        btnSumit = findViewById(R.id.btnSubmit);
        btnSumit.setOnClickListener(onPOST);
        btnLight = findViewById(R.id.btnLight);
        btnLight.setOnClickListener(OnLight);
        edtElabelNumber.requestFocus();

        edtNumBox = findViewById(R.id.edtInQty);
        edtNumRow = findViewById(R.id.edtNumRow);
        edtNumPill = findViewById(R.id.edtNumPill);
    }
    public void hideKeyboard(){
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
    private View.OnClickListener OnLight = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String ElabelNumber = edtElabelNumber.getText().toString();
            String LightJsonStr = String.format("[\n" +
                    "  {\n" +
                    "    \"color\": \"RED\",\n" +
                    "    \"duration\": \"30s\",\n" +
                    "    \"labelCode\": \"%s\"\n" +
                    "  }\n" +
                    "]",ElabelNumber);
            GetFin=false;
            putRequestWithHeaderAndBody("http://192.168.219.100:9003/labels/contents/led",LightJsonStr);
            while(!GetFin)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private View.OnClickListener onGetStockQty = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String ElabelNumber = edtElabelNumber.getText().toString();
            String url = "http://192.168.219.100:9003/articles/label/"+ElabelNumber;
            System.out.println(url);
            getRequestWithHeaderAndBody(url,new VolleyCallback(){
                @Override
                public void onSuccess() {
                    getFin=true;
                }
            });
            while(!getFin)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            edtDrugCode.setText(PageDrugCode);
            edtDrugEnglish.setText(PageDrugEnglish);



           // adapter = new CustomGrid(MainActivity.this, LotNumberArray, StockQtyArray);

         //   gridLotnumber.setAdapter(adapter);
            /*
            gridLotnumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //[+position] +的功用是?
                    Toast.makeText(MainActivity.this, "你選取了" + StockQtyArray[+position], Toast.LENGTH_SHORT).show();
                }
            });

*/
            hideKeyboard();
            /*
            String JsonResult = "[\n" +
                    "  {\n" +
                    "    \"stationCode\": \"medicalBG\",\n" +
                    "    \"id\": \"B-N-34\",\n" +
                    "    \"name\": \"03DFA362B698\",\n" +
                    "    \"nfc\": \"\",\n" +
                    "    \"originPrice\": null,\n" +
                    "    \"salePrice\": null,\n" +
                    "    \"discountPercent\": null,\n" +
                    "    \"data\": {\n" +
                    "      \"STORE_CODE\": \"medicalBG\",\n" +
                    "      \"ARTICLE_ID\": \"N-34\",\n" +
                    "      \"ITEM_NAME\": \"03DFA362B698\",\n" +
                    "      \"DrugCode\": \"IVARI\",\n" +
                    "      \"DrugName\": \"(自費) 水痘疫苗  \",\n" +
                    "      \"DrugEnglish\": \"(自費) VARIVAX\",\n" +
                    "      \"StoreQty\": \"0\",\n" +
                    "      \"DrugCode2\": \"MSD\",\n" +
                    "      \"DrugName2\": \"●須附稀釋液●\",\n" +
                    "      \"DrugEnglish2\": \"\",\n" +
                    "      \"DrugCode3\": \"W000329\",\n" +
                    "      \"DrugName3\": \"2024-01-05\",\n" +
                    "      \"DrugEnglish3\": \"1\",\n" +
                    "      \"DrugCode4\": \"W004421\",\n" +
                    "      \"DrugName4\": \"2024-02-08\",\n" +
                    "      \"DrugEnglish4\": \"10\",\n" +
                    "      \"DrugCode5\": \"\",\n" +
                    "      \"DrugName5\": \"\",\n" +
                    "      \"DrugEnglish5\": \"\",\n" +
                    "      \"DrugCode6\": \"\",\n" +
                    "      \"DrugName6\": \"\",\n" +
                    "      \"DrugEnglish6\": \"\",\n" +
                    "      \"DrugCode7\": \"\",\n" +
                    "      \"DrugName7\": \"\",\n" +
                    "      \"DrugEnglish7\": \"\",\n" +
                    "      \"DrugCode8\": \"\",\n" +
                    "      \"DrugName8\": \"\",\n" +
                    "      \"DrugEnglish8\": \"\",\n" +
                    "      \"DrugFormula\": \"\",\n" +
                    "      \"NFC_URL\": \"2022-10-07 14:40\"\n" +
                    "    },\n" +
                    "    \"createdDate\": \"2022-10-07T14:54:07.486+0800\",\n" +
                    "    \"modifiedDate\": \"2022-10-07T15:00:28.319+0800\",\n" +
                    "    \"reservedOne\": null,\n" +
                    "    \"reservedTwo\": null,\n" +
                    "    \"reservedThree\": null\n" +
                    "  }\n" +
                    "]";
            ReadElabelData(JsonResult);
            */

        }
    };

    private View.OnClickListener onPOST = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String price = edtDrugCode.getText().toString();
            String StockQty = edtDrugEnglish.getText().toString();
            try{
                int SumStockQty = Integer.parseInt(edtNumPill.getText().toString())+Integer.parseInt(edtNumRow.getText().toString())*5+Integer.parseInt(edtNumBox.getText().toString())*25;
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = dateFormat.format(currentTime);
                System.out.println(date);

                String jsonStr = String.format("{ \"dataList\":[ { \"stationCode\": \"medicalBG\",\"id\": \"%s\", \"name\": \"%s\", \"nfc\": \"\", \"originPrice\": null, \"salePrice\": null, \"discountPercent\": \"0.0\", \"data\": {   \"STORE_CODE\": \"medicalBG\",   \"ARTICLE_ID\": \"%s\",   \"ITEM_NAME\": \"%s\",   \"DrugCode\": \"%s\",   \"DrugName\": \"%s\",   \"DrugEnglish\": \"%s\",   \"DrugCode2\": \"\",   \"DrugName2\": \"\",   \"DrugEnglish2\": \"\",   \"DrugCode3\": \"\",   \"DrugName3\":\"\",   \"DrugEnglish3\":\"\",   \"DrugCode4\": \"\",   \"DrugName4\": \"\",   \"DrugEnglish4\": \"\",   \"DrugCode5\": \"\",   \"DrugName5\": \"\",   \"DrugEnglish5\": \"\",   \"DrugCode6\": \"\",   \"DrugName6\": \"\",   \"DrugEnglish6\": \"\",   \"DrugCode7\": \"\",   \"DrugName7\": \"\",   \"DrugEnglish7\": \"\",   \"DrugCode8\": \"\",   \"DrugName8\": \"\",   \"DrugEnglish8\": \"\",   \"DrugFormula\": \"\",\"StoreQty\": \"%s\",   \"NFC_URL\": \"%s\" }, \"createdDate\": \"2022-01-26T11:07:23.200+0800\", \"modifiedDate\": \"\", \"reservedOne\": null, \"reservedTwo\": null, \"reservedThree\": null } ] } ",ElabelID,ElabelName,ElabelArticleID,ElabelName, PageDrugCode,ElabelDrugName,PageDrugEnglish,SumStockQty,date);

                System.out.println(jsonStr);
                GetFin=false;
                sendPOST("http://192.168.219.100:9003/articles",jsonStr);
                while(!GetFin)
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (NumberFormatException ex){
                ex.printStackTrace();
            }


            /*
            Thread mThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String ChPageJsonstr = String.format("{\n" +
                            "  \"labels\": [\n" +
                            "    {\n" +
                            "      \"displayPage\": %s,\n" +
                            "      \"labelCode\": \"%s\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}",SpPage.getSelectedItem().toString(),ElabelNumber);
                    sendPOST("http://192.168.5.42:9003/labels/contents/page",ChPageJsonstr);
                }
            };
            mThread.start();*/
            hideKeyboard();


        }
    };

    private void sendPOST(String url,String json) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/


        RequestBody body = RequestBody.create(json, JSON);

        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                GetFin=true;


            }
        });
    }

    public void ReadElabelData(String ResponseString){
        try{
            //建立一個JSONArray並帶入JSON格式文字，getString(String key)取出欄位的數值

            JSONArray array = new JSONArray(ResponseString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                ElabelID = jsonObject.getString("id");
                ElabelName = jsonObject.getString("name");
                JSONObject data = jsonObject.getJSONObject("data");
                String DrugCode = data.getString("DrugCode");
                String DrugEnglish = data.getString("DrugEnglish");
                ElabelArticleID = data.getString("ARTICLE_ID");
                ElabelDrugName = data.getString("DrugName");
                ElabelDrugCode2 = data.getString("DrugCode2");
                ElabelDrugName2 = data.getString("DrugName2");
                PageDrugCode = DrugCode;
                PageDrugEnglish = DrugEnglish;
                //edtDrugCode.setText(DrugCode);
                //edtDrugEnglish.setText(DrugEnglish);
                PageLotnumber1 = data.getString("DrugCode3");
                PageLotnumber2 = data.getString("DrugCode4");
                PageEffectDate1 = data.getString("DrugName3");
                PageEffectDate2 = data.getString("DrugName4");
                PageStockQty1 = data.getString("DrugEnglish3");
                PageStockQty2 = data.getString("DrugEnglish4");
                //讀取DrugCode3~DrugCode8的批號與庫存
                for(int j = 3; j<=8;j++){

                    LotNumberArray = new String[]{
                            data.getString("DrugCode3"),
                            data.getString("DrugCode4"),
                            data.getString("DrugCode5"),
                            data.getString("DrugCode6"),
                            data.getString("DrugCode7"),
                            data.getString("DrugCode8"),
                    };
                    StockQtyArray = new String[]{
                            data.getString("DrugEnglish3"),
                            data.getString("DrugEnglish4"),
                            data.getString("DrugEnglish5"),
                            data.getString("DrugEnglish6"),
                            data.getString("DrugEnglish7"),
                            data.getString("DrugEnglish8"),
                    };
                }
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
            setToast(e.toString());
        }

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
                GetFin=true;
            }
        });
    }

    public void getRequestWithHeaderAndBody(String url,final VolleyCallback callback) {


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
                GetFin=true;
                callback.onSuccess();
                if(response.isSuccessful()){
                    System.out.println("有結果");

                    ReadElabelData(response.body().string());
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

    private void setListener() {
        gridLotnumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick
                    (AdapterView<?> adapterView, View view, int position, long id) {
                String msg = adapterView.getItemAtPosition(position).toString();
                setToast(msg);
            }
        });
    }

    private void setToast (String text){
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private void setAdapter(){
        ArrayAdapter<String> adapter=
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,LotNumberArray);
        gridLotnumber.setAdapter(adapter);

    }
}