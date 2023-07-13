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

import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.HandlerThread;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import com.opencsv.CSVReader;

import org.jetbrains.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


import data.GlobalData;
import data.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Login extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int PROGRESS_DIALOG_TYPE = 0;

    private Button btnLogin, btnDownloadData, btnScan;
    private List<User> users;
    private TextView edtAccount, edtPassword, edtScannedPassword, txtNetworkConnectStatus, txtDBConnectStatus;
    private GlobalData globalData;

    private ProgressDialog progressDialog;
    private checkConnection_Class checkConnectionClass; // 宣告成員變數


//    TelephonyManager mTelManager;
//    String imei;
//    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        globalData = new GlobalData(this.getApplicationContext());

        requestWriteExternalStoragePermission();
        txtNetworkConnectStatus = findViewById(R.id.txtNetworkConnectStatus);
        txtDBConnectStatus = findViewById(R.id.txtDBConnectStatus);

        initializeViews();
        btnLogin.setOnClickListener(onLogin);
        edtAccount.requestFocus();

        accountAfterScanListener();

        // 建立實例並開始檢查連線
        checkConnectionClass = new checkConnection_Class(); //初始化
        checkConnectionClass.startRepeatingTask();

    }

    @Override
    protected void onDestroy() {// 在 Activity 被銷毀時停止任務以避免內存洩漏
        super.onDestroy();
        checkConnectionClass.startRepeatingTask();
    }


    private View.OnClickListener onLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String encodePassword = PasswordEncoder.encodePassword(edtPassword.getText().toString());
            Log.d("ENCODE PASSWORD", encodePassword);
            String url = globalData.getPHP_LOGIN_SERVER();

            try {
                url += "DBoption=" + URLEncoder.encode("LOGIN", "UTF-8") + "&";
                url += "Account=" + URLEncoder.encode(edtAccount.getText().toString(), "UTF-8") + "&";
                url += "Password=" + URLEncoder.encode(encodePassword, "UTF-8") + "&";

                sendGET(url, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {


                        try {
                            String success = response.getString("success");
                            String message = response.getString("message");

                            if (success == "true") {

                                //Log.d("TAG",message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        globalData.setLoginUserID(edtAccount.getText().toString());
                                        Log.d("TAG", globalData.getLoginUserID());
                                        globalData.setLoginUserName(globalData.getLoginUserID());
                                        //Log.d("TAG", globalData.getLoginUserName());
                                        startActivity(new Intent(Login.this, FragmentActivity.class));
                                        Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    public void onFailure(VolleyError error) {

                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    //掃員工證條碼進入 設定為長度等於11才執行
    private void accountAfterScanListener() {
        edtAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //目前不使用到
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //目前不使用到
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 11) {
                    scanLogin();
                }
            }
        });
    }

    //掃描登入的驗證
    public void scanLogin() {
        String account = edtAccount.getText().toString();
        String url = globalData.getPHP_LOGIN_SERVER();
        try {
            url += "DBoption=" + URLEncoder.encode("SCAN_LOGIN", "UTF-8") + "&";
            url += "Account=" + URLEncoder.encode(account, "UTF-8") + "&";


            sendGET(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {


                    try {
                        String success = response.getString("success");
                        String message = response.getString("message");

                        if (success == "true") {

                            //Log.d("TAG",message);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    globalData.setLoginUserID(edtAccount.getText().toString());
                                    Log.d("TAG", globalData.getLoginUserID());
                                    globalData.setLoginUserName(edtAccount.getText().toString());
                                    Log.d("TAG", globalData.getLoginUserName());
                                    startActivity(new Intent(Login.this, FragmentActivity.class));
                                    Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
                                }
                            });

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                public void onFailure(VolleyError error) {

                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setConnectStatus(boolean server, int textViewId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(textViewId);

                if (server) {
                    textView.setText("已連線");
                    textView.setTextColor(Color.parseColor("#ff99cc00"));
                } else {
                    textView.setText("未連線");
                    textView.setTextColor(Color.parseColor("#ffcc0000"));
                }
            }
        });
    }


    public class checkConnection_Class {
        private int repeatCount = 0;
        private HandlerThread handlerThread;
        private Handler handler;
        private boolean SERVER_STATUS;
        private boolean DB_CONNECT_STATUS;

        public checkConnection_Class() {
            handlerThread = new HandlerThread("CheckConnection_Thread");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkConnection(); // 执行检查连接
            }
        };

        public void startRepeatingTask() {
            handler.post(runnable);
        }

        public void stopRepeatingTask() {
            handler.removeCallbacks(runnable);
            handlerThread.quitSafely();
        }

        public void checkConnection() {
            boolean NetworkConnectStatus = globalData.getNetworkConnectStatus(); // Check if WiFi connect

            // 更新UI状态，表示正在尝试连接
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textViewNetwork = findViewById(R.id.txtNetworkConnectStatus);
                    TextView textViewDB = findViewById(R.id.txtDBConnectStatus);
                    textViewNetwork.setText("連線中...");
                    textViewDB.setText("連線中...");
                    textViewNetwork.setTextColor(Color.parseColor("#ff9a00")); // Set Connecting text
                    textViewDB.setTextColor(Color.parseColor("#ff9a00")); // Set Connecting text
                }
            });

            if (NetworkConnectStatus) {
                String url = globalData.getPHP_CONNECTION_SERVER()+"DBoption=getConnectionStatus";

                sendGET(url, new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            if (response.has("SERVER_STATUS") && response.has("DB_CONNECT_STATUS")) { //Check Sever Status n DB Status

                                SERVER_STATUS = response.getBoolean("SERVER_STATUS");
                                DB_CONNECT_STATUS = response.getBoolean("DB_CONNECT_STATUS");

                                String toastMessage;
                                if (SERVER_STATUS && DB_CONNECT_STATUS) {
                                    toastMessage = "成功連線。";
                                    repeatCount = 3;
                                    handler.removeCallbacks(runnable); //
                                } else if (repeatCount < 3) {
                                    toastMessage = "連線超時，10秒後系統將嘗試重新連線一次。";
                                    repeatCount++;
                                    handler.postDelayed(runnable, 10000); //Delay 10 sec.
                                } else {
                                    toastMessage = "連線失敗，請檢查網路設定或洽詢開發商。";
                                }

                                // 更新UI
                                final String finalToastMessage = toastMessage;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setConnectStatus(SERVER_STATUS, R.id.txtNetworkConnectStatus);
                                        setConnectStatus(DB_CONNECT_STATUS, R.id.txtDBConnectStatus);
                                        Toast.makeText(getApplicationContext(), finalToastMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(VolleyError error) {
                        SERVER_STATUS = false;
                        DB_CONNECT_STATUS = false;
                        handler.postDelayed(runnable, 10000); //Delay 10 sec.
                    }
                });
            } else {
                SERVER_STATUS = false;
                DB_CONNECT_STATUS = false;
                // 更新UI状态
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setConnectStatus(SERVER_STATUS, R.id.txtNetworkConnectStatus);
                        setConnectStatus(DB_CONNECT_STATUS, R.id.txtDBConnectStatus);
                        Toast.makeText(getApplicationContext(), "請連接至無線網路！", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }


    //詢問裝置的讀取存取裝置權限
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
//        btnLogin.setOnClickListener(view -> {
//            hideKeyboard(view.getContext());
//
//            String encodePassword = PasswordEncoder.encodePassword(edtPassword.getText().toString());
//            Log.d("ENCODE PASSWORD", encodePassword);
//
//
//            String loginResult = loginCheck(edtAccount.getText().toString(), edtPassword.getText().toString());
//            if (loginResult.isEmpty()) {
//                Toast.makeText(Login.this, globalData.getLoginUserID() + " 登入成功", Toast.LENGTH_LONG).show();
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

    public void sendGET(String Url, final VolleyCallback callback) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(7, TimeUnit.SECONDS)  // connect timeout
                .readTimeout(7, TimeUnit.SECONDS)     // read timeout
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
                Log.e("TAG", "Error in get request: " + e.getMessage());
                callback.onFailure(new VolleyError(e)); //
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                try {
                    String responseData = response.body().string();
                    //JSONArray jsonArray;
                    JSONObject jsonObject;

                    try {
                        jsonObject = new JSONObject(responseData);
//                        callback.onSuccess(jsonArray);
                        callback.onSuccess(jsonObject);
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
        void onSuccess(JSONObject response);

        void onFailure(VolleyError error);
    }

    public static class PasswordEncoder {
        public static String encodePassword(String password) {

            try {
                //建立SHA-256加密器
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                //將密碼轉位元組陣列
                byte[] encodeedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

                //byte轉16進制
                StringBuilder hexString = new StringBuilder();

                for (byte b : encodeedHash) {
                    String hex = Integer.toHexString(0xff & b);

                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }

                return hexString.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }

        }
    }
}


