package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.gui.Context;
import com.jcraft.jsch.JSchException;
import com.darkzalgo.model.TimeClock;
import com.darkzalgo.utility.SSHHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController extends AbstractController implements Initializable
{
    @FXML TextArea ipTextArea, errorTextArea, commandTextArea;

    @FXML CheckBox repeatCmdBox, getInfoBox;

    @FXML ChoiceBox<String> subnetChoiceBox;

    @FXML RadioButton removeGtFilesRadio, rebootRadio;
    @FXML RadioButton readerNamePwdRadio, macAddrPwdRadio, neitherPwdRadio;

    @FXML TextField timerLengthField, passwordField;

    @FXML Button sendCmdBtn, cancelTimerBtn;

    @FXML Label msgLabel;

    @FXML Pane rebootWindowPane;

    Set<RadioButton> passwordRadioSet;

    ToggleGroup cmdPresetGroup = new ToggleGroup();
    ToggleGroup passwordRadioGroup = new ToggleGroup();

    private boolean darkLight;

    private SSHHandler sshHandler = new SSHHandler(this);

    private Timer timer;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private TableViewController tableViewController;

    private StressTestController stressTestController;

    private ConfigUtilController configUtilController;

    private ObservableList<TimeClock> timeClocks;

    private Parent tableViewRoot, sftpViewRoot, configUtilViewRoot, stressTestViewRoot;

    private Scene tableViewScene, sftpViewScene, configUtilViewScene, stressTestViewScene, currentScene;

    private Stage configUtilViewStage, stressTestViewStage, sftpViewStage, tableViewStage, currentStage;

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
        macAddrPwdRadio.setToggleGroup(passwordRadioGroup);
        readerNamePwdRadio.setToggleGroup(passwordRadioGroup);
        neitherPwdRadio.setToggleGroup(passwordRadioGroup);
        getInfoBox.setSelected(true);

        try {
            tableViewRoot = new FXMLLoader(getClass().getResource("/tableViewWindow.fxml")).load();
            tableViewScene = new Scene(tableViewRoot, 930, 400);
            tableViewController = Context.getInstance().getTableViewController();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sftpViewRoot = new FXMLLoader(getClass().getResource("/sftpViewWindow.fxml")).load();
            sftpViewScene = new Scene(sftpViewRoot, 600,400);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            configUtilViewRoot = new FXMLLoader(getClass().getResource("/configUtilViewWindow.fxml")).load();
            configUtilViewScene = new Scene(configUtilViewRoot, 800,975);
            configUtilController = Context.getInstance().getConfigController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stressTestViewRoot = new FXMLLoader(getClass().getResource("/stressTestViewWindow.fxml")).load();
            stressTestViewScene = new Scene(stressTestViewRoot, 800,950);
            stressTestController = Context.getInstance().getStressTestController();
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
        subnetChoiceBox.getItems().add("192.168.4.");
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
        timeClocks = Context.getInstance().getClockInfoClocks();

        timeClocks.clear();

        if (!(ipTextArea.getText().split(",").length > 0))
        {

            String fullIP = sshHandler.checkHost(ipPrefix + ipTextArea.getText());
            if(fullIP != null)
            {
                tempClock = new TimeClock();
                tempClock.setIpAddress(ipPrefix + ipTextArea.getText());
                logger.info("Added " + tempClock.getIpAddress() + " to ip list");
                tempClock.setPassword(getPassword(ipPrefix + ipTextArea.getText()));
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

                        tempClock.setPassword(getPassword(ipPrefix+ip));

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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit((Callable<Void>) () -> {
                sshHandler.checkAllHosts((String) subnetChoiceBox.getValue());
                return null;
            });

    }

    @FXML
    private void openTableView(ActionEvent event) {

        EventHandler<WindowEvent> eventHandle = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                tableViewController.setStage(tableViewStage);
                tableViewController.setCallingController(MainController.this);
                tableViewController.swapCellFactories();
                tableViewStage.setTitle("Clock Info Table");
                tableViewStage.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
            }
        };

        tableViewStage.addEventHandler(WindowEvent.WINDOW_SHOWING, eventHandle);


        if(timeClocks!=null) {
            tableViewController.refresh(timeClocks);
        }
        Context.getInstance().setTableViewStage(tableViewStage);

        tableViewRoot.setStyle(currentScene.getRoot().getStyle());
        tableViewStage.show();

    }

    @FXML
    private void openSftpView(ActionEvent event)
    {
        Node node = (Node) event.getSource();

        sftpViewStage = new Stage();

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
    private void openConfigUtilView(ActionEvent event)
    {
        configUtilViewRoot.setStyle(currentScene.getRoot().getStyle());
        EventHandler<WindowEvent> eventHandle = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                configUtilController.setStage(stressTestViewStage);
                configUtilViewStage.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
            }
        };
        configUtilViewStage.addEventHandler(WindowEvent.WINDOW_SHOWING, eventHandle);
        configUtilViewStage.show();
        letterbox(configUtilViewScene, (GridPane) configUtilViewScene.getRoot());

    }

    @FXML
    private void openStressTestView(ActionEvent event)
    {
        stressTestViewRoot.setStyle(currentScene.getRoot().getStyle());
        EventHandler<WindowEvent> eventHandle = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stressTestController.setStage(stressTestViewStage);
                stressTestViewStage.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
            }
        };
        stressTestViewStage.addEventHandler(WindowEvent.WINDOW_SHOWING, eventHandle);
        stressTestViewStage.show();

        letterbox(stressTestViewScene, (GridPane)stressTestViewScene.getRoot());

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
    @Override
    public void setMsgLabelText(String msg)
    {
        msgLabel.setText(msg);
    }
    @Override
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

    @Override
    public void setProgress(double progress)
    {
        System.out.println("Progress bar " + progress);
    }

    public void appendErrorTextArea(String msg)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String date = formatter.format(new Date(System.currentTimeMillis()));
        errorTextArea.appendText("["+ date +"] -- " + msg + "\n");
    }
    @Override
    public void setSelectedIps(ObservableList<TimeClock> selectedItems)
    {
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
    public String getPassword(String... ip){
        String pwdStr = this.passwordField.getText();
        String password = "synergy";
        if(passwordRadioGroup.getSelectedToggle() == macAddrPwdRadio)
        {
            String mac = "";
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            int day = Integer.valueOf(dayFormat.format(new Date()));
            int month = Integer.valueOf(monthFormat.format(new Date()));
            try {
                mac = sshHandler.getLastFourMAC(ip[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!mac.contains("80:E2:66:60")) {

                password="$ynEL"+(day*month)+mac;
            } else{
                password="$ynEL"+(day*month);
            }

        } else if (passwordRadioGroup.getSelectedToggle() == readerNamePwdRadio) {
            if (pwdStr.length() > 0 && pwdStr.length() <5) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                int day = Integer.valueOf(dayFormat.format(new Date()));
                int month = Integer.valueOf(monthFormat.format(new Date()));
                password="$ynEL"+(day*month)+pwdStr;
            } else if (pwdStr.length() > 4){
                password=pwdStr;
            }

        } else if (passwordRadioGroup.getSelectedToggle() == neitherPwdRadio)
        {
            if (!pwdStr.equals("synergy"))
            {
                password=pwdStr;
            }
        }

        return password;
    }

    public class SceneSizeChangeListener implements ChangeListener<Number>
    {
        private final Scene scene;
        private final double ratio;
        private final double initHeight;
        private final double initWidth;
        private final GridPane contentPane;

        public SceneSizeChangeListener(Scene scene, double ratio, double initHeight, double initWidth, GridPane contentPane) {
            this.scene = scene;
            this.ratio = ratio;
            this.initHeight = initHeight;
            this.initWidth = initWidth;
            this.contentPane = contentPane;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            final double newWidth = scene.getWidth();
            final double newHeight = scene.getHeight();

            double scaleFactor =
                    newWidth / newHeight > ratio
                            ? newHeight / initHeight
                            : newWidth / initWidth;

            if (scaleFactor >= 1) {
                Scale scale = new Scale(scaleFactor, scaleFactor);
                scale.setPivotX(0);
                scale.setPivotY(0);
                contentPane.getTransforms().setAll(scale);
                contentPane.setPrefWidth(newWidth / scaleFactor);
                contentPane.setPrefHeight(newHeight / scaleFactor);
            } else {
                contentPane.setPrefWidth(Math.max(initWidth, newWidth));
                contentPane.setPrefHeight(Math.max(initHeight, newHeight));
            }
        }
    }

    private void letterbox(final Scene scene, final GridPane contentPane) {
        final double initWidth  = scene.getWidth();
        final double initHeight = scene.getHeight();
        final double ratio      = initWidth / initHeight;

        SceneSizeChangeListener sizeListener = new SceneSizeChangeListener(scene, ratio, initHeight, initWidth, contentPane);
        scene.widthProperty().addListener(sizeListener);
        scene.heightProperty().addListener(sizeListener);
    }

    public void setCurrentStage(Stage stage)
    {
        this.currentStage = stage;
        this.currentScene = stage.getScene();
    }

    public void setStages()
    {
        setUpTableViewStage();
        setUpStressTestViewStage();
        setUpConfigUtilViewStage();
    }





    private void setUpTableViewStage()
    {
        tableViewStage = new Stage();
        tableViewStage.setTitle("Clock Info Table");
        tableViewStage.setScene(tableViewScene);
        tableViewStage.initModality(Modality.WINDOW_MODAL);

        Window primaryWindow = currentScene.getWindow();

        tableViewStage.initOwner(primaryWindow);

        tableViewStage.setX(primaryWindow.getX() + 200);
        tableViewStage.setY(primaryWindow.getY() + 100);



        tableViewStage.setResizable(false);
        Context.getInstance().setTableViewStage(tableViewStage);
    }

    private void setUpStressTestViewStage() {



        stressTestViewStage = new Stage();

        stressTestViewRoot.setStyle(currentScene.getRoot().getStyle());
        stressTestViewStage.setTitle("Stress Test");
        stressTestViewStage.setScene(stressTestViewScene);

//        stressTestViewStage.initModality(Modality.WINDOW_MODAL);

        Window primaryWindow = currentScene.getWindow();

        stressTestViewStage.initOwner(primaryWindow);

        stressTestViewStage.setX(primaryWindow.getX() + 200);
        stressTestViewStage.setY(primaryWindow.getY());

//        stressTestViewStage.setResizable(false);

        stressTestViewStage.setMinHeight(750);
        stressTestViewStage.setMinWidth(500);
        stressTestViewStage.setMaxHeight(1080);
        stressTestViewStage.setMaxWidth(1200);
        Context.getInstance().setStressTestStage(stressTestViewStage);
//        ((GridPane)stressTestViewScene.getRoot()).getChildren().forEach(child->{
//           child.setScaleX(1.25);
//           child.setScaleY(1.25);
//        });
//        ((GridPane)stressTestViewScene.getRoot()).setScaleY(1.25);


        Context.getInstance().setStressTestStage(stressTestViewStage);
    }

    private void setUpConfigUtilViewStage() {


        configUtilViewStage = new Stage();

        configUtilViewRoot.setStyle(currentScene.getRoot().getStyle());
        configUtilViewStage.setTitle("Configuration Utility");
        configUtilViewStage.setScene(configUtilViewScene);
//        configUtilViewStage.initModality(Modality.WINDOW_MODAL);

        Window primaryWindow = currentScene.getWindow();

        configUtilViewStage.initOwner(primaryWindow);

        configUtilViewStage.setX(primaryWindow.getX() + 200);
        configUtilViewStage.setY(primaryWindow.getY());

//        configUtilViewStage.setResizable(false);
        configUtilViewStage.setMinHeight(750);
        configUtilViewStage.setMinWidth(650);
        configUtilViewStage.setMaxHeight(1080);
        configUtilViewStage.setMaxWidth(840);
        Context.getInstance().setConfigUtilStage(configUtilViewStage);
    }


}
