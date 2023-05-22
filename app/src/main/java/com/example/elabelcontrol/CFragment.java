package com.example.elabelcontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
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

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import data.DrugInOut;
import data.Druginfo;
import data.Drugstore;
import data.GlobalData;
import data.Inventory;

public class CFragment extends Fragment {
    EditText edtElabelNumber,edtDrugCode,edtDrugEnglish,edtInQty,edtDrugStore;
    Button btnSumit,btnLight,btnGetDrugStore;
    GlobalData globaldata;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_c, container, false);
        globaldata = (GlobalData)getActivity().getApplicationContext();
        edtElabelNumber = view.findViewById(R.id.edtElabelNumber);
        edtDrugCode = view.findViewById(R.id.edtDrugCode);
        edtDrugEnglish = view.findViewById(R.id.edtDrugEnglish);
        edtDrugStore = view.findViewById(R.id.edtDrugStore);
        edtInQty = view.findViewById(R.id.edtInQty);

        btnSumit = view.findViewById(R.id.btnSumit);
        btnSumit.setOnClickListener(OnSumit);
        btnGetDrugStore = view.findViewById(R.id.btnGetDrugStore);
        btnGetDrugStore.setOnClickListener(OnGetDrugStore);

        Spinner spinner = view.findViewById(R.id.spOutCode);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(view.getContext(),
                        R.array.InCode,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2, false);

        spinner.setOnItemSelectedListener(spnOnItemSelected);
        CodeID = "A";
        /*
        tvhello= (TextView) findViewById(R.id.hello);
        tvhello.setText("選項:"+spinner.getSelectedItem().toString());*/

        Drugstores = new ArrayList<Drugstore>();
        CSVReadDrugStore();

        DrugInOuts = new ArrayList<DrugInOut>();

        Druginfos = new ArrayList<Druginfo>();
        CSVReadDrugInfo();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                Log.d("drugstore",record[6]);
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
    public static void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    private View.OnClickListener OnSumit = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            try {
                exportDataToCSV();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private View.OnClickListener OnGetDrugStore = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard(v.getContext());
            String ElabelNumber  = edtElabelNumber.getText().toString();
            if(Drugstores.stream().count()==0){
                //Log.d("Drugstores", "Drugstores 為 0");
            }
            Log.d("ElabelNumber", ElabelNumber);
            if(Drugstores.stream().filter(drugstore ->(drugstore.getElabelNumber().equals(ElabelNumber) )).count()==0) {
                //無此條碼資料
                Log.d("error", "無結果");
            }
            else {
                List<Drugstore> MatchDrugstore = Drugstores.stream().filter(drugstore -> (drugstore.getElabelNumber().equals(ElabelNumber))).collect(Collectors.toList());
                if(Druginfos.stream().filter(druginfo ->(druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).count()>0) {
                    List<Druginfo> MatchDruginfo = Druginfos.stream().filter(druginfo ->(druginfo.getDrugCode().equals(MatchDrugstore.get(0).getDrugCode()))).collect(Collectors.toList());
                    edtDrugEnglish.setText(MatchDruginfo.get(0).getDrugEnglish());
                }
                edtDrugCode.setText(MatchDrugstore.get(0).getDrugCode());
                edtDrugStore.setText(MatchDrugstore.get(0).getStoreID()+"-"+MatchDrugstore.get(0).getAreaNo()+"-"+MatchDrugstore.get(0).getBlockNo()+"-"+MatchDrugstore.get(0).getBlockType());
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

    private  Spinner.OnItemSelectedListener spnOnItemSelected = new Spinner.OnItemSelectedListener() {

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
}
