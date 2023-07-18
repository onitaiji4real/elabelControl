<?php
include("pda_functionClass.php"); //匯入資料庫連線檔案
$func_Collect = new func_Collect();

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

        case "IN": { //收入

            if (isset($_GET["ElabelNumber"])) {
                $ElabelNumber = $_GET["ElabelNumber"];
            }
            $dataArray = $func_Collect->getDataArray();
            //$remark_CodeID = "B";
            $func_Collect->drugIN($connection, $dataArray, $ElabelNumber);
            $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $AIMS_HOST);

            break;
        }
    }
}

?>