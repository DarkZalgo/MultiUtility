package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.gui.Context;
import com.jcraft.jsch.JSchException;
import com.darkzalgo.model.TimeClock;
import com.darkzalgo.utility.SSHHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MainController implements Initializable
{
    @FXML TextArea ipTextArea, errorTextArea, commandTextArea;

    @FXML CheckBox repeatCmdBox;

    @FXML ChoiceBox subnetChoiceBox;

    @FXML RadioButton removeGtFilesRadio, rebootRadio, getInfoRadio;

    @FXML TextField timerLengthField;

    @FXML Button sendCmdBtn, cancelTimerBtn;

    @FXML Label msgLabel;

    ToggleGroup cmdPresetGroup = new ToggleGroup();

    private boolean darkLight;

    private SSHHandler sshHandler = new SSHHandler(this);

    private Timer timer;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Context.getInstance().setMainController(this);
        ipTextArea.setWrapText(true);
        errorTextArea.setWrapText(true);
        errorTextArea.setEditable(false);

        removeGtFilesRadio.setToggleGroup(cmdPresetGroup);
        rebootRadio.setToggleGroup(cmdPresetGroup);
        getInfoRadio.setToggleGroup(cmdPresetGroup);

        timerLengthField.setDisable(true);

        repeatCmdBox.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1)
            {
                if(repeatCmdBox.isSelected())
                {
                    timerLengthField.setDisable(false);
                    timerLengthField.setText("30");
                }
                else
                {
                    timerLengthField.setDisable(true);
                    timerLengthField.clear();
                }
            }
        });

        cmdPresetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle t1)
            {
                if (cmdPresetGroup.getSelectedToggle() == removeGtFilesRadio)
                {
                    commandTextArea.setText("rm /Arm/Synergy/SY\nrm /Arm/Synergy/SYTransactions");
                }
                else if (cmdPresetGroup.getSelectedToggle() == rebootRadio)
                {
                    commandTextArea.setText("reboot");
                }
                else if (cmdPresetGroup.getSelectedToggle() == getInfoRadio)
                {
                    commandTextArea.setText("");
                }
            }
        });

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

        timerLengthField.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1)
            {
                if (!t1.matches("[0-9]*"))
                {
                    timerLengthField.setText(t1.replaceAll("[^0-9]", ""));
                }
                if (!(t1.length() < 6))
                {
                    timerLengthField.setText(s);
                }
            }
        });

        subnetChoiceBox.getItems().add("192.168.6.");
        subnetChoiceBox.getItems().add("192.168.7.");
        subnetChoiceBox.getItems().add("192.168.1.");
        subnetChoiceBox.getItems().add("10.10.10.");
        subnetChoiceBox.getItems().add("10.10.11.");

        subnetChoiceBox.setValue(subnetChoiceBox.getItems().get(0));
    }

    @FXML
    private void sendCmd(ActionEvent event) throws IOException, JSchException, InterruptedException {
        int seconds = 30;
        String ipPrefix = (String) subnetChoiceBox.getValue();
        String cmd = commandTextArea.getText();
        if (!timerLengthField.getText().equals(""))
        {
            seconds = Integer.parseInt(timerLengthField.getText());
        }

        if (timer != null)
        cancelTimerBtn.fire();

        TimeClock tempClock;
        ArrayList<TimeClock> timeClocks = new ArrayList<>();

        if (!(ipTextArea.getText().split(",").length > 0))
        {

            String fullIP = sshHandler.checkHost(ipPrefix + ipTextArea.getText());
            if(fullIP != null)
            {
                tempClock = new TimeClock();
                tempClock.setHost(ipPrefix + ipTextArea.getText());
                logger.info("Added " + tempClock.getHost() + " to ip list");
                timeClocks.add(tempClock);
            }
        }
        else {
            for (String ip : ipTextArea.getText().split(","))
            {
                if (!ip.equals("") && (Integer.parseInt(ip) > 0 && Integer.parseInt(ip) < 256))
                {
                    String fullIP = sshHandler.checkHost(ipPrefix + ip);
                    if(fullIP != null)
                    {
                        tempClock = new TimeClock();
                        tempClock.setHost(ipPrefix + ip);
                        tempClock.setPort(Integer.parseInt(fullIP.split(",")[1]));
                        logger.info("Added " + tempClock.getHost() + " to ip list");
                        timeClocks.add(tempClock);
                    }
                }
            }
        }

        logger.info("Timeclocks Length " + timeClocks.size());
        timeClocks.forEach((n-> {
            try
            {
                sshHandler.getClockInfo(n);
            } catch (IOException e )
            {
                e.printStackTrace();
            }catch (JSchException | InterruptedException e )
            {
                logger.warn("Could not connect to " + n.getUsername() + "@" + n.getHost() + ":" + n.getPort() +" using password " + n.getPassword());
                appendErrorTextArea("Could not connect to " + n.getUsername() + "@" + n.getHost() + ":" + n.getPort() +" using password " + n.getPassword() + "\n");
            }
        }));

        if(repeatCmdBox.isSelected())
        {
            RepeatTask task = new RepeatTask(timeClocks, cmd);
            timer = new Timer(true);
            timer.scheduleAtFixedRate(task, 0, seconds * 1000);
        }
        else
        {
            logger.info("Sending " + cmd);
            timeClocks.forEach((n -> {
                try {
                        sshHandler.sendCmd(cmd, n);
                    } catch (JSchException | InterruptedException e)
                    {
                        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
                        String date = formatter.format(new Date(System.currentTimeMillis()));
                        errorTextArea.appendText("[" + date + "] -- Could not connect to " + n.getHost() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

        }
        Context.getInstance().setClocks(timeClocks);

    }
    @FXML
    private void cancel(ActionEvent event) throws SocketException, UnknownHostException
    {
        if (timer != null)
        timer.cancel();
        sshHandler.disconnect(null);
        timer = null;
        logger.info("Cancelled reboot timer");
    }

    @FXML private void getIPs(ActionEvent event) throws IOException, InterruptedException, ExecutionException {
        sshHandler.checkAllHosts((String) subnetChoiceBox.getValue());
    }

    @FXML
    private void openTableView(ActionEvent event) {
        Node node = (Node) event.getSource();

        try {
            Stage tableViewStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tableViewWindow.fxml"));
            Parent root = loader.load();

            root.setStyle(node.getScene().getRoot().getStyle());
            tableViewStage.setTitle("Clock Info Table");
            tableViewStage.setScene(new Scene(root, 930, 400));
            tableViewStage.initModality(Modality.WINDOW_MODAL);

            Window primaryWindow = node.getScene().getWindow();

            tableViewStage.initOwner(primaryWindow);

            tableViewStage.setX(primaryWindow.getX() + 200);
            tableViewStage.setY(primaryWindow.getY() + 100);

            tableViewStage.setResizable(false);

            tableViewStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void darkMode(ActionEvent event)
    {
        Node node = (Node)event.getSource();
        if (!darkLight)
        {

            node.getScene().getRoot().setStyle("-fx-base:black");
            darkLight = true;
        }
        else if(darkLight)
        {
            node.getScene().getRoot().setStyle("");
            darkLight = false;
        }

    }


    private class RepeatTask extends TimerTask
    {
        private ArrayList<TimeClock> timeClocks;
        private String cmd;
        private SSHHandler sshHandler = new SSHHandler();

        public RepeatTask(ArrayList<TimeClock> timeClocks, String cmd)
        {
            this.timeClocks = timeClocks;
            this.cmd = cmd;
        }

        @Override
        public void run()
        {
            logger.info("Starting timed reboot event");
            timeClocks.stream().forEach((n -> {
                try
                {
                    sshHandler.sendCmd(cmd, n);

                } catch (JSchException  e)
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                    String date = formatter.format(new Date(System.currentTimeMillis()));
                    errorTextArea.appendText("["+ date +"] -- Could not connect to " + n.getHost()+"\n");
                }catch (IOException | InterruptedException e)
                {
                    e.printStackTrace();
                }
            }));
        }



    }
    public void setMsgLabelText(String msg)
    {
        msgLabel.setText(msg);
    }

    public void setIpTextAreaIPs(List<String> ipAddresses)
    {
        ipTextArea.setText("");
        ipAddresses.forEach((n ->{
            String lastOctet = n.split("\\.")[3];
            ipTextArea.appendText(lastOctet + ",");
        }));
        if(ipTextArea.getLength() > 0)
            ipTextArea.setText(ipTextArea.getText().substring(0,ipTextArea.getLength()-1));
    }

    public void appendErrorTextArea(String msg)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String date = formatter.format(new Date(System.currentTimeMillis()));
        errorTextArea.appendText("["+ date +"] --" + msg + "\n");
    }

    public void setSelectedIps(String ip)
    {
        if (ipTextArea.getText().equals(""))
        {
            ipTextArea.setText(ip);

        }else
        {
            ipTextArea.appendText(","+ip);
        }
    }

}
