package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.opencsv.CSVReader;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import data.Druginfo;
import data.GlobalData;
import data.Inventory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class AFragment extends Fragment {
    RecyclerView mRecyclerView;
    AFragment.MyListAdapter myListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    View view;
    Button btnClearData,btnUpdateData;
    String ServerName="test";
    String SearchAll_url = "http://192.168.5.49/"+ServerName+"/Select_All.php?";
    String GetJson;
    JSONArray array;
    RadioGroup RadG1;
    Button btn_Search;
    EditText SearchStr;
    CheckBox ch1,ch2,ch3,ch4,ch5,ch6;
    ArrayList<String> TypeStr = new ArrayList<String>();
    List<Inventory> Inventorys;
    List<Druginfo> Druginfos;
    boolean getFin;
    File dir;
    GlobalData globaldata;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle saveInstance){

        view = inflater.inflate(R.layout.layout_a,container,false);

        //設置RecycleView
        mRecyclerView = view.findViewById(R.id.recyclerView);
        btnClearData = view.findViewById(R.id.btnClearData);
        btnClearData.setOnClickListener(onClearData);
        btnUpdateData = view.findViewById(R.id.btnUpdateData);
        btnUpdateData.setOnClickListener(onUpdateData);
        /*
        RadG1 = view.findViewById(R.id.DrugType_rg);
        RadG1.check(R.id.rdo_drugcode);


        btn_Search = view.findViewById(R.id.btnSearch);
        btn_Search.setOnClickListener(Search);
        SearchStr = view.findViewById(R.id.edtSearchDrugCode);
        ch1 = view.findViewById(R.id.chk1);
        ch2 = view.findViewById(R.id.chk2);
        ch3 = view.findViewById(R.id.chk3);
        ch4 = view.findViewById(R.id.chk4);
        ch5 = view.findViewById(R.id.chk5);
        ch6 = view.findViewById(R.id.chk6);
        ch1.setOnClickListener(OnCheck);
        ch2.setOnClickListener(OnCheck);
        ch3.setOnClickListener(OnCheck);
        ch4.setOnClickListener(OnCheck);
        ch5.setOnClickListener(OnCheck);
        ch6.setOnClickListener(OnCheck);
        //Toast.makeText(view.getContext(), GetJson, Toast.LENGTH_SHORT).show();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));*/

        Inventorys = new ArrayList<Inventory>();
        CSVReadInventory();

        Druginfos = new ArrayList<Druginfo>();
        CSVReadDrugInfo();

        myListAdapter = new AFragment.MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);
        makeData();
        /*
        getFin=false;
        sendGET(SearchAll_url, new AFragment.VolleyCallback(){
            @Override
            public void onSuccess() {
                getFin=true;
            }
        });
        while(!getFin)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        myListAdapter.notifyDataSetChanged();

        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue_RURI));
        swipeRefreshLayout.setOnRefreshListener(()->{
            SearchStr.setText("");
            Toast.makeText(view.getContext(), "顯示全部資料", Toast.LENGTH_SHORT).show();
            arrayList.clear();
            getFin=false;
            sendGET(SearchAll_url, new BFragment.VolleyCallback(){
                @Override
                public void onSuccess() {
                    getFin=true;
                }
            });
            while(!getFin)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            myListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        });
        dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);*/
        return view;
    }

    public void sendGET(String Url,final AFragment.VolleyCallback callback) {
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
                GetJson = response.body().string();
                try {
                    array = new JSONArray(GetJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if(! array.getJSONObject(0).getString("Formula").equals("no anses")) makeData();
                } catch (JSONException e) {
                    makeData();
                }
                callback.onSuccess();

            }
        });
    }
    private View.OnClickListener onClearData = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d("clear","clear");
            Inventorys.clear();
            try {
            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            String uniqueFileName = "/inventory.csv";
            File file = new File(directory.getAbsolutePath(), uniqueFileName);
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
            Toast.makeText(getActivity(), "File Exported Successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            arrayList.clear();
            myListAdapter.notifyDataSetChanged();
        }
    };
    private View.OnClickListener onUpdateData = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            BFragment bFragment = new BFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_container,bFragment,"B");
            fragmentTransaction.commit();
        }
    };
    public interface VolleyCallback{
        void onSuccess();
    }
    private void makeData() {
        if(arrayList.stream().count()!=0){
            arrayList.clear();
        }
        Integer count = 0;
        for (int i = 0; i < Inventorys.size(); i++) {
            String DrugCode = Inventorys.get(i).getDrugCode();
            String DrugEnglish = "";
            Log.d("Drugonfos count", String.valueOf(Druginfos.stream().count()));

            if(Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(DrugCode))).count() == 0)
            {
                Log.d("Error","無結果");
            }
            else
            {
                List<Druginfo> MatchDruginfo = Druginfos.stream().filter(druginfo -> (druginfo.getDrugCode().equals(DrugCode))).collect(Collectors.toList());
                DrugEnglish = MatchDruginfo.get(0).getDrugEnglish();
            }


            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("DrugStore_SA",Inventorys.get(i).getStoreID()+"-"+Inventorys.get(i).getAreaNo());
            hashMap.put("DrugStore_B",Inventorys.get(i).getBlockNo()+"("+Inventorys.get(i).getBlockType()+")");
            hashMap.put("DrugName",DrugEnglish+"("+DrugCode+")");
            hashMap.put("InventoryTime",Inventorys.get(i).getInvDate()+" "+Inventorys.get(i).getInvTime());
            hashMap.put("DrugQty","盤點量:"+Inventorys.get(i).getInventoryQty()+"("+Inventorys.get(i).getAdjQty()+")");
            arrayList.add(count,hashMap);
            count++;
        }


        /*
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = array.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("Code",jsonObject.getString("DrugCode"));
                hashMap.put("Name",jsonObject.getString("DrugName"));
                hashMap.put("Type",jsonObject.getString("DrugType"));
                hashMap.put("NHI_Code",jsonObject.getString("NHI_Code"));
                hashMap.put("ImageNum",jsonObject.getString("ImageNum"));
                hashMap.put("Version",jsonObject.getString("Version"));
                arrayList.add(i,hashMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        */
    }

    public void CSVReadInventory(){
        try{
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            Log.d("dir", dir.getAbsolutePath());
            // String path =
            CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath()+"/Inventory.csv"));
            String[] nextLine;

            int i = 0;
            String[] record = null;
            while ((record = reader.readNext()) != null) {
                Inventory inventory = new Inventory();
                inventory.setInvDate(record[0]);
                inventory.setDrugCode(record[1]);
                inventory.setMakerID(record[2]);
                inventory.setStoreID(record[3]);
                inventory.setAreaNo(record[4]);
                inventory.setBlockNo(record[5]);
                inventory.setBlockType(record[6]);
                inventory.setLotNumber(record[7]);
                inventory.setStockQty(record[8]);
                inventory.setInventoryQty(record[9]);
                inventory.setAdjQty(record[10]);
                inventory.setShiftNo(record[11]);
                inventory.setInvTime(record[12]);
                inventory.setUserID(record[13]);
                inventory.setRemark(record[14]);
                Inventorys.add(inventory);
            }
            reader.close();
        } catch (IOException e) {
            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
            e.printStackTrace();
        }
    }


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
                Druginfos.add(druginfo);
            }
            reader.close();
        } catch (IOException e) {
            // reader在初始化時可能遭遇問題。記得使用try/catch處理例外情形。
            e.printStackTrace();
        }
    }

    private View.OnClickListener Search = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Toast.makeText(view.getContext(), "搜尋", Toast.LENGTH_SHORT).show();
            String Search_str = SearchStr.getText().toString();
            if("".equals(Search_str))
            {
                Toast.makeText(view.getContext(), "搜尋欄不可為空", Toast.LENGTH_SHORT).show();
            }
            else
            {
                int n = RadG1.getCheckedRadioButtonId();
                //Toast.makeText(view.getContext(), String.valueOf(n), Toast.LENGTH_SHORT).show();
                String SearchUrl="";
                switch (n){
                    /*
                    case R.id.rdo_drugcode:
                        SearchUrl = "http://192.168.5.49/"+ServerName+"/SelectByDrugCode.php?DrugCode="+Search_str;
                        break;
                    case R.id.rdo_drugname:
                        SearchUrl = "http://192.168.5.49/"+ServerName+"/SelectByDrugName.php?DrugName="+Search_str;
                        break;
                    case R.id.rdo_NHI_code:
                        SearchUrl = "http://192.168.5.49/"+ServerName+"/SelectByNHI_Code.php?NHI_Code="+Search_str;
                        break;*/
                }
                arrayList.clear();
                getFin=false;

                sendGET(SearchUrl, new AFragment.VolleyCallback(){
                    @Override
                    public void onSuccess() {
                        getFin=true;
                    }
                });
                while(! getFin) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                myListAdapter.notifyDataSetChanged();
                Toast.makeText(view.getContext(), "搜尋完成", Toast.LENGTH_SHORT).show();
                hideKeyboard(view.getContext());
            }

        }
    };

    private Bitmap getResizedBitmap(String imagePath) {
        final int MAX_WIDTH = 100; // 新圖的寬要小於等於這個值

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //只讀取寬度和高度
        BitmapFactory.decodeFile(imagePath, options);
        int width = options.outWidth, height = options.outHeight;

        // 求出要縮小的 scale 值，必需是2的次方，ex: 1,2,4,8,16...
        int scale = 1;
        while (width > MAX_WIDTH * 2) {
            width /= 2;
            height /= 2;
            scale *= 2;
        }

        // 使用 scale 值產生縮小的圖檔
        BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
        scaledOptions.inSampleSize = scale;
        Bitmap scaledBitmap = BitmapFactory.decodeFile(imagePath, scaledOptions);

        Matrix matrix = new Matrix(); // 產生縮圖需要的參數 matrix

        // 產生縮小後的圖
        Bitmap resizedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }
    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    private class MyListAdapter extends RecyclerView.Adapter<AFragment.MyListAdapter.ViewHolder>{


        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView tvDrugStore_SA,tvDrugStore_B,tvDrugName,tvDrugQty,tvInventoryTime;
            private String type;
            ImageView ImageShower;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDrugStore_SA = itemView.findViewById(R.id.DrugStore_SA);
                tvDrugStore_B = itemView.findViewById(R.id.DrugStore_B);
                tvDrugName = itemView.findViewById(R.id.DrugName);
                tvDrugQty = itemView.findViewById(R.id.DrugQty);
                tvInventoryTime = itemView.findViewById(R.id.InventoryTime);
            }
        }
        @NonNull
        @Override
        public AFragment.MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inventory_view_item,parent,false);
            return new AFragment.MyListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AFragment.MyListAdapter.ViewHolder holder, int position) {
            hideKeyboard(view.getContext());
            holder.tvDrugStore_SA.setText(arrayList.get(position).get("DrugStore_SA"));
            holder.tvDrugStore_B.setText(arrayList.get(position).get("DrugStore_B"));
            holder.tvDrugName.setText(arrayList.get(position).get("DrugName"));
            holder.tvDrugQty.setText(arrayList.get(position).get("DrugQty"));
            holder.tvInventoryTime.setText(arrayList.get(position).get("InventoryTime"));
/*
            holder.itemView.setOnClickListener((v)->{
                //Toast.makeText(view.getContext(),holder.tvCode.getText(), Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("tvCode",holder.tvCode.getText().toString());
                bundle.putString("tvVersion",holder.Version.getText().toString());
                //bundle.putString("tvID",holder.DrugID);!!!!!!!!!!!!!!!!!
                Intent intent = new Intent();
                intent.setClass(view.getContext(),DrugDetail.class);
                intent.putExtras(bundle);
                startActivity(intent);

            });
*/
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


    }

}

