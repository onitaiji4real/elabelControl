package com.example.elabelcontrol;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import data.DrugInOut;
import data.Druginfo;
import data.Drugstore;
import data.GlobalData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class DFragment extends Fragment {
    private Spinner spinner;
//    private TextView ;
    String CodeID;
    GlobalData globaldata;
    Button btnLight,btnSumit,btnDrugOut,btnPreViewIndex,btnNextIndex,btnStatus;
    List<Drugstore> Drugstores;
    List<DrugInOut> DrugInOuts;
    List<Druginfo> Druginfos;
    boolean getFin;
    GlobalData globalData;
    EditText edtDrugStore,edtBlockNo,edtBlockType,edtAreaNo,edtDrugCode,edtDrugEnglish,edtInQty,edtElabelNumber;
    TextView textLotNumber_size,txtLotNumber,textNum,edtMakeDate,edtEffectDate;
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
    //private DateTimePicker dateTimePicker;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        globaldata = (GlobalData) getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.layout_d, container, false);
//        Drugstores = new ArrayList<Drugstore>();
//
//        CSVReadDrugStore();
//        DrugInOuts = new ArrayList<DrugInOut>();
//
//        Druginfos = new ArrayList<Druginfo>();
//        CSVReadDrugInfo();



        btnSumit = view.findViewById(R.id.btnSubmit);
        btnSumit.setOnClickListener(OnSubmit);
        spinner = view.findViewById(R.id.spOutCode);

        edtEffectDate = view.findViewById(R.id.edtEffectDate);
        edtMakeDate = view.findViewById(R.id.edtMakeDate);


        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);


        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugEnglish = view.findViewById(R.id.edtDrugEnglish);
        edtDrugStore = view.findViewById(R.id.edtDrugStore);
        edtInQty = view.findViewById(R.id.edtInQty);

        //btnGetDrugStore = view.findViewById(R.id.btnGetDrugStore);
        edtElabelNumber = view.findViewById(R.id.edtElabelNumber);
        edtElabelNumber.requestFocus();

        btnLight = view.findViewById(R.id.btnLight);
        btnLight.setOnClickListener(OnLight);

//      Spinner spinner = view.findViewById(R.id.spOutCode);
        spinner.setOnItemSelectedListener(spnOnItemSelected);

        CodeID = "A";
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(view.getContext(),
                        R.array.OutCode,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2, false);

        btnDrugOut = view.findViewById(R.id.btnDrugOut);
        btnDrugOut.setEnabled(false);
        //btnDrugOut.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        textLotNumber_size = view.findViewById(R.id.textLotNumber_size);
        txtLotNumber = view.findViewById(R.id.txtLot);

        btnNextIndex = view.findViewById(R.id.btnNextIndex);
        btnNextIndex.setOnClickListener(nextIndex);
        btnPreViewIndex = view.findViewById(R.id.btnPreViewIndex);
        btnPreViewIndex.setOnClickListener(previewIndex);

        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(onClearField);




        textNum = view.findViewById(R.id.textNum);



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
            //edtNumPill.setText("");
            //edtNumRow.setText("");
            //edtNumBox.setText("");
            //txtInventoryQty.setText("");
            edtDrugCode.setText("");
        }
    };

    private View.OnClickListener OnLight = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            hideKeyboard(view.getContext());
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            String labelCode = edtElabelNumber.getText().toString();
            String jsonString = "[\n{\n\"color\": \"RED\",\n\"duration\": \"1\",\n\"labelCode\": \"" + labelCode + "\"\n}\n]";
            RequestBody body = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder()
                    .url("http://192.168.5.137:9003/labels/contents/led")
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
                                Toast.makeText(view.getContext(),"toast", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    };

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
                String input = e.toString();

                if (e.length() == 12) {
                    lsDrugInfo();
                }
            }
        });
    }

    private void lsDrugInfo() {
        String ed = edtElabelNumber.getText().toString();

        String url = globaldata.getPHP_SERVER();
        try {
            url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
            url += "DBoption=GET" + "&";
            //url += "TotalQty=" + URLEncoder.encode(edtInQty.getText().toString(),"UTF-8") + "&";
            //url += "UserID=" + globaldata.getLoginUserID() + "&";
            //url += "spinnerText=" + URLEncoder.encode(spinner.getSelectedItem().toString());

            Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
            Log.d("TAG", "DBoption:GET");
            Log.d("TAG", "TotalQty" + edtInQty.getText().toString());
            Log.d("USERID", "UserID" + globaldata.getLoginUserID());
            Log.d("spinnerText", "Spinner" + spinner.getSelectedItem().toString());

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
//                        String areaNo = response.getString("AreaNo");
//                        String blockNo = response.getString("BlockNo");
//                        String blockType = response.getString("BlockType");
//                        String drugCode = response.getString("DrugCode");
//                        String stockQty = response.getString("StockQty");
//                        String storeID = response.getString("StoreID");
//                        String drugenglish = response.getString("DrugName");

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
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUIWithCurrentIndex();
                                edtInQty.requestFocus();

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }
        Toast.makeText(getActivity(), "掃描成功", Toast.LENGTH_SHORT).show();
    }
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

        //txtInventoryQty.setText(String.valueOf(sumQty)); //該儲區、藥代碼、所有批號之庫存總量
    }
    private View.OnClickListener previewIndex = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lotNumbers != null && currentIndex > 0) {
                currentIndex--;
                updateUIWithCurrentIndex();
            }
            edtInQty.setText("");
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
            edtInQty.setText("");
        }
    };

    private View.OnClickListener OnSubmit = new View.OnClickListener() {
        /**收入*/

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String labelCode = edtElabelNumber.getText().toString();
            OnLight(v, labelCode,"進行支出！"); //亮燈
            String url = globaldata.getPHP_SERVER();
            try {
                //exportDataToCSV();


                url += "DBoption=" + URLEncoder.encode("OUT", "UTF-8") + "&";
                url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
                url += "DrugCode=" + URLEncoder.encode(edtDrugCode.getText().toString(), "UTF-8") + "&";
                url += "StoreID=" + URLEncoder.encode(edtDrugStore.getText().toString(), "UTF-8") + "&";
                url += "AreaNo=" + URLEncoder.encode(edtAreaNo.getText().toString(), "UTF-8") + "&";
                url += "BlockNo=" + URLEncoder.encode(edtBlockNo.getText().toString(), "UTF-8") + "&";
                url += "LotNumber=" + URLEncoder.encode(txtLotNumber.getText().toString(), "UTF-8") + "&";
                url += "MakeDate=" + URLEncoder.encode(edtMakeDate.getText().toString(), "UTF-8") + "&";
                url += "EffectDate=" + URLEncoder.encode(edtEffectDate.getText().toString(), "UTF-8") + "&";
                url += "StockQty=" + URLEncoder.encode(edtInQty.getText().toString(), "UTF-8") + "&";
                url += "StoreType=" + URLEncoder.encode(edtBlockType.getText().toString(), "UTF-8") + "&";
                url += "Remark=" + URLEncoder.encode(spinner.getSelectedItem().toString(), "UTF-8") + "&";
                url += "UserID=" + URLEncoder.encode(globaldata.getLoginUserID(), "UTF-8") + "&";


                url += "TotalQty=" + URLEncoder.encode(edtInQty.getText().toString(), "UTF-8") + "&";
                url += "UserID=" + globaldata.getLoginUserID() + "&";


//                    Log.d("TAG", "DrugCode: " + edtDrugCode.getText().toString());
//                    Log.d("TAG", "AreaNo: " + edtAreaNo.getText().toString());
//                    Log.d("TAG", "BlockNo: " + edtBlockNo.getText().toString());
//                    Log.d("TAG", "BlockType: " + edtBlockType.getText().toString());
//                    Log.d("TAG", "TotalQty: " + edtInQty.getText().toString());
//                    Log.d("TAG", "StoreID: " + edtDrugStore.getText().toString());
//                    Log.d("TAG", "DBoption: in");
//                    Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
//                    Log.d("TAG", "DrugEnglish: " + edtDrugEnglish.getText().toString());
//                    Log.d("TAG", "spinnerText: " + spinner.getSelectedItem().toString());
//                    Log.d("TAG", "UserID: " + globaldata.getLoginUserID());

                sendGET(url, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(v.getContext(),"submit",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Toast.makeText(v.getContext(), "進行支出！", Toast.LENGTH_SHORT).show();
        }
    };

    public void OnLight(View view, String labelCode,String txt) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        String jsonString = "[\n{\n\"color\": \"CYAN\",\n\"duration\": \"1\",\n\"labelCode\": \"" + labelCode + "\"\n}\n]";
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder()
                .url("http://192.168.5.137:9003/labels/contents/led")
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
                            Toast.makeText(view.getContext(),txt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public interface VolleyCallback {
        void onSuccess(JSONArray response);
        // 在這裡可以添加其他方法，如 onFailure 等
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

    public void CSVReadDrugStore() {
        try {
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/drugstore.csv"));
            String[] nextLine;

            int i = 0;
            String[] record = null;
            while ((record = reader.readNext()) != null) {
                Drugstore drugstore = new Drugstore();
                drugstore.setStoreID(record[0]);
                Log.d("drugstore", record[6]);
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

            }
            reader.close();
        } catch (IOException e) {
            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
            e.printStackTrace();
        }
    }

    public void CSVReadDrugInfo() {
        try {
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/druginfo.csv"));
            String[] nextLine;

            int i = 0;
            String[] record = null;
            while ((record = reader.readNext()) != null) {
                Druginfo druginfo = new Druginfo();
                druginfo.setMakerID(record[0]);
                druginfo.setDrugCode(record[1]);
                druginfo.setDrugEnglish(record[2]);
                druginfo.setDrugName(record[3]);
                druginfo.setDrugLabel(record[10]);
                Druginfos.add(druginfo);
            }
            reader.close();
        } catch (IOException e) {
            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //tx1 = (TextView) view.findViewById(R.id.DrugName);
    }

    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    private Spinner.OnItemSelectedListener spnOnItemSelected = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected
                (AdapterView<?> adapterView, View view, int position, long l) {
            String msg = adapterView.getItemAtPosition(position).toString();
            CodeID = "A";
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
