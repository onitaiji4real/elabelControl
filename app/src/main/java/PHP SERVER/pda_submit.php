<?php
$host = '192.168.5.41';
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

            // $SQL = "INSERT INTO inventory 
            //                     (InvDate, DrugCode, StoreID, AreaNo, BlockNo, LotNumber, StockQty, InventoryQty, AdjQty, ShiftNo, InvTime, UserId, User)
            //         VALUES 
            //         (CURDATE(), '$DrugCode', '$StoreID', '$AreaNo', '$BlockNo', '$LotNumber', '$StockQty', '$InventoryQty', '$AdjQty', '1', CURTIME(), '$UserId', '$User')";
            // $result = mysqli_query($connection, $SQL);

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
    }


}


// if (isset($_GET["ElabelNumber"])) {
//     $ElabelNumber = $_GET["ElabelNumber"];
//    getJSON($ElabelNumber,$connection);

// }


// isset($_GET["DBoption"]) ? $DBoptino = $_GET["DBoption"] : null;
// if (isset($_GET["DBoption"])) {

//     $DBoptino = $_GET["DBoption"];

//     switch ($DBoption) {

//         case "in":
//             $response = array(
//                 "DBoption" => $DBoption,
//                 "AreaNo" => $AreaNo,
//                 "BlockNo" => $BlockNo,
//                 "BlockType" => $ElabelType,
//                 "DrugCode" => $DrugCode,
//                 "StoreID" => $StoreID,
//                 "ElabelNumber" => $ElabelNumber,
//                 "DrugName" => $DrugName,
//                 "DrugEnglish" => $DrugEnglish,
//                 "StockQty" => $StockQty,
//                 "TotalQty" => $TotalQty,
//                 "MakerID" => $MakerID,
//                 "MakerName" => $MakerName,
//                 "EffectDate" => $EffectDate,
//                 "spinnerText" => $spinnerText,
//                 "LotNumber" => $LotNumber
//             );

//             $sum = $response["StockQty"] + $response["TotalQty"]; //加總庫存與要變更的數量

//             $sql = "UPDATE drugstock 
//                         SET StockQty = '$sum' 
//                         WHERE 
//                         AreaNo = '{$response["AreaNo"]}' AND 
//                         BlockNo = '{$response["BlockNo"]}' AND
//                          DrugCode = '{$response["DrugCode"]}' AND
//                           StoreID = '{$response["StoreID"]}' ";

//             if (mysqli_query($connection, $sql)) {
//                 //echo "UPDATE Successful<br>";
//             } else {
//                 //echo "UPDATE FAIL<br>";
//             } //卡位

//             $recordSQL = "INSERT INTO drugadd 
//                 (AddTime, DrugCode, LotNumber, 
//                 StoreType, StoreID, AreaNo, 
//                 BlockNo, AddQty, MakerID, 
//                 MakerName, Remark, UserID, 
//                 DrugName, DrugEnglish, 
//                 EffectDate, StockQty, CodeID, ShiftNo) 
//                 VALUES (
//                     NOW(),
//                     '{$response["DrugCode"]}',
//                     '{$response["LotNumber"]}',
//                     '{$response["BlockType"]}',
//                     '{$response["StoreID"]}',
//                     '{$response["AreaNo"]}',
//                     '{$response["BlockNo"]}',
//                     '{$response["TotalQty"]}',
//                     '{$response["MakerID"]}',
//                     '{$response["MakerName"]}',
//                     '{$response["spinnerText"]}',
//                     '$UserID',
//                     '{$response["DrugName"]}',
//                     '{$response["DrugEnglish"]}',
//                     '{$response["EffectDate"]}',
//                     '{$response["TotalQty"]}',
//                     'A',
//                     '1'
//                 )";

//             if (mysqli_query($connection, $recordSQL)) {
//                 //echo "RECORD Successful<br>";
//             } else {
//                 //echo "RECORD FAIL<br>";
//             }
//             break;


//         case "select":

//             break;

//         case "inventory":

//             break;
//     }
// }

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
    di.MakerName
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
                $row["EffectDate"]
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

mysqli_close($connection);
//echo "結束 以下註解<br>"
?>