<?php
date_default_timezone_set('Asia/Taipei');
$DB_CONNECT_STATUS = True;
ini_set('default_charset', 'UTF-8'); //編碼
ini_set('json_encode_options', JSON_UNESCAPED_UNICODE); //JSON 編碼
$func = new func_Collect();

class func_Collect
{
    function my_json_encode($data)
    {
        return json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    }
}



$DB_HOST = '192.168.5.42'; //DB位置
$DB_USER = 'root'; //DB的登入帳號
$DB_NAME = 'baiguo.v2'; //DB名稱
$DB_PASSWORD = 'myt855myt855'; //DB密碼
$AIMS_HOST = '192.168.5.130'; //AIMS 位置

$SERVER_STATUS = true;

try {
    // 建立mysqli物件
    $connection = new mysqli($DB_HOST, $DB_USER, $DB_PASSWORD, $DB_NAME);
    $connection->set_charset("utf8");

    // 檢查是否成功連線
    if ($connection->connect_error) {

        $DB_CONNECT_STATUS = False;
        $MESSAGE = ("連線失敗: " . $connection->connect_error);

    } else {
        mysqli_set_charset($connection, "utf8");
        $DB_CONNECT_STATUS = True;
        $MESSAGE = "連線成功";

    }

} catch (Exception $e) {

    $DB_CONNECT_STATUS = False;
    $MESSAGE = "連線失敗: " . $e->getMessage();

    
    $connection = null;
}

//echo $func->my_json_encode($SERVER_RESPONSE);//打印是否連上該PHP伺服器 其實你看的到瀏覽器或終端機顯示的內容 就代表有連線了 只是這個是給裝置查詢狀態用的ㄏㄏ

//echo $func->my_json_encode($RESPONSE);//打印是否成功連線

$RESPONSE = [
    'SERVER_STATUS' => $SERVER_STATUS,
    'DB_CONNECT_STATUS' => $DB_CONNECT_STATUS,
    'MESSAGE' => $MESSAGE
];



echo $func->my_json_encode($RESPONSE);





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
                "InventoryQty" => $_GET["InventoryQty"],
                "User" => $_GET["User"],
                "UserID" => $_GET["UserId"],
            );

            //DB GET Row n Box Then Count By DrugCode
            if (isset($_GET["numRow"]) && !empty($_GET["numRow"])) {
                $dataArray["numRow"] = $_GET["numRow"];
            } else {
                $dataArray["numRow"] = 0;
            }

            if (isset($_GET["numBox"]) && !empty($_GET["numBox"])) {
                $dataArray["numBox"] = $_GET["numBox"];
            } else {
                $dataArray["numBox"] = 0;
            }

            $data = countTotalNumber($dataArray, $connection);
            $countNum = $dataArray["numBox"] * $data["NumBox"];
            $countNum += $dataArray["numRow"] * $data["NumRow"];

            $InventoryQty += $countNum;
            $dataArray["InventoryQty"] = $InventoryQty;

            echo $InventoryQty . "這是數量";




            $dataArray["StockQty"] = getStockQty($dataArray, $connection); // 取得當前庫存
            $StockQty = getStockQty($dataArray, $connection); // 取得當前庫存

            // echo "StockQTY".$StockQty;
            $AdjQty = $InventoryQty - $StockQty; // 計算盤營盤虧量 會有負數
            $PayQty = abs($InventoryQty - $StockQty); //只會有正數



            // echo "這是SWITCH DATA".$data["NumBox"];
            // echo "這是SWITCH DATA".$data["NumRow"];
            //echo "這是計算後的總量".$countNum;

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
                                    $StockQty, 
                                    $InventoryQty, 
                                    $AdjQty, 
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                                    '" . $dataArray["InvTime"] . "', 
                                    '" . $dataArray["UserID"] . "', 
                                    '" . $dataArray["User"] . "')";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully inserted into inventory table.<br>";
                } else {
                    echo "HHError inserting into inventory table: " . mysqli_error($connection) . "<br>";
                }

                // 寫入盤點班次至 inventoryshift 表內
                $SQL = "INSERT INTO inventoryshift
                                    (StoreID, InvDate, ShiftNo, AreaNo, InvTime, ShiftName, User, Remark)
                        SELECT
                                    '" . $dataArray["StoreID"] . "',
                                    '" . $dataArray["InvDate"] . "',
                                    (SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
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
                WHERE LotNumber = '" . $dataArray["LotNumber"] . "'
                AND DrugCode = '" . $dataArray["DrugCode"] . "'
                AND StoreID = '" . $dataArray["StoreID"] . "'
                AND AreaNo = '" . $dataArray["AreaNo"] . "'
                AND BlockNo = '" . $dataArray["BlockNo"] . "'";

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
                            $AdjQty, 
                            druginfo.MakerID, 
                            druginfo.MakerName,
                            (SELECT CodeDesc FROM codetable WHERE CodeID = '$remark_CodeID'),
                            '" . $dataArray["UserID"] . "', 
                            (SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
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

                drugIN_record($dataArray, $record_SQL, $connection);
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
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
                                    $InventoryQty, 
                                    '" . $dataArray["StockQty"] . "', 
                                    $AdjQty, 
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
                                    (SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
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
                            WHERE LotNumber = '" . $dataArray["LotNumber"] . "'
                            AND DrugCode = '" . $dataArray["DrugCode"] . "'
                            AND StoreID = '" . $dataArray["StoreID"] . "'
                            AND AreaNo = '" . $dataArray["AreaNo"] . "'
                            AND BlockNo = '" . $dataArray["BlockNo"] . "'";

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
                            $PayQty, 
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
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
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
                                    $AdjQty,
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), 
                                    '" . $dataArray["InvTime"] . "', 
                                    '" . $dataArray["UserID"] . "', 
                                    '" . $dataArray["User"] . "')";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully inserted into inventory table.<br>";
                } else {
                    echo "HHError inserting into inventory table: " . mysqli_error($connection) . "<br>";
                }

                // 寫入盤點班次至 inventoryshift 表內
                $SQL = "INSERT INTO inventoryshift
                                    (StoreID, InvDate, ShiftNo, AreaNo, InvTime, ShiftName, User, Remark)
                        SELECT
                                    '" . $dataArray["StoreID"] . "',
                                    '" . $dataArray["InvDate"] . "',
                                    (SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 0, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    //echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    //echo "Error inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
            }



            break;





        case "IN": { //收入

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = getDataArray();
                //$remark_CodeID = "B";
                drugIN($connection, $dataArray, $ElabelNumber);
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);

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
                        WHERE LotNumber = ?
                        AND DrugCode = ?
                        AND StoreID = ?
                        AND AreaNo = ?
                        AND BlockNo =? ";

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
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM drugpay AS temp WHERE LotNumber = '" . $dataArray["LotNumber"] . "'), 
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
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
                break;
            }

        case "GET_Inventory_Record": {

                getInventory_record($connection);

                break;
            }
        case "Search_BY_Drug_Label": {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = array(
                    "DrugLabel" => $_GET["DrugLabel"]
                );

                get_Store_withDrugLabel($dataArray, $connection);
                break;
            }
        case "Search_BY_DrugCode": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = getDataArray();
                get_Store_with_DrugCode($dataArray, $connection);
                break;
            }

        case "Search_BY_DrugEnglish": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = getDataArray();
                get_Store_with_DrugEnlgish($dataArray, $connection);
                break;
            }

        case "Search_BY_DrugName": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
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
        case "ITEM_BLINK": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);

                break;
            }
    }
}
//BlinkElabel("CYAN","1","05DBCD6FB69F",$aims_host);
function blinkElabel($color, $duration, $labelCode, $aimsHost)
{
    $url = "http://{$aimsHost}/labels/contents/led";
    $data = array(
        array(
            'color' => $color,
            'duration' => $duration,
            'labelCode' => $labelCode,
        ),
    );
    $headers = array(
        'Content-Type: application/json',
        'Accept: */*',
    );
    $postData = json_encode($data);

    $ch = curl_init();
    curl_setopt_array(
        $ch,
        array(
            CURLOPT_URL => $url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_CUSTOMREQUEST => 'PUT',
            CURLOPT_POSTFIELDS => $postData,
            CURLOPT_HTTPHEADER => $headers,
        )
    );
    $result = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode == 202) {
        //echo "執行成功！　執行結果為：${httpCode} 顏色：${color} 、持續時間：{$duration}0秒 >>> {$labelCode} 進行亮燈<br>";
    } else {
        //echo "執行不成功！　執行結果為：${httpCode}、 >>> ${labelCode} 不進行亮燈<br>";
    }
}


function countTotalNumber($dataArray, $connection)
{

    $SQL = "SELECT NumBox,NumRow FROM druginfo WHERE DrugCode = '" . $dataArray["DrugCode"] . "'";

    // 执行 SQL 查询
    $result = mysqli_query($connection, $SQL);

    // 检查是否有结果
    if ($result) {
        // 获取结果
        while ($row = mysqli_fetch_assoc($result)) {
            $data["NumRow"] = $row["NumRow"];
            $data["NumBox"] = $row["NumBox"];
            // echo $data["NumBox"].$data["NumRow"];
            echo $data["NumBox"] . "<br>";
            echo $data["NumRow"] . "<br>";
        }
    } else {
        echo "druginfo 無結果";
    }

    return $data;
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
                                DrugName3 = (SELECT EffectDate FROM drugstock 
                                            WHERE LotNumber = ?
                                            AND DrugCode = ?
                                            AND StoreID = ?
                                            AND AreaNo = ? 
                                            AND BlockNo =?), 
                                DrugEnglish3 = (SELECT StockQty FROM drugstock 
                                            WHERE LotNumber = ?
                                            AND DrugCode = ?
                                            AND StoreID = ?
                                            AND AreaNo = ? 
                                            AND BlockNo =?)
                            WHERE 
                                ElabelNumber = ?";
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
        "ssssssssssss",
        $dataArray["LotNumber"],
        $dataArray["LotNumber"],
        $dataArray["DrugCode"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $dataArray["LotNumber"],
        $dataArray["DrugCode"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $ElabelNumber
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
    $dataArray = array(
        "DrugCode" => isset($_GET["DrugCode"]) ? $_GET["DrugCode"] : null,
        "StoreID" => isset($_GET["StoreID"]) ? $_GET["StoreID"] : null,
        "AreaNo" => isset($_GET["AreaNo"]) ? $_GET["AreaNo"] : null,
        "BlockNo" => isset($_GET["BlockNo"]) ? $_GET["BlockNo"] : null,
        "LotNumber" => isset($_GET["LotNumber"]) ? $_GET["LotNumber"] : null,
        "MakeDate" => isset($_GET["MakeDate"]) ? $_GET["MakeDate"] : null,
        "EffectDate" => isset($_GET["EffectDate"]) ? $_GET["EffectDate"] : null,
        "StockQty" => isset($_GET["StockQty"]) ? $_GET["StockQty"] : null,
        "StoreType" => isset($_GET["StoreType"]) ? $_GET["StoreType"] : null,
        "Remark" => isset($_GET["Remark"]) ? $_GET["Remark"] : null,
        "UserID" => isset($_GET["UserID"]) ? $_GET["UserID"] : null,
        "ReMark_CodeID" => isset($_GET["ReMark_CodeID"]) ? $_GET["ReMark_CodeID"] : null,
        "Search_KEY" => isset($_GET["Search_KEY"]) ? $_GET["Search_KEY"] : null
    );

    return $dataArray;
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
        echo "紀錄失敗" . mysqli_error($connection) . "<br>";
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
        "ssssss",
        $dataArray["StockQty"],
        $dataArray["LotNumber"],
        $dataArray["DrugCode"],
        $dataArray["StoreID"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
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
function getStockQty($dataArray, $connection)
{
    $sql = "SELECT StockQty FROM drugstock WHERE LotNumber = ?
            AND DrugCode = ?
            AND AreaNo = ?
            AND BlockNo =?
            AND StoreID = ?";
    $stmt = $connection->prepare($sql);
    $stmt->bind_param(
        "sssss",
        $dataArray["LotNumber"],
        $dataArray["DrugCode"],
        $dataArray["AreaNo"],
        $dataArray["BlockNo"],
        $dataArray["StoreID"]
    );

    $stmt->execute();

    if ($stmt->errno) {
        echo "Error executing statement: " . $stmt->error;
        return null;
    }

    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        // echo "getStockQty 有取得";
        return $row['StockQty'];
    } else {
        echo "getStockQty 無取得: " . $stmt->error;
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
                                    WHERE LotNumber = '" . $dataArray["LotNumber"] . "'
                                    AND DrugCode = '" . $dataArray["DrugCode"] . "'
                                    AND StoreID = '" . $dataArray["StoreID"] . "'
                                    AND AreaNo = '" . $dataArray["AreaNo"] . "'
                                    AND BlockNo = '" . $dataArray["BlockNo"] . "'), 
                    DrugEnglish3 = (SELECT StockQty
                                    FROM drugstock
                                    WHERE LotNumber = '" . $dataArray["LotNumber"] . "'
                                    AND DrugCode = '" . $dataArray["DrugCode"] . "'
                                    AND StoreID = '" . $dataArray["StoreID"] . "'
                                    AND AreaNo = '" . $dataArray["AreaNo"] . "'
                                    AND BlockNo = '" . $dataArray["BlockNo"] . "')
            WHERE 
                    ElabelNumber = '$ElabelNumber' 
                    AND DrugCode = '" . $dataArray["DrugCode"] . "'
                    AND StoreID = '" . $dataArray["StoreID"] . "'
                    AND AreaNo = '" . $dataArray["AreaNo"] . "'
                    AND BlockNo = '" . $dataArray["BlockNo"] . "'
                    ";

    $result = mysqli_query($connection, $SQL);

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            echo "Successfully Update elabeldrug table.<br>";
        } else {
            echo "No rows updated in elabeldrug table.<br>";
        }
    } else {
        echo "Error Update elabeldrug table: " . mysqli_error($connection) . "<br>";
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
        JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
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
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
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
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
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
    JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
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



if ($connection !== null) {
    mysqli_close($connection);
}


//echo "結束 以下註解<br>"
