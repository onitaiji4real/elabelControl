package com.example.elabelcontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DrugLabelSearch_Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 在這裡設置並返回你的 DrugLabelSearch Fragment 的布局
        View view = inflater.inflate(R.layout.drug_label_search_fragment, container, false);
        // 初始化你的介面元素和邏輯
        return view;
    }
}

