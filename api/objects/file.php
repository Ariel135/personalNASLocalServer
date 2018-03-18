<?php
class File{
 
    // database connection and table name
    private $conn;
    private $table_name = "files";
 
    // object properties
    public $id;
    public $created;
    public $in;
    public $modified;
    public $name;
    public $path;
 
    // constructor with $db as database connection
    public function __construct($db){
        $this->conn = $db;
    }
    // read products
    function read(){
 
        $query = "SELECT * 
                FROM " . $this->table_name . " f 
                WHERE f.in = ? AND f.id != ?";

        // prepare query statement
        $stmt = $this->conn->prepare($query);

          // bind id of product to be updated
        $stmt->bindParam(1, $this->id);
        $stmt->bindParam(2, $this->id);
        // execute query
        $stmt->execute();
     
        return $stmt;
    }

    
}