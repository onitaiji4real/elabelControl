<?php
include("pda_functionClass.php"); //匯入資料庫連線檔案
$func_Collect = new func_Collect();

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];
    switch ($DBoption) {
        case "LOGIN": {
                if (isset($_GET["Account"]) && isset($_GET["Password"])) {
                    $account = $_GET["Account"];
                    $password = $_GET["Password"];

                    // 使用參數化查詢來避免 SQL 注入攻擊
                    $stmt = $connection->prepare("SELECT Password FROM baiguo_demo.user WHERE UserID = ?");
                    $stmt->bind_param("s", $account);
                    $stmt->execute();
                    $stmt->bind_result($storedPassword);
                    $stmt->fetch();

                    // 在這裡進行密碼比對
                    if ($storedPassword === $password) {
                        // 密碼正確
                        $response["success"] = true;
                        $response["message"] = $account . " 成功登入";


                    } else {
                        // 密碼錯誤
                        $response["success"] = false;
                        $response["message"] = "無此使用者或密碼錯誤";
                    }

                    $stmt->close();
                } else {
                    // 未提供帳號或密碼
                    $response["success"] = false;
                    $response["message"] = "請輸入帳號密碼";
                }

                // 回傳 JSON 格式的資料
                // echo json_encode($response);

                $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
                echo $json;
                break;
            }
        case "SCAN_LOGIN": {
                if (isset($_GET["Account"])) {
                    $account = $_GET["Account"];


                    // 使用參數化查詢來避免 SQL 注入攻擊
                    $stmt = $connection->prepare("SELECT UserID FROM baiguo_demo.user WHERE UserID = ?");
                    $stmt->bind_param("s", $account);
                    $stmt->execute();
                    $stmt->store_result();
                    $num_rows = $stmt->num_rows;

                    
                   


                    //echo "你好" . $num_rows;
                    // 在這裡進行密碼比對
                    if ($num_rows == 1) {
                        // 密碼正確
                        $response["success"] = true;
                        $response["message"] = $account . " 成功登入";
                    } else {
                        // 密碼錯誤或無此使用者
                        if ($num_rows == 0) {
                            // 無此使用者
                            $response["success"] = false;
                            $response["message"] = "無此使用者";
                            
                        } else {
                            // 密碼錯誤
                            $response["success"] = false;
                            $response["message"] = "密碼錯誤";
                        }
                    }

                    $stmt->close();
                } else {
                    // 未提供帳號或密碼
                    $response["success"] = false;
                    $response["message"] = "請再輸入一次";
                }

                // 回傳 JSON 格式的資料
                // echo json_encode($response);

                $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
                echo $json;
                break;
            }
    }
}
?>