package com.example.elabelcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class CustomGrid extends BaseAdapter {
    private Context context;
    private final String[] LotNumberArray;
    private final String[] StockQtyArray;

    public CustomGrid(Context context, String[] LotNumberArray, String[] StockQtyArray) {
        this.context = context;
        this.LotNumberArray = LotNumberArray;
        this.StockQtyArray = StockQtyArray;
    }

    @Override
    public int getCount() {
        return LotNumberArray.length;
    }

    @Override
    public String getItem(int position) {

        return this.StockQtyArray[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        // Context 動態放入mainActivity
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(context);
            // 將grid_single 動態載入(image+text)
            grid = layoutInflater.inflate(R.layout.lotnumber, null);
            TextView textLotnumber = (TextView) grid.findViewById(R.id.gridLotnumber);
            EditText textStockQty = (EditText) grid.findViewById(R.id.gridStockQty);
            textLotnumber.setText(LotNumberArray[position]);
            textStockQty.setText(StockQtyArray[position]);
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}
