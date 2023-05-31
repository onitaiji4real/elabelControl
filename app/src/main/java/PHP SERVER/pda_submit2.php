<?php
$dbConfig = [
    'host' => '192.168.5.41',
    'username' => 'root',
    'password' => 'myt855myt855',
    'db' => 'baiguo_demo'
];

$connection = new mysqli($dbConfig['host'], $dbConfig['username'], $dbConfig['password'], $dbConfig['db']);

if ($connection->connect_error) {
    die("Connection failed: " . $connection->connect_error);
}

$connection->set_charset("utf8");

$params = [
    'AreaNo' => $_GET["AreaNo"] ?? null,
    'BlockNo' => $_GET["BlockNo"] ?? null,
    'BlockType' => $_GET["BlockType"] ?? null,
    'DrugCode' => $_GET["DrugCode"] ?? null,
    'TotalQty' => $_GET["TotalQty"] ?? null,
    'StoreID' => $_GET["StoreID"] ?? null,
    'DBoption' => $_GET["DBoption"] ?? null,
    'ElabelNumber' => $_GET["ElabelNumber"] ?? null,
    'DrugEnglish' => $_GET["DrugEnglish"] ?? null,
    'spinnerText' => $_GET["spinnerText"] ?? null,
    'UserID' => $_GET["UserID"] ?? null
];

switch ($params['DBoption']) {
    case 'in':
        updateDrugStock($connection, $params, 'add');
        insertDrugAdd($connection, $params);
        break;
    case 'select':
        selectElabelDrug($connection, $params);
        break;
    case 'out':
        updateDrugStock($connection, $params, 'subtract');
        insertDrugAdd($connection, $params);
        break;
}

$connection->close();

function updateDrugStock($connection, $params, $operation) {
    $sql = "SELECT StockQty FROM drugstock WHERE AreaNo = ? AND BlockNo = ? AND DrugCode = ? AND StoreID = ?";
    $stmt = $connection->prepare($sql);
    $stmt->bind_param("ssss", $params['AreaNo'], $params['BlockNo'], $params['DrugCode'], $params['StoreID']);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $qty = $row['StockQty'];
        $sumQty = $operation === 'add' ? $qty + $params['TotalQty'] : $qty - $params['TotalQty'];
        
        $updateSql = "UPDATE drugstock SET StockQty = ? WHERE AreaNo = ? AND BlockNo = ? AND DrugCode = ? AND StoreID = ?";
        $updateStmt = $connection->prepare($updateSql);
        $updateStmt->bind_param("issss", $sumQty, $params['AreaNo'], $params['BlockNo'], $params['DrugCode'], $params['StoreID']);
        $updateStmt->execute();
        
        echo $updateStmt->affected_rows > 0 ? "Update Success" : "Update Fail";
    } else {
        echo "No results found";
    }
}

function insertDrugAdd($connection, $params) {
    // Similar to your original drugAdd_SQL
    $drugAddSql = "INSERT INTO drugadd (AddTime, DrugCode, ...) SELECT NOW(), ds.DrugCode, ...";
    // Execute the statement, bind parameters and process results
    // More logic here...
}

function selectElabelDrug($connection, $params) {
    // Similar to your original ElabelDrug_SQL
    $elabelDrugSql = "SELECT * FROM drugstock WHERE ElabelNumber = ?";
    // Execute the statement, bind parameters and process results
    // More logic here...
}
?>
