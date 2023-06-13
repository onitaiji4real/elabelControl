package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


import data.DrugInOut;
import data.Druginfo;
import data.Drugstore;
import data.GlobalData;

public class CFragment extends Fragment {
    Spinner spinner;
    EditText edtElabelNumber, edtDrugCode, edtDrugEnglish, edtInQty, edtDrugStore, edtAreaNo, edtBlockNo, edtBlockType, edtEffectDate, edtMakeDate;
    Button btnSumit, btnLight, btnGetDrugStore, btnClear, btnPreViewIndex, btnNextIndex, btnNewDrugIN,btnStatus;
    GlobalData globaldata;
    TextView textNum, txtLotNumber, textLotNumber_size;


    List<Drugstore> Drugstores;
    List<DrugInOut> DrugInOuts;
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
    String CodeID;
    boolean getFin;
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

//    DatePicker datePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_c, container, false);
        globaldata = (GlobalData) getActivity().getApplicationContext();

        edtElabelNumber = view.findViewById(R.id.edtElabelNumber);
        edtElabelNumber.requestFocus();//聚焦 電子紙條碼欄位

        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);


        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugEnglish = view.findViewById(R.id.edtDrugEnglish);
        edtDrugStore = view.findViewById(R.id.edtDrugStore);
        edtInQty = view.findViewById(R.id.edtInQty);

        btnSumit = view.findViewById(R.id.btnSumit);
        btnSumit.setOnClickListener(OnSubmit);

        btnLight = view.findViewById(R.id.btnLight);
        btnLight.setOnClickListener(OnLight);

        textNum = view.findViewById(R.id.textNum);
        btnNewDrugIN = view.findViewById(R.id.btnNewDrugIN);
        btnNewDrugIN.setOnClickListener(onNewDrugIN);

        txtLotNumber = view.findViewById(R.id.txtLotNumber);

        btnNextIndex = view.findViewById(R.id.btnNextIndex);
        btnNextIndex.setOnClickListener(nextIndex);

        btnPreViewIndex = view.findViewById(R.id.btnPreViewIndex);
        btnPreViewIndex.setOnClickListener(previewIndex);

        edtEffectDate = view.findViewById(R.id.edtEffectDate);

        edtMakeDate = view.findViewById(R.id.edtMakeDate);

        textLotNumber_size = view.findViewById(R.id.textLotNumber_size);

        spinner = view.findViewById(R.id.spOutCode);

        List<spinner_Selection> spinner_Selection = new ArrayList<>();
        spinner_Selection.add(new spinner_Selection("購買", "A"));
        spinner_Selection.add(new spinner_Selection("入庫", "A"));
        spinner_Selection.add(new spinner_Selection("初期庫存量", "A"));
        spinner_Selection.add(new spinner_Selection("新藥收入(新批)", "A"));

        ArrayAdapter<spinner_Selection> adapter =
                new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_spinner_item, spinner_Selection);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(1, false);
        CodeID = spinner_Selection.get(1).getREMARK_INFO_ID();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_Selection selectedOption = (spinner_Selection) parent.getItemAtPosition(position);
                String selectedOptionId = selectedOption.getREMARK_INFO_ID();
                Log.d("TAG", "ID: " + selectedOptionId);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 如果沒有選擇任何選項時的處理
            }
        });

        spinner.setOnItemSelectedListener(spnOnItemSelected);


        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(onClearField);



        labelAfterScanListener();
        return view;
    }

    public class spinner_Selection {
        private String REMARK_INFO;
        private String REMARK_INFO_ID;

        public spinner_Selection(String REMARK_INFO, String REMARK_INFO_ID) {
            this.REMARK_INFO = REMARK_INFO;
            this.REMARK_INFO_ID = REMARK_INFO_ID;
        }

        public String getREMARK_INFO() {
            return REMARK_INFO;
        }

        public String getREMARK_INFO_ID() {
            return REMARK_INFO_ID;
        }

        @Override
        public String toString() {
            return REMARK_INFO; // 返回要顯示的選項名稱
        }
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

    private View.OnClickListener onNewDrugIN = new View.OnClickListener() {

        boolean isPressed = false;
        GradientDrawable drawable = new GradientDrawable();


        //drawable.setColor(Color.RED);

        @Override
        public void onClick(View v) {
            if (isPressed) {


                //已按下、回復原本狀態
                //updateUIWithCurrentIndex();
                if (edtElabelNumber.length()==12){

                    updateUIWithCurrentIndex();

                    spinner.setEnabled(true);
                    spinner.setSelection(1, false);
                    btnNewDrugIN.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    btnNextIndex.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    btnPreViewIndex.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    Toast.makeText(v.getContext(), "離開新藥收入模式！", Toast.LENGTH_SHORT).show();

                    btnPreViewIndex.setEnabled(true);
                    btnNextIndex.setEnabled(true);


                }else {
//                    btnNewDrugIN.setEnabled(false);
                    edtInQty.setText("");
                    txtLotNumber.setText("");
                    edtEffectDate.setText("");
                    edtMakeDate.setText("");
                    textNum.setText("");
                    spinner.setEnabled(true);
                    spinner.setSelection(1, false);
                    edtElabelNumber.requestFocus();

                    btnNewDrugIN.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    btnNextIndex.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    btnPreViewIndex.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green)));
                    Toast.makeText(v.getContext(), "離開新藥收入模式！", Toast.LENGTH_SHORT).show();

                    btnPreViewIndex.setEnabled(true);
                    btnNextIndex.setEnabled(true);
                }



                isPressed = false;
            } else {
                //第一次按下、

                Date currentDate = new Date();
                SimpleDateFormat LotNumberFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                SimpleDateFormat MakeDateFormat = new SimpleDateFormat("yyy-MM-dd");
                String formattedDate = LotNumberFormat.format(currentDate);
                String MakeDateFormate = MakeDateFormat.format(currentDate);

                //txtLotNumber.setText(formattedDate);
                textNum.setText("新藥收入中");
                txtLotNumber.setText(formattedDate);
                edtMakeDate.setText(MakeDateFormate);
                spinner.setSelection(3, false);
                edtInQty.setText("");
                edtInQty.requestFocus();
                btnNewDrugIN.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                btnNextIndex.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                btnNextIndex.setEnabled(false);
                btnPreViewIndex.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                btnPreViewIndex.setEnabled(false);

                spinner.setEnabled(false);

                edtEffectDate.setText("2023-12-31");
                Toast.makeText(v.getContext(), "開始執行新藥收入！", Toast.LENGTH_SHORT).show();
                Toast.makeText(v.getContext(), "請輸入收入數量！", Toast.LENGTH_SHORT).show();
                isPressed = true;
            }
        }
    };

    private View.OnClickListener onClear = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edtElabelNumber.setText("");
            edtDrugStore.setText("");
            edtAreaNo.setText("");
            edtBlockNo.setText("");
            edtDrugStore.setText("");
            edtDrugEnglish.setText("");
            edtInQty.setText("");
            edtDrugCode.setText("");
            edtBlockType.setText("");
            edtElabelNumber.requestFocus();
            hideKeyboard(v.getContext());
        }
    };

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        String url = "http://192.168.5.41/pda_submit.php?";
        try {
            url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
            url += "DBoption=GET" + "&";
            //url += "TotalQty=" + URLEncoder.encode(edtInQty.getText().toString(),"UTF-8") + "&";
            //url += "UserID=" + globaldata.getLoginUserID() + "&";
            //url += "spinnerText=" + URLEncoder.encode(spinner.getSelectedItem().toString());

            Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
            Log.d("TAG", "DBoption:");
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

    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private View.OnClickListener OnSubmit = new View.OnClickListener() {
        /**收入*/

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String labelCode = edtElabelNumber.getText().toString();
            OnLight(v, labelCode,"收入成功！"); //亮燈
            String url = "http://192.168.5.41/pda_submit.php?";
            try {
                //exportDataToCSV();


                url += "DBoption=" + URLEncoder.encode("IN", "UTF-8") + "&";
                //url += "ElabelNumber=" + URLEncoder.encode(edtElabelNumber.getText().toString(), "UTF-8") + "&";
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
            Toast.makeText(v.getContext(), "進行收入！", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener OnLight = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String labelCode = edtElabelNumber.getText().toString();
            OnLight(view, labelCode,"亮燈成功！"); //亮燈
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
                            Toast.makeText(view.getContext(), txt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
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

    private void exportDataToCSV() throws IOException {

        String csvData = "";
        Integer count = 0;
/*
        for (int i = 0; i < DrugInOuts.size(); i++) {
            if ((StoreID == Inventorys.get(i).getStoreID()) && (AreaNo == Inventorys.get(i).getAreaNo()) && (BlockNo == Inventorys.get(i).getBlockNo())
                    && (BlockType == Inventorys.get(i).getBlockType())) {
                count += 1;
                Log.d("count", count.toString());
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
                Calendar c = Calendar.getInstance();
                String date = sdfDate.format(c.getTime());
                String time = sdfTime.format(c.getTime());

                Integer IntStockQty = Integer.valueOf(StockQty);
                Integer InventoryQty = Integer.valueOf(txtInventoryQty.getText().toString());
                Integer AdjQty = InventoryQty - IntStockQty;
                Inventorys.get(i).setInventoryQty(InventoryQty.toString());
                Inventorys.get(i).setAdjQty(AdjQty.toString());
                Inventorys.get(i).setInvDate(date);
                Inventorys.get(i).setInvTime(time);
                Inventorys.get(i).setUserID(globaldata.getLoginUserID());
            }
        }
        if (count == 0) {
            Log.d("save", globaldata.getLoginUserID());
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
            Calendar c = Calendar.getInstance();
            String date = sdfDate.format(c.getTime());
            String time = sdfTime.format(c.getTime());

            Integer IntStockQty = Integer.valueOf(StockQty);
            Integer InventoryQty = Integer.valueOf(txtInventoryQty.getText().toString());
            Integer AdjQty = InventoryQty - IntStockQty;

            DrugInOut druginout = new DrugInOut();
            druginout.setInOutTime(date+" "+time);
            druginout.setGroupID("A");
            druginout.setCodeID(CodeID);
            druginout.setStoreID(StoreID);
            druginout.setAreaNo(AreaNo);
            druginout.setBlockNo(BlockNo);
            druginout.setBlockType(BlockType);
            druginout.setDrugCode(DrugCode);
            druginout.setLotNumber(LotNumber);
            druginout.setEffectDate(EffectDate);
            druginout.setMakerID(MakerID);
            druginout.setInQty();
            druginout.setOutQty;
            druginout.setRemark;
            druginout.setUserID;
            Inventorys.add(inventory);
        }
        for (int i = 0; i < Inventorys.size(); i++) {
            String currentLIne = Inventorys.get(i).getRowString();
            String[] cells = currentLIne.split(",");
            csvData += toCSV(cells) + "\n";
        }
        File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String uniqueFileName = "/inventory.csv";
        File file = new File(directory.getAbsolutePath(), uniqueFileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(csvData);
        fileWriter.flush();
        fileWriter.close();
        Toast.makeText(getActivity(), "File Exported Successfully", Toast.LENGTH_SHORT).show();

 */
    }

    private View.OnClickListener OnGetDrugStore = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String ElabelNumber = edtElabelNumber.getText().toString();
            if (Drugstores.stream().count() == 0) {
                //Log.d("Drugstores", "Drugstores 為 0");
            }
            Log.d("ElabelNumber", ElabelNumber);
            if (Drugstores.stream().filter(drugstore -> (drugstore.getElabelNumber().equals(ElabelNumber))).count() == 0) {
                //無此條碼資料
                Log.d("error", "無結果");
            } else {
                List<Drugstore> MatchDrugstore = Drugstores.stream().filter(drugstore -> (drugstore.getElabelNumber().equals(ElabelNumber))).collect(Collectors.toList());
                if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).count() > 0) {
                    List<Druginfo> MatchDruginfo = Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).collect(Collectors.toList());
                    edtDrugEnglish.setText(MatchDruginfo.get(0).getDrugEnglish());
                }
                edtDrugCode.setText(MatchDrugstore.get(0).getDrugCode());
                edtDrugStore.setText(MatchDrugstore.get(0).getStoreID() + "-" + MatchDrugstore.get(0).getAreaNo() + "-" + MatchDrugstore.get(0).getBlockNo() + "-" + MatchDrugstore.get(0).getBlockType());
                StoreID = MatchDrugstore.get(0).getStoreID();
                AreaNo = MatchDrugstore.get(0).getAreaNo();
                BlockNo = MatchDrugstore.get(0).getBlockNo();
                BlockType = MatchDrugstore.get(0).getBlockType();
                DrugCode = MatchDrugstore.get(0).getDrugCode();
                MakerID = MatchDrugstore.get(0).getMakerID();
                TemPt_Kind = MatchDrugstore.get(0).getTemPt_Kind();
                SafeStock = MatchDrugstore.get(0).getSafeStock();
                TotalQty = MatchDrugstore.get(0).getTotalQty();
                SetTime = MatchDrugstore.get(0).getSetTime();
                SetUserID = MatchDrugstore.get(0).getSetUserID();
                InvQtyTime = MatchDrugstore.get(0).getInvQtyTime();
                InvQtyUserID = MatchDrugstore.get(0).getInvQtyUserID();
                LotNumber = MatchDrugstore.get(0).getLotNumber();
                EffectDate = MatchDrugstore.get(0).getEffectDate();
                StockQty = MatchDrugstore.get(0).getStockQty();
                UpdateUserID = MatchDrugstore.get(0).getUpdateUserID();
                UpdateTime = MatchDrugstore.get(0).getUpdateTime();
            }
        }
    };
}
