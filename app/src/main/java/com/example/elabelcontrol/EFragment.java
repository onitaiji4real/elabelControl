package com.example.elabelcontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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


import com.android.volley.VolleyError;

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


public class EFragment extends Fragment {
    MyListAdapter myListAdapter;
    Spinner spinner;
    Button btnStatus, btnLabelSearch, btnNewDrugIN, btnSearchDrugCode, btnSearchDrugEnglish, btnSearchDrugName;
    EditText edtDrugLabel;
    RecyclerView mRecyclerView;

    ArrayList<HashMap<String, String>> arrayList;


    RadioButton radSearchLabel, radSearchCode, radSearchEnglish, radSearchName;
    EditText edtDrugStore, edtAreaNo, edtBlockNo, edtBlockType, edtDrugCode, edtInQty;

    private GlobalData globaldata;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.layout_drug_in, container, false);
//      globaldata = (GlobalData) getActivity().getApplicationContext();

//        globaldata = (GlobalData) getActivity().getApplicationContext();
        globaldata = GlobalData.getInstance();


        edtAreaNo = view.findViewById(R.id.edtAreaNo);
        edtBlockNo = view.findViewById(R.id.edtBlockNo);
        edtBlockType = view.findViewById(R.id.edtBlockType);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugLabel = view.findViewById(R.id.edtDrugLabel);
        edtDrugLabel.requestFocus();

        btnStatus = view.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(onChangeMode);

        globaldata.getPHP_LOGIN_SERVER();

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
            private String BLINK_URL = "";

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


            holder.url = globaldata.getPHP_IN_SERVER();
            holder.BLINK_URL=globaldata.getPHP_functionClass_Server();
            Log.d("TEST","get"+globaldata.getLoginUserID());

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

                holder.BLINK_URL += "DBoption="+URLEncoder.encode("ITEM_BLINK","UTF-8")+"&";
                holder.BLINK_URL += "ElabelNumber=" + URLEncoder.encode(elabelnumber, "UTF-8") + "&";


                Log.d("ITEM的URL", holder.url);
                Log.d("BLINK的URL",holder.BLINK_URL);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundColor(ContextCompat.getColor(getContext(), com.google.android.material.R.color.design_default_color_primary_dark));

                    Toast.makeText(getContext(), elabelnumber, Toast.LENGTH_SHORT).show();
                    String url = globaldata.getPHP_SERVER();

//                    String BLINK_URL = globaldata.getPHP_SERVER();


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




                    send_BLINK_GET(holder.BLINK_URL, new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONArray response) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Display the Toast message
                                    Toast.makeText(v.getContext(), drugStore_SA +"進行亮燈", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        @Override
                        public void onFailure(VolleyError error) {

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
                        @Override
                        public void onFailure(VolleyError error) {


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
        String url = globaldata.getPHP_SEARCH_SERVER();
        String Search_KEY = edtDrugLabel.getText().toString();

        if (!TextUtils.isEmpty(Search_KEY)) {
            try {
                url += "DBoption=Search_BY_DrugCode" + "&";
                url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        JSONObject resultObject = response.getJSONObject(0);
                        boolean result = resultObject.getBoolean("Result");
                        Log.d("Search Result", "Search Result = " + result);

                        if (result) {
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
                                String drugEnglish = jsonObject.getString("DrugEnglish");
                                String MakeDate = jsonObject.getString("MakeDate");

                                HashMap<String, String> item = new HashMap<>();
                                item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                                item.put("DrugStoreID", storeID);
                                item.put("AreaNo", areaNo);
                                item.put("BlockNo", blockNo);
                                item.put("ElabelType", elabelType);
                                item.put("DrugName", drugName);
                                item.put("DrugCode", drugCode);
                                item.put("DrugEnglish", drugEnglish);
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
                        } else {
                            String message = resultObject.getString("Response");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Handle the case when the result is false
                            // You can display an error message or take other actions
                            Log.d("Search Result", "False: " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    // Handle the case when the request fails
                }
            });
        } else {
            // When Search_KEY is empty
            // You can display an error message or take other actions
            Toast.makeText(getContext(), "請輸入關鍵字！", Toast.LENGTH_SHORT).show();
        }
    }

    private void get_Search_Item_BY_DrugEnglish() {
        String url = globaldata.getPHP_SEARCH_SERVER();
        String Search_KEY = edtDrugLabel.getText().toString();

        if (!TextUtils.isEmpty(Search_KEY)) {
            try {
                url += "DBoption=Search_BY_DrugEnglish" + "&";
                url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        JSONObject resultObject = response.getJSONObject(0);
                        boolean result = resultObject.getBoolean("Result");
                        Log.d("Search Result", "Search Result = " + result);

                        if (result) {
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
                                String drugEnglish = jsonObject.getString("DrugEnglish");
                                String MakeDate = jsonObject.getString("MakeDate");

                                HashMap<String, String> item = new HashMap<>();
                                item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                                item.put("DrugStoreID", storeID);
                                item.put("AreaNo", areaNo);
                                item.put("BlockNo", blockNo);
                                item.put("ElabelType", elabelType);
                                item.put("DrugName", drugName);
                                item.put("DrugCode", drugCode);
                                item.put("DrugEnglish", drugEnglish);
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
                        } else {
                            String message = resultObject.getString("Response");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Handle the case when the result is false
                            // You can display an error message or take other actions
                            Log.d("Search Result", "False: " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    // Handle the case when the request fails
                }
            });
        } else {
            // When Search_KEY is empty
            // You can display an error message or take other actions
            Toast.makeText(getContext(), "請輸入關鍵字！", Toast.LENGTH_SHORT).show();
        }
    }

    private void get_Search_Item_BY_DrugName() {
        String url = globaldata.getPHP_SEARCH_SERVER();
        String Search_KEY = edtDrugLabel.getText().toString();

        if (!TextUtils.isEmpty(Search_KEY)) {
            try {
                url += "DBoption=Search_BY_DrugName" + "&";
                url += "Search_KEY=" + URLEncoder.encode(Search_KEY, "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        JSONObject resultObject = response.getJSONObject(0);
                        boolean result = resultObject.getBoolean("Result");
                        Log.d("Search Result", "Search Result = " + result);

                        if (result) {
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
                                String drugEnglish = jsonObject.getString("DrugEnglish");
                                String MakeDate = jsonObject.getString("MakeDate");

                                HashMap<String, String> item = new HashMap<>();
                                item.put("DrugStore_SA", storeID + "-" + areaNo + "-" + blockNo + "-" + elabelType);
                                item.put("DrugStoreID", storeID);
                                item.put("AreaNo", areaNo);
                                item.put("BlockNo", blockNo);
                                item.put("ElabelType", elabelType);
                                item.put("DrugName", drugName);
                                item.put("DrugCode", drugCode);
                                item.put("DrugEnglish", drugEnglish);
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
                        } else {
                            String message = resultObject.getString("Response");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Handle the case when the result is false
                            // You can display an error message or take other actions
                            Log.d("Search Result", "False: " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    // Handle the case when the request fails
                }
            });
        } else {
            // When Search_KEY is empty
            // You can display an error message or take other actions
            Toast.makeText(getContext(), "請輸入關鍵字！", Toast.LENGTH_SHORT).show();
        }
    }


    private void get_Search_Item() {
        String url = globaldata.getPHP_SEARCH_SERVER();
        String DrugLabel = edtDrugLabel.getText().toString().replaceAll("[\n\r]", "");

        if (!TextUtils.isEmpty(DrugLabel)) {
            try {
                url += "DBoption=Search_BY_Drug_Label" + "&";
                url += "DrugLabel=" + URLEncoder.encode(DrugLabel, "UTF-8") + "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        JSONObject resultObject = response.getJSONObject(0);
                        boolean result = resultObject.getBoolean("Result");
                        Log.d("Search Result", "Result: " + result);

                        if (result) {
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
                        } else {
                            String message = resultObject.getString("Response");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // 处理结果为 false 的情况
                            // 可以显示错误信息或采取其他操作
                            Log.d("Search Result", "False: " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    // 处理请求失败的情况
                }
            });
        } else {
            // 当 DrugLabel 为空时的处理逻辑
            // 可以显示错误提示或采取其他操作
            Toast.makeText(getContext(), "請掃描條碼後，再按下搜尋。", Toast.LENGTH_SHORT).show();
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
    public void send_BLINK_GET(String BLINK_URL, final VolleyCallback callback) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url(BLINK_URL)
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
                    JSONObject jsonObject;

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
        //        void onSuccess(JSONObject response);
        void onSuccess(JSONArray response);
        void onFailure(VolleyError error);
    }
}


