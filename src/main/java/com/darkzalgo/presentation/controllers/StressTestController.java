package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.Cmds;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class StressTestController extends AbstractController implements Initializable {

    private ExecutorService pushTestPool = Executors.newFixedThreadPool(8);

    private SSHHandler sshHandler = new SSHHandler(this);

    private static final Logger logger = LoggerFactory.getLogger(StressTestController.class);

    @FXML ChoiceBox<String> selectIPCBox, selectReaderCBox;

    @FXML TextField stClockPwdField;

    @FXML TextArea ipTextArea, resultTextArea;

    @FXML RadioButton onStressTestRadio, offStressTestRadio, allInforRadio, customGroupRadio;

    @FXML Label outputLbl1, outputLbl2, outputLbl3, outputLbl4, outputLbl5, outputLbl6, outputLbl7, outputLbl8;
    @FXML Label successCountLbl;

    @FXML ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5, progressBar6, progressBar7, progressBar8;
    private int msgCount=0;


    private ObservableList<TimeClock> failedClocks;

    private ArrayList<Label> outputLblList;

    private ArrayList<ProgressBar> progressBarList;

    private ToggleGroup stressTestGroup = new ToggleGroup();

    private Stage currentStage;

    private Scene currentScene;

    private Semaphore lblMutex = new Semaphore(1), progMutex = new Semaphore(1), countMutex = new Semaphore(1);

    private int successCount = 0, totalCount;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Context.getInstance().setStressTestController(this);
        onStressTestRadio.setToggleGroup(stressTestGroup);
        offStressTestRadio.setToggleGroup(stressTestGroup);
        allInforRadio.setToggleGroup(stressTestGroup);
        customGroupRadio.setToggleGroup(stressTestGroup);
        outputLblList = new ArrayList(Arrays.asList(outputLbl1, outputLbl2, outputLbl3, outputLbl4, outputLbl5, outputLbl6, outputLbl7, outputLbl8));
        progressBarList = new ArrayList<>(Arrays.asList(progressBar1, progressBar2, progressBar3, progressBar4, progressBar5, progressBar6, progressBar7, progressBar8));
        for (Label tempLbl : outputLblList)
            tempLbl.setUserData(false);
        for (ProgressBar progressBar : progressBarList)
            progressBar.setVisible(false);
       // successCountLbl.setVisible(false);
        selectIPCBox.getItems().add("192.168.4.");

        selectIPCBox.getItems().add("192.168.6.");
        selectIPCBox.getItems().add("10.10.10.");
        selectIPCBox.getItems().add("10.10.11.");
        selectIPCBox.getItems().add("192.168.1.");
        selectIPCBox.setValue(selectIPCBox.getItems().get(0));


        ipTextArea.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1)
            {
                if (!t1.matches("[0-9,]*"))
                {
                    ipTextArea.setText(t1.replaceAll("[^0-9,]", ""));
                }
            }
        });
    }

    @Override
    public void setMsgLabelText(String msg) {
        if (!progressBar1.isVisible())
        {
            progressBar1.setVisible(true);
        }
        msgCount++;
        msgCount = msgCount % 256;


        Platform.runLater(()->{
            double progVal = (double)msgCount/255;
            if(msg.contains("Found"))
                progVal=1;
            outputLbl1.setText(msg);
            progressBar1.setProgress(progVal);
        });
    }

    @Override
    public void setIpTextAreaIPs(List<String> ipAddresses) {
        ipTextArea.setText("");

        Platform.runLater(()->{
            successCountLbl.setText("Checking if IPs are Infor Clocks. . .");
        });
        ipAddresses.forEach((ip ->{
            TimeClock tempClock = new TimeClock();
            tempClock.setIpAddress(ip);
            tempClock.setPassword(getPassword());
            try {
                if(sshHandler.isInforClock(tempClock))
                {

                    String lastOctet = ip.split("\\.")[3];
                    Platform.runLater(()->{

                        ipTextArea.appendText(lastOctet + ",");
                        outputLbl1.setText(ip + " Is an Infor Clock");
                        successCountLbl.setText("Checking if IPs are Infor Clocks. . . Found " + ipTextArea.getText().split(",").length);
                    });

                }
                Platform.runLater(()->{
//                    logger.info("index val " + ipAddresses.indexOf(ip));
//                    logger.info("length val " + ipAddresses.size());
//                    logger.info("prog bar val " + );
                    msgCount = msgCount+ (int)(256*(double)(ipAddresses.indexOf(ip)+1)/ipAddresses.size());
                    if (msgCount > 256)
                        msgCount=256;
                    msgCount--;
//                    progressBar1.setProgress((double)(ipAddresses.indexOf(ip)+1)/ipAddresses.size());
                });

            } catch (JSchException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }));

        Platform.runLater(()->{
            if(ipTextArea.getLength() > 0)
                ipTextArea.setText(ipTextArea.getText().substring(0,ipTextArea.getLength()-1));
            progressBar1.setProgress(1);
            successCountLbl.setText("Done! Found " + ipTextArea.getText().split(",").length + " Infor Clocks");

        });


    }

    @Override
    public void setProgress(double progress) {

    }

    @Override
    public void appendErrorTextArea(String msg) {

    }

    @Override
    public void setSelectedIps(ObservableList<TimeClock> selectedItems) {

        selectedItems.forEach((clock -> {
            String ip =clock.getIpAddress().split("\\.")[3];
            if (ipTextArea.getText().equals(""))
            {
                ipTextArea.setText(ip);

            }else
            {
                ipTextArea.appendText(","+ip);
            }
        }));

    }

    @Override
    public String getPassword(String... ip) {
            String pwdStr = stClockPwdField.getText().trim();
            String password = "synergy";
            if (pwdStr.length() > 0 && pwdStr.length() <5) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                int day = Integer.valueOf(dayFormat.format(new Date()));
                int month = Integer.valueOf(monthFormat.format(new Date()));
                password="$ynEL"+(day*month)+pwdStr;
            } else if (pwdStr.length() > 4){
                password=pwdStr;
            }
            return password;
    }

    public void pushTest(String[] ips)
    {
        Arrays.stream(ips).forEach(ip->{
            TimeClock tempClock = new TimeClock();
            tempClock.setPassword(this.getPassword());
            this.pushTestPool.submit(new pushTestThread(tempClock));
        });
    }

    @FXML public void pushTest(ActionEvent event)
    {
        Platform.runLater(()->{
            successCountLbl.setText("");
            outputLblList.forEach(lbl->{
                lbl.setText("");
                lbl.setVisible(true);
            });
        });
        failedClocks = Context.getInstance().getFailedClocks();
        failedClocks.clear();

        String ipPrefix = selectIPCBox.getValue();
        if(!ipTextArea.getText().isEmpty() && !ipTextArea.getText().equals(""))
        {
            String ips[] = ipTextArea.getText().split(",");
            totalCount = ips.length;

            Arrays.stream(ips).forEach(ip-> {
                logger.info("Adding ip " + ipPrefix+ ip);
                if (!ip.equals("") && (Integer.parseInt(ip) > 0 && Integer.parseInt(ip) < 256))
                {
                    TimeClock  tempClock = new TimeClock();

                    tempClock.setIpAddress(ipPrefix + ip);

                        this.pushTestPool.submit(new pushTestThread(tempClock));

                    }

                    });
        }
    }

    @FXML public void gogo(ActionEvent event) throws InterruptedException {
        failedClocks = Context.getInstance().getClockInfoClocks();
        failedClocks.clear();
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));



    }

    @FXML
    public void openTableView(ActionEvent event)
    {
        Stage tableViewStage = Context.getInstance().getTableViewStage();
        TableViewController tableViewController = Context.getInstance().getTableViewController();
        EventHandler<WindowEvent> eventHandle = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                tableViewController.setCallingController(StressTestController.this);
                tableViewController.swapCellFactories();
                tableViewController.setClocks();
                tableViewStage.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
            }
        };

        tableViewStage.addEventHandler(WindowEvent.WINDOW_SHOWING, eventHandle);
        tableViewStage.setTitle("Failed Clocks");
        tableViewStage.show();
    }

    @FXML private void getIPs(ActionEvent event) throws IOException, InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Platform.runLater(()->{
            progressBarList.forEach(bar->{
                bar.setVisible(false);
            });
            outputLblList.forEach(lbl->{
                lbl.setVisible(false);
            });
            outputLbl1.setVisible(true);
            outputLbl1.setText("");
        });
        executorService.submit((Callable<Void>) () -> {
            Platform.runLater(()->{
                successCountLbl.setText("Checking ip Addresses that are up. . .");
            });

            sshHandler.checkAllHosts((String) selectIPCBox.getValue(), new int[]{22});
            return null;
        });
    }

    @FXML private void cancel(ActionEvent event)
    {
        this.pushTestPool.shutdown();
    }

    @FXML private void forceCancel(ActionEvent event)
    {
        this.pushTestPool.shutdownNow();
    }

    private void setProgressBarProgress(ProgressBar bar, double val)
    {
        Platform.runLater(()->{
            bar.setProgress(val);
        });
    }

    private void setLabelText(Label lbl, String text)
    {
        Platform.runLater(()->{
            lbl.setText(text);
        });
    }



    private class pushTestThread extends SwingWorker<String, String>
    {
        Label outputLbl;
        ProgressBar progressBar;

        TimeClock clock;
        int counter = 0;
        public pushTestThread(TimeClock clock)
        {
            this.clock = clock;
        }
        private Label getUsableLbl()
        {
            for (Label tempLbl : StressTestController.this.outputLblList)
            {
                boolean inUse = (boolean)tempLbl.getUserData();
                if (!(inUse))
                {
                        tempLbl.setUserData(true);
                    return tempLbl;
                }
            }
            return null;
        }
        @Override
        protected String doInBackground() throws Exception {
            try {
                lblMutex.acquire();
                outputLbl = getUsableLbl();
            } catch (InterruptedException e){

            } finally {
                lblMutex.release();
            }
            while(outputLbl == null)
            {
                try {
               Thread.sleep(5000);
                lblMutex.acquire();
                outputLbl = getUsableLbl();
                Thread.sleep(1000);
                } catch (InterruptedException e){

                } finally {
                    lblMutex.release();
                }
                counter++;
                if (counter >=18)
                return "ERROR_NOLBLS";
            }

//            logger.info("THREAD "+ Thread.currentThread() + " USING OUTPUTLBL WITH INDEX "+ StressTestController.this.outputLblList.indexOf(outputLbl));
            try {
                progMutex.acquire();
                progressBar = StressTestController.this.progressBarList.get(StressTestController.this.outputLblList.indexOf(outputLbl));
//                logger.info("THREAD " + Thread.currentThread() + " USING PROGBAR WITH INDEX " + StressTestController.this.progressBarList.indexOf(progressBar));
            } catch (InterruptedException e){

            }finally {
                progMutex.release();
            }

            try {
               // String ip = "192.168.4.39";
//                clock.setIpAddress(ip);
//                clock.setPassword("synergy");
                clock.setModel("Synergy/X A20");
                Platform.runLater(()->{
                    progressBar.setVisible(true);
                });



                setProgressBarProgress(progressBar, -1);
                setLabelText(outputLbl, "Connecting to " + clock.getIpAddress());
                Session session = null;//sshHandler.longConnect(clock);
//                String stFile = "";
//                sshHandler.sendThroughSFTP(clock, new String[]{"put", stFile, "/"});

               // TimeClock clock = new TimeClock();
//                if (ipField.getText().trim().length() < 1 || ipField.getText().isEmpty())
//                {
//                    return "noIPAddress";
//                }
//                if (!sshHandler.isIpValid(ipField.getText()))
//                {
//
//                    return "invalidIPAddress";
//                }
//                if (ConfigUtilController.this.modelChoiceBox.getValue() == null)
//                {
//                    return "noModelChosen";
//                }

//                clock.setModel(ConfigUtilController.this.modelChoiceBox.getValue());
                setProgressBarProgress(progressBar, -1);

                String ip = clock.getIpAddress();
                setLabelText(outputLbl, "Attempting to connect to " + ip);
                String fullIP = sshHandler.checkHost(ip, new int[]{22});
                if(fullIP != null) {
                   clock.setPassword(getPassword());

                    clock.setPort(Integer.parseInt(fullIP.split(",")[1]));
                }  else {

                    return "cantconnect";

                }
                try {
                    session  = sshHandler.longConnect(clock);
                }catch (JSchException e)
                {
                    return "badPwd";
                }
                if (session==null)
                {
                    return "";
                }

                StringBuilder getInfoStringBuilder = new StringBuilder();

                try {
                    setProgressBarProgress(progressBar, 0);
                    if(!sshHandler.isInforClock(clock))
                    {
                        setLabelText(outputLbl, ip + " is not an Infor clock");
                        outputLbl.setUserData(false);
                        return "notInfor";
                    }
                    String res = "";

                    setLabelText(outputLbl,"");
                    res = sshHandler.sendCmdBlocking(clock, session, "output=$(); res=$? ; if [ $res -eq 0 ]; then echo $res; else echo $output; fi");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }

                    setLabelText(outputLbl,"Backing up WBCS directories");
                    res = sshHandler.sendCmdBlocking(clock, session, "output=$(cp -rf /home/admin/wbcs /home/admin/wbcs-bak); res=$? ; if [ $res -eq 0 ]; then echo $res; else echo $output; fi");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }

                    setLabelText(outputLbl,"Backing up keypad daemon");
                    res = sshHandler.sendCmdBlocking(clock, session, "output=$(cp /usr/sbin/spikbdinputattach /usr/sbin/spikbdinputattach-bak && cp /usr/sbin/spikbdinputattachdebug /usr/sbin/spikbdinputattachdebug-bak); res=$? ; if [ $res -eq 0 ]; then echo $res; else echo $output; fi");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }

                    setLabelText(outputLbl,"Shutting down watchdog");
                    res = sshHandler.sendCmdBlocking(clock, session, "killall watchdog; killall 2416_dog ; killall watchdogSoft ; shutdownwdt");

                    setLabelText(outputLbl,"Shutting down Java Application");
                    res = sshHandler.sendCmdBlocking(clock, session, "killall java spikbdinputattach spikbdinputattachdebug");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }

                    setLabelText(outputLbl,"Unzipping Stress Test tarball");
                    res = sshHandler.sendCmdBlocking(clock, session, "output=$(tar -zxvf /st*.tar.gz -C / && sync); res=$? ; if [ $res -eq 0 ]; then echo $res; else echo $output|tail -n 1; fi");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }
                    res = sshHandler.sendCmdBlocking(clock, session, "output=$(tar -zxvf /st*.tar.gz -C / && sync); res=$? ; if [ $res -eq 0 ]; then echo $res; else echo $output|tail -n 1; fi");
                    if(res.equals("0")) {
                        setProgressBarProgress(progressBar, (double) 1 / 15);
                    } else {

                    }






                    setLabelText(outputLbl, "Getting NTP servers from " + ip);
                    getInfoStringBuilder.append("NTP Servers (/etc/default/ntpd): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTPD));
                    setProgressBarProgress(progressBar, (double)1/12);

                    getInfoStringBuilder.append("\nNTP Servers (/etc/ntp.conf): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTP));
                    setProgressBarProgress(progressBar, (double)2/12);

                    setLabelText(outputLbl, "Getting Customer URL from " + ip);
                    getInfoStringBuilder.append("\nCustomer URL: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETURL));
                    setProgressBarProgress(progressBar, (double)3/12);

                    setLabelText(outputLbl, "Getting Time Zone from " + ip);
                    getInfoStringBuilder.append("\nTime Zone (/etc/TZ): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTZ));
                    setProgressBarProgress(progressBar, (double)4/12);
                    getInfoStringBuilder.append("\nTime Zone (/etc/timezone): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTIMEZONE));
                    setProgressBarProgress(progressBar, (double)5/12);

                    setLabelText(outputLbl, "Getting Mac Address from " + ip);
                    getInfoStringBuilder.append("\nMAC Address: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETMAC));
                    setProgressBarProgress(progressBar, (double)6/12);
                    switch (clock.getModel())
                    {
                        case "SYnergy/X  / A20":
                            getInfoStringBuilder.append("\nVolume: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETA20VOL));
                            break;
                        case "SYnergy/A 2416":
                            getInfoStringBuilder.append("\nVolume: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GET2416VOL));
                            break;
                        case "SYnergy/A 2410":
                            getInfoStringBuilder.append("\nVolume: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GET2410VOL));
                            break;
                        default:
                            break;
                    }
                    setLabelText(outputLbl, "Getting Reader Name from " + ip);
                    getInfoStringBuilder.append("\nReader Name: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETREADERNAME));
                    setProgressBarProgress(progressBar, (double)7/12);

                    setLabelText(outputLbl, "Getting Software Update Type from " + ip);
                    getInfoStringBuilder.append("\nSoftware Update Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETUPDATETYPE));
                    setProgressBarProgress(progressBar, (double)8/12);

                    setLabelText(outputLbl, "Getting WiFi Network information from " + ip);
                    getInfoStringBuilder.append("\nWifi Net Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFINET).replace("address","IP Address:").replace("netmask", "Subnet Mask:").replace("gateway", "Gateway:"));
                    setProgressBarProgress(progressBar, (double)9/12);
                    String temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFIDNS);
                    setProgressBarProgress(progressBar, (double)10/12);
                    getInfoStringBuilder.append("\nWiFi DNS Servers: "+ (temp.length() > 2 ? temp:"N/A"));
                    temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFICONF);
                    setProgressBarProgress(progressBar, (double)11/12);
                    getInfoStringBuilder.append("\nWiFi Conf: " + (temp.contains("SSID") ? temp.replace("\n"," "):"NONE"));

                    setProgressBarProgress(progressBar, 1);
                    setLabelText(outputLbl, "Done getting info from " + ip);

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("CAUSE "+e.getCause());
                    logger.error("!!ERROR!! "+ e.getLocalizedMessage());

                } finally{
                    session.disconnect();
                }


                session = null;
               // return getInfoStringBuilder.toString();

            }catch (Exception ex)
            {
                logger.error("!!ERROR!! "+ ex.getLocalizedMessage());
                return "failedToConnect";
            }
            return "Success";
        }


        @Override
        protected void done() {
            try {
                logger.info("Before Res");
                String res = get();

                logger.info(res);
                if (res != null)
                {
                    if (res.equals("Success"))
                    {

                        logger.info("Successfully pushed to " + clock.getIpAddress());

                        countMutex.acquire();
                        successCount++;
                        updateCount();
                        countMutex.release();


                    } else if (res.equals("cantconnect")) {
                        setLabelText(outputLbl, "Unable to connect to " + clock.getIpAddress());
                        setProgressBarProgress(progressBar, 0);
                        clock.setMacAddress("N/A");
                        clock.setReason("Cannot Connect");
                        failedClocks.add(clock);
                    }  else if (res.equals("badPwd")) {
                        setLabelText(outputLbl, "Incorrect password for " + clock.getIpAddress());
                        setProgressBarProgress(progressBar, 0);
                        clock.setReason("Incorrect Password. Password used: " + clock.getPassword());
                        if (clock.getMacAddress().equals(""))
                        {
                            clock.setMacAddress(sshHandler.getMac(clock.getIpAddress()).toUpperCase());
                        }
                        failedClocks.add(clock);
                    }else {
                        clock.setReason("Failed to push stress test");
                        setLabelText(outputLbl, "Unable to push test to " + clock.getIpAddress());
                        setProgressBarProgress(progressBar, 0);
                        if (clock.getMacAddress().equals(""))
                        {
                            clock.setMacAddress(sshHandler.getMac(clock.getIpAddress()).toUpperCase());
                        }
                        failedClocks.add(clock);
                    }
                }

            } catch (Exception e) {
               logger.error(e.getLocalizedMessage());
            } finally{
                outputLbl.setUserData(false);
            }
        }
    }
    public void setStage(Stage stage)
    {
        this.currentStage = stage;
        this.currentScene = stage.getScene();
        logger.info(currentScene.getRoot().getStyle());
        if (currentScene.getRoot().getStyle().contains("black")) {
            progressBarList.forEach(bar -> {
                Platform.runLater(() -> {
                    bar.setStyle("-fx-accent: darkred");
                });

            });
        } else
        {
            progressBarList.forEach(bar -> {
                Platform.runLater(() -> {
                    bar.setStyle("");
                });
        });
        }
    }

    private void updateCount()
    {
        Platform.runLater(()->{
            successCountLbl.setText("Successfully pushed to " + successCount + " out of " + totalCount + " clocks");
        });
    }
}
