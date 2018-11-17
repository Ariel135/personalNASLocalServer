/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edp;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.scene.text.Text;

/**
 *
 * @author Deon
 */
public class RegisterPanel 
{
    @FXML
    private ImageView h_valid,h_invalid;
    private AnchorPane p_register;
    private Button b_reg,reg_fin;
    private TextField reg_pi,reg_user;
    private PasswordField reg_pw1,reg_pw2;
    private Text reg_ermsg;
    
    private boolean userStatus;//0 new 1 used
    private boolean inputStatus1,inputStatus2;//0 bad 1 good
    private String users;//file name to be read
    
    public RegisterPanel(String users)
    {
        this.users = users;        
    }
    
    //checks that the user does not already exist
    public void confirmId() 
    {
        String pi = reg_pi.getText();
        String id = reg_user.getText();
        
        try{
            Connection c = database.getConnection(pi);
            inputStatus1 = database.checkUser(c,id);
            if(!inputStatus1)            
                throw new IllegalArgumentException();
            reg_ermsg.setText("");
        }
        catch(SQLException e){            
            reg_ermsg.setText("Cannot connect to NAS");     
       }
        catch(IllegalArgumentException e2){
            reg_ermsg.setText("User already exisits");
        }
        
    }    
    
    //checks that both passwords entered are identical
    public void confirmPasswords()
    {
        String pw1 = reg_pw1.getText();
        String pw2 = reg_pw2.getText();
        inputStatus2 = pw1.equals(pw2);
        h_valid.setVisible(inputStatus2);
        h_invalid.setVisible(!inputStatus2);
        if(!inputStatus2)
            reg_ermsg.setText("Password mismatch");        
    }
        
    //If all fields are valid , add user to table
    public void continueRegistration()
    {
        if(inputStatus1 & inputStatus2)
        {
            try{
                database.addUser(reg_pi.getText(),reg_user.getText(),reg_pw1.getText());
                       
            System.out.println(reg_pi.getText());
            System.out.println(reg_user.getText());
            System.out.println(reg_pw1.getText());
            System.out.println(reg_pw2.getText()); 
            }
            catch(SQLException e){
                reg_ermsg.setText(e.getMessage());
            }
        }        
    }
    
    
    
    
    
    
    
}
