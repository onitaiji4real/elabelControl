package com.example.elabelcontrol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import data.GlobalData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class EFragment extends Fragment {
    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    ArrayList<HashMap<String, String>> arrayList;
    RadioGroup radGroup;
    RadioButton radSearchLabel, radSearchCode, radSearchEnglish, radSearchName;
    EditText edtDrugLabel;
    Button btnSearch;
    GlobalData globaldata;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_e, container, false);
        globaldata = (GlobalData) getActivity().getApplicationContext();

        mRecyclerView = view.findViewById(R.id.searchList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        edtDrugLabel = view.findViewById(R.id.edtDrugLabel);
        btnSearch = view.findViewById(R.id.btnLabelSearch);
        btnSearch.setOnClickListener(onSearch);

        radGroup = view.findViewById(R.id.radGroup);

        radSearchLabel = view.findViewById(R.id.radSearchLabel);
        radSearchCode = view.findViewById(R.id.radSearchCode);
        radSearchEnglish = view.findViewById(R.id.radSearchEnglish);
        radSearchName = view.findViewById(R.id.radSearchName);

        radGroup.check(R.id.radSearchLabel); //預設藥物條碼選項
        edtDrugLabel.requestFocus();

        arrayList = new ArrayList<>();

        // 建立一個預設的ArrayList來儲存預設的資料
        HashMap<String, String> testData = new HashMap<>();
        testData.put("DrugStore", "目前無搜尋結果");
//        testData.put("DrugEnglish", "TEST");
//        testData.put("DrugName", "TEST");
//        testData.put("DrugCode", "IHBV001");
//        testData.put("StockNum", "50.00");
//        testData.put("LotNumber", "202306101345");
//        testData.put("InventoryTime", "2023-06-16 13:45");


        myListAdapter = new MyListAdapter(arrayList);
        mRecyclerView.setAdapter(myListAdapter);
        makeData(Collections.singletonList(testData));
        return view;
    }


    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
        private ArrayList<HashMap<String, String>> arrayList;

        public MyListAdapter(ArrayList<HashMap<String, String>> arrayList) {
            this.arrayList = arrayList;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView drugStore, drugEnglish, drugName, drugCode, stockNum, lotNumber, txtElabelNumber,inventoryTime;
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
                //inventoryTime = itemView.findViewById(R.id.InventoryTime);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item_view, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, String> item = arrayList.get(position);
            holder.drugStore.setText(item.get("DrugStore"));
            holder.drugEnglish.setText(item.get("DrugEnglish"));
            holder.drugName.setText(item.get("DrugName"));
            holder.drugCode.setText(item.get("DrugCode"));
            holder.stockNum.setText(item.get("StockNum"));
            holder.lotNumber.setText(item.get("LotNumber"));

            holder.txtElabelNumber.setText(item.get("elabelNumber"));


            Toast.makeText(getContext(), item.get("message"), Toast.LENGTH_SHORT).show();
            holder.btnLight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    private void makeData(List<HashMap<String, String>> searchData) {
        arrayList.clear();
        arrayList.addAll(searchData);
        Log.d("TAG", "makeData: "+searchData);

        myListAdapter.notifyDataSetChanged();
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
                            String InventoryDate = jsonObject.getString("DrugName3");
                            String drugEnlglish = jsonObject.getString("DrugEnglish");

                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore", storeID + " - " + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("InventoryDate", InventoryDate);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);

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

    private View.OnClickListener onSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            String DrugLabel = edtDrugLabel.getText().toString();
            get_Search_Item();

        }
    };

    public interface VolleyCallback {
        void onSuccess(JSONArray  response);
    }
}
