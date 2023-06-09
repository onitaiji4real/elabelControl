package com.example.elabelcontrol;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.opencsv.CSVReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import data.GlobalData;
import data.User;

public class Login extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int PROGRESS_DIALOG_TYPE = 0;

    private Button btnLogin, btnDownloadData,btnScan;
    private List<User> users;
    private TextView edtAccount, edtPassword, edtScannedPassword;
    private GlobalData globalData;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        requestWriteExternalStoragePermission();

        initializeViews();

        globalData = (GlobalData) getApplicationContext();

        users = new ArrayList<>();
        try {
            openCSVUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            boolean permissionGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;

            if (!permissionGranted) {
                requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void initializeViews() {
        edtAccount = findViewById(R.id.edtAccount);
        edtPassword = findViewById(R.id.edtPassword);
//        edtScannedPassword = findViewById(R.id.edtScannedPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(view -> {
            hideKeyboard(view.getContext());
            String loginResult = loginCheck(edtAccount.getText().toString(), edtPassword.getText().toString());
            if (loginResult.isEmpty()) {
                Toast.makeText(Login.this, globalData.getLoginUserID() + " 登入成功", Toast.LENGTH_LONG).show();
                startActivity(new Intent(view.getContext(), FragmentActivity.class));
            } else {
                Toast.makeText(Login.this, loginResult, Toast.LENGTH_LONG).show();
            }
        });
//        btnScan = findViewById(R.id.btnScan);
//        btnScan.setOnClickListener(view -> {
//            hideKeyboard(view.getContext());
//            String scannedQRCodeData = edtScannedPassword.getText().toString();
//            String loginResult = processScannedQRCode(scannedQRCodeData);
//
//            // 处理登录验证结果
//            if (loginResult.equals("")) {
//                Toast.makeText(Login.this, globalData.getLoginUserID() + " " + globalData.getLoginUserName() + " 登录成功", Toast.LENGTH_LONG).show();
//                startActivity(new Intent(view.getContext(), FragmentActivity.class));
//            } else {
//                Toast.makeText(Login.this, loginResult, Toast.LENGTH_LONG).show();
//            }
//        });



        btnDownloadData = findViewById(R.id.btnDownloadData);
        btnDownloadData.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        btnDownloadData.setEnabled(false);
        //btnDownloadData.setOnClickListener(view -> new DownloadFileTask().execute("user.csv", "codetable.csv", "drugstore.csv", "druginfo.csv"));
    }

    private String loginCheck(String account, String password) {
        if (users.stream().noneMatch(user -> user.getUserID().equals(account))) {
            return "無此使用者!!" + account + "不存在";
        } else if (users.stream().noneMatch(user -> user.getUserID().equals(account) && user.getPassword().equals(password))) {
            return "密碼錯誤!! " + globalData.getLoginUserID() + " " + globalData.getLoginUserName();
        } else {
            Optional<User> loginUser = users.stream()
                    .filter(user -> user.getUserID().equals(account) && user.getPassword().equals(password))
                    .findFirst();
            loginUser.ifPresent(user -> {
                globalData.setLoginUserID(user.getUserID());
                globalData.setLoginUserName(user.getUserName());
            });
            return "";
        }
    }
//    private String processScannedQRCode(String qrCodeData) {
//        String[] data = qrCodeData.split(",");
//        if (data.length >= 2) {
//            String account = data[0].trim();
//            String password = data[1].trim();
//            return loginCheck(account, password);
//        } else {
//
//            return "QR 码数据无效 " ;
//        }
//    }

    private void openCSVUser() throws IOException {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        Log.d("dir", dir.getAbsolutePath());
        CSVReader reader = new CSVReader(new FileReader(dir.getAbsolutePath() + "/user.csv"));
        users.clear();

        String[] record;
        while ((record = reader.readNext()) != null) {
            User user = new User();
            user.setUserID(record[0]);
            user.setUserName(record[1]);
            user.setPassword(record[2]);
            user.setUGroupID(record[3]);
            user.setSetUserID(record[4]);
            user.setSetTime(record[5]);
            user.setStartDate(record[6]);
            user.setEndDate(record[7]);
            user.setStartTime(record[8]);
            user.setEndTime(record[9]);
            user.setUpdated(record[10]);
            users.add(user);
        }
        reader.close();
    }

    private void hideKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private class DownloadFileTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... fileNames) {
            int count;
            try {
                for (String fileName : fileNames) {
                    URL url = new URL(globalData.Server + fileName);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    int lengthOfFile = connection.getContentLength();

                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + fileName);

                    byte data[] = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            try {
                openCSVUser();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            //dismissDialog(progress_bar_type);
        }
    }
}

