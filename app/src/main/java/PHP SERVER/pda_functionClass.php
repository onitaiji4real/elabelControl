<?php
//其他副程式會使用到的function 都包裝在檔案
//另外這個檔案會執行一些 比較common的switch class
$DB_HOST = 'localhost'; //DB位置
$DB_USER = 'root'; //DB的登入帳號
$DB_NAME = 'baiguo_demo'; //DB名稱
$DB_PASSWORD = 'myt855myt855'; //DB密碼
$AIMS_HOST = '192.168.5.130'; //AIMS 位置
ini_set('memory_limit', '1024M');

ini_set('default_charset', 'UTF-8'); //編碼
ini_set('json_encode_options', JSON_UNESCAPED_UNICODE); //JSON 編碼

isset($_GET["ElabelNumber"]) ? $ElabelNumber = $_GET["ElabelNumber"] : null;

isset($_GET["UserID"]) ? $UserID = $_GET["UserID"] : null;
isset($_GET["TotalQty"]) ? $TotalQty = $_GET["TotalQty"] : null;
isset($_GET["spinnerText"]) ? $spinnerText = $_GET["spinnerText"] : null;
isset($_GET["DrugLabel"]) ? $DrugLabel = $_GET["DrugLabel"] : null;

global $connection;

$func_Collect = new func_Collect();


if (isset($_GET["DBoption"])) {
    $DBoption = $_GET["DBoption"];

    switch ($DBoption) {

        case "GET": //取得掃描電子紙後的資料
            if (isset($_GET["ElabelNumber"])) {
                $ElabelNumber = $_GET["ElabelNumber"];
                $func_Collect->getJSON($ElabelNumber, $connection);
            }
            break;

        case "getConnectionStatus":
            $func_Collect->getConnectionStatus();
            break;

        case "ITEM_BLINK":
            $func_Collect->BlinkElabel("CYAN", "1", $ElabelNumber, $aims_host);
            break;
    }
}
class func_Collect
{
    private $connection;
    public function __construct()
    {
        $this->initConnection();
    }
    private function initConnection()
    {
        global $DB_HOST, $DB_USER, $DB_PASSWORD, $DB_NAME;
        global $connection;
        $connection = new mysqli($DB_HOST, $DB_USER, $DB_PASSWORD, $DB_NAME);
        if ($connection->connect_error) {
            die("連線失敗: " . $connection->connect_error);
        }
        $connection->set_charset("utf8");
        $this->connection = $connection;
    }
    public function getConnectionStatus()
    {
        $SERVER_STATUS = true;
        $DB_CONNECT_STATUS = $this->connection ? true : false;
        $MESSAGE = $DB_CONNECT_STATUS ? "連線成功" : "連線失敗";

        $response = [
            'SERVER_STATUS' => $SERVER_STATUS,
            'DB_CONNECT_STATUS' => $DB_CONNECT_STATUS,
            'MESSAGE' => $MESSAGE
        ];

        $RESPONSE = $this->my_json_encode($response);
        echo $RESPONSE;
    }

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
    //BlinkElabel("CYAN","1","05DBCD6FB69F",$aims_host);
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
    function getInsertOrUpdateSQL()
    {
        return "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ? ;";
    }
    function drugIN($connection, $dataArray, $ElabelNumber) //收入function
    {
        $response = array();
        $insertOrUpdateSQL = "INSERT INTO drugstock (DrugCode, StoreID, AreaNo, BlockNo, LotNumber, MakeDate, EffectDate, StockQty)
                          VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                          ON DUPLICATE KEY UPDATE StockQty = IFNULL(StockQty, 0) + ?";

        $recordInsertSQL = "INSERT INTO drugadd
                        (AddTime, DrugCode, CodeID, LotNumber, StoreType, StoreID, AreaNo, BlockNo, AddQty, MakerID, MakerName, Remark, UserID, ShiftNo, DrugName, DrugEnglish, EffectDate, StockQty) 
                        SELECT 
                            NOW(),
                            ?,
                            ?,
                            ?,
                            (SELECT StoreType FROM store WHERE StoreID = ?),
                            ?,
                            ?,
                            ?,
                            ?,
                            druginfo.MakerID,
                            druginfo.MakerName,
                            CONCAT(?, '-', (SELECT CodeDesc FROM codetable WHERE CodeID = ?)),
                            ?,
                            (SELECT IFNULL(MAX(ShiftNo) + 1, 1) FROM drugadd AS temp WHERE LotNumber = ?),
                            druginfo.DrugName,
                            druginfo.DrugEnglish,
                            drugstock.EffectDate,
                            drugstock.StockQty
                        FROM 
                            druginfo 
                        JOIN 
                            drugstock ON druginfo.DrugCode = drugstock.DrugCode
                        WHERE 
                            druginfo.DrugCode = ?
                            AND drugstock.LotNumber = ?";



        $updateElabelDrugSQL = "UPDATE elabeldrug 
                            SET 
                                DrugCode3 = ?,
                                DrugName3 = (SELECT EffectDate FROM drugstock 
                                            WHERE LotNumber = ?
                                            AND DrugCode = ?
                                            AND StoreID = ?
                                            AND AreaNo = ? 
                                            AND BlockNo =?), 
                                DrugEnglish3 = (SELECT StockQty FROM drugstock 
                                            WHERE LotNumber = ?
                                            AND DrugCode = ?
                                            AND StoreID = ?
                                            AND AreaNo = ? 
                                            AND BlockNo =?)
                            WHERE 
                                ElabelNumber = ?";
        //AND DrugCode = ?
        $stmt = $connection->prepare($insertOrUpdateSQL);

        if ($stmt === false) {
            // $item = new stdClass;
            // $item -> response = '"收入作業準備陳述式失敗: "' . $connection->error;
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";

            //die("收入作業準備陳述式失敗: " . $connection->error . "<br>");

        }

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

        if ($stmt->execute() === true) {

            $DRUGSTOK_STATUS = true;
            $DRUGSTOK_MESSAGE = "成功更新drugstock資料表";
            // $DRUGSTOK_AFFECTEDROWS = $stmt->affected_rows;
            //$affectedRows = $stmt->affected_rows;
            //$item -> response1 = '"drugStock收入作業資料插入成功，影響了"'. $affectedRows." 行";
            //echo "drugStock收入作業資料插入成功，影響了 {$affectedRows} 行<br>";

        } else {
            //$affectedRows = $stmt->affected_rows;
            //$item -> response1 = 'drugStock收入作業插入資料時發生錯誤: "' . $stmt->error;
            //echo "drugStock收入作業插入資料時發生錯誤: " . $stmt->error . "<br>";
            $DRUGSTOK_STATUS = false;
            $DRUGSTOK_MESSAGE = "更新drugstock資料表 失敗" . $stmt->error;
            // $DRUGSTOK_AFFECTEDROWS = $stmt->affected_rows;
        }

        $stmt->close();

        $recordStmt = $connection->prepare($recordInsertSQL);
        if ($recordStmt === false) {
            // $affectedRows = $stmt->affected_rows;
            // $item = new stdClass;
            // $item -> response = 'drugadd 表準備陳述式失敗: "' . $connection->error  ;
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            //die("drugadd 表準備陳述式失敗: " . $connection->error . "<br>");
        }
        $recordStmt->bind_param(
            "ssssssssssssss",
            $dataArray["DrugCode"],
            $dataArray["ReMark_CodeID"],
            $dataArray["LotNumber"],
            $dataArray["StoreID"],
            $dataArray["StoreID"],
            $dataArray["AreaNo"],
            $dataArray["BlockNo"],
            $dataArray["StockQty"],
            $dataArray["ReMark_CodeID"],
            $dataArray["ReMark_CodeID"],
            $dataArray["UserID"],
            $dataArray["LotNumber"],
            $dataArray["DrugCode"],
            $dataArray["LotNumber"]

        );


        if ($recordStmt->execute() === true) {
            // $affectedRows = $stmt->affected_rows;
            // //$item = new stdClass;
            // $item -> response = 'drugADD紀錄成功"';
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            // //echo "drugADD紀錄成功<br>";
            $DRUGADD_STATUS = true;
            $DRUGADD_MESSAGE = "成功更新drugadd資料表";
            // $DRUGADD_AFFECTEDROWS = $stmt->affected_rows;
        } else {
            // $affectedRows = $stmt->affected_rows;
            // $item = new stdClass;
            // $item -> response = 'drugADD紀錄成功'.$recordStmt->error;
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            // //echo "drugADD紀錄失敗: " . $recordStmt->error . "<br>";
            $DRUGADD_STATUS = false;
            $DRUGADD_MESSAGE = "更新drugadd資料表 失敗" . $stmt->error;
            // $DRUGADD_AFFECTEDROWS = $stmt->affected_rows;
        }

        $recordStmt->close();

        $updateStmt = $connection->prepare($updateElabelDrugSQL);

        if ($updateStmt === false) {
            // $affectedRows = $stmt->affected_rows;
            // $item = new stdClass;
            // $item -> response = '更新 elabeldrug 表準備陳述式失敗: '.$connection->error;
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            // //die("更新 elabeldrug 表準備陳述式失敗: " . $connection->error . "<br>");
        }

        $updateStmt->bind_param(
            "ssssssssssss",
            $dataArray["LotNumber"],
            $dataArray["LotNumber"],
            $dataArray["DrugCode"],
            $dataArray["StoreID"],
            $dataArray["AreaNo"],
            $dataArray["BlockNo"],
            $dataArray["LotNumber"],
            $dataArray["DrugCode"],
            $dataArray["StoreID"],
            $dataArray["AreaNo"],
            $dataArray["BlockNo"],
            $ElabelNumber
        );

        if ($updateStmt->execute() === true) {
            if ($updateStmt->affected_rows > 0) {
                //     $affectedRows = $stmt->affected_rows;
                // //$item = new stdClass;
                // $item -> response = 'Successfully Update elabeldrug table.';
                // $response[] = $item;
                // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
                // echo $json . "<br>";
                //echo "Successfully Update elabeldrug table.<br>";

                $ELABELDRUG_STATUS = true;
                $ELABELDRUG_MESSAGE = "成功更新elabeldrug資料表";
                // $ELABELDRUG_AFFECTEDROWS = $stmt->affected_rows;
            } else {
                //     $affectedRows = $stmt->affected_rows;
                // $item = new stdClass;
                // $item -> response = 'No rows updated in elabeldrug table.';
                // $response[] = $item;
                // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
                // echo $json . "<br>";
                //     //echo "No rows updated in elabeldrug table.<br>";
                $ELABELDRUG_STATUS = false;
                $ELABELDRUG_MESSAGE = "更新elabeldrug資料表 失敗" . $stmt->error;
                // $ELABELDRUG_AFFECTEDROWS = $stmt->affected_rows;
            }
        } else {
            // $affectedRows = $stmt->affected_rows;
            // $item = new stdClass;
            // $item -> response = 'Error Update elabeldrug table: '.$updateStmt->error;
            // $response[] = $item;
            // $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            // echo $json . "<br>";
            // echo "Error Update elabeldrug table: " . $updateStmt->error . "<br>";
            $ELABELDRUG_STATUS = false;
            $ELABELDRUG_MESSAGE = "更新elabeldrug資料表 失敗" . $stmt->error;
            // $ELABELDRUG_AFFECTEDROWS  = $stmt->affected_rows;
        }

        $updateStmt->close();




        $response = [
            'DRUGSTOK_STATUS' => $DRUGSTOK_STATUS,
            'DRUGSTOK_MESSAGE' => $DRUGSTOK_MESSAGE,
            
            'DRUGADD_STATUS' => $DRUGADD_STATUS,
            'DRUGADD_MESSAGE' => $DRUGADD_MESSAGE,
            
            'ELABELDRUG_STATUS' => $ELABELDRUG_STATUS,
            'ELABELDRUG_MESSAGE' => $ELABELDRUG_MESSAGE,
            

        ];

        $RESPONSE = $this->my_json_encode($response);
        echo $RESPONSE;

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
        $dataArray = array(
            "DrugCode" => isset($_GET["DrugCode"]) ? $_GET["DrugCode"] : null,
            "StoreID" => isset($_GET["StoreID"]) ? $_GET["StoreID"] : null,
            "AreaNo" => isset($_GET["AreaNo"]) ? $_GET["AreaNo"] : null,
            "BlockNo" => isset($_GET["BlockNo"]) ? $_GET["BlockNo"] : null,
            "LotNumber" => isset($_GET["LotNumber"]) ? $_GET["LotNumber"] : null,
            "MakeDate" => isset($_GET["MakeDate"]) ? $_GET["MakeDate"] : null,
            "EffectDate" => isset($_GET["EffectDate"]) ? $_GET["EffectDate"] : null,
            "StockQty" => isset($_GET["StockQty"]) ? $_GET["StockQty"] : null,
            "StoreType" => isset($_GET["StoreType"]) ? $_GET["StoreType"] : null,
            "Remark" => isset($_GET["Remark"]) ? $_GET["Remark"] : null,
            "UserID" => isset($_GET["UserID"]) ? $_GET["UserID"] : null,
            "ReMark_CodeID" => isset($_GET["ReMark_CodeID"]) ? $_GET["ReMark_CodeID"] : null,
            "Search_KEY" => isset($_GET["Search_KEY"]) ? $_GET["Search_KEY"] : null
        );

        return $dataArray;
    }

    function drugOUT_record($dataArray, $record_SQL, $connection)
    {
        if ($connection->query($record_SQL)) {
            echo "紀錄成功<br>";
        } else {
            echo "紀錄失敗" . mysqli_error($connection) . ".<br>";
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
    function drugOUT($dataArray, $SQL, $connection)
    {
        $stmt = $connection->prepare($SQL);
        if ($stmt === false) {
            die("準備陳述式失敗: " . $connection->error);
        }

        // 將資料綁定至陳述式的參數
        $stmt->bind_param(
            "ssssss",
            $dataArray["StockQty"],
            $dataArray["LotNumber"],
            $dataArray["DrugCode"],
            $dataArray["StoreID"],
            $dataArray["AreaNo"],
            $dataArray["BlockNo"],
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
    function drugIN_2($dataArray, $SQL, $connection)
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
            $affectedRows = $stmt->affected_rows;
            echo "收入作業資料插入成功，影響了 {$affectedRows} 行<br>";
        } else {
            echo "收入作業插入資料時發生錯誤: " . $stmt->error . "<br>";
        }

        // 關閉陳述式
        $stmt->close();
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
    function getInventory_record($connection)
    {
        // $SQL = "SELECT i.*, ed.ElabelType, di.DrugName
        // FROM baiguo_demo.inventory AS i
        // JOIN elabeldrug AS ed ON i.DrugCode = ed.DrugCode
        // JOIN druginfo AS di ON i.DrugCode = di.DrugCode";
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

    function get_Store_withDrugLabel($dataArray, $connection)
    {
        // $SQL = "SELECT ElabelNumber, StoreID, ElabelType, DrugCode, DrugName, DrugEnglish, AreaNo, BlockNo, DrugCode3, DrugName3,DrugEnglish3
        //         FROM elabeldrug
        //         WHERE DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')
        //  ";
        $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
        FROM elabeldrug ed
        JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
        WHERE ed.DrugCode = (SELECT DrugCode FROM druginfo WHERE DrugLabel = '" . $dataArray["DrugLabel"] . "')";


        $result = mysqli_query($connection, $SQL);
        $response = array(); // Create an empty array to store the response

        if ($result) {
            if (mysqli_affected_rows($connection) > 0) {
                // Fetch and process each row
                while ($row = mysqli_fetch_assoc($result)) {
                    $item = new stdClass();
                    $item->Result = true;
                    $item->Response = '成功執行搜尋!!';
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
                    $item->MakeDate = $row['MakeDate'];

                    $response[] = $item; // Add the item to the response array
                }
            } else {
                $item = new stdClass();
                $item->Result = false;
                $item->Response = '無搜尋結果。';
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
    //搜尋不同的關鍵字
    function get_Store_with_DrugCode($dataArray, $connection)
    {
        $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
            FROM elabeldrug ed
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
            WHERE ed.DrugCode LIKE '%" . $dataArray["Search_KEY"] . "%'";



        $result = mysqli_query($connection, $SQL);
        $response = array(); // Create an empty array to store the response

        if ($result) {
            if (mysqli_affected_rows($connection) > 0) {
                // Fetch and process each row
                while ($row = mysqli_fetch_assoc($result)) {
                    $item = new stdClass();
                    $item->Result = true;
                    $item->Response = '成功執行搜尋!!';
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
                    $item->MakeDate = $row['MakeDate'];

                    $response[] = $item; // Add the item to the response array
                }
            } else {
                $item = new stdClass();
                $item->Result = false;
                $item->Response = '無搜尋結果。';
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
    function get_Store_with_DrugEnlgish($dataArray, $connection)
    {
        $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
            FROM elabeldrug ed
            JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
            WHERE ed.DrugEnglish LIKE '%" . $dataArray["Search_KEY"] . "%'";



        $result = mysqli_query($connection, $SQL);
        $response = array(); // Create an empty array to store the response

        if ($result) {
            if (mysqli_affected_rows($connection) > 0) {
                // Fetch and process each row

                while ($row = mysqli_fetch_assoc($result)) {
                    $item = new stdClass();
                    $item->Result = true;
                    $item->Response = '成功執行搜尋!!';
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
                    $item->MakeDate = $row['MakeDate'];

                    $response[] = $item; // Add the item to the response array
                }
            } else {
                $item = new stdClass();
                $item->Result = false;
                $item->Response = '無搜尋結果。';
                $response[] = $item;
            }
        } else {
            $item = new stdClass();
            $item->Result = false;
            $item->Response = 'Error Search table: ' . mysqli_error($connection);
            $response[] = $item;
        }

        $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        echo $json . "<br>";
    }
    function get_Store_with_DrugName($dataArray, $connection)
    {
        $SQL = "SELECT ed.ElabelNumber, ed.StoreID, ed.ElabelType, ed.DrugCode, ed.DrugName, ed.DrugEnglish, ed.AreaNo, ed.BlockNo, ed.DrugCode3, ed.DrugName3, ed.DrugEnglish3, ds.MakeDate
    FROM elabeldrug ed
    JOIN drugstock ds ON ed.DrugCode3 = ds.LotNumber AND ed.AreaNo = ds.AreaNo AND ed.BlockNo = ds.BlockNo AND ed.DrugCode = ds.DrugCode AND ed.StoreID = ds.StoreID
    WHERE ed.DrugName LIKE '%" . $dataArray["Search_KEY"] . "%'";

        $result = mysqli_query($connection, $SQL);
        $response = array(); // Create an empty array to store the response

        if ($result) {
            if (mysqli_affected_rows($connection) > 0) {
                // Fetch and process each row
                while ($row = mysqli_fetch_assoc($result)) {
                    $item = new stdClass();
                    $item->Result = true;
                    $item->Response = '成功執行搜尋!!';
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
                    $item->MakeDate = $row['MakeDate'];

                    $response[] = $item; // Add the item to the response array
                }
            } else {
                $item = new stdClass();
                $item->Result = false;
                $item->Response = '無搜尋結果。';
                $response[] = $item;
            }
        } else {
            $item = new stdClass();
            $item->Result = false;
            $item->Response = 'Error Search table: ' . mysqli_error($connection);
            $response[] = $item;
        }

        $json = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        echo $json . "<br>";
    }

}


?>
