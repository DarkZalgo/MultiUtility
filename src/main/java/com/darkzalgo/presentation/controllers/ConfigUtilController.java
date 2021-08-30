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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigUtilController extends AbstractController implements Initializable {


    private SSHHandler sshHandler = new SSHHandler(this);

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtilController.class);

    @FXML ProgressBar progressBar;

    @FXML ChoiceBox<String> modelChoiceBox, tzChoiceBox;

    @FXML TextField urlField, ipField, curPwdField, ntpField, newPwdField, volField, macAddrField, readerNameField, swupdateTypeField;
    @FXML TextField wiredIpField, wiredSubnetField, wiredGatewayField, wiredDNSField1, wiredDNSField2, wiredDNSField3;
    @FXML TextField ssidField, passphraseField, wifiIpField, wifiSubnetField, wifiGatewayField,  wifiDNSField1, wifiDNSField2, wifiDNSField3;

    @FXML Label wiredIpLbl, wiredSubnetLbl, wiredGatewayLbl, wiredDNS1Lbl, wiredDNS2Lbl, wiredDNS3Lbl;
    @FXML Label wifiIpLbl, wifiSubnetLbl, wifiGatewayLbl, ssidLbl, passphraseLbl, wifiDNS1Lbl, wifiDNS2Lbl, wifiDNS3Lbl;
    @FXML Label resultLbl;

    @FXML TextArea outputTextArea;

    @FXML CheckBox wiredCheckBox, wifiCheckBox;

    @FXML RadioButton wiredDHCPRadio, wiredStaticRadio, wifiDHCPRadio, wifiStaticRadio;
    Set<RadioButton> wiredRadioSet;
    Set<RadioButton> wifiRadioSet;

    Set<TextField> wifiFieldSet;
    Set<TextField> wiredFieldSet;
    Set<TextField> clockInfoFieldSet;

    Set<Label> wiredLabelSet;
    Set<Label> wifiLabelSet;

    ToggleGroup wiredNetInfoGroup = new ToggleGroup();
    ToggleGroup wifiNetInfoGroup = new ToggleGroup();

    private Stage currentStage;
    private Scene currentScene;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Context.getInstance().setConfigController(this);
        wiredDHCPRadio.setToggleGroup(wiredNetInfoGroup);
        wiredStaticRadio.setToggleGroup(wiredNetInfoGroup);

        wiredRadioSet = new HashSet<>(Arrays.asList(wiredDHCPRadio, wiredStaticRadio));
        wifiRadioSet = new HashSet<>(Arrays.asList(wifiDHCPRadio, wifiStaticRadio));
        wiredFieldSet = new HashSet<>(Arrays.asList(wiredIpField, wiredSubnetField, wiredGatewayField, wiredDNSField1, wiredDNSField2, wiredDNSField3));
        wifiFieldSet = new HashSet<>(Arrays.asList(ssidField, passphraseField, wifiIpField, wifiSubnetField, wifiGatewayField,  wifiDNSField1, wifiDNSField2, wifiDNSField3));
        clockInfoFieldSet = new HashSet<>(Arrays.asList(urlField, ipField, curPwdField, ntpField, newPwdField, volField, macAddrField, readerNameField, swupdateTypeField));
        wiredLabelSet = new HashSet<>(Arrays.asList(wiredIpLbl, wiredSubnetLbl, wiredGatewayLbl, wiredDNS1Lbl, wiredDNS2Lbl, wiredDNS3Lbl));
        wifiLabelSet = new HashSet<>(Arrays.asList(wifiIpLbl, wifiSubnetLbl, wifiGatewayLbl, ssidLbl, passphraseLbl, wifiDNS1Lbl, wifiDNS2Lbl, wifiDNS3Lbl));
        wifiDHCPRadio.setToggleGroup(wifiNetInfoGroup);
        wifiStaticRadio.setToggleGroup(wifiNetInfoGroup);

        for (RadioButton tmpRadio: wiredRadioSet)
            tmpRadio.setVisible(false);
        for (RadioButton tmpRadio: wifiRadioSet)
            tmpRadio.setVisible(false);
        for (TextField tempField : wiredFieldSet)
            tempField.setVisible(false);
        for (TextField tempField : wifiFieldSet)
            tempField.setVisible(false);
        for (Label tmpLbl : wiredLabelSet)
            tmpLbl.setVisible(false);
        for (Label tmpLbl : wifiLabelSet)
            tmpLbl.setVisible(false);
        resultLbl.setText("");
        tzChoiceBox.getItems().add("GMT-08:00Y\t-\tAmerica/Los_Angeles\t-\t(Pacific/PDT)");
        tzChoiceBox.getItems().add("GMT-07:00Y\t-\tAmerica/Denver\t\t-\t(Mountain Daylight/MDT)");
        tzChoiceBox.getItems().add("GMT-06:00Y\t-\tAmerica/Chicago\t\t-\t(Central/CDT)");
        tzChoiceBox.getItems().add("GMT-05:00Y\t-\tAmerica/New_York\t\t-\t(Eastern/EST)");
        tzChoiceBox.getItems().add("GMT-07:00Y\t-\tAmerica/Phoenix\t\t-\t(Mountain Standard/MST)");
        tzChoiceBox.getItems().add("GMT-10:00Y\t-\tPacific/Honolulu\t\t-\t(Hawaii Standard/HST)");
        tzChoiceBox.getItems().add("GMT-4:00Y\t-\tAmerica/Toronto\t\t-\t(Eastern Canadian/EDT)");
        tzChoiceBox.getItems().add("GMT+1:00Y\t-\tEurope/London\t\t\t-\t(Western European/WET)");
        tzChoiceBox.getItems().add("GMT+2:00Y\t-\tEurope/Berlin\t\t\t-\t(Central European/CET)");
        tzChoiceBox.getItems().add("GMT+3:00Y\t-\tEurope/Helsinki\t\t-\t(Eastern European/EET)");

        modelChoiceBox.getItems().add("SYnergy/X  / A20");
        modelChoiceBox.getItems().add("SYnergy/A 2416");
        modelChoiceBox.getItems().add("SYnergy/A 2410");

        Context.getInstance().setConfigController(this);

        wiredCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (wiredCheckBox.isSelected())
                {
                    for (RadioButton tmpRadio: wiredRadioSet)
                        tmpRadio.setVisible(true);
                    wiredDHCPRadio.fire();

                } else {
                    for (RadioButton tmpRadio: wiredRadioSet)
                    {
                        tmpRadio.setVisible(false);
                        tmpRadio.setSelected(false);
                    }
                    for (TextField tempField : wiredFieldSet)
                    {
                        tempField.setVisible(false);
                        tempField.clear();
                    }
                    for (Label tmpLbl : wiredLabelSet)
                        tmpLbl.setVisible(false);

                }

            }
        });

        wifiCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (wifiCheckBox.isSelected())
                {
                    for (RadioButton tmpRadio: wifiRadioSet)
                        tmpRadio.setVisible(true);

                    wifiDHCPRadio.fire();

                    ssidField.setVisible(true);
                    passphraseField.setVisible(true);

                    ssidLbl.setVisible(true);
                    passphraseLbl.setVisible(true);

                } else {
                    for (RadioButton tmpRadio: wifiRadioSet) {
                        tmpRadio.setVisible(false);
                        tmpRadio.setSelected(false);
                    }
                    for (TextField tempField : wifiFieldSet)
                    {
                        tempField.setVisible(false);
                        tempField.clear();
                    }
                    for (Label tmpLbl : wifiLabelSet)
                        tmpLbl.setVisible(false);
                }

            }
        });

        wiredNetInfoGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle t1) {
                if (wiredNetInfoGroup.getSelectedToggle() == wiredDHCPRadio)
                {
                    for(TextField tempField: wiredFieldSet)
                    {
                        tempField.setVisible(false);
                        tempField.clear();
                    }
                    for (Label tmpLbl : wiredLabelSet)
                        tmpLbl.setVisible(false);
                }
                if  (wiredNetInfoGroup.getSelectedToggle() == wiredStaticRadio)
                {
                    for(TextField tempField: wiredFieldSet)
                    {
                        tempField.setVisible(true);
                    }
                    for (Label tmpLbl : wiredLabelSet)
                        tmpLbl.setVisible(true);
                }
            }
        });

        wifiNetInfoGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle t1) {
                if (wifiNetInfoGroup.getSelectedToggle() == wifiDHCPRadio)
                {
                    for(TextField tempField: wifiFieldSet)
                    {
                        if (tempField!= ssidField && tempField !=passphraseField) {
                            tempField.setVisible(false);
                            tempField.clear();
                        }
                    }
                    for (Label tmpLbl : wifiLabelSet)
                        if (tmpLbl != ssidLbl && tmpLbl != passphraseLbl) {
                            tmpLbl.setVisible(false);
                        }

                }
                if  (wifiNetInfoGroup.getSelectedToggle() == wifiStaticRadio)
                {
                    for(TextField tempField: wifiFieldSet)
                    {
                        tempField.setVisible(true);
                    }
                    for (Label tmpLbl : wifiLabelSet)
                        tmpLbl.setVisible(true);
                }
            }
        });
    }

    @Override
    public void setMsgLabelText(String msg) {
        Platform.runLater(() -> {
            resultLbl.setText(msg.replace("\n", ""));
        });
    }

    @Override
    public void setIpTextAreaIPs(List<String> ipAddresses) {

    }

    @Override
    public void appendErrorTextArea(String msg) {
        System.out.println("Appenderrortextarea");
    }

    @Override
    public void setSelectedIps(String ip) {
        System.out.println("setSelectedIps");
    }

    @Override
    public String getPassword(String... ip) {
        String pwdStr = curPwdField.getText();
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

    @Override
    public void setProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
        });
    }

    @FXML
    private void clear(ActionEvent event)
    {
        for (RadioButton tmpRadio: wiredRadioSet)
            tmpRadio.setSelected(false);
        for (RadioButton tmpRadio: wifiRadioSet)
            tmpRadio.setSelected(false);
        for (TextField tempField : wiredFieldSet)
            tempField.clear();
        for (TextField tempField : wifiFieldSet)
            tempField.clear();
        for (TextField tempField : clockInfoFieldSet)
            tempField.clear();
        wifiCheckBox.setSelected(false);
        wiredCheckBox.setSelected(false);
        outputTextArea.clear();
        setProgress(0);
        setMsgLabelText("");
        System.out.println(outputTextArea.getScene().getWidth());




    }

    @FXML
    private void getInfo(ActionEvent event) throws InterruptedException, JSchException, IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        getInfoThread getInfoTask = new getInfoThread();
        executorService.submit(getInfoTask);
    }
    @FXML
    private void setInfo(ActionEvent event)
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        setInfoThread setInfoTask = new setInfoThread();
        executorService.submit(setInfoTask);
        getInfoThread getInfoTask = new getInfoThread();
        executorService.submit(getInfoTask);

    }

    @FXML
    private void previewSetInfo(ActionEvent event)
    {

    }

    private void showAlertMsg()
    {

    }

    private class getInfoThread extends SwingWorker<String, String>
    {
        @Override
        protected String doInBackground() throws Exception {
            TimeClock clock = new TimeClock();
            if (ipField.getText().trim().length() < 1 || ipField.getText().isEmpty())
            {
                return "noIPAddress";
            }
            if (!sshHandler.isIpValid(ipField.getText()))
            {

                return "invalidIPAddress";
            }
            if (ConfigUtilController.this.modelChoiceBox.getValue() == null)
            {
                return "noModelChosen";
            }
            clock.setIpAddress(ipField.getText());
            clock.setPassword(getPassword(ipField.getText()));
            clock.setModel(ConfigUtilController.this.modelChoiceBox.getValue());
            ConfigUtilController.this.setProgress(-1);
            ConfigUtilController.this.setMsgLabelText("Attempting to connect to " + ipField.getText());
            Session session = null;
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
                    ConfigUtilController.this.setProgress(0);
                    if(!sshHandler.isInforClock(clock))
                    {
                        return "notInfor";
                    }
                    ConfigUtilController.this.setMsgLabelText("Getting NTP servers from " + ipField.getText());
                    getInfoStringBuilder.append("NTP Servers (/etc/default/ntpd): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTPD));
                    ConfigUtilController.this.setProgress((double)1/13);

                    getInfoStringBuilder.append("\nNTP Servers (/etc/ntp.conf): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETNTP));
                    ConfigUtilController.this.setProgress((double)2/13);

                    ConfigUtilController.this.setMsgLabelText("Getting Customer URL from " + ipField.getText());
                    getInfoStringBuilder.append("\nCustomer URL: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETURL));
                    ConfigUtilController.this.setProgress((double)3/13);

                    ConfigUtilController.this.setMsgLabelText("Getting Time Zone from " + ipField.getText());
                    getInfoStringBuilder.append("\nTime Zone (/etc/TZ): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTZ));
                    ConfigUtilController.this.setProgress((double)4/13);
                    getInfoStringBuilder.append("\nTime Zone (/etc/timezone): " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETTIMEZONE));
                    ConfigUtilController.this.setProgress((double)5/13);

                    ConfigUtilController.this.setMsgLabelText("Getting Mac Address from " + ipField.getText());
                    getInfoStringBuilder.append("\nMAC Address: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETMAC));
                    ConfigUtilController.this.setProgress((double)6/13);
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
                    ConfigUtilController.this.setMsgLabelText("Getting Reader Name from " + ipField.getText());
                    getInfoStringBuilder.append("\nReader Name: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETREADERNAME));
                    ConfigUtilController.this.setProgress((double)7/13);

                    ConfigUtilController.this.setMsgLabelText("Getting Software Update Type from " + ipField.getText());
                    getInfoStringBuilder.append("\nSoftware Update Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETUPDATETYPE));
                    ConfigUtilController.this.setProgress((double)8/13);

                    ConfigUtilController.this.setMsgLabelText("Getting ethernet DNS from " + ipField.getText());
                    String temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETETHDNS);
                    getInfoStringBuilder.append("\nEthernet DNS Servers: "+ (temp.length() > 2 ? temp.replace("\n",", "):"N/A"));
                    ConfigUtilController.this.setProgress((double)9/13);

                    ConfigUtilController.this.setMsgLabelText("Getting WiFi Network information from " + ipField.getText());
                    getInfoStringBuilder.append("\nWifi Net Type: " + sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFINET).replace("address","IP Address:").replace("netmask", "Subnet Mask:").replace("gateway", "Gateway:"));
                    ConfigUtilController.this.setProgress((double)10/13);
                    temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFIDNS);
                    ConfigUtilController.this.setProgress((double)11/13);
                    getInfoStringBuilder.append("\nWiFi DNS Servers: "+ (temp.length() > 2 ? temp.replace("\n",", "):"N/A"));
                    temp = sshHandler.sendCmdBlocking(clock, session, Cmds.GETWIFICONF);
                    ConfigUtilController.this.setProgress((double)12/13);
                    getInfoStringBuilder.append("\nWiFi Conf: " + (temp.contains("SSID") ? temp.replace("\n"," "):"NONE"));


                } catch (Exception e) {
                    logger.error("!!ERROR!! "+ e.getLocalizedMessage());

                } finally{
                    session.disconnect();
                }


            session = null;
            return getInfoStringBuilder.toString();
        }

        @Override
        protected void done() {
            final String res;
            try {
                res = get();
                if(res.equals("invalidIPAddress"))
                {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText(ipField.getText() + " is an invalid IP address");
                } else if (res.equals("invalidMACAddress"))
                {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText(macAddrField.getText() + " is an invalid IP address");
                } else if(res.equals("noIPAddress")) {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText("IP address field is empty");
                }  else if(res.equals("noModelChosen")) {
                ConfigUtilController.this.setProgress(0);
                ConfigUtilController.this.setMsgLabelText("No model has been chosen.");
            } else if(res.equals("badPwd")) {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText("Incorrect password. Cannot Connect to " + ipField.getText());
                } else if(res.equals("notInfor")) {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText( ipField.getText() + " is not an Infor clock");
                }else {
                    outputTextArea.setText(res);
                    ConfigUtilController.this.setProgress(1);
                    ConfigUtilController.this.setMsgLabelText("Done getting settings from " + ipField.getText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class setInfoThread extends SwingWorker<String,String>
    {

        @Override
        protected String doInBackground() throws Exception {
            TimeClock clock = new TimeClock();
            if (ipField.getText().trim().length() < 1 || ipField.getText().isEmpty())
            {
                return "noIPAddress";
            }
            if (!sshHandler.isIpValid(ipField.getText()))
            {

                return "invalidIPAddress";
            }
            clock.setIpAddress(ipField.getText());
            clock.setPassword(getPassword(ipField.getText()));
            clock.setModel("Synergy/X A20");
            ConfigUtilController.this.setProgress(-1);
            Session session = sshHandler.longConnect(clock);

            ConfigUtilController.this.setProgress(0);
            if(!sshHandler.isInforClock(clock))
            {
                return "notInfor";
            }
            if (urlField.getText().trim().length() > 0)
            {
                ConfigUtilController.this.setProgress(0);
                ConfigUtilController.this.setMsgLabelText("Setting Customer URL to "+urlField.getText()+" for " + ipField.getText());

                sshHandler.sendCmdBlocking(clock, session, Cmds.SETCUSTURL, urlField.getText());
                ConfigUtilController.this.setProgress((double)1/14);
            }
            if (ntpField.getText().trim().length() > 0)
            {
                ConfigUtilController.this.setMsgLabelText("Setting NTP to "+ ntpField.getText() +" for " + ipField.getText());
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETNTPD, "# Get initial time via ntpdate?\\n" +
                        "NTPDATE=yes\\n" +
                        "NTPDATE_OPTS=\\42-t 5\\42\\n" +
                        "# Start the ntp daemon?\\n" +
                        "NTPD=yes\\n" +
                        "# NTP Servers to use for ntpdate\\n" +
                        "NTPSERVERS=\\42" + ntpField.getText());
                ConfigUtilController.this.setProgress((double)2/14);
                sshHandler.sendCmdBlocking(clock, session, Cmds.REMOVENTP);
                ConfigUtilController.this.setProgress((double)3/14);
                for (String server : ntpField.getText().split(" "))
                {
                    sshHandler.sendCmdBlocking(clock, session, Cmds.SETNTP, "server " + server);
                }
                ConfigUtilController.this.setProgress((double)4/14);

            }
            if (tzChoiceBox.getValue() != null)
            {

                String[] tzVal=tzChoiceBox.getValue().split("-");
                ConfigUtilController.this.setMsgLabelText("Setting Timezone to "+ (tzVal.length > 3 ? tzVal[2]:tzVal[1])+ "for" + ipField.getText());
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETTZ, tzVal.length > 3 ? tzVal[2]:tzVal[1]);
                ConfigUtilController.this.setProgress((double)5/14);
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETTIMEZONE, tzVal.length > 3 ? tzVal[2]:tzVal[1]);
                ConfigUtilController.this.setProgress((double)6/14);

            }
            if (newPwdField.getText().trim().length() > 0)
            {

            }
            if (volField.getText().trim().length() > 0)
            {

            }
            if (macAddrField.getText().trim().length() > 0)
            {
                if (!sshHandler.isValidMAC(macAddrField.getText().trim()))
                {
                    return "invalidMacAddress";
                }
                ConfigUtilController.this.setMsgLabelText("Setting MAC Address to "+ macAddrField.getText()+ " for" + ipField.getText());
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETMAC, macAddrField.getText().trim());
                ConfigUtilController.this.setProgress((double)7/14);
            }
            if (readerNameField.getText().trim().length() > 0)
            {
                ConfigUtilController.this.setMsgLabelText("Setting readerName to "+ macAddrField.getText()+ " for" + ipField.getText());
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETREADERNAME, readerNameField.getText());
                ConfigUtilController.this.setProgress((double)8/14);
            }
            if (swupdateTypeField.getText().trim().length() > 0)
            {
                ConfigUtilController.this.setMsgLabelText("Setting Software Update Type to "+ macAddrField.getText()+ " for" + ipField.getText());
                sshHandler.sendCmdBlocking(clock, session, Cmds.SETUPDATETYPE, swupdateTypeField.getText());
                ConfigUtilController.this.setProgress((double)9/14);
            }

            if (wiredCheckBox.isSelected())
            {
                if(wiredStaticRadio.isSelected())
                {
                    ConfigUtilController.this.setMsgLabelText("Setting Static IP to IP: "+wiredIpField.getText()+" Subnet: "+wiredSubnetField.getText()+" Gateway:"+wiredGatewayField.getText()+ " for" + ipField.getText());
                    sshHandler.sendCmdBlocking(clock, session, Cmds.SETNETINTERFACES,"eth0", "static", wiredIpField.getText(), wiredSubnetField.getText(), wiredGatewayField.getText());
                    ConfigUtilController.this.setProgress((double)10/14);
                    sshHandler.sendCmdBlocking(clock, session, Cmds.SETDNS, "eth0", wiredDNSField1.getText(),  wiredDNSField2.getText(),  wiredDNSField3.getText());
                    ConfigUtilController.this.setProgress((double)11/14);
                } else if (wiredDHCPRadio.isSelected())
                {
                    sshHandler.sendCmdBlocking(clock, session, Cmds.SETNETINTERFACES,"eth0", "dhcp");
                    ConfigUtilController.this.setProgress((double)11/14);
                }
            }

            if (wifiCheckBox.isSelected())
            {
                try {

                    if (passphraseField.getText().isEmpty())
                    {
                        if (!ssidField.getText().isEmpty()) {
                           boolean confirm = CompletableFuture.supplyAsync(()->{
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("WiFi Change Confirmation");
                                alert.setHeaderText("Passphrase field is empty. Change anyway?");
                                //alert.setContentText("From\n" + cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);


                                Optional<ButtonType> res = alert.showAndWait();
                                return res.get()==ButtonType.OK;

                            },Platform::runLater).join();

                            if (confirm) {
                                sshHandler.sendCmdBlocking(clock, session, "echo \"SSID="+ssidField.getText()+"\" > /etc/wifi.conf");
                                sshHandler.sendCmdBlocking(clock, session, "echo \"PASSPHRASE=\" >> /etc/wifi.conf");
                            }
                        }

                    } else {
                        if (ssidField.getText().isEmpty()) {
                            boolean confirm = CompletableFuture.supplyAsync(()->{
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("WiFi Change Confirmation");
                            alert.setHeaderText("SSID field is empty, but Passphrase field is not. Change anyway?");
                            //alert.setContentText("From\n" + cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);


                            Optional<ButtonType> res = alert.showAndWait();
                            return res.get()==ButtonType.OK;
                            },Platform::runLater).join();
                            if (confirm) {
                                sshHandler.sendCmdBlocking(clock, session, "echo -e \"SSID=\nPASSPHRASE="+passphraseField.getText()+"\" > /etc/wifi.conf");
                            }
                        } else {
                            sshHandler.sendCmdBlocking(clock, session, "echo \"SSID="+ssidField.getText()+"\" > /etc/wifi.conf");
                            sshHandler.sendCmdBlocking(clock, session, "echo \"PASSPHRASE="+passphraseField.getText()+"\" >> /etc/wifi.conf");
                        }
                    }

                    if (ssidField.getText().equals("DELETE") && passphraseField.getText().equals("DELETE"))
                    {
                        sshHandler.sendCmdBlocking(clock, session, "rm -rf /etc/wifi.conf; rm -rf /etc/wifi.conf.current");
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (wifiStaticRadio.isSelected()) {
                    try {
                        ConfigUtilController.this.setProgress((double)10/14);
                        sshHandler.sendCmdBlocking(clock, session, Cmds.SETNETINTERFACES,"wlan0", "static", wifiIpField.getText(), wifiSubnetField.getText(), wifiGatewayField.getText()  );
                        ConfigUtilController.this.setProgress((double)11/14);
                        sshHandler.sendCmdBlocking(clock, session, Cmds.SETDNS, "wlan0", wifiDNSField1.getText(), wifiDNSField2.getText(), wifiDNSField3.getText());


                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (wifiDHCPRadio.isSelected()) {

                    try {
                        sshHandler.sendCmdBlocking(clock, session, Cmds.SETNETINTERFACES,"wlan0", "dhcp");
                        ConfigUtilController.this.setProgress((double)11/14);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            session.disconnect();
            session = null;
            return "success";
        }

        @Override
        protected void done() {
            try {

                String res = get();
                if(res.equals("invalidIPAddress"))
                {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText(ipField.getText() + " is an invalid IP address");
                } else if (res.equals("invalidMACAddress"))
                {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText(macAddrField.getText() + " is an invalid IP address");
                }  else if(res.equals("noIPAddress")) {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText("IP address field is empty");
                } else if(res.equals("notInfor")) {
                    ConfigUtilController.this.setProgress(0);
                    ConfigUtilController.this.setMsgLabelText( ipField.getText() + " is not an Infor clock");
                }else {

                    try {
                        ConfigUtilController.this.setProgress(1);
                        ConfigUtilController.this.setMsgLabelText("Done updating settings for " +ipField.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    }

    public void setStage(Stage stage)
    {
        this.currentStage = stage;
        this.currentScene = stage.getScene();
        if (currentScene.getRoot().getStyle().contains("black")) {
            Platform.runLater(() -> {
                progressBar.setStyle("-fx-accent: darkred");
            });
        } else {
            Platform.runLater(() -> {
                progressBar.setStyle("");
            });
        }

    }


}
