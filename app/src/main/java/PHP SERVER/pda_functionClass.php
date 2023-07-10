<?php
class func_Collect
{
    function my_json_encode($data)
    {
        return json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
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
    
}


?>