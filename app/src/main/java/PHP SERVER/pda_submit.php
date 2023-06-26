<?php
$host = '192.168.5.42';
$name = 'root';
$pwd = 'myt855myt855';
$db = 'baiguo_demo';
date_default_timezone_set('Asia/Taipei');
//header('Content-Type: application/json; charset=UTF-8');

// 建立与MySQL数据库的连接
$connection = mysqli_connect($host, $name, $pwd, $db) or die("Error " . mysqli_error($connection));
mysqli_set_charset($connection, "utf8");
//ini_set('display_errors', '0');

// $AreaNo = isset($_GET["AreaNo"]) ? $_GET["AreaNo"] : null;
// $BlockNo = $_GET["BlockNo"];
// $BlockType = $_GET["BlockType"];
// $DrugCode = $_GET["DrugCode"];
// $StoreID = $_GET["StoreID"];
//$TotalQty = $_GET["TotalQty"];

//Android studio 傳送的五個GET值
isset($_GET["ElabelNumber"]) ? $ElabelNumber = $_GET["ElabelNumber"] : null;
// $ElabelNumber = $_GET["ElabelNumber"];
// $DrugEnglish = $_GET["DrugEnglish"];
isset($_GET["UserID"]) ? $UserID = $_GET["UserID"] : null;
isset($_GET["TotalQty"]) ? $TotalQty = $_GET["TotalQty"] : null;
isset($_GET["spinnerText"]) ? $spinnerText = $_GET["spinnerText"] : null;
isset($_GET["DrugLabel"]) ? $DrugLabel = $_GET["DrugLabel"] : null;

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

        case "GET":
            if (isset($_GET["ElabelNumber"])) {
                $ElabelNumber = $_GET["ElabelNumber"];
                getJSON($ElabelNumber, $connection);
            }
            break;

        case "INVENTORY":
            isset($_GET["ElabelNumber"]) ? $ElabelNumber = $_GET["ElabelNumber"] : null;
            $LotNumber = $_GET["LotNumber"];
            $InventoryQty = $_GET["InventoryQty"];
            $StockQty = getStockQty($LotNumber, $connection); // 取得當前庫存

            $AdjQty = $InventoryQty - $StockQty; // 計算盤營盤虧量 會有負數
            $PayQty = abs($InventoryQty - $StockQty); //只會有正數
            $dataArray = array(
                "InvDate" => date("Y-m-d"),
                // 取得當前日期
                "InvTime" => date("H:i:s"),
                // 取得當前日期和時間
                "DrugCode" => $_GET["DrugCode"],
                "StoreID" => $_GET["StoreID"],
                "AreaNo" => $_GET["AreaNo"],
                "BlockNo" => $_GET["BlockNo"],
                "LotNumber" => $_GET["LotNumber"],
                "AdjQty" => $AdjQty,
                "PayQty" => $PayQty,
                "StockQty" => $StockQty,
                "InventoryQty" => $_GET["InventoryQty"],
                "User" => $_GET["User"],
                "UserID" => $_GET["UserId"],
            );

            $InvDateTime = $dataArray["InvDate"] . " " . $dataArray["InvTime"];



            //print_r($dataArray);
            echo "<br>";

            if ($AdjQty > 0) {
                //echo "盤盈<br>";
                $remark_CodeID = "D"; // 藥品盤盈
                // 寫入該筆盤點紀錄至 inventory 表內
                $SQL = "INSERT INTO inventory 
                                    (InvDate, DrugCode, StoreID, AreaNo, BlockNo, LotNumber, StockQty, InventoryQty, AdjQty, ShiftNo, InvTime, UserID, User)
                        VALUES
                                    ('" . $dataArray["InvDate"] . "', 
                                    '" . $dataArray["DrugCode"] . "', 
                                    '" . $dataArray["StoreID"] . "', 
                                    '" . $dataArray["AreaNo"] . "', 
                                    '" . $dataArray["BlockNo"] . "', 
                                    '" . $dataArray["LotNumber"] . "', 
                                    '" . $dataArray["StockQty"] . "', 
                                    '" . $dataArray["InventoryQty"] . "', 
                                    '" . $dataArray["AdjQty"] . "', 
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                                    '" . $dataArray["InvTime"] . "', 
                                    '" . $dataArray["UserID"] . "', 
                                    '" . $dataArray["User"] . "')";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventory table.<br>";
                } else {
                    //echo "HHError inserting into inventory table: " . mysqli_error($connection) . "<br>";
                }

                // 寫入盤點班次至 inventoryshift 表內
                $SQL = "INSERT INTO inventoryshift
                                    (StoreID, InvDate, ShiftNo, AreaNo, InvTime, ShiftName, User, Remark)
                        SELECT
                                    '" . $dataArray["StoreID"] . "',
                                    '" . $dataArray["InvDate"] . "',
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    //echo "JJJError inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }

                // 依據盤點數量更新 drugstock 的 StockQty 值
                $SQL = "UPDATE drugstock 
                            SET StockQty = '" . $dataArray["InventoryQty"] . "'
                            WHERE LotNumber = '" . $dataArray["LotNumber"] . "'";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully updated drugstock table.<br>";
                } else {
                    //echo "Error updating drugstock table: " . mysqli_error($connection) . "<br>";
                }

                $record_SQL = "INSERT INTO drugadd
                            (AddTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, BlockNo, AddQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
                            SELECT 
                            '$InvDateTime',
                            '" . $dataArray["DrugCode"] . "', 
                            '$remark_CodeID', 
                            '" . $dataArray["LotNumber"] . "', 
                            (SELECT StoreType FROM store WHERE StoreID = '" . $dataArray["StoreID"] . "'), 
                            '" . $dataArray["StoreID"] . "', 
                            '" . $dataArray["AreaNo"] . "', 
                            '" . $dataArray["BlockNo"] . "', 
                            '" . $dataArray["AdjQty"] . "', 
                            druginfo.MakerID, 
                            druginfo.MakerName,
                            (SELECT CodeDesc FROM codetable WHERE CodeID = '$remark_CodeID'),
                            '" . $dataArray["UserID"] . "', 
                            (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                            druginfo.DrugName, 
                            druginfo.DrugEnglish, 
                            drugstock.EffectDate,
                            drugstock.StockQty
                            FROM 
                            druginfo 
                            JOIN 
                            drugstock ON druginfo.DrugCode = drugstock.DrugCode
                            WHERE 
                            druginfo.DrugCode = '" . $dataArray["DrugCode"] . "' 
                            AND drugstock.LotNumber = '" . $dataArray["LotNumber"] . "'";

                drugOUT_record($dataArray, $record_SQL, $connection);
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
            } else if ($AdjQty < 0) {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }

                //echo "盤虧<br>";
                $remark_CodeID = "H"; // 藥品盤虧

                // 寫入該筆盤點紀錄至 inventory 表內
                $SQL = "INSERT INTO inventory 
                                    (InvDate, DrugCode, StoreID, AreaNo, BlockNo, LotNumber, StockQty, InventoryQty, AdjQty, ShiftNo, InvTime, UserID, User)
                        VALUES
                                    ('" . $dataArray["InvDate"] . "', 
                                    '" . $dataArray["DrugCode"] . "', 
                                    '" . $dataArray["StoreID"] . "', 
                                    '" . $dataArray["AreaNo"] . "', 
                                    '" . $dataArray["BlockNo"] . "', 
                                    '" . $dataArray["LotNumber"] . "', 
                                    '" . $dataArray["StockQty"] . "', 
                                    '" . $dataArray["InventoryQty"] . "', 
                                    '" . $dataArray["AdjQty"] . "', 
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                                    '" . $dataArray["InvTime"] . "', 
                                    '" . $dataArray["UserID"] . "', 
                                    '" . $dataArray["User"] . "')";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventory table.<br>";
                } else {
                    //echo "HHError inserting into inventory table: " . mysqli_error($connection) . "<br>";
                }

                // 寫入盤點班次至 inventoryshift 表內
                $SQL = "INSERT INTO inventoryshift
                                    (StoreID, InvDate, ShiftNo, AreaNo, InvTime, ShiftName, User, Remark)
                        SELECT
                                    '" . $dataArray["StoreID"] . "',
                                    '" . $dataArray["InvDate"] . "',
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    //echo "JJJError inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }

                // 依據盤點數量更新 drugstock 的 StockQty 值
                $SQL = "UPDATE drugstock 
                            SET StockQty = '" . $dataArray["InventoryQty"] . "'
                            WHERE LotNumber = '" . $dataArray["LotNumber"] . "'";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully updated drugstock table.<br>";
                } else {
                    //echo "Error updating drugstock table: " . mysqli_error($connection) . "<br>";
                }

                $record_SQL = "INSERT INTO drugpay
                            (PayTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, BlockNo, PayQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
                            SELECT 
                            NOW(),
                            '" . $dataArray["DrugCode"] . "', 
                            '$remark_CodeID', 
                            '" . $dataArray["LotNumber"] . "', 
                            (SELECT StoreType FROM store WHERE StoreID = '" . $dataArray["StoreID"] . "'),
                            '" . $dataArray["StoreID"] . "', 
                            '" . $dataArray["AreaNo"] . "', 
                            '" . $dataArray["BlockNo"] . "', 
                            '" . $dataArray["PayQty"] . "', 
                            druginfo.MakerID, 
                            druginfo.MakerName,
                            (SELECT CodeDesc FROM codetable WHERE CodeID = '$remark_CodeID'), 
                            '" . $dataArray["UserID"] . "', 
                            (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                            druginfo.DrugName, 
                            druginfo.DrugEnglish, 
                            drugstock.EffectDate,
                            drugstock.StockQty
                            FROM 
                            druginfo 
                            JOIN 
                            drugstock ON druginfo.DrugCode = drugstock.DrugCode
                            WHERE 
                            druginfo.DrugCode = '" . $dataArray["DrugCode"] . "' 
                            AND drugstock.LotNumber = '" . $dataArray["LotNumber"] . "'";

                drugOUT_record($dataArray, $record_SQL, $connection);
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
            } else {

                isset($_GET["ElabelNumber"]) ? $ElabelNumber = $_GET["ElabelNumber"] : null;

                //echo "盤平<br>";
                // 寫入該筆盤點紀錄至 inventory 表內
                $SQL = "INSERT INTO inventory 
                                    (InvDate, DrugCode, StoreID, AreaNo, BlockNo, LotNumber, StockQty, InventoryQty, AdjQty, ShiftNo, InvTime, UserID, User)
                        VALUES
                                    ('" . $dataArray["InvDate"] . "', 
                                    '" . $dataArray["DrugCode"] . "', 
                                    '" . $dataArray["StoreID"] . "', 
                                    '" . $dataArray["AreaNo"] . "', 
                                    '" . $dataArray["BlockNo"] . "', 
                                    '" . $dataArray["LotNumber"] . "', 
                                    '" . $dataArray["StockQty"] . "', 
                                    '" . $dataArray["InventoryQty"] . "', 
                                    '" . $dataArray["AdjQty"] . "', 
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                                    '" . $dataArray["InvTime"] . "', 
                                    '" . $dataArray["UserID"] . "', 
                                    '" . $dataArray["User"] . "')";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventory table.<br>";
                } else {
                    //echo "HHError inserting into inventory table: " . mysqli_error($connection) . "<br>";
                }

                // 寫入盤點班次至 inventoryshift 表內
                $SQL = "INSERT INTO inventoryshift
                                    (StoreID, InvDate, ShiftNo, AreaNo, InvTime, ShiftName, User, Remark)
                        SELECT
                                    '" . $dataArray["StoreID"] . "',
                                    '" . $dataArray["InvDate"] . "',
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    //echo "Error inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
            }



            break;





        case "IN": { //收入

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = getDataArray();
                //$remark_CodeID = "B";
                drugIN($connection, $dataArray, $ElabelNumber);

                // $remark_CodeID = "B";

                // $dataArray = getDataArray();

                // $SQL = getInsertOrUpdateSQL();
                // drugIN($dataArray, $SQL, $connection);

                // $record_SQL = getRecordInsertSQL($dataArray, $remark_CodeID);
                // drugIN_record($dataArray, $record_SQL, $connection);
                // echo "CASE IN";
                // update_elabeldrug($ElabelNumber, $dataArray, $connection);

                break;
            }

        case "OUT": {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                //$remark_CodeID = "G";
                // $dataArray = array(
                //     "DrugCode" => $_GET["DrugCode"],
                //     "StoreID" => $_GET["StoreID"],
                //     "AreaNo" => $_GET["AreaNo"],
                //     "BlockNo" => $_GET["BlockNo"],
                //     "LotNumber" => $_GET["LotNumber"],
                //     "MakeDate" => $_GET["MakeDate"],
                //     "EffectDate" => $_GET["EffectDate"],
                //     "StoreType" => $_GET["StoreType"],
                //     "Remark" => $_GET["Remark"],
                //     "StockQty" => $_GET["StockQty"],
                //     "UserID" => $_GET["UserID"],
                // );
                $dataArray = getDataArray();

                $SQL = "UPDATE drugstock 
                        SET StockQty = StockQty - ?
                        WHERE LotNumber = ?";

                drugOUT($dataArray, $SQL, $connection);

                $remark = urldecode($dataArray["Remark"]);

                $record_SQL = "INSERT INTO drugpay
                                            (PayTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, 
                                            BlockNo, PayQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
                                SELECT 
                                NOW(),
                                    '" . $dataArray["DrugCode"] . "', 
                                    '" . $dataArray["ReMark_CodeID"] . "',
                                    '" . $dataArray["LotNumber"] . "', 
                                    (SELECT StoreType FROM store WHERE StoreID = '" . $dataArray["StoreID"] . "'), 
                                    '" . $dataArray["StoreID"] . "', 
                                    '" . $dataArray["AreaNo"] . "', 
                                    '" . $dataArray["BlockNo"] . "', 
                                    '" . $dataArray["StockQty"] . "', 
                                    druginfo.MakerID, 
                                    druginfo.MakerName,
                                    CONCAT('" . $dataArray["ReMark_CodeID"] . "', '-', (SELECT CodeDesc FROM codetable WHERE CodeID = '" . $dataArray["ReMark_CodeID"] . "'), ' 至 " . $dataArray["Remark"] . "'),
                                    '" . $dataArray["UserID"] . "', 
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM drugpay AS temp WHERE LotNumber = '".$dataArray["LotNumber"]."'), 
                                    druginfo.DrugName, 
                                    druginfo.DrugEnglish, 
                                    drugstock.EffectDate,
                                    drugstock.StockQty
                                FROM 
                                    druginfo 
                                JOIN 
                                    drugstock ON druginfo.DrugCode = drugstock.DrugCode
                                WHERE 
                                    druginfo.DrugCode = '" . $dataArray["DrugCode"] . "' 
                                    AND drugstock.LotNumber = '" . $dataArray["LotNumber"] . "'";

                drugOUT_record($dataArray, $record_SQL, $connection);
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
                break;
            }

        case "GET_Inventory_Record": {

                getInventory_record($connection);

                break;
            }
        case "Search_BY_Drug_Label": {
                $dataArray = array(
                    "DrugLabel" => $_GET["DrugLabel"]
                );

                get_Store_withDrugLabel($dataArray, $connection);
                break;
            }
        case "Search_BY_DrugCode": {
                $dataArray = getDataArray();
                get_Store_with_DrugCode($dataArray, $connection);
                break;
            }

        case "Search_BY_DrugEnglish": {
                $dataArray = getDataArray();
                get_Store_with_DrugEnlgish($dataArray, $connection);
                break;
            }

        case "Search_BY_DrugName": {
                $dataArray = getDataArray();
                get_Store_with_DrugName($dataArray, $connection);
                break;
            }

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


function getInsertOrUpdateSQL()
{
    return "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ? ;";
}
function drugIN($connection, $dataArray, $ElabelNumber) //收入function
{
    $response = array();
    $insertOrUpdateSQL = "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
                          VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                          ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ?";

    $recordInsertSQL = "INSERT INTO drugadd
                        (AddTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, BlockNo, AddQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
                        SELECT 
                            NOW(),
                            ?,
                            ?,
                            ?,
                            (SELECT StoreType FROM store WHERE StoreID = ?),
                            ?,
                            ?,
                            ?,
                            ?,
                            druginfo.MakerID,
                            druginfo.MakerName,
                            CONCAT(?, '-', (SELECT CodeDesc FROM codetable WHERE CodeID = ?)),
                            ?,
                            (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM drugadd AS temp WHERE LotNumber = ?),
                            druginfo.DrugName,
                            druginfo.DrugEnglish,
                            drugstock.EffectDate,
                            drugstock.StockQty
                        FROM 
                            druginfo 
                        JOIN 
                            drugstock ON druginfo.DrugCode = drugstock.DrugCode
                        WHERE 
                            druginfo.DrugCode = ?
                            AND drugstock.LotNumber = ?";



    $updateElabelDrugSQL = "UPDATE elabeldrug 
                            SET 
                                DrugCode3 = ?,
                                DrugName3 = (SELECT EffectDate FROM drugstock WHERE LotNumber = ?), 
                                DrugEnglish3 = (SELECT StockQty FROM drugstock WHERE LotNumber = ?)
                            WHERE 
                                ElabelNumber = ?
                                AND DrugCode3 = ?";
    //AND DrugCode = ?
    $stmt = $connection->prepare($insertOrUpdateSQL);

    if ($stmt === false) {
        // $item = new stdClass;
        // $item -> response = '"收入作業準備陳述式失敗: "' . $connection->error;
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";

        //die("收入作業準備陳述式失敗: " . $connection->error . "<br>");

    }

    $stmt->bind_param(
        "sssssssss",
        $dataArray["DrugCode"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $dataArray["LotNumber"],
        $dataArray["MakeDate"],
        $dataArray["EffectDate"],
        $dataArray["StockQty"],
        $dataArray["StockQty"]
    );

    if ($stmt->execute() === true) {
        //$affectedRows = $stmt->affected_rows;
        //$item -> response1 = '"drugStock收入作業資料插入成功，影響了"'. $affectedRows." 行";
        //echo "drugStock收入作業資料插入成功，影響了 {$affectedRows} 行<br>";
        echo "[{}]";
    } else {
        //$affectedRows = $stmt->affected_rows;
        //$item -> response1 = 'drugStock收入作業插入資料時發生錯誤: "' . $stmt->error;
        //echo "drugStock收入作業插入資料時發生錯誤: " . $stmt->error . "<br>";
    }

    $stmt->close();

    $recordStmt = $connection->prepare($recordInsertSQL);
    if ($recordStmt === false) {
        // $affectedRows = $stmt->affected_rows;
        // $item = new stdClass;
        // $item -> response = 'drugadd 表準備陳述式失敗: "' . $connection->error  ;
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";
        //die("drugadd 表準備陳述式失敗: " . $connection->error . "<br>");
    }
    $recordStmt->bind_param(
        "ssssssssssssss",
        $dataArray["DrugCode"],
        $dataArray["ReMark_CodeID"],
        $dataArray["LotNumber"],
        $dataArray["StoreID"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $dataArray["StockQty"],
        $dataArray["ReMark_CodeID"],
        $dataArray["ReMark_CodeID"],
        $dataArray["UserID"],
        $dataArray["LotNumber"],
        $dataArray["DrugCode"],
        $dataArray["LotNumber"]

    );


    if ($recordStmt->execute() === true) {
        // $affectedRows = $stmt->affected_rows;
        // //$item = new stdClass;
        // $item -> response = 'drugADD紀錄成功"';
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";
        // //echo "drugADD紀錄成功<br>";
    } else {
        // $affectedRows = $stmt->affected_rows;
        // $item = new stdClass;
        // $item -> response = 'drugADD紀錄成功'.$recordStmt->error;
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";
        // //echo "drugADD紀錄失敗: " . $recordStmt->error . "<br>";
    }

    $recordStmt->close();

    $updateStmt = $connection->prepare($updateElabelDrugSQL);

    if ($updateStmt === false) {
        // $affectedRows = $stmt->affected_rows;
        // $item = new stdClass;
        // $item -> response = '更新 elabeldrug 表準備陳述式失敗: '.$connection->error;
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";
        // //die("更新 elabeldrug 表準備陳述式失敗: " . $connection->error . "<br>");
    }

    $updateStmt->bind_param(
        "sssss",
        $dataArray["LotNumber"],
        $dataArray["LotNumber"],
        $dataArray["LotNumber"],
        $ElabelNumber,
        $dataArray["LotNumber"]
    );

    if ($updateStmt->execute() === true) {
        if ($updateStmt->affected_rows > 0) {
            //     $affectedRows = $stmt->affected_rows;
            // //$item = new stdClass;
            // $item -> response = 'Successfully Update elabeldrug table.';
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            //echo "Successfully Update elabeldrug table.<br>";
        } else {
            //     $affectedRows = $stmt->affected_rows;
            // $item = new stdClass;
            // $item -> response = 'No rows updated in elabeldrug table.';
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            //     //echo "No rows updated in elabeldrug table.<br>";
        }
    } else {
        // $affectedRows = $stmt->affected_rows;
        // $item = new stdClass;
        // $item -> response = 'Error Update elabeldrug table: '.$updateStmt->error;
        // $response[] = $item;
        // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        // echo $json . "<br>";
        // echo "Error Update elabeldrug table: " . $updateStmt->error . "<br>";
    }

    $updateStmt->close();
}

function getRecordInsertSQL($dataArray, $remark_CodeID)
{
    return "INSERT INTO drugadd
            (AddTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, 
            BlockNo, AddQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
            SELECT 
            NOW(),
                '" . $dataArray["DrugCode"] . "', 
                '$remark_CodeID', 
                '" . $dataArray["LotNumber"] . "', 
                (SELECT StoreType FROM store WHERE StoreID = '" . $dataArray["StoreID"] . "'), 
                '" . $dataArray["StoreID"] . "', 
                '" . $dataArray["AreaNo"] . "', 
                '" . $dataArray["BlockNo"] . "', 
                '" . $dataArray["StockQty"] . "', 
                druginfo.MakerID, 
                druginfo.MakerName,
                (SELECT CodeDesc FROM codetable WHERE CodeID = '$remark_CodeID'), 
                '" . $dataArray["UserID"] . "', 
                '1', 
                druginfo.DrugName, 
                druginfo.DrugEnglish, 
                drugstock.EffectDate,
                drugstock.StockQty
            FROM 
                druginfo 
            JOIN 
                drugstock ON druginfo.DrugCode = drugstock.DrugCode
            WHERE 
                druginfo.DrugCode = '" . $dataArray["DrugCode"] . "' 
                AND drugstock.LotNumber = '" . $dataArray["LotNumber"] . "'";
}
function getDataArray()
{
    return array(
        "DrugCode" => $_GET["DrugCode"],
        "StoreID" => $_GET["StoreID"],
        "AreaNo" => $_GET["AreaNo"],
        "BlockNo" => $_GET["BlockNo"],
        "LotNumber" => $_GET["LotNumber"],
        "MakeDate" => $_GET["MakeDate"],
        "EffectDate" => $_GET["EffectDate"],
        "StockQty" => $_GET["StockQty"],
        "StoreType" => $_GET["StoreType"],
        "Remark" => $_GET["Remark"],
        "UserID" => $_GET["UserID"],
        "ReMark_CodeID" => $_GET["ReMark_CodeID"],
        "Search_KEY" => $_GET["Search_KEY"]
    );

        // 檢查 Search_KEY 是否存在
        if (isset($_GET["Search_KEY"])) {
            $dataArray["Search_KEY"] = $_GET["Search_KEY"];
        }
}
function drugOUT_record($dataArray, $record_SQL, $connection)
{
    if ($connection->query($record_SQL)) {
        echo "紀錄成功<br>";
    } else {
        echo "紀錄失敗" . mysqli_error($connection) . ".<br>";
    }
}
function drugIN_record($dataArray, $record_SQL, $connection)
{

    if ($connection->query($record_SQL)) {
        echo "紀錄成功<br>";
    } else {
        echo "紀錄失敗<br>";
    }
}
function drugOUT($dataArray, $SQL, $connection)
{
    $stmt = $connection->prepare($SQL);
    if ($stmt === false) {
        die("準備陳述式失敗: " . $connection->error);
    }

    // 將資料綁定至陳述式的參數
    $stmt->bind_param(
        "ss",
        $dataArray["StockQty"],
        $dataArray["LotNumber"]
    );
    // 執行陳述式
    if ($stmt->execute() === true) {
        echo "支出作業資料插入成功<br>";
    } else {
        echo "支出作業插入資料時發生錯誤: " . $stmt->error . "<br>";
    }
    // 關閉陳述式
    $stmt->close();
}
function drugIN_2($dataArray, $SQL, $connection)
{
    $stmt = $connection->prepare($SQL);

    if ($stmt === false) {
        die("收入作業準備陳述式失敗: " . $connection->error . "<br>");
    }

    // 將資料綁定至陳述式的參數
    $stmt->bind_param(
        "sssssssss",
        $dataArray["DrugCode"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $dataArray["LotNumber"],
        $dataArray["MakeDate"],
        $dataArray["EffectDate"],
        $dataArray["StockQty"],
        $dataArray["StockQty"]
    );

    // 執行陳述式
    if ($stmt->execute() === true) {
        $affectedRows = $stmt->affected_rows;
        echo "收入作業資料插入成功，影響了 {$affectedRows} 行<br>";
    } else {
        echo "收入作業插入資料時發生錯誤: " . $stmt->error . "<br>";
    }

    // 關閉陳述式
    $stmt->close();
}

function getJSON($ElabelNumber, $connection)
{
    $sql = "SELECT 
    ed.StoreID, 
    ed.ElabelType, 
    ed.DrugCode, 
    ed.DrugName, 
    ed.DrugEnglish, 
    ed.AreaNo, 
    ed.BlockNo, 
    ds.LotNumber,
    ds.EffectDate,
    ds.StockQty ,
    di.MakerID,
    di.MakerName,
    ds.MakeDate
    FROM elabeldrug ed 
    INNER JOIN drugstock ds ON 
        ed.StoreID = ds.StoreID AND 
        ed.AreaNo = ds.AreaNo AND 
        ed.BlockNo = ds.BlockNo AND 
        ed.DrugCode = ds.DrugCode
    INNER JOIN druginfo di ON
        ed.DrugCode = di.DrugCode
    WHERE ed.ElabelNumber = '$ElabelNumber'";

    $result = mysqli_query($connection, $sql);
    $response = [];

    if ($result && mysqli_num_rows($result) > 0) {
        $response = array();
        while ($row = mysqli_fetch_assoc($result)) {
            $item = array(
                $row["StoreID"],
                $row["ElabelType"],
                $row["DrugCode"],
                $row["DrugName"],
                $row["DrugEnglish"],
                $row["AreaNo"],
                $row["BlockNo"],
                $row["StockQty"],
                $row["LotNumber"],
                $row["MakerID"],
                $row["MakerName"],
                $row["EffectDate"],
                $row["MakeDate"]
            );
            array_push($response, $item);
        }
        $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        echo $json . "<br>";
    }
}
function getStockQty($LotNumber, $connection)
{
    $sql = "SELECT StockQty FROM drugstock WHERE LotNumber = ?";
    $stmt = $connection->prepare($sql);
    $stmt->bind_param("s", $LotNumber);

    $stmt->execute();

    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        return $row['StockQty'];
    } else {
        return null;
    }
}
function getInventory_record($connection)
{
    $SQL = "SELECT i.*, ed.ElabelType, di.DrugName
    FROM baiguo_demo.inventory AS i
    JOIN elabeldrug AS ed ON i.DrugCode = ed.DrugCode
    JOIN druginfo AS di ON i.DrugCode = di.DrugCode";
    $stmt = $connection->prepare($SQL);
    $stmt->execute();

    $result = $stmt->get_result();


    $response = [];
    if ($result && mysqli_num_rows($result) > 0) {
        $response = array();
        while ($row = $result->fetch_assoc()) {
            $item = new stdClass();
            $item->InvDate = $row["InvDate"];
            $item->DrugCode = $row["DrugCode"];
            $item->DrugName = $row["DrugName"];
            $item->StoreID = $row["StoreID"];
            $item->AreaNo = $row["AreaNo"];
            $item->BlockNo = $row["BlockNo"];
            $item->BlockType = $row["ElabelType"];
            $item->LotNumber = $row["LotNumber"];
            $item->StockQty = $row["StockQty"];
            $item->InventoryQty = $row["InventoryQty"];
            $item->AdjQty = $row["AdjQty"];
            $item->ShiftNo = $row["ShiftNo"];
            $item->InvTime = $row["InvTime"];
            $item->UserID = $row["UserID"];
            $item->Remark = $row["Remark"];
            $item->User = $row["User"];

            array_push($response, $item);
        }
        $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        echo $json . "<br>";
    }
}

function update_elabeldrug($ElabelNumber, $dataArray, $connection)
{
    $SQL = "UPDATE elabeldrug 
            SET 
                    DrugCode3 = '" . $dataArray["LotNumber"] . "',
                    DrugName3 = (
                                    SELECT EffectDate 
                                    FROM drugstock 
                                    WHERE LotNumber = '" . $dataArray["LotNumber"] . "'), 
                    DrugEnglish3 = (SELECT StockQty
                                    FROM drugstock
                                    WHERE LotNumber = '" . $dataArray["LotNumber"] . "')
            WHERE 
                    ElabelNumber = '$ElabelNumber' AND 
                    DrugCode = '" . $dataArray["DrugCode"] . "'";

    $result = mysqli_query($connection, $SQL);

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            //echo "Successfully Update elabeldrug table.<br>";
        } else {
            //echo "No rows updated in elabeldrug table.<br>";
        }
    } else {
        //echo "Error Update elabeldrug table: " . mysqli_error($connection) . "<br>";
    }
}

function get_Store_withDrugLabel($dataArray, $connection)
{
    // $SQL = "SELECT ElabelNumber, StoreID, ElabelType, DrugCode, DrugName, DrugEnglish, AreaNo, BlockNo, DrugCode3, DrugName3,DrugEnglish3
    //         FROM elabeldrug
    //         WHERE DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')
    //  ";
    $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
        FROM elabeldrug ed
        JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber
        WHERE ed.DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')";


    $result = mysqli_query($connection, $SQL);
    $response = array(); // Create an empty array to store the response

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            // Fetch and process each row
            while ($row = mysqli_fetch_assoc($result)) {
                $item = new stdClass();
                $item->Response = '成功執行搜尋!!';
                $item->ElabelNumber = $row['ElabelNumber'];
                $item->StoreID = $row['StoreID'];
                $item->ElabelType = $row['ElabelType'];
                $item->DrugCode = $row['DrugCode'];
                $item->DrugName = $row['DrugName'];
                $item->DrugEnglish = $row['DrugEnglish'];
                $item->AreaNo = $row['AreaNo'];
                $item->BlockNo = $row['BlockNo'];
                $item->DrugCode3 = $row['DrugCode3'];
                $item->DrugName3 = $row['DrugName3'];
                $item->DrugEnglish3 = $row['DrugEnglish3'];
                $item->MakeDate = $row['MakeDate'];

                $response[] = $item; // Add the item to the response array
            }
        } else {
            $item = new stdClass();
            $item->Response = 'No rows return Search BY DrugLabel.';
            $response[] = $item;
        }
    } else {
        $item = new stdClass();
        $item->Response = 'Error Search table: ' . mysqli_error($connection);
        $response[] = $item;
    }

    $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    echo $json . "<br>";
}



//搜尋不同的關鍵字
function get_Store_with_DrugCode($dataArray, $connection)
{
    $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
            FROM elabeldrug ed
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber
            WHERE ed.DrugCode LIKE '%" . $dataArray["Search_KEY"] . "%'";



    $result = mysqli_query($connection, $SQL);
    $response = array(); // Create an empty array to store the response

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            // Fetch and process each row
            while ($row = mysqli_fetch_assoc($result)) {
                $item = new stdClass();
                $item->Response = '成功執行搜尋!!';
                $item->ElabelNumber = $row['ElabelNumber'];
                $item->StoreID = $row['StoreID'];
                $item->ElabelType = $row['ElabelType'];
                $item->DrugCode = $row['DrugCode'];
                $item->DrugName = $row['DrugName'];
                $item->DrugEnglish = $row['DrugEnglish'];
                $item->AreaNo = $row['AreaNo'];
                $item->BlockNo = $row['BlockNo'];
                $item->DrugCode3 = $row['DrugCode3'];
                $item->DrugName3 = $row['DrugName3'];
                $item->DrugEnglish3 = $row['DrugEnglish3'];
                $item->MakeDate = $row['MakeDate'];

                $response[] = $item; // Add the item to the response array
            }
        } else {
            $item = new stdClass();
            $item->Response = 'No rows return Search BY DrugCode.';
            $response[] = $item;
        }
    } else {
        $item = new stdClass();
        $item->Response = 'Error Search table: ' . mysqli_error($connection);
        $response[] = $item;
    }

    $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    echo $json . "<br>";
}

function get_Store_with_DrugEnlgish($dataArray, $connection)
{
    $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
            FROM elabeldrug ed
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber
            WHERE ed.DrugEnglish LIKE '%" . $dataArray["Search_KEY"] . "%'";



    $result = mysqli_query($connection, $SQL);
    $response = array(); // Create an empty array to store the response

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            // Fetch and process each row
            while ($row = mysqli_fetch_assoc($result)) {
                $item = new stdClass();
                $item->Response = '成功執行搜尋!!';
                $item->ElabelNumber = $row['ElabelNumber'];
                $item->StoreID = $row['StoreID'];
                $item->ElabelType = $row['ElabelType'];
                $item->DrugCode = $row['DrugCode'];
                $item->DrugName = $row['DrugName'];
                $item->DrugEnglish = $row['DrugEnglish'];
                $item->AreaNo = $row['AreaNo'];
                $item->BlockNo = $row['BlockNo'];
                $item->DrugCode3 = $row['DrugCode3'];
                $item->DrugName3 = $row['DrugName3'];
                $item->DrugEnglish3 = $row['DrugEnglish3'];
                $item->MakeDate = $row['MakeDate'];

                $response[] = $item; // Add the item to the response array
            }
        } else {
            $item = new stdClass();
            $item->Response = 'No rows return Search BY DrugCode.';
            $response[] = $item;
        }
    } else {
        $item = new stdClass();
        $item->Response = 'Error Search table: ' . mysqli_error($connection);
        $response[] = $item;
    }

    $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    echo $json . "<br>";
}

function get_Store_with_DrugName($dataArray, $connection)
{
    $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
    FROM elabeldrug ed
    JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber
    WHERE ed.DrugName LIKE '%" . $dataArray["Search_KEY"] . "%'";



    $result = mysqli_query($connection, $SQL);
    $response = array(); // Create an empty array to store the response

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            // Fetch and process each row
            while ($row = mysqli_fetch_assoc($result)) {
                $item = new stdClass();
                $item->Response = '成功執行搜尋!!';
                $item->ElabelNumber = $row['ElabelNumber'];
                $item->StoreID = $row['StoreID'];
                $item->ElabelType = $row['ElabelType'];
                $item->DrugCode = $row['DrugCode'];
                $item->DrugName = $row['DrugName'];
                $item->DrugEnglish = $row['DrugEnglish'];
                $item->AreaNo = $row['AreaNo'];
                $item->BlockNo = $row['BlockNo'];
                $item->DrugCode3 = $row['DrugCode3'];
                $item->DrugName3 = $row['DrugName3'];
                $item->DrugEnglish3 = $row['DrugEnglish3'];
                $item->MakeDate = $row['MakeDate'];

                $response[] = $item; // Add the item to the response array
            }
        } else {
            $item = new stdClass();
            $item->Response = 'No rows return Search BY DrugCode.';
            $response[] = $item;
        }
    } else {
        $item = new stdClass();
        $item->Response = 'Error Search table: ' . mysqli_error($connection);
        $response[] = $item;
    }

    $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    echo $json . "<br>";
}





mysqli_close($connection);
//echo "結束 以下註解<br>"
