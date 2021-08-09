package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.Cmds;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
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



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
                        tempField.setVisible(false);
                        tempField.clear();
                    }
                    for (Label tmpLbl : wifiLabelSet)
                        tmpLbl.setVisible(false);
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
        resultLbl.setText(msg.replace("\n",""));
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
    public String getPassword(String ip) {
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
        progressBar.setProgress(progress);
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

    }

    @FXML
    private void getInfo(ActionEvent event) throws InterruptedException, JSchException, IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        getInfoAsyncThread getInfoTask = new getInfoAsyncThread();
        executorService.submit(getInfoTask);
    }
    @FXML
    private void setInfo(ActionEvent event)
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        setInfoAsyncThread setInfoTask = new setInfoAsyncThread();
        executorService.submit(setInfoTask);
        getInfoAsyncThread getInfoTask = new getInfoAsyncThread();
        executorService.submit(getInfoTask);
        setMsgLabelText("Done checking info after applying");
    }

    @FXML
    private void previewSetInfo(ActionEvent event)
    {

    }

    private class getInfoAsyncThread extends SwingWorker<String, String>
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
            StringBuilder getInfoStringBuilder = new StringBuilder();

                try {


                   getInfoStringBuilder.append("NTP Servers (/etc/default/ntpd): " + sshHandler.sendCmdBlocking(clock, Cmds.GETNTPD));
                    getInfoStringBuilder.append("\nNTP Servers (/etc/ntp.conf): " + sshHandler.sendCmdBlocking(clock, Cmds.GETNTP));
                    getInfoStringBuilder.append("\nCustomer URL: " + sshHandler.sendCmdBlocking(clock, Cmds.GETURL));
                    getInfoStringBuilder.append("\nTime Zone (/etc/TZ): " + sshHandler.sendCmdBlocking(clock, Cmds.GETTZ));
                    getInfoStringBuilder.append("\nTime Zone (/etc/timezone): " + sshHandler.sendCmdBlocking(clock, Cmds.GETTIMEZONE));
                    getInfoStringBuilder.append("\nMAC Address: " + sshHandler.sendCmdBlocking(clock, Cmds.GETMAC));
                    switch (clock.getModel())
                    {
                        case "Synergy/X A20":
                            getInfoStringBuilder.append("\nVolume: " + sshHandler.sendCmdBlocking(clock, Cmds.GETA20VOL));
                            break;
                        default:
                            break;

                    }
                    getInfoStringBuilder.append("\nReader Name: " + sshHandler.sendCmdBlocking(clock, Cmds.GETREADERNAME));
                    getInfoStringBuilder.append("\nSoftware Update Type: " + sshHandler.sendCmdBlocking(clock, Cmds.GETUPDATETYPE));
                    getInfoStringBuilder.append("\nWifi Net Type: " + sshHandler.sendCmdBlocking(clock, Cmds.GETWIFINET).replace("address","IP Address:").replace("netmask", "Subnet Mask:").replace("gateway", "Gateway:"));
                    String temp = sshHandler.sendCmdBlocking(clock, Cmds.GETWIFIDNS);
                    getInfoStringBuilder.append("\nWiFi DNS Servers: "+ (temp.length() > 2 ? temp:"N/A"));
                    temp = sshHandler.sendCmdBlocking(clock, Cmds.GETWIFICONF);
                    getInfoStringBuilder.append("\nWiFi Conf: " + (temp.contains("SSID") ? temp.replace("\n"," "):"NONE"));

                    setProgress(1/12);
                } catch (Exception e) {
                    logger.error("!!ERROR!! "+ e.getLocalizedMessage());

                }


            return getInfoStringBuilder.toString();
        }

        @Override
        protected void done() {
            final String res;
            try {
                res = get();
                Platform.runLater(() -> {
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
                }else {
                    outputTextArea.setText(res);
                    ConfigUtilController.this.setProgress(1);
                    ConfigUtilController.this.setMsgLabelText("Done getting settings from " + ipField.getText());
                }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class setInfoAsyncThread extends SwingWorker<String,String>
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
            if (urlField.getText().trim().length() > 0)
            {
                sshHandler.sendCmdBlocking(clock, Cmds.SETCUSTURL, urlField.getText());
            }
            if (ntpField.getText().trim().length() > 0)
            {
                sshHandler.sendCmdBlocking(clock, Cmds.SETNTPD, "# Get initial time via ntpdate?\\n" +
                        "NTPDATE=yes\\n" +
                        "NTPDATE_OPTS=\\42-t 5\\42\\n" +
                        "# Start the ntp daemon?\\n" +
                        "NTPD=yes\\n" +
                        "# NTP Servers to use for ntpdate\\n" +
                        "NTPSERVERS=\\42" + ntpField.getText());
                sshHandler.sendCmdBlocking(clock, Cmds.REMOVENTP);
                for (String server : ntpField.getText().split(" "))
                {
                    sshHandler.sendCmdBlocking(clock, Cmds.SETNTP, "server " + server);
                }

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
                sshHandler.sendCmdBlocking(clock, Cmds.SETMAC, macAddrField.getText().trim());
            }
            if (readerNameField.getText().trim().length() > 0)
            {
                sshHandler.sendCmdBlocking(clock, Cmds.SETREADERNAME, readerNameField.getText());
            }
            if (swupdateTypeField.getText().trim().length() > 0)
            {
                sshHandler.sendCmdBlocking(clock, Cmds.SETUPDATETYPE, swupdateTypeField.getText());
            }

            if (wiredCheckBox.isSelected())
            {
                if(wiredStaticRadio.isSelected())
                {
                    sshHandler.sendCmdBlocking(clock, Cmds.SETNETINTERFACES,"eth0", "static", wiredIpField.getText(), wiredSubnetField.getText(), wiredGatewayField.getText());
                    sshHandler.sendCmdBlocking(clock, Cmds.SETDNS, "eth0", wiredDNSField1.getText(),  wiredDNSField2.getText(),  wiredDNSField3.getText());
                } else if (wiredDHCPRadio.isSelected())
                {
                    sshHandler.sendCmdBlocking(clock, Cmds.SETNETINTERFACES,"eth0", "dhcp");
                }
            }

            if (wifiCheckBox.isSelected())
            {
                try {
                    if (!ssidField.getText().isEmpty())
                    {
                        sshHandler.sendCmdBlocking(clock, "echo \"SSID="+ssidField.getText()+"\" > /etc/wifi.conf");
                    }
                    if (passphraseField.getText().isEmpty())
                    {
                        if (!ssidField.getText().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("WiFi Change Confirmation");
                            alert.setHeaderText("Passphrase field is empty. Change anyway?");
                            //alert.setContentText("From\n" + cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);


                            Optional<ButtonType> res = alert.showAndWait();
                            if (res.get() == ButtonType.OK) {
                                sshHandler.sendCmdBlocking(clock, "echo \"PASSPHRASE=\" >> /etc/wifi.conf");
                            }
                        }

                    } else {
                        if (ssidField.getText().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("WiFi Change Confirmation");
                            alert.setHeaderText("SSID field is empty, but Passphrase field is not. Change anyway?");
                            //alert.setContentText("From\n" + cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);


                            Optional<ButtonType> res = alert.showAndWait();
                            if (res.get() == ButtonType.OK) {
                                sshHandler.sendCmdBlocking(clock, "echo -e \"SSID=\nPASSPHRASE="+passphraseField.getText()+"\" > /etc/wifi.conf");
                            }
                        } else {
                            sshHandler.sendCmdBlocking(clock, "echo \"PASSPHRASE="+passphraseField.getText()+"\" >> /etc/wifi.conf");
                        }
                    }

                    if (ssidField.getText().equals("DELETE") && passphraseField.getText().equals("DELETE"))
                    {
                        sshHandler.sendCmdBlocking(clock, "rm -rf /etc/wifi.conf; rm -rf /etc/wifi.conf.current");
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (wifiStaticRadio.isSelected()) {
                    try {
                        sshHandler.sendCmdBlocking(clock, Cmds.SETNETINTERFACES,"wlan0", "static", wifiIpField.getText(), wifiSubnetField.getText(), wifiGatewayField.getText()  );
                        sshHandler.sendCmdBlocking(clock, Cmds.SETDNS, "wlan0", wifiDNSField1.getText(), wifiDNSField2.getText(), wifiDNSField3.getText());


                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (wifiDHCPRadio.isSelected()) {

                    try {
                        sshHandler.sendCmdBlocking(clock, Cmds.SETNETINTERFACES,"wlan0", "dhcp");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                String res = get();
                Platform.runLater(()->{
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
                }else {

                    try {
                        ConfigUtilController.this.setProgress(1);
                        ConfigUtilController.this.setMsgLabelText("Done getting settings from " +ipField.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    }


}
