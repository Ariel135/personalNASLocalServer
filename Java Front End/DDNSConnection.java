package edp;

import java.awt.Point;
import java.util.Base64;
import java.util.ArrayList;
import javafx.scene.image.Image;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DDNSConnection {

    private final String APIROOT, FILE, FOLDER, JSONNAME;
    private final int timeout;
    private final ArrayList<JSONArray> currentFolder;
    private Point fileLoc;
    private final Image FOLDERIMG, FILEIMG;
    private String userName, password;

    public DDNSConnection() {

        this.userName = "user1";
        this.password = "Welcome2";
        //this.APIROOT = "https://dmaspersonalnas.ddns.net/";
        this.APIROOT = "https://192.168.1.100/";
        this.FOLDER = "api/folder/read.php?id=";
        this.FILE = "api/file/read.php?id=";
        this.JSONNAME = "records";
        this.currentFolder = new ArrayList<JSONArray>();
        this.timeout = 5000; // 5000 ms = 5 sec
        this.fileLoc = new Point(-1, 0);
        this.FOLDERIMG = new Image("file:" + System.getProperty("user.dir") + "/src/edp/images/filefolderIcon.png");
        this.FILEIMG = new Image("file:" + System.getProperty("user.dir") + "/src/edp/images/txtfileIcon.png");

    }

    /*Returns a String of the JSON text from a HTTP URL connection
    *https://stackoverflow.com/questions/10500775/parse-json-from-httpurlconnection-object*/
    private String getJSONString_HTTP(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            System.out.println(c);
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        return null;
    }

    /*Returns a String of the JSON text from a HTTPS URL connection
    *https://stackoverflow.com/questions/13022717/java-and-https-url-connection-without-downloading-certificate*/
    private String getJSONString_HTTPS(String url, int timeout) {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL u = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) u.openConnection();            
            String encoding = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes("UTF-8"));            
            con.setRequestProperty("Authorization", "Basic " + encoding);
            
            final Reader reader = new InputStreamReader(con.getInputStream());
            final BufferedReader br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();

        } catch (KeyManagementException e1) {
            System.out.println(e1.getMessage());
        } catch (NoSuchAlgorithmException e2) {
            System.out.println("SSL connection failed");
        } catch (MalformedURLException e3) {
            System.out.println("Fail URL");
        } catch (IOException e4) {
            System.out.println(e4.getMessage());
        }
        return null;
    }

    /*Returns JSONArray object from a give formated JSON String*/
    private JSONArray getJSONArray(String stringToParse) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(stringToParse);
        JSONArray records = (JSONArray) json.get(JSONNAME);
        return records;
    }

    /*Get JSONArray containing the subfolders in folder = ID*/
    private JSONArray getSubFolders(int folderID) throws ParseException {
        String stringToParse = getJSONString_HTTPS(APIROOT + FOLDER + folderID, timeout);
        JSONArray records = getJSONArray(stringToParse);
        return records;
    }

    /*Get JSONArray containing the files in folder = ID*/
    private JSONArray getFiles(int folderID) throws ParseException {
        String stringToParse = getJSONString_HTTPS(APIROOT + FILE + folderID, timeout);
        JSONArray records = getJSONArray(stringToParse);
        return records;
    }

    /*Loads view with current info of files/subfolders*/
    public void updateDisplay(GridPane grid, int currentFolderID) {

        try {
            JSONArray subFolders = getSubFolders(currentFolderID);
            JSONArray subFiles = getFiles(currentFolderID);
            final int ICON = 0, LABELINDEX = 1;
            currentFolder.add(subFolders);
            currentFolder.add(subFiles);

            grid.getChildren().clear();
            fileLoc.x = -1;
            fileLoc.y = 0;

            //Add folders     
            for (int i = 0; i < subFolders.size(); i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FileFolderIcon.fxml"));
                loader.load();
                AnchorPane fileFolder = loader.getRoot();
                //AnchorPane fileFolder = FXMLLoader.load(getClass().getResource("FileFolderIcon.fxml"));
                AnchorPane container = (AnchorPane) fileFolder.getChildren().get(0);
                ImageView img = (ImageView) container.getChildren().get(ICON);
                Label fileName = (Label) container.getChildren().get(LABELINDEX);

                JSONObject folderInfo = (JSONObject) subFolders.get(i);
                ((FXMLController) loader.getController()).type = FXMLController.TYPEFOLDER;
                ((FXMLController) loader.getController()).o = folderInfo;
                fileName.setText(folderInfo.get("name").toString());

                fileLoc.y = fileLoc.y + fileLoc.x / 3;
                fileLoc.x = fileLoc.x < 3 ? ++fileLoc.x : 0;
                img.setImage(FOLDERIMG);
                img.setVisible(true);
                grid.add(fileFolder, fileLoc.x, fileLoc.y);

                /*Testing*
            System.out.println("Folder " + i + ": (" + fileLoc.x + "," + fileLoc.y + ")");
            System.out.println("\t" + folderInfo.get("id"));
            System.out.println("\t" + folderInfo.get("name"));
            System.out.println("\t" + folderInfo.get("created"));
            System.out.println("\t" + folderInfo.get("modifed"));
            System.out.println("\t" + folderInfo.get("path"));
            System.out.println();
            /**/
            }

            //Add files
            for (int i = 0; i < subFiles.size(); i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FileFolderIcon.fxml"));
                loader.load();
                AnchorPane fileFolder = loader.getRoot();
                //AnchorPane fileFolder = FXMLLoader.load(getClass().getResource("FileFolderIcon.fxml"));                        
                AnchorPane container = (AnchorPane) fileFolder.getChildren().get(0);
                ImageView img = (ImageView) container.getChildren().get(ICON);
                Label fileName = (Label) container.getChildren().get(LABELINDEX);

                JSONObject fileInfo = (JSONObject) subFiles.get(i);
                ((FXMLController) loader.getController()).type = FXMLController.TYPEFILE;
                ((FXMLController) loader.getController()).o = fileInfo;
                fileName.setText(fileInfo.get("name").toString());

                fileLoc.y = fileLoc.y + fileLoc.x / 3;
                fileLoc.x = fileLoc.x < 3 ? ++fileLoc.x : 0;
                img.setImage(FILEIMG);
                img.setVisible(true);
                grid.add(fileFolder, fileLoc.x, fileLoc.y);

                /*Testing*
            System.out.println("File " + i + ": (" + fileLoc.x + "," + fileLoc.y + ")");
            System.out.println("\t" + fileInfo.get("id"));
            System.out.println("\t" + fileInfo.get("name"));
            System.out.println("\t" + fileInfo.get("created"));
            System.out.println("\t" + fileInfo.get("modifed"));
            System.out.println("\t" + fileInfo.get("path"));
            System.out.println();
            /**/
            }
        } catch (IOException | ParseException e) {
            System.out.println("Log>> Exception Thrown : " + e);
        } catch (Exception e) {
            //ignore
        }
    }

    public void downloadFile(String URL, ProgressBarController ctrl, String fileName) throws UnsupportedEncodingException {

        ProgressBar progressBar = ctrl.getBar();
        System.out.println("log>> Attempting to connect to : " + APIROOT + URL);
        String encoding = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes("UTF-8"));
        //System.out.println(encoding);
        ctrl.downloadFile(APIROOT + URL, fileName, encoding);

    }
    
    
    /*
    private int mStartByte = 0;
    private int mEndByte;
    private int mBytesRead;

    private class DownloadTask extends Task<Void> {

        private String url, fileName;
        private ProgressBarController ctrl;
        private int bytesToRead;

        public DownloadTask(String url, String fileName, ProgressBarController ctrl) {
            this.url = url;
            this.fileName = fileName;
            this.ctrl = ctrl;
            bytesToRead = 4096;//8192;

        }

        // @Override
        protected Void call2() throws Exception {
            try {

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                //String ext = url.substring(url.lastIndexOf("."), url.length());
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                System.out.println("log>> Staring download ...");
                long fileLength = connection.getContentLengthLong();

                try (InputStream is = connection.getInputStream();
                        OutputStream os = Files.newOutputStream(Paths.get(fileName))) {

                    long nread = 0L;
                    byte[] buf = new byte[bytesToRead];
                    int n;
                    while ((n = is.read(buf)) > 0) {
                        os.write(buf, 0, n);
                        nread += n;
                        updateProgress(nread, fileLength);
                        if (ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE)) {
                            //continue on my wayward son...
                        } else if (ctrl.currentState().equals(ProgressBarController.PAUSEDSTATE)) {
                            break;
                        } else if (ctrl.currentState().equals(ProgressBarController.CANCLEDSTATE)) {
                            break;
                        }
                    }
                }
            } catch (IllegalMonitorStateException e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected Void call() throws Exception {

            BufferedInputStream in = null;
            RandomAccessFile raf = null;

            try {
                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                // Install the all-trusting trust manager
                final SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                //String ext = url.substring(url.lastIndexOf("."), url.length());
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                // connect to server
                connection.connect();
                // Make sure the response code is in the 200 range.
                if (connection.getResponseCode() / 100 != 2) {
                    throw new Exception();
                }
                System.out.println("log>> Staring download ...");

                mEndByte = connection.getContentLength();
                // set the range of byte to download
                String byteRange = mStartByte + "-" + mEndByte;
                connection.setRequestProperty("Range", "bytes=" + byteRange);
                System.out.println("bytes=" + byteRange);


                // get the input stream
                in = new BufferedInputStream(connection.getInputStream());

                // open the output file and seek to the start location
                raf = new RandomAccessFile(fileName, "rw");
                raf.seek(mStartByte);

                byte data[] = new byte[bytesToRead];
                int numRead;
                while ((ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE))
                        && ((numRead = in.read(data, 0, bytesToRead)) != -1)) {
                    // write to buffer
                    raf.write(data, 0, numRead);
                    // increase the startByte for resume later
                    mStartByte += numRead;
                    // increase the downloaded size
                    mBytesRead += numRead;
                }

                if (ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE)) {
                    ctrl.taskComplete();
                }

            } catch (IllegalMonitorStateException e) {
                System.out.println(e);
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                    }
                }

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }

            return null;
        }

        @Override
        protected void failed() {
            System.out.println("log>> Download Failed");
        }

        @Override
        protected void succeeded() {
            System.out.println("log>> Download Successful");
            ctrl.taskComplete();
        }
    }
     */
}
