package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.opencsv.CSVReader;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import data.Druginfo;
import data.Drugstore;
import data.GlobalData;
import data.Inventory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class BFragment extends Fragment {
    EditText edtElabelNumber, edtDrugCode, edtDrugEnglish, edtNumBox, edtNumRow, edtNumPill, edtDrugStore, edtAreaNo, edtBlockNo, edtBlockType;
    TextView txtInventoryQty, txtInventoryNum, textNum,txtLotNumber;
    private Activity activity;
    Button btnSumit, btnLight, btnClear;
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
    String ElabelID, ElabelName, ElabelArticleID, ElabelDrugName, ElabelDrugCode2, ElabelDrugName2;
    boolean getFin;
    private String[] LotNumberArray;
    private String[] StockQtyArray;
    GlobalData globaldata;
    List<Drugstore> Drugstores;
    List<Inventory> Inventorys;
    List<Druginfo> Druginfos;

    String StoreID;
    String AreaNo;
    String BlockNo;
    String BlockType;
    String DrugCode;
    String MakerID;
    String ElabelNumber;
    String TemPt_Kind;
    String SafeStock;
    String TotalQty;
    String SetTime;
    String SetUserID;
    String InvQtyTime;
    String InvQtyUserID;
    String LotNumber;
    String EffectDate;
    String StockQty;
    String UpdateUserID;
    String UpdateTime;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_b, container, false);
        globaldata = (GlobalData) getActivity().getApplicationContext();
        edtElabelNumber = view.findViewById(R.id.edtElabelNumber);
//        btnGetDrugStore = view.findViewById(R.id.btnGetDrugStore);
        //btnGetDrugStore.setOnClickListener(onGetDrugStore);

        edtDrugStore = view.findViewById(R.id.edtDrugStore);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugEnglish = view.findViewById(R.id.edtDrugEnglish);

        btnSumit = view.findViewById(R.id.btnSumit);
//        btnSumit.setOnClickListener(onSave);
        btnLight = view.findViewById(R.id.btnLight);
        btnLight.setOnClickListener(OnLight);
        edtElabelNumber.requestFocus();

        textNum = view.findViewById(R.id.textNum);


        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);

        txtLotNumber = view.findViewById(R.id.txtLotNumber);


//        edtNumBox = view.findViewById(R.id.edtInQty);
//        edtNumRow = view.findViewById(R.id.edtNumRow);
        edtNumPill = view.findViewById(R.id.edtNumPill);
        txtInventoryQty = view.findViewById(R.id.txtInventoryQty);

        btnClear = view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(onClear);
//        txtInventoryNum = view.findViewById(R.id.txtInventoryNum);

//        edtNumBox.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {}
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                Log.d("TextChanged","TextChanged");
//                if(s.length() != 0) {
//                    try{
//                        Integer NumBox = Integer.valueOf(edtNumBox.getText().toString())*4*4;
//                        Integer NumRow = Integer.valueOf(edtNumRow.getText().toString())*4;
//                        Integer NumPill = Integer.valueOf(edtNumPill.getText().toString());
//                        Integer Sum = NumBox+NumRow+NumPill;
//                        txtInventoryQty.setText(Sum.toString());
//                    }
//                    catch (NumberFormatException ex){
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });

//        Drugstores = new ArrayList<Drugstore>();
//        CSVReadDrugStore();
//
//        Inventorys = new ArrayList<Inventory>();
//        CSVReadInventory();
//
//        Druginfos = new ArrayList<Druginfo>();
//        CSVReadDrugInfo();

//        String InventoryNum = String.valueOf(Inventorys.size());
//        String DrugStore = String.valueOf(Drugstores.size());
//        String ShowNum = " " + InventoryNum + " / " + DrugStore;
        //txtInventoryNum.setText(ShowNum);

        labelAfterScanListener();

        return view;
    }

    private View.OnClickListener onClear = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            edtElabelNumber.setText("");
//            edtDrugStore.setText("");
//            edtAreaNo.setText("");
//            edtBlockType.setText("");
//            edtBlockNo.setText("");
//            edtDrugCode.setText("");
//            edtDrugEnglish.setText("");
//            textNum.setText("");


            edtElabelNumber.requestFocus();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void labelAfterScanListener() {

        edtElabelNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //目前不使用到
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //目前不使用到
            }

            @Override
            public void afterTextChanged(Editable e) {
                //String input = e.toString();
                if (e.length() == 12) {
                    lsDrugInfo();
                }
            }
        });
    }

    public void lsDrugInfo() {
        String ed = edtElabelNumber.getText().toString();

        String url = "http://192.168.5.41/pda_submit.php?";
        try {
            url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
//            url += "DBoption=select";
            url += "DBoption=";

            Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
            Log.d("TAG", "DBoption: select");

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {

                        ArrayList<String> storeIDs = new ArrayList<>();
                        ArrayList<String> elabelTypes = new ArrayList<>();
                        ArrayList<String> drugCodes = new ArrayList<>();
                        ArrayList<String> drugNames = new ArrayList<>();
                        ArrayList<String> drugEnglishs = new ArrayList<>();
                        ArrayList<String> areaNos = new ArrayList<>();
                        ArrayList<String> blockNos = new ArrayList<>();
                        ArrayList<String> stockQtys = new ArrayList<>();
                        ArrayList<String> lotNumbers = new ArrayList<>();
                        ArrayList<String> makerIDs = new ArrayList<>();
                        ArrayList<String> makerNames = new ArrayList<>();
                        ArrayList<String> effectDates = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {

                            JSONArray arr = response.getJSONArray(i);
                            storeIDs.add(arr.getString(0));
                            elabelTypes.add(arr.getString(1));
                            drugCodes.add(arr.getString(2));
                            drugNames.add(arr.getString(3));
                            drugEnglishs.add(arr.getString(4));
                            areaNos.add(arr.getString(5));
                            blockNos.add(arr.getString(6));
                            stockQtys.add(arr.getString(7));
                            lotNumbers.add(arr.getString(8));
                            makerIDs.add(arr.getString(9));
                            makerNames.add(arr.getString(10));
                            effectDates.add(arr.getString(11));

//                            String storeID = arr.getString(0);
//                            String elabelType = arr.getString(1);
//                            String drugCode = arr.getString(2);
//                            String drugName = arr.getString(3);
//                            String drugEnglish = arr.getString(4);
//                            String areaNo = arr.getString(5);
//                            String blockNo = arr.getString(6);
//                            String stockQty = arr.getString(7);
//                            String lotNumber = arr.getString(8);
//                            String makerID = arr.getString(9);
//                            String makerName = arr.getString(10);
//                            String effectDate = arr.getString(11);
                            /**擷取取得的陣列*/
                        }


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                int sumQty = 0;
                                for (int i = 0; i < stockQtys.size(); i++) {
                                    sumQty += Double.parseDouble(stockQtys.get(i));
                                }

                                textNum.setText(stockQtys.get(0));
                                edtDrugStore.setText(storeIDs.get(0)); //卡位StoreID
                                edtAreaNo.setText(areaNos.get(0));
                                edtBlockNo.setText(blockNos.get(0));
                                edtBlockType.setText(elabelTypes.get(0));
                                edtDrugCode.setText(drugCodes.get(0));
                                edtDrugEnglish.setText(drugEnglishs.get(0));


                                txtLotNumber.setText(lotNumbers.get(0));
                                txtInventoryQty.setText(String.valueOf(sumQty)); //該儲區、藥代碼、所有批號之庫存總量


                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // 处理编码异常
        }

        textNum.setText(ed);
    }

    public void sendGET(String Url, final VolleyCallback callback) {

        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(Url)
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                /** 如果傳送過程有錯誤*/
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                /** 取得回傳*/
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray;

                    try {
                        jsonArray = new JSONArray(responseData);
                        callback.onSuccess(jsonArray);
                    } catch (JSONException e) {
                        Log.e("TAG", "Invalid JSON response: " + responseData);
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public interface VolleyCallback {

        void onSuccess(JSONArray response);
        // 在這裡可以添加其他方法，如 onFailure 等
    }

    private View.OnClickListener OnLight = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard(view.getContext());
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            String labelCode = edtElabelNumber.getText().toString();
            String jsonString = "[\n{\n\"color\": \"CYAN\",\n\"duration\": \"1\",\n\"labelCode\": \"" + labelCode + "\"\n}\n]";
            RequestBody body = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder()
                    .url("http://192.168.5.130:9003/labels/contents/led")
                    .method("PUT", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "*/*")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        // Remember to run this on UI thread if you're planning to update UI
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(view.getContext(), "亮燈", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    };

//    private View.OnClickListener onSave = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            hideKeyboard(v.getContext());
//            try {
//                exportDataToCSV();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    private View.OnClickListener onGetElabelArticle = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String ElabelNumber = edtElabelNumber.getText().toString();
            String url = "http://192.168.219.100:9003/articles/label/" + ElabelNumber;
            System.out.println(url);
            /*
            getRequestWithHeaderAndBody(url, FragmentActivity.VolleyCallback(){
                @Override
                public void onSuccess() {
                    getFin=true;
                }
            });*/
            while (!getFin) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            edtDrugCode.setText(PageDrugCode);
            edtDrugEnglish.setText(PageDrugEnglish);

            // hideKeyboard();
        }
    };

    public void getRequestWithHeaderAndBody(String url, final FragmentActivity.VolleyCallback callback) {

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
                GetFin = true;
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

    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }
//    public void CSVReadDrugStore() {
//        try {
//            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//            Log.d("dir", dir.getAbsolutePath());
//            // String path =
//            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/drugstore.csv"));
//            String[] nextLine;
//
//            int i = 0;
//            String[] record = null;
//            while ((record = reader.readNext()) != null) {
//                Drugstore drugstore = new Drugstore();
//                drugstore.setStoreID(record[0]);
//                Log.d("drugstore", record[6]);
//                drugstore.setAreaNo(record[1]);
//                drugstore.setBlockNo(record[2]);
//                drugstore.setBlockType(record[3]);
//                drugstore.setDrugCode(record[4]);
//                drugstore.setMakerID(record[5]);
//                drugstore.setElabelNumber(record[6]);
//                drugstore.setTemPt_Kind(record[7]);
//                drugstore.setSafeStock(record[8]);
//                drugstore.setTotalQty(record[9]);
//                drugstore.setSetTime(record[10]);
//                drugstore.setSetUserID(record[11]);
//                drugstore.setInvQtyTime(record[12]);
//                drugstore.setInvQtyUserID(record[13]);
//                drugstore.setLotNumber(record[14]);
//                drugstore.setEffectDate(record[15]);
//                drugstore.setStockQty(record[16]);
//                drugstore.setUpdateUserID(record[17]);
//                drugstore.setUpdateTime(record[18]);
//                Drugstores.add(drugstore);
//
//            }
//            reader.close();
//        } catch (IOException e) {
//            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
//            e.printStackTrace();
//        }
//    }
//
//    public void CSVReadInventory() {
//        try {
//            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//            Log.d("dir", dir.getAbsolutePath());
//            // String path =
//            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/inventory.csv"));
//            String[] nextLine;
//
//            int i = 0;
//            String[] record = null;
//            while ((record = reader.readNext()) != null) {
//                Inventory inventory = new Inventory();
//                inventory.setInvDate(record[0]);
//                inventory.setDrugCode(record[1]);
//                inventory.setMakerID(record[2]);
//                inventory.setStoreID(record[3]);
//                inventory.setAreaNo(record[4]);
//                inventory.setBlockNo(record[5]);
//                inventory.setBlockType(record[6]);
//                inventory.setLotNumber(record[7]);
//                inventory.setStockQty(record[8]);
//                inventory.setInventoryQty(record[9]);
//                inventory.setAdjQty(record[10]);
//                inventory.setShiftNo(record[11]);
//                inventory.setInvTime(record[12]);
//                inventory.setUserID(record[13]);
//                inventory.setRemark(record[14]);
//
//                Inventorys.add(inventory);
//            }
//            reader.close();
//        } catch (IOException e) {
//            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
//            e.printStackTrace();
//        }
//    }
//
//    public void CSVReadDrugInfo() {
//        try {
//            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//            Log.d("dir", dir.getAbsolutePath());
//            // String path =
//            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/druginfo.csv"));
//            String[] nextLine;
//
//            int i = 0;
//            String[] record = null;
//            while ((record = reader.readNext()) != null) {
//                Druginfo druginfo = new Druginfo();
//                druginfo.setMakerID(record[0]);
//                druginfo.setDrugCode(record[1]);
//                druginfo.setDrugEnglish(record[2]);
//                druginfo.setDrugName(record[3]);
//                druginfo.setDrugLabel(record[10]);
//                Druginfos.add(druginfo);
//            }
//            reader.close();
//        } catch (IOException e) {
//            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
//            e.printStackTrace();
//        }
//    }

//    public static String toCSV(String[] array) {
//        String result = "";
//        if (array.length > 0) {
//            StringBuilder sb = new StringBuilder();
//            for (String s : array) {
//                sb.append(s.trim()).append(",");
//            }
//            result = sb.deleteCharAt(sb.length() - 1).toString();
//        }
//        return result;
//    }
//
//    private void exportDataToCSV() throws IOException {
//        String csvData = "";
//        Integer count = 0;
//
//        for (int i = 0; i < Inventorys.size(); i++) {
//            if ((StoreID == Inventorys.get(i).getStoreID()) && (AreaNo == Inventorys.get(i).getAreaNo()) && (BlockNo == Inventorys.get(i).getBlockNo())
//                    && (BlockType == Inventorys.get(i).getBlockType())) {
//                count += 1;
//                Log.d("count", count.toString());
//                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
//                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
//                Calendar c = Calendar.getInstance();
//                String date = sdfDate.format(c.getTime());
//                String time = sdfTime.format(c.getTime());
//
//                Integer IntStockQty = Integer.valueOf(StockQty);
//                Integer InventoryQty = Integer.valueOf(txtInventoryQty.getText().toString());
//                Integer AdjQty = InventoryQty - IntStockQty;
//                Inventorys.get(i).setInventoryQty(InventoryQty.toString());
//                Inventorys.get(i).setAdjQty(AdjQty.toString());
//                Inventorys.get(i).setInvDate(date);
//                Inventorys.get(i).setInvTime(time);
//                Inventorys.get(i).setUserID(globaldata.getLoginUserID());
//            }
//        }
//        if (count == 0) {
//            Log.d("save", globaldata.getLoginUserID());
//            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
//            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
//            Calendar c = Calendar.getInstance();
//            String date = sdfDate.format(c.getTime());
//            String time = sdfTime.format(c.getTime());
//
//            Integer IntStockQty = Integer.valueOf(StockQty);
//            Integer InventoryQty = Integer.valueOf(txtInventoryQty.getText().toString());
//            Integer AdjQty = InventoryQty - IntStockQty;
//
//            Inventory inventory = new Inventory();
//            inventory.setInvDate(date);
//            inventory.setDrugCode(edtDrugCode.getText().toString());
//            inventory.setMakerID(MakerID);
//            inventory.setStoreID(StoreID);
//            inventory.setAreaNo(AreaNo);
//            inventory.setBlockNo(BlockNo);
//            inventory.setLotNumber(LotNumber);
//            inventory.setStockQty(IntStockQty.toString());
//            inventory.setInventoryQty(InventoryQty.toString());
//            inventory.setAdjQty(AdjQty.toString());
//            inventory.setShiftNo("1");
//            inventory.setInvTime(time);
//            inventory.setUserID(globaldata.getLoginUserID());
//            inventory.setRemark("APP");
//            inventory.setBlockType(BlockType);
//            Inventorys.add(inventory);
//        }
//        for (int i = 0; i < Inventorys.size(); i++) {
//            String currentLIne = Inventorys.get(i).getRowString();
//            String[] cells = currentLIne.split(",");
//            csvData += toCSV(cells) + "\n";
//        }
//        File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        String uniqueFileName = "/inventory.csv";
//        File file = new File(directory.getAbsolutePath(), uniqueFileName);
//        FileWriter fileWriter = new FileWriter(file);
//        fileWriter.write(csvData);
//        fileWriter.flush();
//        fileWriter.close();
//        Toast.makeText(getActivity(), "File Exported Successfully", Toast.LENGTH_SHORT).show();
//    }

        //    private View.OnClickListener onGetDrugStore = new View.OnClickListener() {

//        @Override
//        public void onClick(View v) {
//            hideKeyboard(v.getContext());
//            String ElabelNumber = edtElabelNumber.getText().toString();
//            if (Drugstores.stream().count() == 0) {
//                //Log.d("Drugstores", "Drugstores 為 0");
//            }
//            Log.d("ElabelNumber", ElabelNumber);
//            if (Drugstores.stream().filter(drugstore -> (drugstore.getElabelNumber().equals(ElabelNumber))).count() == 0) {
//                //無此條碼資料
//                Log.d("error", "無結果");
//            } else {
//                List<Drugstore> MatchDrugstore = Drugstores.stream().filter(drugstore -> (drugstore.getElabelNumber().equals(ElabelNumber))).collect(Collectors.toList());
//                if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).count() > 0) {
//                    List<Druginfo> MatchDruginfo = Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).collect(Collectors.toList());
//                    edtDrugEnglish.setText(MatchDruginfo.get(0).getDrugEnglish());
//                }
//                edtDrugCode.setText(MatchDrugstore.get(0).getDrugCode());
//                edtDrugStore.setText(MatchDrugstore.get(0).getStoreID() + "-" + MatchDrugstore.get(0).getAreaNo() + "-" + MatchDrugstore.get(0).getBlockNo() + "-" + MatchDrugstore.get(0).getBlockType());
//                StoreID = MatchDrugstore.get(0).getStoreID();
//                AreaNo = MatchDrugstore.get(0).getAreaNo();
//                BlockNo = MatchDrugstore.get(0).getBlockNo();
//                BlockType = MatchDrugstore.get(0).getBlockType();
//                DrugCode = MatchDrugstore.get(0).getDrugCode();
//                MakerID = MatchDrugstore.get(0).getMakerID();
//                TemPt_Kind = MatchDrugstore.get(0).getTemPt_Kind();
//                SafeStock = MatchDrugstore.get(0).getSafeStock();
//                TotalQty = MatchDrugstore.get(0).getTotalQty();
//                SetTime = MatchDrugstore.get(0).getSetTime();
//                SetUserID = MatchDrugstore.get(0).getSetUserID();
//                InvQtyTime = MatchDrugstore.get(0).getInvQtyTime();
//                InvQtyUserID = MatchDrugstore.get(0).getInvQtyUserID();
//                LotNumber = MatchDrugstore.get(0).getLotNumber();
//                EffectDate = MatchDrugstore.get(0).getEffectDate();
//                StockQty = MatchDrugstore.get(0).getStockQty();
//                UpdateUserID = MatchDrugstore.get(0).getUpdateUserID();
//                UpdateTime = MatchDrugstore.get(0).getUpdateTime();
//            }
//        }
//    };
    }
