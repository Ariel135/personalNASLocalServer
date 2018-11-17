package edp;

import java.io.BufferedInputStream;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.concurrent.Task;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class ProgressBarController {

    private AnchorPane container;
    private ToggleButton state, cancle;
    private ProgressBar progress;
    private Text name;
    private ImageView state_img;

    /**
     * ****************************************************
     */
    private final Image startIMG;//= new Image("file:/images/media_play.png");
    private final Image pauseIMG;//= new Image("file:/images/media_pause.png");
    private final Image stopIMG;//= new Image("file:/images/media_pause.png");
    private StateType currentState;
    private static final StateType RUNNINGSTATE = new StateType("RUNNINGSTATE");
    private static final StateType PAUSEDSTATE = new StateType("PAUSEDSTATE");
    private static final StateType COMPLETEDSTATE = new StateType("COMPLETEDSTATE");
    private static final StateType CANCLEDSTATE = new StateType("CANCLEDSTATE");

    private String fileName, URL;
    private Thread runningThread;
    private String encoding;
    
    public ProgressBarController() {
        this.startIMG = new Image("file:" + System.getProperty("user.dir") + "/src/edp/images/media_play.png");
        this.pauseIMG = new Image("file:" + System.getProperty("user.dir") + "/src/edp/images/media_pause.png");
        this.stopIMG = new Image("file:" + System.getProperty("user.dir") + "/src/edp/images/media_stop.png");
        this.currentState = RUNNINGSTATE;//start -- running
        this.fileName = "";
        this.URL = "";
        this.runningThread = null;
    }

    public void initalize(Parent root) {
        this.container = (AnchorPane) root.getChildrenUnmodifiable().get(0);
        this.name = (Text) container.getChildren().get(0);
        this.state = (ToggleButton) container.getChildren().get(1);
        this.cancle = (ToggleButton) container.getChildren().get(2);
        this.progress = (ProgressBar) container.getChildren().get(3);
        this.state_img = (ImageView) state.getGraphic();

        state_img.imageProperty().bind(Bindings
                .when(state.selectedProperty())
                .then(startIMG)
                .otherwise(pauseIMG)
        );

        System.out.println("ProgressBarController: [" + this + "] has been initalized");
    }

    @FXML
    private void changeState() {
        if (currentState.equals(PAUSEDSTATE)) {
            currentState = RUNNINGSTATE;
            try {
                downloadFile(URL, fileName,encoding);
            } catch (Exception e) {
                System.out.println("log>>" + e);
                currentState = PAUSEDSTATE;
                state.setSelected(true);
            }
        } else if (currentState.equals(RUNNINGSTATE)) {
            currentState = PAUSEDSTATE;

        } else {
            //do nothing
        }
        System.out.println("log>> " + currentState);

    }

    public StateType currentState() {
        return currentState;//true -> not running
    }

    @FXML
    private void cancleTask() {
        if (currentState != COMPLETEDSTATE) {
            currentState = CANCLEDSTATE;
            System.out.println("log>> " + currentState);
            runningThread.interrupt();
            taskComplete();
            //cancle.setDisable(true);
            progress.setDisable(true);
            //delete download filename
        } else {
            AnchorPane parent = (AnchorPane) container.getParent();
            int targetIndex = parent.getChildren().indexOf(container);
            ObservableList<Node> children = parent.getChildren();
            children.remove(container);

            /*children.forEach(c -> {
                int sourceIndex = children.indexOf(c);
                if (sourceIndex < targetIndex) {
                    java.util.Collections.rotate(
                            children.subList(sourceIndex, targetIndex + 1), -1);
                } else {
                    java.util.Collections.rotate(
                            children.subList(targetIndex, sourceIndex + 1), 1);
                }

            });*/
        }
    }

    public void taskComplete() {
        currentState = COMPLETEDSTATE;
        state_img.imageProperty().bind(Bindings
                .when(state.selectedProperty())
                .then(stopIMG)
                .otherwise(stopIMG));
        state.setDisable(true);

        //progress.setProgress(1);
    }

    public ProgressBar getBar() {
        return progress;
    }

    public void setFile(String s) {
        fileName = s;
        name.setText(fileName);
    }

    private static class StateType {

        String type;

        StateType(String t) {
            type = t;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public void downloadFile(String URL, String fileName, String encoding) {

        ProgressBarController ctrl = this;
        ProgressBar progressBar = ctrl.getBar();
        this.encoding = encoding;
        Task<Void> task = new DownloadTask(URL, fileName, ctrl);

        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);

        this.fileName = fileName;
        this.runningThread = thread;
        this.URL = URL;
        thread.start();
        System.out.println("log>> Task set to run");

    }
    private int mStartByte = 0;
    private int mEndByte;
    private int mBytesRead;
    private BufferedInputStream in = null;
    private RandomAccessFile raf = null;
    private String downloadFilename;

    private class DownloadTask extends Task<Void> {

        private String url, fileName;
        private int bytesToRead;
        private ProgressBarController ctrl;
        private int timeout = 5000;        

        public DownloadTask(String url, String fileName, ProgressBarController ctrl) {
            this.url = url;
            String home = System.getProperty("user.home");
            this.fileName = home + "/Downloads/Newfolder/" + fileName;
            downloadFilename = this.fileName;
            bytesToRead = 4096;//8192;
            this.ctrl = ctrl;            
        }

        //@Override
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
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
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

        //@Override
        protected Void call() throws Exception {

            int timeout = 5000;
            try {
                mStartByte = mBytesRead;
                this.updateProgress(mBytesRead, mEndByte);
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
                connection.setRequestProperty("Authorization", "Basic " + encoding);
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                mEndByte = connection.getContentLength();
                connection.disconnect();
                // connect to server
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("Authorization", "Basic " + encoding);
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);

                // set the range of byte to download
                String byteRange = mStartByte + "-" + mEndByte;
                connection.setRequestProperty("Range", "bytes=" + byteRange);
                System.out.println("bytes=" + byteRange);

                //connection.connect();
                // Make sure the response code is in the 200 range.
                //https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
                if (connection.getResponseCode() / 100 != 2) {
                    throw new Exception();
                }
                System.out.println("log>> Staring download ...");

                // get the input stream
                in = new BufferedInputStream(connection.getInputStream());

                // open the output file and seek to the start location
                raf = new RandomAccessFile(fileName, "rw");
                raf.seek(mStartByte);

                byte data[] = new byte[bytesToRead];
                int numRead;
                while ((ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE))
                        && ((numRead = in.read(data, 0, bytesToRead)) != -1)) {
                    //System.out.print("log>> Bytes starting from "+ mStartByte+" Bytes - ");                    

                    raf.write(data, 0, numRead);// write to buffer                    

                    mBytesRead += numRead;// increase the downloaded size

                    //System.out.println(mBytesRead + " Bytes");//--------------------------
                    this.updateProgress(mBytesRead, mEndByte);
                }

                if (ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE)) {
                    ctrl.taskComplete();
                }

            } catch (IllegalMonitorStateException e) {
                System.out.println(e);
            } catch (SocketTimeoutException ex) {
                System.out.println("log>> Connection Lost ... current bytes read " + mBytesRead + "/" + mEndByte);
                ctrl.changeState();//pause
                state.setSelected(true);
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
            ctrl.taskComplete();
        }

        @Override
        protected void succeeded() {
            if (ctrl.currentState().equals(ProgressBarController.RUNNINGSTATE)) {
                System.out.println("log>> Download Successful");
                ctrl.taskComplete();
            } else if (ctrl.currentState().equals(ProgressBarController.PAUSEDSTATE)) {
                System.out.println("log>> Download PAUSED");
            } else if (ctrl.currentState().equals(ProgressBarController.CANCLEDSTATE)) {
                System.out.println("log>> Download CANACLED");
            }
        }
    }

}
