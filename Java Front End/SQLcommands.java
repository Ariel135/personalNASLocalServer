/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Deon
 */
public class SQLcommands {

    public static void createTable(Connection c, String tableName, String cols[]) {
        String sql = "CREATE TABLE " + tableName + " (";
        for (int i = 0; i < cols.length; i++) {
            sql += cols[i] + (i < cols.length - 1 ? " ," : "");
        }
        sql += ")";
        //System.out.println(sql);
        try {
            c.createStatement().executeUpdate(sql);
            System.out.println("Table successfully created in database...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteTable(Connection c, String tableName) {
        try {
            c.createStatement().executeUpdate("DROP TABLE " + tableName);
            System.out.println("Table successfully deleted in database...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insert(Connection c, String tableName, String values[]) {
        String sql = "INSERT INTO " + tableName + " VALUES (";
        for (int i = 0; i < values.length; i++) {
            sql += values[i] + (i < values.length - 1 ? " ," : "");
        }
        sql += ")";
        //System.out.println(sql);
        try {
            c.createStatement().executeUpdate(sql);
            System.out.println("Table successfully updated in database...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void delete(Connection c, String tableName, String value) {
        String sql = "DELETE FROM " + tableName + " WHERE " + value;

        //System.out.println(sql);
        try {
            c.createStatement().executeUpdate(sql);
            System.out.println("Table successfully updated in database...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void update(Connection c, String tableName, String value) {
        String sql = "UPDATE " + tableName + " SET " + value;
        try {
            c.createStatement().executeUpdate(sql);
            System.out.println("Table successfully updated in database...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
