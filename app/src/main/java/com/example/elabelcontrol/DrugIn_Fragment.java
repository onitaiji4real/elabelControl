package com.example.elabelcontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;



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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
    Button btnStatus, btnLabelSearch, btnNewDrugIN, btnSearchDrugCode, btnSearchDrugEnglish, btnSearchDrugName;
    EditText edtDrugLabel;
    RecyclerView mRecyclerView;

    ArrayList<HashMap<String, String>> arrayList;


    RadioButton radSearchLabel, radSearchCode, radSearchEnglish, radSearchName;
    EditText edtDrugStore, edtAreaNo, edtBlockNo, edtBlockType, edtDrugCode, edtInQty;

    GlobalData globaldata;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.layout_drug_in, container, false);
        globaldata = (GlobalData) getActivity().getApplicationContext();
        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugLabel = view.findViewById(R.id.edtDrugLabel);
        edtDrugLabel.requestFocus();

        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(onChangeMode);

//        RadioGroup radGroup = view.findViewById(R.id.radGroup);
//        radGroup.check(R.id.radSearchLabel);

//        radSearchLabel = view.findViewById(R.id.radSearchLabel);
//        radSearchCode = view.findViewById(R.id.radSearchCode);
//        radSearchEnglish = view.findViewById(R.id.radSearchEnglish);
//        radSearchName = view.findViewById(R.id.radSearchName);
//        btnNewDrugIN = view.findViewById(R.id.btnNewDrugIN);


        //spinner = view.findViewById(R.id.spOutCode);

//        ArrayAdapter<CharSequence> adapter =
//                ArrayAdapter.createFromResource(view.getContext(),
//                        R.array.InCode,
//                        android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setSelection(1, false);

        btnLabelSearch = view.findViewById(R.id.btnLabelSearch);
        btnLabelSearch.setOnClickListener(onSearch);

        btnSearchDrugCode = view.findViewById(R.id.btnSearchDrugCode);
        btnSearchDrugCode.setOnClickListener(onSearchDrugCode);

        btnSearchDrugEnglish = view.findViewById(R.id.btnSearchDrugEnglish);
        btnSearchDrugEnglish.setOnClickListener(onSearchDrugEnglish);

        btnSearchDrugName = view.findViewById(R.id.btnSearchDrugName);
        btnSearchDrugName.setOnClickListener(onSearchDrugName);


        mRecyclerView = view.findViewById(R.id.searchList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        edtInQty = view.findViewById(R.id.edtInQty);
        arrayList = new ArrayList<>();

        myListAdapter = new MyListAdapter(arrayList);
        mRecyclerView.setAdapter(myListAdapter);


//      建立一個預設的ArrayList來儲存預設的資料
//        HashMap<String, String> testData = new HashMap<>();
//        testData.put("DrugStore", "目前無搜尋結果");
//        makeData(Collections.singletonList(testData));


        return view;
    }
    private View.OnClickListener onChangeMode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(getActivity(), FragmentActivity.class);
            intent.putExtra("fragment", CFragment.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
    };

    private View.OnClickListener onSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            get_Search_Item();
        }
    };

    private View.OnClickListener onSearchDrugCode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            get_Search_Item_BY_DrugCode();
        }
    };

    private View.OnClickListener onSearchDrugEnglish = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            get_Search_Item_BY_DrugEnglish();
        }
    };

    private View.OnClickListener onSearchDrugName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            get_Search_Item_BY_DrugName();
        }
    };

    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
        private ArrayList<HashMap<String, String>> arrayList;

        public MyListAdapter(ArrayList<HashMap<String, String>> arrayList) {
            this.arrayList = arrayList;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView drugStore, drugEnglish, drugName, drugCode, stockNum, lotNumber, txtElabelNumber, txtEffectDate;
            private Button btnLight, btnSubmit;
            private HashMap<String, String> SelectedItem;
            private Spinner spOutCode;
            private EditText edtInQty;
            private String url = "";
            private String selectedId = "";  // Add this variable

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                drugStore = itemView.findViewById(R.id.DrugStore_SA);
                //drugStore.setText("目前無搜尋結果");
                drugEnglish = itemView.findViewById(R.id.DrugEnglish);
                drugName = itemView.findViewById(R.id.DrugName);
                drugCode = itemView.findViewById(R.id.DrugCode);
                stockNum = itemView.findViewById(R.id.StockNum);
                lotNumber = itemView.findViewById(R.id.txtLotNumber);
                btnLight = itemView.findViewById(R.id.btnLight);
                txtElabelNumber = itemView.findViewById(R.id.txtElabelNumber);
                txtEffectDate = itemView.findViewById(R.id.txtEffectDate);
                spOutCode = itemView.findViewById(R.id.spOutCode);
                edtInQty = itemView.findViewById(R.id.edtInQty);
                btnSubmit = itemView.findViewById(R.id.btnSubmit);


                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(itemView.getContext(),
                        R.array.InCode, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spOutCode.setAdapter(adapter);
                spOutCode.setSelection(1);

                spOutCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String[] inCodeIds = getResources().getStringArray(R.array.InCodeIDs);
                        selectedId = inCodeIds[position];

                        Log.d("SELECTID", selectedId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
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
            holder.url = globaldata.getPHP_SERVER();
            String CodeID = holder.selectedId;
            String elabelnumber = item.get("elabelNumber");
            String drugStore_SA = item.get("DrugStore_SA");
            String drugStoreID = item.get("DrugStoreID");
            String areaNo = item.get("AreaNo");
            String blockNo = item.get("BlockNo");
            String elabelType = item.get("ElabelType");
            String MakeDate = item.get("MakeDate");

            String drugEnglish = item.get("DrugEnglish");
            String DrugName = item.get("DrugName");
            String DrugCode = item.get("DrugCode");
            String StockNum = item.get("StockNum");
            String LotNumber = item.get("LotNumber");
            String EffectDate = item.get("EffectDate");


            String StockQty = holder.edtInQty.getText().toString();


            //holder.drugStore.setText(drugStoreID+"-"+areaNo+"-"+blockNo+"-"+elabelType);
            holder.drugStore.setText(drugStore_SA);
            holder.drugEnglish.setText(drugEnglish);
            holder.drugName.setText(DrugName);
            holder.drugCode.setText(DrugCode);
            holder.stockNum.setText(StockNum);
            holder.lotNumber.setText(LotNumber);
            holder.txtEffectDate.setText(EffectDate);
            holder.txtElabelNumber.setText(elabelnumber);

            holder.url = globaldata.getPHP_SERVER();
            try {

                holder.url += "DBoption=" + URLEncoder.encode("IN", "UTF-8") + "&";
                holder.url += "ElabelNumber=" + URLEncoder.encode(elabelnumber, "UTF-8") + "&";
                holder.url += "DrugCode=" + URLEncoder.encode(DrugCode, "UTF-8") + "&";
                holder.url += "StoreID=" + URLEncoder.encode(drugStoreID, "UTF-8") + "&";
                holder.url += "AreaNo=" + URLEncoder.encode(areaNo, "UTF-8") + "&";
                holder.url += "BlockNo=" + URLEncoder.encode(blockNo, "UTF-8") + "&";
                holder.url += "LotNumber=" + URLEncoder.encode(LotNumber, "UTF-8") + "&";
                holder.url += "MakeDate=" + URLEncoder.encode(MakeDate, "UTF-8") + "&";
                holder.url += "EffectDate=" + URLEncoder.encode(EffectDate, "UTF-8") + "&";
                //holder.url += "StockQty=" + URLEncoder.encode(StockQty, "UTF-8") + "&";
                holder.url += "StoreType=" + URLEncoder.encode(elabelType, "UTF-8") + "&";
                holder.url += "UserID=" + URLEncoder.encode(globaldata.getLoginUserID(), "UTF-8") + "&";


                Log.d("ITEM的URL", holder.url);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundColor(ContextCompat.getColor(getContext(), com.google.android.material.R.color.design_default_color_primary_dark));

                    Toast.makeText(getContext(), elabelnumber, Toast.LENGTH_SHORT).show();
                    String url = globaldata.getPHP_SERVER();


                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }, 500);

                    // 其他处理...
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
                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(v.getContext(), drugStore_SA + " 進行亮燈", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });
            holder.btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String StockQty = holder.edtInQty.getText().toString();
                    String CodeID = holder.selectedId;
                    try {
                        holder.url += "StockQty=" + URLEncoder.encode(StockQty, "UTF-8") + "&";
                        holder.url += "ReMark_CodeID=" + URLEncoder.encode(CodeID, "UTF-8") + "&";
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return; // Abort the onClick operation if encoding fails
                    }

                    sendGET(holder.url, new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONArray response) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Display the Toast message
                                    Toast.makeText(v.getContext(), drugStore_SA + " 收入 " + StockQty + " 單位", Toast.LENGTH_SHORT).show();
                                }
                            });

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


    private void get_Search_Item_BY_DrugCode() {
        String url = globaldata.getPHP_SERVER();
        String Search_KEY = edtDrugLabel.getText().toString();

        try {
            url += "DBoption=Search_BY_DrugCode" + "&";
            url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
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
                            String MakeDate = jsonObject.getString("MakeDate");


                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugStoreID", storeID);
                            item.put("AreaNo", areaNo);
                            item.put("BlockNo", blockNo);
                            item.put("ElabelType", elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);
                            item.put("EffectDate", EffectDate);
                            item.put("MakeDate", MakeDate);

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

    private void get_Search_Item_BY_DrugEnglish() {
        String url = globaldata.getPHP_SERVER();
        //String DrugLabel = edtDrugLabel.getText().toString();
        String Search_KEY = edtDrugLabel.getText().toString();
        try {
            url += "DBoption=Search_BY_DrugEnglish" + "&";
            //url += "DrugLabel=" + URLEncoder.encode(DrugLabel, "UTF-8") + "&";
            url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
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
                            String MakeDate = jsonObject.getString("MakeDate");


                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugStoreID", storeID);
                            item.put("AreaNo", areaNo);
                            item.put("BlockNo", blockNo);
                            item.put("ElabelType", elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);
                            item.put("EffectDate", EffectDate);
                            item.put("MakeDate", MakeDate);

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

    private void get_Search_Item_BY_DrugName() {
        String url = globaldata.getPHP_SERVER();
        String DrugLabel = edtDrugLabel.getText().toString();
        String Search_KEY = edtDrugLabel.getText().toString();
        try {
            url += "DBoption=Search_BY_DrugName" + "&";
            //url += "DrugLabel=" + URLEncoder.encode(DrugLabel, "UTF-8") + "&";
            url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
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
                            String MakeDate = jsonObject.getString("MakeDate");


                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugStoreID", storeID);
                            item.put("AreaNo", areaNo);
                            item.put("BlockNo", blockNo);
                            item.put("ElabelType", elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);
                            item.put("EffectDate", EffectDate);
                            item.put("MakeDate", MakeDate);

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

    private void get_Search_Item() {
        String url = globaldata.getPHP_SERVER();
        String DrugLabel = edtDrugLabel.getText().toString().replaceAll("[\n\r]", "");


        try {
            url += "DBoption=Search_BY_Drug_Label" + "&";
            url += "DrugLabel=" + URLEncoder.encode(DrugLabel, "UTF-8") + "&";
            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        List<HashMap<String, String>> searchData = new ArrayList<>();

//                        if(response != null && response.length()>0){
//                            JSONObject jsonObject = response.getJSONObject(0);
//                            String message = jsonObject.getString("message");
//                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//                        }

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
                            String MakeDate = jsonObject.getString("MakeDate");


                            HashMap<String, String> item = new HashMap<>();
                            item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                            item.put("DrugStoreID", storeID);
                            item.put("AreaNo", areaNo);
                            item.put("BlockNo", blockNo);
                            item.put("ElabelType", elabelType);
                            item.put("DrugName", drugName);
                            item.put("DrugCode", drugCode);
                            item.put("DrugEnglish", drugEnlglish);
                            item.put("StockNum", StokQty);
                            item.put("LotNumber", LotNumber);
                            item.put("elabelNumber", elabelNumber);
                            item.put("message", message);
                            item.put("EffectDate", EffectDate);
                            item.put("MakeDate", MakeDate);

                            searchData.add(item);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                makeData(searchData);
                                //Toast.makeText(getContext(), "message", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


//                public void onFailure(JSONArray response) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeData(List<HashMap<String, String>> searchData) {
        arrayList.clear();
        arrayList.addAll(searchData);
        Log.d("TAG", "makeData: " + searchData);

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
        void onSuccess(JSONArray response);
    }
}


