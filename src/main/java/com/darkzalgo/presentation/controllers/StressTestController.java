package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.Cmds;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javafx.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class StressTestController extends AbstractController implements Initializable {

    private ExecutorService pushTestPool = Executors.newFixedThreadPool(8);

    private SSHHandler sshHandler = new SSHHandler(this);

    private static final Logger logger = LoggerFactory.getLogger(StressTestController.class);

    @FXML ChoiceBox selectIPCBox, selectReaderCBox;

    @FXML TextField stClockPwdField;

    @FXML RadioButton onStressTestRadio, offStressTestRadio, allInforRadio, customGroupRadio;

    @FXML Label outputLbl1, outputLbl2, outputLbl3, outputLbl4, outputLbl5, outputLbl6, outputLbl7, outputLbl8;

    @FXML ProgressBar progressBar1, progressBar2, progressBar3, progressBar4, progressBar5, progressBar6, progressBar7, progressBar8;

    ArrayList<String[]> failureList = new ArrayList<>();

    ArrayList<Label> outputLblList;

    ArrayList<ProgressBar> progressBarList;

    ToggleGroup stressTestGroup = new ToggleGroup();

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
    }

    @Override
    public void setMsgLabelText(String msg) {

    }

    @Override
    public void setIpTextAreaIPs(List<String> ipAddresses) {

    }

    @Override
    public void setProgress(double progress) {

    }

    @Override
    public void appendErrorTextArea(String msg) {

    }

    @Override
    public void setSelectedIps(String ip) {

    }

    @Override
    public String getPassword(String ip) {
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
            tempClock.setPassword(this.getPassword(ip));
            this.pushTestPool.submit(new pushTestThread(tempClock));
        });
    }

    @FXML public void gogo(ActionEvent event)
    {
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
//        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
    }



    private class pushTestThread extends SwingWorker<String, String>
    {
        Label outputLbl;
        ProgressBar progressBar;
        Semaphore mutex = new Semaphore(1);
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
                if (!((boolean) tempLbl.getUserData()))
                {
                    tempLbl.setUserData(true);
                    return tempLbl;
                }
            }
            return null;
        }
        @Override
        protected String doInBackground() throws Exception {
            mutex.acquire();
            outputLbl = getUsableLbl();
            Thread.sleep(1000);
            mutex.release();
            while(outputLbl == null)
            {
               Thread.sleep(5000);
                mutex.acquire();
                outputLbl = getUsableLbl();
                Thread.sleep(1000);
                mutex.release();
                counter++;
                if (counter >=18)
                return "ERROR_NOLBLS";
            }
            mutex.acquire();
            progressBar = StressTestController.this.progressBarList.get(StressTestController.this.outputLblList.indexOf(outputLbl));
            Thread.sleep(1000);
            mutex.release();
            try {
                progressBar.setVisible(true);
                progressBar.setProgress(-1);
                outputLbl.setText("Connecting to " + clock.getIpAddress());
                Session session = sshHandler.longConnect(clock);
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
                String ip = "192.168.4.50";
                clock.setIpAddress(ip);
                clock.setPassword("$ynEL88RVER");
//                clock.setModel(ConfigUtilController.this.modelChoiceBox.getValue());
                progressBar.setProgress(-1);
                outputLbl.setText("Attempting to connect to " + ip);
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
                    progressBar.setProgress(0);

                    outputLbl.setText("Getting NTP servers from " + ip);
                    getInfoStringBuilder.append("NTP Servers (/etc/default/ntpd): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTPD));
                    progressBar.setProgress((double)1/12);

                    getInfoStringBuilder.append("\nNTP Servers (/etc/ntp.conf): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTP));
                    progressBar.setProgress((double)2/12);

                    outputLbl.setText("Getting Customer URL from " + ip);
                    getInfoStringBuilder.append("\nCustomer URL: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETURL));
                    progressBar.setProgress((double)3/12);

                    outputLbl.setText("Getting Time Zone from " + ip);
                    getInfoStringBuilder.append("\nTime Zone (/etc/TZ): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTZ));
                    progressBar.setProgress((double)4/12);
                    getInfoStringBuilder.append("\nTime Zone (/etc/timezone): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTIMEZONE));
                    progressBar.setProgress((double)5/12);

                    outputLbl.setText("Getting Mac Address from " + ip);
                    getInfoStringBuilder.append("\nMAC Address: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETMAC));
                    progressBar.setProgress((double)6/12);
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
                    outputLbl.setText("Getting Reader Name from " + ip);
                    getInfoStringBuilder.append("\nReader Name: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETREADERNAME));
                    progressBar.setProgress((double)7/12);

                    outputLbl.setText("Getting Software Update Type from " + ip);
                    getInfoStringBuilder.append("\nSoftware Update Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETUPDATETYPE));
                    progressBar.setProgress((double)8/12);

                    outputLbl.setText("Getting WiFi Network information from " + ip);
                    getInfoStringBuilder.append("\nWifi Net Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFINET).replace("address","IP Address:").replace("netmask", "Subnet Mask:").replace("gateway", "Gateway:"));
                    progressBar.setProgress((double)9/12);
                    String temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFIDNS);
                    progressBar.setProgress((double)10/12);
                    getInfoStringBuilder.append("\nWiFi DNS Servers: "+ (temp.length() > 2 ? temp:"N/A"));
                    temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFICONF);
                    progressBar.setProgress((double)11/12);
                    getInfoStringBuilder.append("\nWiFi Conf: " + (temp.contains("SSID") ? temp.replace("\n"," "):"NONE"));


                } catch (Exception e) {
                    logger.error("!!ERROR!! "+ e.getLocalizedMessage());

                } finally{
                    session.disconnect();
                }


                session = null;
               // return getInfoStringBuilder.toString();

            }catch (JSchException ex)
            {
                return "failedToConnect";
            }
            return "Success";
        }


        @Override
        protected void done() {
            try {
                String res = get();
            } catch (Exception e) {
               logger.error(e.getLocalizedMessage());
            } finally{
                outputLbl.setUserData(false);
            }
        }
    }
}
