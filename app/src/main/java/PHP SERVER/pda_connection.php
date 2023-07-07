<?php

date_default_timezone_set('Asia/Taipei');
ini_set('default_charset', 'UTF-8'); //編碼
ini_set('json_encode_options', JSON_UNESCAPED_UNICODE); //JSON 編碼

$SERVER_STATUS = true; //給PDA判斷連線狀態
$DB_CONNECT_STATUS = false; //給PDA判斷連線狀態

$DB_HOST = '192.168.5.42'; //DB位置
$DB_USER = 'root'; //DB的登入帳號
$DB_NAME = 'baiguo.demo'; //DB名稱
$DB_PASSWORD = 'myt855myt855'; //DB密碼
$AIMS_HOST = '192.168.5.130'; //AIMS 位置

$func_Collect = new func_Collect();

try {
    // 建立mysqli物件
    $connection = new mysqli($DB_HOST, $DB_USER, $DB_PASSWORD, $DB_NAME);
    $GLOBALS['connection'] = $connection;

    $connection->set_charset("utf8");

    // 檢查是否成功連線
    if ($connection->connect_error) {

        $DB_CONNECT_STATUS = False;
        $MESSAGE = ("連線失敗: " . $connection->connect_error);

    } else {
        mysqli_set_charset($connection, "utf8");
        $DB_CONNECT_STATUS = True;
        $MESSAGE = "連線成功";

    }

} catch (Exception $e) {

    $DB_CONNECT_STATUS = False;
    $MESSAGE = "連線失敗: " . $e->getMessage();


    $connection = null;
}

$RESPONSE = [
    'SERVER_STATUS' => $SERVER_STATUS,
    'DB_CONNECT_STATUS' => $DB_CONNECT_STATUS,
    'MESSAGE' => $MESSAGE
];

$getRESPONSE = $func_Collect->my_json_encode($RESPONSE);

$OPtion = isset($_GET["OPtion"]) ? $OPtion = $_GET["OPtion"] : null;

switch ($OPtion) {
    case "getConnectionStatus":
        echo $getRESPONSE;
        break;

    default:

        break;
}




class func_Collect
{
    function my_json_encode($data)
    {
        return json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    }

}



?>