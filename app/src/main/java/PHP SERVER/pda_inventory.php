<?php
include("pda_connection.php"); //匯入資料庫連線檔案
$connection = $GLOBALS["connection"];

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

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

        case "GET_Inventory_Record": {

            getInventory_record($connection);

            break;
        }
        
        case "GET":
            if (isset($_GET["ElabelNumber"])) {
                $ElabelNumber = $_GET["ElabelNumber"];
                getJSON($ElabelNumber, $connection);
            }

            $func_Collect->my_json_encode($_COOKIE);
            
            break;
        

    }
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
function drugIN_record($dataArray, $record_SQL, $connection)
{

    if ($connection->query($record_SQL)) {
        echo "紀錄成功<br>";
    } else {
        echo "紀錄失敗" . mysqli_error($connection) . "<br>";
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

?>