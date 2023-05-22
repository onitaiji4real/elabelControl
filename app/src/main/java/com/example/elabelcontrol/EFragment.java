package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.opencsv.CSVReader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

public class EFragment extends Fragment {
    GlobalData globaldata;
    EditText edtDrugLabel,edtSearchDrug,edtDrugStore,edtDrugCode,edtDrugEnglish;
    Button btnGetDrugStore,btnGetDrugStore2,btnLight,btnLastData,btnNextData;
    TextView txtResultNum,txtResultString;
    List<Druginfo> Druginfos;
    List<Drugstore> Drugstores;
    List<Drugstore> ResultDrugStore;
    Integer ResultIndex;
    String ElabelNumber;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    Boolean GetFin;
    public interface VolleyCallback{
        void onSuccess();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_e, container, false);
        globaldata = (GlobalData)getActivity().getApplicationContext();
        edtDrugLabel = view.findViewById(R.id.edtElabelNumber);
        edtSearchDrug = view.findViewById(R.id.edtSearchDrug);
        edtDrugStore = view.findViewById(R.id.edtDrugStore);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugEnglish = view.findViewById(R.id.edtDrugEnglish);
        btnGetDrugStore = view.findViewById(R.id.btnGetDrugStore);
        btnGetDrugStore.setOnClickListener(onGetDrugStore);
        btnGetDrugStore2 = view.findViewById(R.id.btnGetDrugStore2);
        btnGetDrugStore2.setOnClickListener(onGetDrugStore2);
        btnLight = view.findViewById(R.id.btnLight);
        btnLight.setOnClickListener(onLight);

        Druginfos = new ArrayList<Druginfo>();
        CSVReadDrugInfo();
        Drugstores = new ArrayList<Drugstore>();
        CSVReadDrugStore();
        ResultDrugStore = new ArrayList<Drugstore>();

        txtResultNum = view.findViewById(R.id.txtResultNum);
        txtResultString = view.findViewById(R.id.txtResultString);
        btnLastData = view.findViewById(R.id.btnLastData);
        btnLastData.setOnClickListener(onGoLast);
        btnNextData = view.findViewById(R.id.btnNextData);
        btnNextData.setOnClickListener(onGoNext);
        txtResultNum.setVisibility(View.INVISIBLE);
        txtResultString.setVisibility(View.INVISIBLE);
        btnLastData.setVisibility(View.INVISIBLE);
        btnNextData.setVisibility(View.INVISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private View.OnClickListener onGetDrugStore = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String DrugLabel = edtDrugLabel.getText().toString();
            if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugLabel().equals(DrugLabel))).count() == 0) {
                //無此條碼資料
                Toast.makeText(v.getContext(), "此包裝條碼無符合結果", Toast.LENGTH_SHORT).show();
            } else {
                List<Druginfo> MatchDruginfo = Druginfos.stream().filter(druginfo -> (druginfo.getDrugLabel().equals(DrugLabel))).collect(Collectors.toList());
                if (Drugstores.stream().filter(drugstore -> (drugstore.getDrugCode().equals(MatchDruginfo.get(0).getDrugCode()))).count() == 0) {
                    //無此條碼資料
                    Toast.makeText(v.getContext(), "此包裝條碼無符合結果", Toast.LENGTH_SHORT).show();
                } else {
                    List<Drugstore> MatchDrugstore = Drugstores.stream().filter(drugstore -> (drugstore.getDrugCode().equals(MatchDruginfo.get(0).getDrugCode()))).collect(Collectors.toList());
                    edtDrugCode.setText(MatchDrugstore.get(0).getDrugCode());
                    edtDrugStore.setText(MatchDrugstore.get(0).getStoreID() + "-" + MatchDrugstore.get(0).getAreaNo() + "-" + MatchDrugstore.get(0).getBlockNo() + "-" + MatchDrugstore.get(0).getBlockType());
                    edtDrugEnglish.setText(MatchDruginfo.get(0).getDrugEnglish());
                    ElabelNumber = MatchDrugstore.get(0).getElabelNumber();
                }
            }
        }
    };

    private View.OnClickListener onGetDrugStore2 = new View.OnClickListener() {
//btn.setBackgroundColor(Color.WHITE);
        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            txtResultNum.setVisibility(View.VISIBLE);
            txtResultString.setVisibility(View.VISIBLE);
            btnLastData.setVisibility(View.VISIBLE);
            btnNextData.setVisibility(View.VISIBLE);
            String SearchString = edtSearchDrug.getText().toString();
            ResultDrugStore.clear();
            ResultIndex = 0;
            List<Druginfo> MatchDruginfo;
            MatchDruginfo = new ArrayList<Druginfo>();  //儲存模糊搜尋結果
            Log.d("Druginfos DrugCode比對",String.valueOf(Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().contains(SearchString))).count()));
            Log.d("Druginfos DrugEnglish比對",String.valueOf(Druginfos.stream().filter(druginfo -> (druginfo.getDrugEnglish().contains(SearchString))).count()));
            //模糊搜尋藥品代碼後放入搜尋結果
            if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().contains(SearchString))).count() > 0) {
                MatchDruginfo = Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().contains(SearchString))).collect(Collectors.toList());
            }
            //模糊搜尋藥品英文後再比對已有的搜尋結果後沒重複的再放入
            if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugEnglish().contains(SearchString))).count() > 0){
                List<Druginfo> MatchDruginfo_English = new ArrayList<Druginfo>();
                MatchDruginfo_English = Druginfos.stream().filter(druginfo -> (druginfo.getDrugEnglish().contains(SearchString))).collect(Collectors.toList());
                for(int i=0;i<MatchDruginfo_English.size();i++){
                    Boolean RepeatData = false;
                    for(int j=0;j<MatchDruginfo.size();j++) {
                        if (MatchDruginfo_English.get(i).getDrugCode() == MatchDruginfo.get(j).getDrugCode()){
                            RepeatData = true;
                        }
                    }
                    if(!RepeatData){
                        MatchDruginfo.add(MatchDruginfo_English.get(i));
                    }
                }
            }
            //由目前篩選藥品的結果中撈取其儲位資料放入模糊搜尋結果中(ResultDrugStore)
            if (MatchDruginfo.size() > 0){
                for (int i = 0; i < MatchDruginfo.size(); i++) {
                    Log.d("Druginfo比對",MatchDruginfo.get(i).getDrugCode());
                    String MatchDrugCode = MatchDruginfo.get(i).getDrugCode();
                    if (Drugstores.stream().filter(drugstore -> (drugstore.getDrugCode().equals(MatchDrugCode))).count() > 0) {
                        List<Drugstore> MatchDrugstore = Drugstores.stream().filter(drugstore -> (drugstore.getDrugCode().equals(MatchDrugCode))).collect(Collectors.toList());
                        MatchDrugstore.forEach(drugstore->{Log.d("Drugstore比對",drugstore.getElabelNumber());
                            ResultDrugStore.add(drugstore);
                        });
                    }
                }
            }
            //若有結果，則預設先顯示第一筆
            if(ResultDrugStore.size()==0){
                //無搜尋結果
                Toast.makeText(v.getContext(), "此搜尋無符合結果", Toast.LENGTH_SHORT).show();
                txtResultNum.setText("0");
                btnLastData.setBackgroundColor(Color.GRAY);
                btnNextData.setBackgroundColor(Color.GRAY);
            }
            else {
                txtResultNum.setText(String.valueOf(ResultDrugStore.size()));
                btnLastData.setBackgroundColor(Color.GRAY);
                if(ResultDrugStore.size() == 1) {
                    btnNextData.setBackgroundColor(Color.GRAY);
                }else{
                    btnNextData.setBackgroundColor(Color.parseColor("#FF018786"));
                }
                edtDrugStore.setText(ResultDrugStore.get(ResultIndex).getStoreID() + "-" + ResultDrugStore.get(ResultIndex).getAreaNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockType());
                edtDrugCode.setText(ResultDrugStore.get(ResultIndex).getDrugCode());
                if(Druginfos.stream().filter(druginfo ->(druginfo.getDrugCode().equals(ResultDrugStore.get(0).getDrugCode()))).count()>0) {
                    edtDrugEnglish.setText(Druginfos.stream().filter(druginfo ->(druginfo.getDrugCode().equals(ResultDrugStore.get(ResultIndex).getDrugCode()))).collect(Collectors.toList()).get(0).getDrugEnglish());
                }
                ElabelNumber = ResultDrugStore.get(ResultIndex).getElabelNumber();
            }
        }
    };

    private View.OnClickListener onLight = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String url = globaldata.AIMSServer+"articles/label/"+ElabelNumber;
            Log.d("url:",url);
            String LightJsonStr = String.format("[\n" +
                    "  {\n" +
                    "    \"color\": \"RED\",\n" +
                    "    \"duration\": \"30s\",\n" +
                    "    \"labelCode\": \"%s\"\n" +
                    "  }\n" +
                    "]",ElabelNumber);
            GetFin=false;
            putRequestWithHeaderAndBody(url,LightJsonStr,new MainActivity.VolleyCallback(){
                @Override
                public void onSuccess() {
                    GetFin=true;
                }
            });
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

    private View.OnClickListener onGoLast = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(ResultIndex>0) {
                ResultIndex--;
                if (ResultIndex == 0) {
                    btnLastData.setBackgroundColor(Color.GRAY);
                }
                btnNextData.setBackgroundColor(Color.parseColor("#FF018786"));
                edtDrugStore.setText(ResultDrugStore.get(ResultIndex).getStoreID() + "-" + ResultDrugStore.get(ResultIndex).getAreaNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockType());
                edtDrugCode.setText(ResultDrugStore.get(ResultIndex).getDrugCode());
                if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(ResultDrugStore.get(ResultIndex).getDrugCode()))).count() > 0) {
                    edtDrugEnglish.setText(Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(ResultDrugStore.get(ResultIndex).getDrugCode()))).collect(Collectors.toList()).get(0).getDrugEnglish());
                }
                ElabelNumber = ResultDrugStore.get(ResultIndex).getElabelNumber();
            }
        }
    };

    private View.OnClickListener onGoNext = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if(ResultIndex < ResultDrugStore.size()-1) {
                ResultIndex++;
                btnLastData.setBackgroundColor(Color.parseColor("#FF018786"));
                if (ResultIndex == ResultDrugStore.size() - 1) {
                    btnNextData.setBackgroundColor(Color.GRAY);
                }
                edtDrugStore.setText(ResultDrugStore.get(ResultIndex).getStoreID() + "-" + ResultDrugStore.get(ResultIndex).getAreaNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockNo() + "-" + ResultDrugStore.get(ResultIndex).getBlockType());
                edtDrugCode.setText(ResultDrugStore.get(ResultIndex).getDrugCode());
                if (Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(ResultDrugStore.get(ResultIndex).getDrugCode()))).count() > 0) {
                    edtDrugEnglish.setText(Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(ResultDrugStore.get(ResultIndex).getDrugCode()))).collect(Collectors.toList()).get(0).getDrugEnglish());
                }
                ElabelNumber = ResultDrugStore.get(ResultIndex).getElabelNumber();
            }
        }
    };
    public void CSVReadDrugInfo(){
        try{
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath()+"/druginfo.csv"));
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
    public void CSVReadDrugStore(){
        try{
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath()+"/drugstore.csv"));
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

    public void putRequestWithHeaderAndBody(String url, String jsonstr,final MainActivity.VolleyCallback callback) {

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
