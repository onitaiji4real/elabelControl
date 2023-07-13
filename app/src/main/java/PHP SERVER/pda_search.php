<?php
include("pda_functionClass.php"); //匯入資料庫連線檔案
$func_Collect = new func_Collect();

if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

        case "Search_BY_Drug_Label": {

                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = array(
                    "DrugLabel" => $_GET["DrugLabel"]
                );

                $func_Collect->get_Store_withDrugLabel($dataArray, $connection);
                break;
            }
        case "Search_BY_DrugCode": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = $func_Collect->getDataArray();
                $func_Collect->get_Store_with_DrugCode($dataArray, $connection);
                break;
            }
        case "Search_BY_DrugEnglish": {
                // if (isset($_GET["ElabelNumber"])) {
                //     $ElabelNumber = $_GET["ElabelNumber"];
                // }
                isset($_GET["ElabelNumber"]) ? $ElabelNumber = $_GET["ElabelNumber"] : null;
                
                $dataArray = $func_Collect->getDataArray();
                $func_Collect->get_Store_with_DrugEnlgish($dataArray, $connection);
                break;
            }
        case "Search_BY_DrugName": {
                if (isset($_GET["ElabelNumber"])) {
                    $ElabelNumber = $_GET["ElabelNumber"];
                }
                $dataArray = $func_Collect->getDataArray();
                $func_Collect->get_Store_with_DrugName($dataArray, $connection);
                break;
            }


    }
}

?>