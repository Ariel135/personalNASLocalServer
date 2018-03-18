<?php
class Folder{
 
    // database connection and table name
    private $conn;
    private $table_name = "folder";
 
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

        /*// select all query
        $query = "SELECT
                     fo1.name, fo1.id, fo1.created, f.in, f.modified, f.name, f.path
                FROM
                    " . $this->table_name . " f, files fi, folder f1
                    LEFT JOIN
                        categories c
                            ON p.category_id = c.id
                ORDER BY
                    p.created DESC";*/
     
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