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
                echo "盤盈<br>";
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
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    echo "JJJError inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }

                // 依據盤點數量更新 drugstock 的 StockQty 值
                $SQL = "UPDATE drugstock 
                            SET StockQty = '" . $dataArray["InventoryQty"] . "'
                            WHERE LotNumber = '" . $dataArray["LotNumber"] . "'";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully updated drugstock table.<br>";
                } else {
                    echo "Error updating drugstock table: " . mysqli_error($connection) . "<br>";
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

                echo "盤虧<br>";
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
                                    (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'),
                                    '" . $dataArray["AreaNo"] . "',
                                    '" . $dataArray["InvTime"] . "',
                                    CONCAT((SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM (SELECT ShiftNo, InvDate, LotNumber FROM inventory) AS temp WHERE InvDate = '" . $dataArray["InvDate"] . "' AND LotNumber = '" . $dataArray["LotNumber"] . "'), ' - " . $dataArray["InvTime"] . " " . $dataArray["UserID"] . "'),
                                    '" . $dataArray["StoreID"] . "',
                                    ''";
                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully inserted into inventoryshift table.<br>";
                } else {
                    echo "JJJError inserting into inventoryshift table: " . mysqli_error($connection) . "<br>";
                }

                // 依據盤點數量更新 drugstock 的 StockQty 值
                $SQL = "UPDATE drugstock 
                            SET StockQty = '" . $dataArray["InventoryQty"] . "'
                            WHERE LotNumber = '" . $dataArray["LotNumber"] . "'";

                $result = mysqli_query($connection, $SQL);

                if ($result) {
                    echo "Successfully updated drugstock table.<br>";
                } else {
                    echo "Error updating drugstock table: " . mysqli_error($connection) . "<br>";
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





        case "IN": {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }

                $remark_CodeID = "B";

                $dataArray = getDataArray();

                $SQL = getInsertOrUpdateSQL();
                drugIN($dataArray, $SQL, $connection);

                $record_SQL = getRecordInsertSQL($dataArray, $remark_CodeID);
                drugIN_record($dataArray, $record_SQL, $connection);
                echo "CASE IN";
                update_elabeldrug($ElabelNumber, $dataArray, $connection);

                break;
            }

        case "OUT": {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $remark_CodeID = "G";
                $dataArray = array(
                    "DrugCode" => $_GET["DrugCode"],
                    "StoreID" => $_GET["StoreID"],
                    "AreaNo" => $_GET["AreaNo"],
                    "BlockNo" => $_GET["BlockNo"],
                    "LotNumber" => $_GET["LotNumber"],
                    "MakeDate" => $_GET["MakeDate"],
                    "EffectDate" => $_GET["EffectDate"],
                    "StoreType" => $_GET["StoreType"],
                    "Remark" => $_GET["Remark"],
                    "StockQty" => $_GET["StockQty"],
                    "UserID" => $_GET["UserID"],
                );

                $SQL = "UPDATE drugstock 
                        SET StockQty = StockQty - ?
                        WHERE LotNumber = ?";

                drugOUT($dataArray, $SQL, $connection);

                $record_SQL = "INSERT INTO drugpay
                                            (PayTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, 
                                            BlockNo, PayQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
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

                drugOUT_record($dataArray, $record_SQL, $connection);
                update_elabeldrug($ElabelNumber, $dataArray, $connection);
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
            }
    }
}

function getInsertOrUpdateSQL()
{
    return "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ? ;";
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
    );
}
function drugOUT_record($dataArray, $record_SQL, $connection)
{
    if ($connection->query($record_SQL)) {
        echo "紀錄成功<br>";
    } else {
        echo "紀錄失敗<br>";
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
function drugIN($dataArray, $SQL, $connection)
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
        echo "收入作業資料插入成功<br>";
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

// function get_Store_withDrugLabel($dataArray, $connection)
// {
//     $SQL = "SELECT ElabelNumber, StoreID, ElabelType, DrugCode, DrugName, DrugEnglish, AreaNo, BlockNo, DrugCode3, DrugName3
//             FROM elabeldrug
//             WHERE DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')
//      ";

//     $result = mysqli_query($connection, $SQL);
//     $response = array(); // Create an empty array to store the response

//     if ($result) {
//         if (mysqli_affected_rows($connection) > 0) {
            
//             // Fetch and process each row
//             while ($row = mysqli_fetch_assoc($result)) {
//                 $item = array(
//                     'Response' => 'Successfully return Search BY DrugLabel.',
//                     'ElabelNumber' => $row['ElabelNumber'],
//                     'StoreID' => $row['StoreID'],
//                     'ElabelType' => $row['ElabelType'],
//                     'DrugCode' => $row['DrugCode'],
//                     'DrugName' => $row['DrugName'],
//                     'DrugEnglish' => $row['DrugEnglish'],
//                     'AreaNo' => $row['AreaNo'],
//                     'BlockNo' => $row['BlockNo'],
//                     'DrugCode3' => $row['DrugCode3'],
//                     'DrugName3' => $row['DrugName3']
//                 );
    
//                 $response[] = $item; // Add the item to the response array
//             }
//         } else {
//             // $response['status'] = '不成功';
//             // $response[] = array('status' => '不成功', 'message' => "No rows return Search BY DrugLabel.");
//             $item = array('Response' => 'No rows return Search BY DrugLabel.');
//             $response[] = $item;
//         }
//     } else {
//         // $response['status'] = '不成功';
//         // $response[] = array('status' => '不成功', 'message' => "Error Search table: " . mysqli_error($connection));
//         $item = array('Response' => 'Error Search table: ' . mysqli_error($connection));
//         $response[] = $item;

//     }

//     $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_FORCE_OBJECT);
//     echo $json . "<br>";
    
    
// }
function get_Store_withDrugLabel($dataArray, $connection)
{
    $SQL = "SELECT ElabelNumber, StoreID, ElabelType, DrugCode, DrugName, DrugEnglish, AreaNo, BlockNo, DrugCode3, DrugName3,DrugEnglish3
            FROM elabeldrug
            WHERE DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')
     ";

    $result = mysqli_query($connection, $SQL);
    $response = array(); // Create an empty array to store the response

    if ($result) {
        if (mysqli_affected_rows($connection) > 0) {
            // Fetch and process each row
            while ($row = mysqli_fetch_assoc($result)) {
                $item = new stdClass();
                $item->Response = 'Successfully return Search BY DrugLabel.';
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



mysqli_close($connection);
//echo "結束 以下註解<br>"
?>