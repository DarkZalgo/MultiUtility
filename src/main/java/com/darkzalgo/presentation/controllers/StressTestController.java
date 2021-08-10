package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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

            }catch (JSchException ex)
            {

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
