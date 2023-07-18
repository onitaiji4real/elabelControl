<?php
include("pda_functionClass.php"); //匯入資料庫連線檔案
$func_Collect = new func_Collect();

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

            $data = $func_Collect->countTotalNumber($dataArray, $connection);
            $countNum = $dataArray["numBox"] * $data["NumBox"];
            $countNum += $dataArray["numRow"] * $data["NumRow"];

            $InventoryQty += $countNum;
            $dataArray["InventoryQty"] = $InventoryQty;

            echo $InventoryQty . "這是數量";




            $dataArray["StockQty"] = $func_Collect->getStockQty($dataArray, $connection); // 取得當前庫存
            $StockQty = $func_Collect->getStockQty($dataArray, $connection); // 取得當前庫存

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

                            $func_Collect->drugIN_record($dataArray, $record_SQL, $connection);
                            $func_Collect->update_elabeldrug($ElabelNumber, $dataArray, $connection);
                            $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
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
                                    $InventoryQty, 
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

                            $func_Collect->drugOUT_record($dataArray, $record_SQL, $connection);
                            $func_Collect->update_elabeldrug($ElabelNumber, $dataArray, $connection);
                            $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
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
                $func_Collect->update_elabeldrug($ElabelNumber, $dataArray, $connection);
                $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
            }

        break;

        case "GET_Inventory_Record": {

            $func_Collect->getInventory_record($connection);

            break;
        }
        
        case "GET":
            if (isset($_GET["ElabelNumber"])) {
                $ElabelNumber = $_GET["ElabelNumber"];
                $func_Collect->getJSON($ElabelNumber, $connection);
            }

            $func_Collect->my_json_encode($_COOKIE);
            
            break;
        

    }
}



?>