package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.gui.Context;
import com.jcraft.jsch.JSchException;
import com.darkzalgo.model.TimeClock;
import com.darkzalgo.utility.SSHHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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

    @FXML CheckBox repeatCmdBox, getInfoBox;

    @FXML ChoiceBox subnetChoiceBox;

    @FXML RadioButton removeGtFilesRadio, rebootRadio;

    @FXML TextField timerLengthField;

    @FXML Button sendCmdBtn, cancelTimerBtn;

    @FXML Label msgLabel;

    @FXML Pane rebootWindowPane;

    ToggleGroup cmdPresetGroup = new ToggleGroup();

    private boolean darkLight;

    private SSHHandler sshHandler = new SSHHandler(this);

    private Timer timer;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private TableViewController tableViewController;

    private ObservableList<TimeClock> timeClocks;

    private Parent tableViewRoot, sftpViewRoot;

    private Scene tableViewScene, sftpViewScene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Context.getInstance().setMainController(this);
        ipTextArea.setWrapText(true);
        errorTextArea.setWrapText(true);
        errorTextArea.setEditable(false);
        /*final double START_WIDTH_ERROR_TEXT = Double.valueOf(errorTextArea.getPrefWidth());
        final double START_PANE_WIDTH = rebootWindowPane.getPrefWidth();
        logger.info(START_WIDTH_ERROR_TEXT + " asdf");*/
        removeGtFilesRadio.setToggleGroup(cmdPresetGroup);
        rebootRadio.setToggleGroup(cmdPresetGroup);
        getInfoBox.setSelected(true);

        try {
            tableViewRoot = new FXMLLoader(getClass().getResource("/tableViewWindow.fxml")).load();
            tableViewScene = new Scene(tableViewRoot, 930, 400);
            sftpViewRoot = new FXMLLoader(getClass().getResource("/sftpViewWindow.fxml")).load();
            sftpViewScene = new Scene(sftpViewRoot, 600,400);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                    commandTextArea.setText("rm /Arm/Synergy/SY\nrm /Arm/Synergy/SYTransactions\nfbv /root/synergyX/SynelAmericas_320x240.jpg");
                }
                else if (cmdPresetGroup.getSelectedToggle() == rebootRadio)
                {
                    commandTextArea.setText("reboot");
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

       /* errorTextArea.focusedProperty().addListener((obs, oldValue, newValue) ->{
       if(newValue)
            {
                Text text = new Text(errorTextArea.getText());
                text.setFont(errorTextArea.getFont());
                double width = text.getLayoutBounds().getWidth();
                logger.info("width " + width + " wrappingwidth " + text.getLayoutBounds().getWidth());
                if(width > START_WIDTH_ERROR_TEXT)
                {
                    errorTextArea.setPrefWidth(width * 1.2);
                }
            }
       if(oldValue)
       {
           errorTextArea.setPrefWidth(START_WIDTH_ERROR_TEXT);
       }

        });*/

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
        tableViewController = Context.getInstance().getTableViewController();
        String ipPrefix = (String) subnetChoiceBox.getValue();
        String cmd = commandTextArea.getText();
        if (!timerLengthField.getText().equals(""))
        {
            seconds = Integer.parseInt(timerLengthField.getText());
        }

        if (timer != null)
        cancelTimerBtn.fire();

        TimeClock tempClock;
        timeClocks = Context.getInstance().currentClocks();

        timeClocks.clear();

        if (!(ipTextArea.getText().split(",").length > 0))
        {

            String fullIP = sshHandler.checkHost(ipPrefix + ipTextArea.getText());
            if(fullIP != null)
            {
                tempClock = new TimeClock();
                tempClock.setIpAddress(ipPrefix + ipTextArea.getText());
                logger.info("Added " + tempClock.getIpAddress() + " to ip list");
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
                        tempClock.setIpAddress(ipPrefix + ip);
                        tempClock.setPort(Integer.parseInt(fullIP.split(",")[1]));
                        logger.info("Added " + tempClock.getIpAddress() + " to ip list");
                        timeClocks.add(tempClock);
                    }
                }
            }
        }

        if(getInfoBox.isSelected())
        {
            timeClocks.forEach((clock -> {
                try {
                    sshHandler.getClockInfo(clock);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSchException | InterruptedException e) {
                    logger.warn("Could not connect to " + clock.getUsername() + "@" + clock.getIpAddress() + ":" + clock.getPort() + " using password " + clock.getPassword());
                    appendErrorTextArea("Could not connect to " + clock.getUsername() + "@" + clock.getIpAddress() + ":" + clock.getPort() + " using password " + clock.getPassword() + "\n");
                }
            }));
        }

        if(repeatCmdBox.isSelected() && !cmd.equals(""))
        {
            RepeatTask task = new RepeatTask(timeClocks, cmd);
            timer = new Timer(true);
            timer.scheduleAtFixedRate(task, 0, seconds * 1000);
        }
        else if (!cmd.equals(""))
        {
            logger.info("Sending " + cmd);
            timeClocks.forEach((n -> {
                try {
                        sshHandler.sendCmd(cmd, n);
                    } catch (JSchException | InterruptedException e)
                    {
                        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
                        String date = formatter.format(new Date(System.currentTimeMillis()));
                        errorTextArea.appendText("[" + date + "] -- Could not connect to " + n.getIpAddress() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

        }
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

        Stage tableViewStage = new Stage();
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tableViewWindow.fxml"));
//            Parent root = loader.load();
        tableViewRoot.setStyle(node.getScene().getRoot().getStyle());
        tableViewStage.setTitle("Clock Info Table");
        tableViewStage.setScene(tableViewScene);
        tableViewStage.initModality(Modality.WINDOW_MODAL);

            /*timeClocks.addListener(new ListChangeListener<TimeClock>() {
                @Override
                public void onChanged(Change<? extends TimeClock> change)
                {
                    while (change.next())
                    {
                        if (tableViewController!=null)
                        {
                            tableViewController.refresh((ObservableList<TimeClock>) change.getList());
                        }
                    }
                }
            });*/
        Window primaryWindow = node.getScene().getWindow();

        tableViewStage.initOwner(primaryWindow);

        tableViewStage.setX(primaryWindow.getX() + 200);
        tableViewStage.setY(primaryWindow.getY() + 100);

        tableViewStage.setResizable(false);
        if(timeClocks!=null) {
            tableViewController.refresh(timeClocks);
        }
        tableViewStage.show();

    }

    @FXML
    private void openSftpView(ActionEvent event)
    {
        Node node = (Node) event.getSource();

        Stage sftpViewStage = new Stage();

        sftpViewRoot.setStyle(node.getScene().getRoot().getStyle());
        sftpViewStage.setTitle("SFTP");
        sftpViewStage.setScene(sftpViewScene);
        sftpViewStage.initModality(Modality.WINDOW_MODAL);

        Window primaryWindow = node.getScene().getWindow();

        sftpViewStage.initOwner(primaryWindow);

        sftpViewStage.setX(primaryWindow.getX() + 200);
        sftpViewStage.setY(primaryWindow.getY() + 100);

        sftpViewStage.setResizable(false);

        sftpViewStage.show();

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
        private ObservableList<TimeClock> timeClocks;
        private String cmd;
        private SSHHandler sshHandler = new SSHHandler();

        public RepeatTask(ObservableList<TimeClock> timeClocks, String cmd)
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
                    errorTextArea.appendText("["+ date +"] -- Could not connect to " + n.getIpAddress()+"\n");
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
        errorTextArea.appendText("["+ date +"] -- " + msg + "\n");
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
