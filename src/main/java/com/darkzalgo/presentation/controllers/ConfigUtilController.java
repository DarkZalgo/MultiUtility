package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
        resultLbl.setText(msg);
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
        return "getPasswordConfigUtil";
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
        TimeClock clock = new TimeClock();
        clock.setIpAddress("192.168.4.50");
        clock.setPassword("$ynEL72RVER");
        sshHandler.getClockInfo(clock);
    }


}
