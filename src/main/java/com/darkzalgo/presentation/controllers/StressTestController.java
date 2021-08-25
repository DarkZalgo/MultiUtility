package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.Cmds;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javafx.event.ActionEvent;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
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

    Semaphore lblMutex = new Semaphore(1), progMutex = new Semaphore(1);

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

    @FXML public void gogo(ActionEvent event) throws InterruptedException {
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

//        this.pushTestPool.submit(new pushTestThread(new TimeClock()));
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
                String ip = "192.168.1.108";
                clock.setIpAddress(ip);
                clock.setPassword("$ynEL2001313");
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
                setLabelText(outputLbl, "Attempting to connect to " + ip);
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
                String res = get();
            } catch (Exception e) {
               logger.error(e.getLocalizedMessage());
            } finally{
                outputLbl.setUserData(false);
            }
        }
    }
}
