/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Deon
 */
public class database {

    public static Connection getConnection(String path) throws SQLException {
        Connection c = null;
        String host = "jdbc:mysql://" + path;
        String uName = "root";
        String uPass = "Welcome2";
        c = DriverManager.getConnection(host, uName, uPass);

        return c;
    }
    
    public static boolean checkUser(Connection c,String id)
    {
        return true;
    }
    
    public static void addUser(String pi,String u,String p)throws SQLException
    {
        Connection c = getConnection(pi);
        String cols[] = {"ID","USER","PW"};
        SQLcommands.createTable(c, "piUsers",cols);
        String cols2[] = {"1",u,p};
        SQLcommands.insert(c, p, cols2);
        
    }
}
