package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.widget.Spinner;

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
    private TextView edtElabelNumber;
    String CodeID;
    GlobalData globaldata;
    Button btnLight,btnSumit;
    List<Drugstore> Drugstores;
    List<DrugInOut> DrugInOuts;
    List<Druginfo> Druginfos;
    boolean getFin;
    GlobalData globalData;
    EditText edtDrugStore,edtBlockNo,edtBlockType,edtAreaNo,edtDrugCode,edtDrugEnglish,edtInQty;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        globaldata = (GlobalData) getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.layout_d, container, false);
        Drugstores = new ArrayList<Drugstore>();
        CSVReadDrugStore();
        DrugInOuts = new ArrayList<DrugInOut>();

        Druginfos = new ArrayList<Druginfo>();
        CSVReadDrugInfo();

        btnSumit = view.findViewById(R.id.btnSumit);
        btnSumit.setOnClickListener(OnSubmit);
        spinner = view.findViewById(R.id.spOutCode);


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

        labelAfterScanListener();

        return view;
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
                    .url("http://192.168.5.42:9003/labels/contents/led")
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

        Druginfo target_DrugInfo = null;
        Drugstore target_DrugStore = null;
        for (Drugstore drugstore : Drugstores) {
            if (drugstore.getElabelNumber().equals(ed)) {
                target_DrugStore = drugstore;
                break;
            }
        }

        for (Druginfo druginfo : Druginfos) {
            if (druginfo.getDrugCode().equals(target_DrugStore.getDrugCode())) {
                target_DrugInfo = druginfo;
            }
        }


        edtDrugStore.setText(target_DrugStore.getStoreID());
        edtAreaNo.setText(target_DrugStore.getAreaNo());
        edtBlockNo.setText(target_DrugStore.getBlockNo());
        edtBlockType.setText(target_DrugStore.getBlockType());
        edtDrugCode.setText(target_DrugStore.getDrugCode());
        edtDrugEnglish.setText(target_DrugInfo.getDrugName());

        Toast.makeText(getActivity(), "掃描成功", Toast.LENGTH_SHORT).show();

    }

    private View.OnClickListener OnSubmit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String url = "http://192.168.5.41/pda_submit.php?";
            try {
                String DBoption;
                url = url + "DrugCode=" + URLEncoder.encode(edtDrugCode.getText().toString(), "UTF-8") + "&";
                url = url + "AreaNo=" + URLEncoder.encode(edtAreaNo.getText().toString(), "UTF-8") + "&";
                url = url + "BlockNo=" + URLEncoder.encode(edtBlockNo.getText().toString(), "UTF-8") + "&";
                url = url + "BlockType=" + URLEncoder.encode(edtBlockType.getText().toString(), "UTF-8") + "&";
                url = url + "TotalQty=" + URLEncoder.encode(edtInQty.getText().toString(), "UTF-8") + "&";
                url = url + "StoreID=" + URLEncoder.encode(edtDrugStore.getText().toString(), "UTF-8") + "&";
                url = url + "DBoption=" +URLEncoder.encode("out", "UTF-8") + "&";
                url = url + "ElabelNumber=" +URLEncoder.encode(edtElabelNumber.getText().toString(),"UTF-8")+"&";
                url = url + "DrugEnglish=" +URLEncoder.encode(edtDrugEnglish.getText().toString(),"UTF-8")+"&";
                url = url + "spinnerText=" +URLEncoder.encode(spinner.getSelectedItem().toString(),"UTF-8")+"&";
                url = url + "UserID=" +URLEncoder.encode(globaldata.getLoginUserID(),"UTF-8");

                Log.d("TAG", "DrugCode: " + edtDrugCode.getText().toString());
                Log.d("TAG", "AreaNo: " + edtAreaNo.getText().toString());
                Log.d("TAG", "BlockNo: " + edtBlockNo.getText().toString());
                Log.d("TAG", "BlockType: " + edtBlockType.getText().toString());
                Log.d("TAG", "TotalQty: " + edtInQty.getText().toString());
                Log.d("TAG", "StoreID: " + edtDrugStore.getText().toString());
                Log.d("TAG", "DBoption:out");
                Log.d("TAG", "ElabelNumber: " + edtElabelNumber.getText().toString());
                Log.d("TAG", "DrugEnglish: " + edtDrugEnglish.getText().toString());
                Log.d("TAG", "spinnerText: " + spinner.getSelectedItem().toString());
                Log.d("TAG", "UserID: " + globaldata.getLoginUserID());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Toast.makeText(v.getContext(), url, Toast.LENGTH_SHORT).show();
            getFin = false;
            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess() {
                    getFin = true;
                }
            });
            while (!getFin) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
    };

    public interface VolleyCallback {
        void onSuccess();
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
                callback.onSuccess();
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
