package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
    EditText edtElabelNumber, edtDrugCode, edtDrugEnglish, edtNumBox, edtNumRow, edtNumPill, edtDrugStore, edtAreaNo, edtBlockNo, edtBlockType, edtEffectDate, edtMakeDate;
    TextView txtInventoryQty, txtInventoryNum, textNum, txtLotNumber, textLotNumber_size;
    private Activity activity;
    Button btnSumit, btnLight, btnClear, btnPreview, btnNextIndex, btnStatus;
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
    private int currentIndex = 0;
    private ArrayList<String> storeIDs;
    private ArrayList<String> elabelTypes;
    private ArrayList<String> drugCodes;
    private ArrayList<String> drugNames;
    private ArrayList<String> drugEnglishs;
    private ArrayList<String> areaNos;
    private ArrayList<String> blockNos;
    private ArrayList<String> stockQtys;
    private ArrayList<String> lotNumbers;
    private ArrayList<String> makerIDs;
    private ArrayList<String> makerNames;
    private ArrayList<String> effectDates;
    private ArrayList<String> makeDates;

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

        btnSumit = view.findViewById(R.id.btnSubmit);
        btnSumit.setOnClickListener(onSubmit);
        btnLight = view.findViewById(R.id.btnLight);
        btnLight.setOnClickListener(OnLight);
        edtElabelNumber.requestFocus();

        textNum = view.findViewById(R.id.textNum);


        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);

        txtLotNumber = view.findViewById(R.id.txtLot);


//        edtNumBox = view.findViewById(R.id.edtInQty);
//        edtNumRow = view.findViewById(R.id.edtNumRow);
        edtNumPill = view.findViewById(R.id.edtNumPill);
        edtNumBox = view.findViewById(R.id.edtNumBox);
        edtNumRow = view.findViewById(R.id.edtNumRow);

        txtInventoryQty = view.findViewById(R.id.txtInventoryQty);

        btnNextIndex = view.findViewById(R.id.btnNextIndex);
        btnNextIndex.setOnClickListener(nextIndex);

        btnPreview = view.findViewById(R.id.btnPreViewIndex);
        btnPreview.setOnClickListener(previewIndex);

        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(onClearField);

        textLotNumber_size = view.findViewById(R.id.textLotNumber_size);
        edtEffectDate = view.findViewById(R.id.edtEffectDate);
        edtMakeDate = view.findViewById(R.id.edtMakeDate);


        labelAfterScanListener();

        return view;
    }


    private View.OnClickListener onClearField = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edtElabelNumber.setText("");
            edtElabelNumber.requestFocus();
            edtDrugStore.setText("");
            edtAreaNo.setText("");
            edtBlockNo.setText("");
            edtBlockType.setText("");
            edtDrugEnglish.setText("");
            txtLotNumber.setText("");
            edtEffectDate.setText("");
            edtMakeDate.setText("");
            textNum.setText("");
            textLotNumber_size.setText("0/0");
            edtNumPill.setText("");
            //edtNumRow.setText("");
            //edtNumBox.setText("");
            txtInventoryQty.setText("0");
            edtDrugCode.setText("");

        }
    };

    private View.OnClickListener onSubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());

            String labelCode = edtElabelNumber.getText().toString();

            String sElabelNumber = edtElabelNumber.getText().toString();
            String sDrugStore = edtDrugStore.getText().toString();
            String sDrugCode = edtDrugCode.getText().toString();

            String sAreaNo = edtAreaNo.getText().toString();
            String sBlockNo = edtBlockNo.getText().toString();
            String sBlockType = edtBlockType.getText().toString();

            String sDrugEnglish = edtDrugEnglish.getText().toString();
            String sLotNumber = txtLotNumber.getText().toString();
            String sedtNumPill = edtNumPill.getText().toString();
            String numRow = edtNumRow.getText().toString();
            String numBox = edtNumBox.getText().toString();

            String DBoption = "INVENTORY";
            String url = globaldata.getPHP_SERVER();

            try {
//                url += URLEncoder.encode(DBoption,"UTF-8") + "&";

                //InvDate從資料庫代入當前時間
                url += "DBoption=" + URLEncoder.encode(DBoption,"UTF-8")+"&";
                url += "ElabelNumber=" + URLEncoder.encode(sElabelNumber,"UTF-8") + "&";
                url += "DrugCode=" + URLEncoder.encode(sDrugCode, "UTF-8") + "&";
                url += "StoreID=" + URLEncoder.encode(sDrugStore, "UTF-8") + "&";
                url += "AreaNo=" + URLEncoder.encode(sAreaNo, "UTF-8") + "&";
                url += "BlockNo=" + URLEncoder.encode(sBlockNo, "UTF-8") + "&";
                url += "LotNumber=" + URLEncoder.encode(sLotNumber, "UTF-8") + "&";
                //StockQty 從PHP取得
                url += "InventoryQty=" + URLEncoder.encode(sedtNumPill, "UTF-8") + "&";
                //AdjQty 數量從PHP計算
                //Shift從PHP固定填1
                //InvTime從資料庫代入
                url += "UserId=" + URLEncoder.encode(globaldata.getLoginUserID(), "UTF-8") + "&";
                url += "User=" + URLEncoder.encode(globaldata.getLoginUserName(), "UTF-8")+"&";

                url += "numRow=" + URLEncoder.encode(numRow,"UTF-8")+"&";
                url += "numBox=" + URLEncoder.encode(numBox,"UTF-8")+"&";

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    Toast.makeText(v.getContext(), "盤點成功！", Toast.LENGTH_SHORT).show();
                }
            });


            OnLight(v, labelCode,"盤點成功！"); //亮燈
        }
    };

    public void OnLight(View view, String labelCode,String txt) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        String jsonString = "[\n{\n\"color\": \"CYAN\",\n\"duration\": \"1\",\n\"labelCode\": \"" + labelCode + "\"\n}\n]";
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder()
                .url(globaldata.getAIMS_BLINK_URL())
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
                            Toast.makeText(view.getContext(), txt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private View.OnClickListener previewIndex = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lotNumbers != null && currentIndex > 0) {
                currentIndex--;
                updateUIWithCurrentIndex();
            }
            edtNumPill.setText("");
            edtElabelNumber.requestFocus();
        }
    };

    private View.OnClickListener nextIndex = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lotNumbers != null && currentIndex < lotNumbers.size() - 1) {
                currentIndex++;
                updateUIWithCurrentIndex();
            }
            edtElabelNumber.requestFocus();
            edtNumPill.setText("");
        }
    };

    private View.OnClickListener OnLight = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String labelCode = edtElabelNumber.getText().toString();
            OnLight(view, labelCode,"亮燈成功！"); //亮燈
        }
    };

    private void updateUIWithCurrentIndex() {
        int Lot_length = stockQtys.size();
        String Lot_number = (currentIndex + 1) + "/" + Lot_length;
        textLotNumber_size.setText(Lot_number);
        int sumQty = 0;
        for (int i = 0; i < stockQtys.size(); i++) {
            sumQty += Double.parseDouble(stockQtys.get(i));
        }

        edtDrugStore.setText(storeIDs.get(currentIndex)); //卡位StoreID
        edtAreaNo.setText(areaNos.get(currentIndex));
        edtBlockNo.setText(blockNos.get(currentIndex));
        edtBlockType.setText(elabelTypes.get(currentIndex));
        edtDrugCode.setText(drugCodes.get(currentIndex));
        edtDrugEnglish.setText(drugEnglishs.get(currentIndex));
        txtLotNumber.setText(lotNumbers.get(currentIndex));
        textNum.setText(stockQtys.get(currentIndex));
        edtEffectDate.setText(effectDates.get(currentIndex));
        edtMakeDate.setText(makeDates.get(currentIndex));

        txtInventoryQty.setText(String.valueOf(sumQty)); //該儲區、藥代碼、所有批號之庫存總量
    }

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

        String url = globaldata.getPHP_SERVER();
        try {
            url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
//            url += "DBoption=select";
            url += "DBoption=GET";

            Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
            Log.d("TAG", "DBoption: select");

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {

                        storeIDs = new ArrayList<>();
                        elabelTypes = new ArrayList<>();
                        drugCodes = new ArrayList<>();
                        drugNames = new ArrayList<>();
                        drugEnglishs = new ArrayList<>();
                        areaNos = new ArrayList<>();
                        blockNos = new ArrayList<>();
                        stockQtys = new ArrayList<>();
                        lotNumbers = new ArrayList<>();
                        makerIDs = new ArrayList<>();
                        makerNames = new ArrayList<>();
                        effectDates = new ArrayList<>();
                        makeDates = new ArrayList<>();

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
                            makeDates.add(arr.getString(12));

                            /**擷取取得的陣列*/

                        }


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                updateUIWithCurrentIndex();

//                                int sumQty = 0;
//                                for (int i = 0; i < stockQtys.size(); i++) {
//                                    sumQty += Double.parseDouble(stockQtys.get(i));
//                                }

//                                textNum.setText(stockQtys.get(0));
//                                edtDrugStore.setText(storeIDs.get(0)); //卡位StoreID
//                                edtAreaNo.setText(areaNos.get(0));
//                                edtBlockNo.setText(blockNos.get(0));
//                                edtBlockType.setText(elabelTypes.get(0));
//                                edtDrugCode.setText(drugCodes.get(0));
//                                edtDrugEnglish.setText(drugEnglishs.get(0));
//
//
//                                txtLotNumber.setText(lotNumbers.get(0));
//                                txtInventoryQty.setText(String.valueOf(sumQty)); //該儲區、藥代碼、所有批號之庫存總量


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


    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }
//
}
