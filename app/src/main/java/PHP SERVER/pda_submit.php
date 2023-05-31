<?php
$host = '192.168.5.41'; //数据库位置
$name = 'root'; //帐号
$pwd = 'myt855myt855'; //密码
$db = 'baiguo_demo'; //数据库名称
//header('Content-Type: application/json; charset=UTF-8');



// 建立与MySQL数据库的连接
$connection = mysqli_connect($host, $name, $pwd, $db) or die("Error " . mysqli_error($connection));
mysqli_set_charset($connection, "utf8");
ini_set('display_errors', '0');


$AreaNo = isset($_GET["AreaNo"]) ? $_GET["AreaNo"] : null;
$BlockNo = $_GET["BlockNo"];
$BlockType = $_GET["BlockType"];
$DrugCode = $_GET["DrugCode"];
$TotalQty = $_GET["TotalQty"];
$StoreID = $_GET["StoreID"];
$DBoption = $_GET["DBoption"];
$ElabelNumber = $_GET["ElabelNumber"];
$DrugEnglish = $_GET["DrugEnglish"];
$spinnerText = $_GET["spinnerText"];
$UserID = $_GET["UserID"];

// echo "AreaNo: " . $AreaNo . "<br>";
// echo "BlockNo: " . $BlockNo . "<br>";
// echo "BlockType: " . $BlockType . "<br>";
// echo "DrugCode: " . $DrugCode . "<br>";
// echo "TotalQty: " . $TotalQty . "<br>";
// echo "StoreID: " . $StoreID . "<br>";
// echo "DBoption: " . $DBoption . "<br>";
// echo "ElabelNumber: " . $ElabelNumber . "<br>";
// echo "DrugEnglish: " . $DrugEnglish . "<br>";
// echo "spinnerText: " . $spinnerText . "<br>";
// echo "UserID: " . $UserID . "<br>";

switch ($DBoption) {
    case "in":
        echo "DBoption = " . $DBoption . "<br>";

        //取得原先的庫存數量
        $sql = "SELECT StockQty 
FROM drugstock 
WHERE AreaNo = '$AreaNo' AND BlockNo = '$BlockNo' AND DrugCode = '$DrugCode' AND StoreID = '$StoreID'";
        $result = mysqli_query($connection, $sql);

        if ($result && mysqli_num_rows($result) > 0) {
            $row = mysqli_fetch_assoc($result);
            $Qty = $row["StockQty"];

            //庫存量與要新增的量加總
            $SumQty = $TotalQty + $Qty;
            //update 資料庫的數量
            $updateSql = "UPDATE drugstock 
            SET StockQty = '$SumQty' 
            WHERE AreaNo = '$AreaNo' AND BlockNo = '$BlockNo' AND DrugCode = '$DrugCode' AND StoreID = '$StoreID' ";

            if (mysqli_query($connection, $updateSql)) {
                echo "Update Success";
            } else {
                echo "Update Fail";
            }
        } else {
            echo "No results found";
        }

        $drugAdd_SQL = "INSERT INTO drugadd (AddTime, DrugCode, LotNumber, StoreType, StoreID, AreaNo, BlockNo, AddQty, MakerID, MakerName, Remark, UserID, DrugName, DrugEnglish, EffectDate, StockQty,CodeID,ShiftNo)
        SELECT 
            NOW(), 
            ds.DrugCode, 
            ds.LotNumber, 
            '$BlockType', 
            ds.StoreID, 
            ds.AreaNo, 
            ds.BlockNo, 
            '$TotalQty', 
            di.MakerID, 
            di.MakerName, 
            '$spinnerText', 
            '$UserID', 
            di.DrugName, 
            di.DrugEnglish, 
            ds.EffectDate, 
            '$TotalQty',
            'A',
            '1'
        FROM 
            drugstock ds
        JOIN 
            druginfo di ON ds.DrugCode = di.DrugCode
        WHERE 
            ds.DrugCode = '$DrugCode' 
        AND 
            ds.StoreID = '$StoreID' 
        AND 
            ds.BlockNo = '$BlockNo'";

        if (mysqli_query($connection, $drugAdd_SQL)) {
            echo "Record successfully inserted into drugadd table." . "<br>";
        } else {
            echo "ERROR: Could not able to execute DrugAdd" . mysqli_error($connection);
        }

        break;

    case "select":
        //echo "DBoption = " . $DBoption . "<br>";
        // 处理 "select" 情况的逻辑

        $sql = "SELECT 
        ed.StoreID, 
        ed.ElabelType, 
        ed.DrugCode, 
        ed.DrugName, 
        ed.DrugEnglish, 
        ed.AreaNo, 
        ed.BlockNo, 
        ds.StockQty 
        FROM elabeldrug ed 
        INNER JOIN drugstock ds ON 
            ed.StoreID = ds.StoreID AND 
            ed.AreaNo = ds.AreaNo AND 
            ed.BlockNo = ds.BlockNo AND 
            ed.DrugCode = ds.DrugCode
        WHERE ed.ElabelNumber = '$ElabelNumber'";


        $result = mysqli_query($connection, $sql);

        if ($result && mysqli_num_rows($result) > 0) {
            $row = mysqli_fetch_assoc($result);

            $StoreID = $row["StoreID"];
            $ElabelType = $row["ElabelType"];
            $DrugCode = $row["DrugCode"];
            $DrugName = $row["DrugName"];
            $DrugEnglish = $row["DrugEnglish"];
            $AreaNo = $row["AreaNo"];
            $BlockNo = $row["BlockNo"];
            $StockQty = $row["StockQty"];

            // echo "StoreID: " . $StoreID . "<br>";
            // echo "ElabelType: " . $ElabelType . "<br>";
            // echo "DrugCode: " . $DrugCode . "<br>";
            // echo "DrugName: " . $DrugName . "<br>";
            // echo "DrugEnglish: " . $DrugEnglish . "<br>";
            // echo "AreaNo: " . $AreaNo . "<br>";
            // echo "BlockNo: " . $BlockNo . "<br>";
            // echo "StockQty" . $StockQty."<br>";
            // echo "以下為SELECT的json資料<br>";
            $response = array(
                "AreaNo" => $AreaNo,
                "BlockNo" => $BlockNo,
                "BlockType" => $ElabelType,
                "DrugCode" => $DrugCode,
                "StoreID" => $StoreID,
                "DBoption" => $DBoption,
                "ElabelNumber" => $ElabelNumber,
                "DrugName" => $DrugName,
                "DrugEnglish" => $DrugEnglish,
                "StockQty" => $StockQty
            );

            // $json = json_encode($response);
            $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            echo $json . "<br>";


            //header('Content-Type: application/json; charset=UTF-8');


        } else {
            echo "No results found";
        }
        break;

    case "out":
        echo "DBoption = " . $DBoption . "<br>";

        //取得原先的庫存數量
        $sql = "SELECT StockQty 
FROM drugstock 
WHERE AreaNo = '$AreaNo' AND BlockNo = '$BlockNo' AND DrugCode = '$DrugCode' AND StoreID = '$StoreID'";
        $result = mysqli_query($connection, $sql);

        if ($result && mysqli_num_rows($result) > 0) {
            $row = mysqli_fetch_assoc($result);
            $Qty = $row["StockQty"];

            //庫存量與要新增的量加總
            $SumQty = $Qty - $TotalQty;
            //update 資料庫的數量
            $updateSql = "UPDATE drugstock 
            SET StockQty = '$SumQty' 
            WHERE AreaNo = '$AreaNo' AND BlockNo = '$BlockNo' AND DrugCode = '$DrugCode' AND StoreID = '$StoreID' ";

            if (mysqli_query($connection, $updateSql)) {
                echo "Update Success";
            } else {
                echo "Update Fail";
            }
        } else {
            echo "No results found";
        }

        $drugAdd_SQL = "INSERT INTO drugadd (AddTime, DrugCode, LotNumber, StoreType, StoreID, AreaNo, BlockNo, AddQty, MakerID, MakerName, Remark, UserID, DrugName, DrugEnglish, EffectDate, StockQty,CodeID,ShiftNo)
        SELECT 
            NOW(), 
            ds.DrugCode, 
            ds.LotNumber, 
            '$BlockType', 
            ds.StoreID, 
            ds.AreaNo, 
            ds.BlockNo, 
            '$TotalQty', 
            di.MakerID, 
            di.MakerName, 
            '$spinnerText', 
            '$UserID', 
            di.DrugName, 
            di.DrugEnglish, 
            ds.EffectDate, 
            '$TotalQty',
            'A',
            '1'
        FROM 
            drugstock ds
        JOIN 
            druginfo di ON ds.DrugCode = di.DrugCode
        WHERE 
            ds.DrugCode = '$DrugCode' 
        AND 
            ds.StoreID = '$StoreID' 
        AND 
            ds.BlockNo = '$BlockNo'";

        if (mysqli_query($connection, $drugAdd_SQL)) {
            echo "Record successfully inserted into drugadd table." . "<br>";
        } else {
            echo "ERROR: Could not able to execute DrugAdd" . mysqli_error($connection);
        }

        break;

}

mysqli_close($connection);
//echo "結束 以下註解<br>"
?>