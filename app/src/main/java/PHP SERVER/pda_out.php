<?php
include("pda_connection.php");
$connection = $GLOBALS['connection'];

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

        case "OUT": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }

                $dataArray = $func_Collect->getDataArray();

                $SQL = "UPDATE drugstock 
                    SET StockQty = StockQty - ?
                    WHERE LotNumber = ?
                    AND DrugCode = ?
                    AND StoreID = ?
                    AND AreaNo = ?
                    AND BlockNo =? ";

                $func_Collect->drugOUT($dataArray, $SQL, $connection);

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

                $func_Collect->drugOUT_record($dataArray, $record_SQL, $connection);
                $func_Collect->update_elabeldrug($ElabelNumber, $dataArray, $connection);
                $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
                break;
            }

    }
}

?>