package com.example.elabelcontrol;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import data.GlobalData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class DrugIn_Fragment extends Fragment {
    MyListAdapter myListAdapter;
    Spinner spinner;
    Button btnStatus,btnSearch,btnNewDrugIN;
    EditText edtDrugLabel;
    RecyclerView mRecyclerView;

    ArrayList<HashMap<String, String>> arrayList;

    RadioGroup radGroup;
    RadioButton radSearchLabel, radSearchCode, radSearchEnglish, radSearchName;
    EditText edtDrugStore,edtAreaNo,edtBlockNo,edtBlockType,edtDrugCode,edtInQty;

    GlobalData globaldata;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState){
        View view  = inflater.inflate(R.layout.layout_drug_in,container,false);
        globaldata = (GlobalData) getActivity().getApplicationContext();
        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugLabel = view.findViewById(R.id.edtDrugLabel);
        edtDrugLabel.requestFocus();
        radGroup = view.findViewById(R.id.radGroup);
        radSearchLabel = view.findViewById(R.id.radSearchLabel);
        radSearchCode = view.findViewById(R.id.radSearchCode);
        radSearchEnglish = view.findViewById(R.id.radSearchEnglish);
        radSearchName = view.findViewById(R.id.radSearchName);
        btnNewDrugIN = view.findViewById(R.id.btnNewDrugIN);



        spinner = view.findViewById(R.id.spOutCode);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(view.getContext(),
                        R.array.InCode,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(1, false);

        btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(onSearch);

        mRecyclerView = view.findViewById(R.id.searchList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        edtInQty = view.findViewById(R.id.edtInQty);
        arrayList = new ArrayList<>();
        // 建立一個預設的ArrayList來儲存預設的資料
        HashMap<String, String> testData = new HashMap<>();
        testData.put("DrugStore", "目前無搜尋結果");
        myListAdapter = new MyListAdapter(arrayList);
        mRecyclerView.setAdapter(myListAdapter);
        makeData(Collections.singletonList(testData));
        return view;
    }

    private View.OnClickListener onSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            get_Search_Item();
        }
    };
    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
        private ArrayList<HashMap<String, String>> arrayList;

        public MyListAdapter(ArrayList<HashMap<String, String>> arrayList) {
            this.arrayList = arrayList;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView drugStore, drugEnglish, drugName, drugCode, stockNum, lotNumber, txtElabelNumber,txtEffectDate;
            private Button btnLight;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                drugStore = itemView.findViewById(R.id.DrugStore_SA);
                drugEnglish = itemView.findViewById(R.id.DrugEnglish);
                drugName = itemView.findViewById(R.id.DrugName);
                drugCode = itemView.findViewById(R.id.DrugCode);
                stockNum = itemView.findViewById(R.id.StockNum);
                lotNumber = itemView.findViewById(R.id.txtLotNumber);
                btnLight = itemView.findViewById(R.id.btnLight);
                txtElabelNumber = itemView.findViewById(R.id.txtElabelNumber);
                txtEffectDate = itemView.findViewById(R.id.txtEffectDate);
                //inventoryTime = itemView.findViewById(R.id.InventoryTime);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item_view, parent, false);

            View.OnClickListener itemOnSubmit = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            };

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DrugIn_Fragment.MyListAdapter.ViewHolder holder, int position) {
            HashMap<String, String> item = arrayList.get(position);
            String elabelnumber = item.get("elabelNumber");
            String drugStore = item.get("DrugStore");
            String areaNo = item.get("areaNo");
            String blockNo = item.get("blockNo");

            String drugEnglish = item.get("DrugEnglish");
            String DrugName= item.get("DrugName");
            String DrugCode= item.get("DrugCode");
            String StockNum= item.get("StockNum");
            String LotNumber = item.get("LotNumber");
            String EffectDate= item.get("EffectDate");

            holder.drugStore.setText(drugStore);
            holder.drugEnglish.setText(drugEnglish);
            holder.drugName.setText(DrugName);
            holder.drugCode.setText(DrugCode);
            holder.stockNum.setText(StockNum);
            holder.lotNumber.setText(LotNumber);
            holder.txtEffectDate.setText(EffectDate);
            holder.txtElabelNumber.setText(elabelnumber);



            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 處理點擊事件
//
                    v.setBackgroundColor(ContextCompat.getColor(getContext(), com.google.android.material.R.color.design_default_color_primary_dark));

                    // 其他字段...
                    Toast.makeText(getContext(),elabelnumber, Toast.LENGTH_SHORT).show();

                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }, 500); // 500毫秒后恢复背景颜色

                    // 在这里进行处理，例如显示对话框、跳转到其他界面等
                }
            });
            holder.btnLight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaType mediaType = MediaType.parse("application/json");
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    String labelCode = item.get("elabelNumber");
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
                            //Toast.makeText(getContext(), "網路異常: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                throw new IOException("Unexpected code " + response);
                            } else {
                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(v.getContext(),"亮燈", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });

            Toast.makeText(getContext(), item.get("message"), Toast.LENGTH_SHORT).show();

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }


    private void get_Search_Item() {
        String url = globaldata.getPHP_SERVER();
        String DrugLabel = edtDrugLabel.getText().toString();
        try {
            url += "DBoption=Search_BY_Drug_Label" + "&";
            url += "DrugLabel=" + URLEncoder.encode(DrugLabel, "UTF-8") + "&";
            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        List<HashMap<String, String>> searchData = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            String message = jsonObject.getString("Response");
                            String elabelNumber = jsonObject.getString("ElabelNumber");
                            String storeID = jsonObject.getString("StoreID");
                            String elabelType = jsonObject.getString("ElabelType");
                            String drugCode = jsonObject.getString("DrugCode");
                            String drugName = jsonObject.getString("DrugName");
                            String StokQty = jsonObject.getString("DrugEnglish3");
                            String areaNo = jsonObject.getString("AreaNo");
                            String blockNo = jsonObject.getString("BlockNo");
                            String LotNumber = jsonObject.getString("DrugCode3");
                            String EffectDate = jsonObject.getString("DrugName3");
                            String drugEnlglish = jsonObject.getString("DrugEnglish");

                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore", storeID + " - " + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);
                            item.put("EffectDate",EffectDate);

                            searchData.add(item);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                makeData(searchData);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void makeData(List<HashMap<String, String>> searchData) {
        arrayList.clear();
        arrayList.addAll(searchData);
        Log.d("TAG", "makeData: "+searchData);

        myListAdapter.notifyDataSetChanged();
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
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                //Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
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
        void onSuccess(JSONArray  response);
    }
}


