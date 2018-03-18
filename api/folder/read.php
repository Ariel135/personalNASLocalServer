<?php
// required headers
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
 
// include database and object files
include_once '../config/directory_database.php';
include_once '../objects/folder.php';
 
// instantiate database and product object
$database = new Database();
$db = $database->getConnection();
 
// initialize object
$product = new Folder($db);

$product->id = isset($_GET['id']) ? $_GET['id'] : die();
 
// query products
$stmt = $product->read();
$num = $stmt->rowCount();
 
// check if more than 0 record found
if($num>0){
 
    // products array
    $folders_arr=array();
    $folders_arr["records"]=array();
 
    // retrieve our table contents
    // fetch() is faster than fetchAll()
    // http://stackoverflow.com/questions/2770630/pdofetchall-vs-pdofetch-in-a-loop
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)){
        // extract row
        // this will make $row['name'] to
        // just $name only
        extract($row);
 
        $folder_item=array(
            "id" => $id,
            "name" => $name,
            "created" => html_entity_decode($created),
            "modifed" => html_entity_decode($modifed),
            "path" => html_entity_decode($path)
        );
 
        array_push($folders_arr["records"], $folder_item);
    }
 
    echo json_encode($folders_arr);
}
 
else{
    echo json_encode(
        array("message" => "No folders found.")
    );
}
?>