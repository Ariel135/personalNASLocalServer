package edp;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;
import java.util.ArrayList;

public class FXMLController implements Initializable {
    @FXML
    private ImageView h_settings, h_documents, h_login, h_valid, h_invalid, h_minimize;
    @FXML
    private AnchorPane p_settings, p_documents, p_login, p_register, foldertest;
    @FXML
    private Button reg_fin, b_reg, b_login;
    @FXML
    private TextField reg_pi, reg_user, doc_path;
    private static TextField path;
    @FXML
    private PasswordField reg_pw1, reg_pw2;
    @FXML
    private Text reg_ermsg;
    @FXML
    private GridPane docs_grid;
    private static GridPane grid;
    @FXML
    private VBox dl_grid;
    private static DDNSConnection c;
    public String type;
    public static final String TYPEFOLDER = "folder", TYPEFILE = "file";
    public JSONObject o;
    private static int currentFolderID = 0;

    @FXML
    private void handleButtonAction(MouseEvent event) {
        Object currentPanel = event.getTarget();
        p_settings.setVisible(false);
        p_documents.setVisible(false);
        p_login.setVisible(false);
        p_register.setVisible(false);
        h_minimize.setVisible(false);
        if (currentPanel == h_settings) {
            p_settings.setVisible(true);
            h_minimize.setVisible(true);
        } else if (currentPanel == h_documents) {
            p_documents.setVisible(true);
            h_minimize.setVisible(true);
            c.updateDisplay(docs_grid, currentFolderID);
        } else if (currentPanel == h_login) {
            p_login.setVisible(true);
            h_minimize.setVisible(true);
       }
    }
    @FXML
    private void piRegister(ActionEvent e) {
        System.out.println("333");
        if (e.getTarget() == reg_fin) {
            try {
                System.out.println(reg_pi.getText());
                System.out.println(reg_user.getText());
                System.out.println(reg_pw1.getText());
                System.out.println(reg_pw2.getText());
                database.addUser(reg_pi.getText(), reg_user.getText(), reg_pw1.getText());

            } catch (SQLException e2) {
                reg_ermsg.setText(e2.getMessage());
            }
        } else if (e.getTarget() == b_reg) {
            p_settings.setVisible(false);
            p_documents.setVisible(false);
            p_login.setVisible(false);
            p_register.setVisible(true);
        }
    }
    private boolean inputStatus1 = true;
    private boolean inputStatus2 = true;
    @FXML
    private void pwcheck() {
        String pw1 = reg_pw1.getText();
        String pw2 = reg_pw2.getText();
        inputStatus2 = pw1.equals(pw2);
        h_valid.setVisible(inputStatus2);
        h_invalid.setVisible(!inputStatus2);
        if (!inputStatus2) {
            reg_ermsg.setText("Password mismatch");
        } else {
            reg_ermsg.setText("");
        }
    }
    @FXML
    private void idcheck() {
        String pi = reg_pi.getText();
        String id = reg_user.getText();
        try {
            Connection c = database.getConnection(pi);
            inputStatus1 = database.checkUser(c, id);
            if (!inputStatus1) {
                throw new IllegalArgumentException();
            }
            reg_ermsg.setText("Success");
        } catch (SQLException e) {
            reg_ermsg.setText("Cannot connect to NAS");
        } catch (IllegalArgumentException e2) {
            reg_ermsg.setText("User already exisits");
        }
    }
    private static ImageView lastClickedImg;
    @FXML
    private void fileInfo(MouseEvent e) {
        if (lastClickedImg != null) {
            lastClickedImg.setOpacity(1);
        } else {
            lastClickedImg = new ImageView();
        }
        ImageView tmp = (ImageView) e.getSource();

        if (!lastClickedImg.equals(tmp)) {
            tmp.setOpacity(0.5);
            lastClickedImg = tmp;
            System.out.println("log>>" + o.get("path").toString());
            String ext = o.get("path").toString();
            path.setText(ext);
        } else {
            lastClickedImg = null;
            path.setText("");
        }
    }
    private static ArrayList<Integer> lastClickedFolderID = new ArrayList<>();
    @FXML
    private void switchFolder(MouseEvent mouseEvent) {
        //double click
        if (mouseEvent.getClickCount() >= 2) {

            int tmp = currentFolderID;
            try {
                if (type.equals(TYPEFOLDER)) {
                    int tmp2 = Integer.parseInt(o.get("id").toString());
                    if (tmp2 != tmp) {
                        currentFolderID = tmp2;
                        lastClickedFolderID.add(tmp);
                        c.updateDisplay(grid, currentFolderID);
                        System.out.print("log>> Switching to folder path : ");
                        lastClickedFolderID.forEach((I) -> {
                            System.out.print("/" + I);
                        });
                        System.out.println("/" + currentFolderID);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Log>> Invalid ID number; Cannot switch folders");
            }
        }
    }
    @FXML
    private void switchPath(MouseEvent e) {
        try {
            c.updateDisplay(docs_grid, lastClickedFolderID.get(lastClickedFolderID.size() - 1));
            System.out.print("log>> Reverting to folder path : ");
            lastClickedFolderID.forEach((I) -> {
                System.out.print("/" + I);
            });
            System.out.println();
            currentFolderID = lastClickedFolderID.get(lastClickedFolderID.size() - 1);
            lastClickedFolderID.remove(lastClickedFolderID.size() - 1);
        } catch (Exception ex) {
            System.out.println("Log>> Something went wrong...Defaulting back to root folder;Path : 1/");
            c.updateDisplay(docs_grid, 1);
            currentFolderID = 1;
        }
    }
    @FXML
    private void downloadLink() {        
        FXMLLoader loader;
        try {
            String s = path.getText();
            String name;
            if (s.contains(".") && s.lastIndexOf("/") != s.length() - 1) {
                name = s.substring(s.lastIndexOf("/") + 1, s.length());
                loader = new FXMLLoader(getClass().getResource("FileProgress.fxml"));
                loader.load();
                ProgressBarController ctrl = (ProgressBarController) loader.getController();
                ctrl.initalize(loader.getRoot());

                ctrl.setFile(name);
                c.downloadFile(s, ctrl, name);
                dl_grid.getChildren().add(loader.getRoot());
            } else {
                System.out.println("log>>Folder download not supported");
            }
        } catch (Exception ex) {            
        }
    }
    @FXML
    private void login(MouseEvent e)
    {
        try{
            c.updateDisplay(docs_grid, 1);
            p_documents.setDisable(false);
            p_settings.setDisable(false);
            p_login.setDisable(true);
        }
        catch(Exception ex)
        {
            p_documents.setDisable(true);
            p_settings.setDisable(true);
            p_login.setDisable(false);
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {     
        if (c == null) {
            c = new DDNSConnection();
        }
        if (grid == null) {
            grid = docs_grid;
        }
        if (path == null) {
            path = doc_path;
        }
        if (currentFolderID == 0) {
            currentFolderID = 1;            
        }
    }
}