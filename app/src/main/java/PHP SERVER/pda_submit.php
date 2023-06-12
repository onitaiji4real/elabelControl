<?php
$host = '192.168.5.42';
$name = 'root';
$pwd = 'myt855myt855';
$db = 'baiguo_demo';
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
            $DrugCode = $_GET["DrugCode"];
            $StoreID = $_GET["StoreID"];
            $AreaNo = $_GET["AreaNo"];
            $BlockNo = $_GET["BlockNo"];
            $LotNumber = $_GET["LotNumber"];
            $InventoryQty = $_GET["InventoryQty"];
            $UserId = $_GET["UserId"];
            $User = $_GET["User"];
            // $InventoryQty = $_GET["InventoryQty"];

            $StockQty = getStockQty($LotNumber, $connection);
            $AdjQty = $InventoryQty - $StockQty;

            $SQL = "INSERT INTO inventory 
                                (InvDate, DrugCode, StoreID, AreaNo, BlockNo, LotNumber, StockQty, InventoryQty, AdjQty, ShiftNo, InvTime, UserId, User)
                    SELECT 
                    CURDATE(),
                    '$DrugCode',
                        '$StoreID',
                        '$AreaNo',
                        '$BlockNo',
                        '$LotNumber',
                        '$StockQty',
                        '$InventoryQty',
                        '$AdjQty',
                        IFNULL((SELECT MAX(ShiftNo) + 1 FROM inventory WHERE LotNumber = '$LotNumber'), 1),
                        CURTIME(),
                        '$UserId',
                        '$User'";

            $result = mysqli_query($connection, $SQL);

            break;

            case "IN": {

                $dataArray = getDataArray();
                
                $SQL = getInsertOrUpdateSQL();
                drugIN($dataArray, $SQL, $connection);
            
                $record_SQL = getRecordInsertSQL($dataArray);
                drugIN_record($dataArray, $record_SQL, $connection);
            
                break;
            }

        case "OUT": {
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
                                    'A', 
                                    '" . $dataArray["LotNumber"] . "', 
                                    '" . $dataArray["StoreType"] . "', 
                                    '" . $dataArray["StoreID"] . "', 
                                    '" . $dataArray["AreaNo"] . "', 
                                    '" . $dataArray["BlockNo"] . "', 
                                    '" . $dataArray["StockQty"] . "', 
                                    druginfo.MakerID, 
                                    druginfo.MakerName,
                                    '" . $dataArray["Remark"] . "', 
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
            }

        case "GET_Inventory_Record": {

                getInventory_record($connection);

                break;
            }
    }
}

function getInsertOrUpdateSQL() {
    return "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ? ;";
}
function getRecordInsertSQL($dataArray) {
    return "INSERT INTO drugadd
            (AddTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, 
            BlockNo, AddQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
            SELECT 
            NOW(),
                '" . $dataArray["DrugCode"] . "', 
                'A', 
                '" . $dataArray["LotNumber"] . "', 
                '" . $dataArray["StoreType"] . "', 
                '" . $dataArray["StoreID"] . "', 
                '" . $dataArray["AreaNo"] . "', 
                '" . $dataArray["BlockNo"] . "', 
                '" . $dataArray["StockQty"] . "', 
                druginfo.MakerID, 
                druginfo.MakerName,
                '" . $dataArray["Remark"] . "', 
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
function getDataArray() {
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
        echo "支出作業紀錄成功<br>";
    } else {
        echo "支出作業紀錄失敗<br>";
    }
}
function drugIN_record($dataArray, $record_SQL, $connection)
{

    if ($connection->query($record_SQL)) {
        echo "收入作業紀錄成功<br>";
    } else {
        echo "收入作業紀錄失敗<br>";
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

mysqli_close($connection);
//echo "結束 以下註解<br>"
?>