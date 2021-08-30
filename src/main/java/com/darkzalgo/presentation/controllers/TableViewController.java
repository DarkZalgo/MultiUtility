package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.customcomponent.EditingCell;
import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TableViewController implements Initializable
{
    @FXML TableColumn<TimeClock, String> modelColumn, imageColumn, ipColumn, macColumn, kernelVersionColumn, uptimeColumn, versionColumn, rebootColumn, reasonColumn;

    @FXML TableView<TimeClock> clockInfoTable;

    @FXML ChoiceBox<String> selectIpByImageBox;

    @FXML Button refreshBtn, selectIpByImageBtn, selectIPsBtn;

    private SSHHandler sshHandler = new SSHHandler();

    private  Set<String> imageSet;

    MainController mainController = Context.getInstance().getMainController();

    Stage currentStage;

    Scene currentScene;

    AbstractController callingController;

    private static final Logger logger = LoggerFactory.getLogger(TableViewController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        Context.getInstance().setTableViewController(this);


        ObservableList<TimeClock> clocks = Context.getInstance().getClockInfoClocks();

        clockInfoTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        clockInfoTable.setEditable(true);

        imageSet = new HashSet<String>();



        modelColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("model"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("image"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("ipAddress"));
        macColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("macAddress"));
        kernelVersionColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("kernelVersion"));
        uptimeColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("uptime"));
        rebootColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("rebootCount"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("version"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("reason"));

        Callback<TableColumn<TimeClock, String>,
                TableCell<TimeClock, String>> cellFactory
                = (TableColumn<TimeClock, String> p) -> new EditingCell();
        macColumn.setCellFactory(cellFactory);

        macColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TimeClock, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<TimeClock, String> cell) {
                TimeClock currentCellProperties = cell.getTableView().getItems().get(cell.getTablePosition().getRow());

                if (!cell.getNewValue().trim().equals(cell.getOldValue().trim()))
                {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("MAC Change Confirmation");
                        alert.setHeaderText("Are you sure you want to change MAC Address?");
                        alert.setContentText("From\n" + cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

                        Optional<ButtonType> res = alert.showAndWait();
                        if (res.get() == ButtonType.OK) {
                            TimeClock clock = new TimeClock();
                            clock.setIpAddress(currentCellProperties.getIpAddress());

                            logger.info("Changing MAC to " + cell.getNewValue());
                            sshHandler.sendCmd("echo " + cell.getNewValue() + " > /etc/mac.txt ; reboot", clock);
                            currentCellProperties.setMacAddress(cell.getNewValue());
                        } else {
                            cell.getTableView().getColumns().get(cell.getTablePosition().getColumn()).setVisible(false);
                            cell.getTableView().getColumns().get(cell.getTablePosition().getColumn()).setVisible(true);
                        }
                    } catch (JSchException | IOException | InterruptedException e) {
                        logger.info(e.getLocalizedMessage());
                    }
                }


            }

        });


        ipColumn.setComparator((o, t1) -> Integer.parseInt(o.toString().split("\\.")[3]) -  Integer.parseInt(t1.toString().split("\\.")[3]));
        kernelVersionColumn.setComparator((Comparator<String>) (o, t1) -> {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
            Date firstDate = null;
            Date secondDate = null;
            try {
                firstDate = format.parse(o.toString());
                secondDate = format.parse(t1.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert firstDate != null;
            assert secondDate != null;
            return firstDate.compareTo(secondDate);
        });

        clockInfoTable.setItems(clocks);
        this.refresh(clocks);

    }

    public void swapCellFactories()
    {
        System.out.println(callingController.getClass().getName());
        if (callingController.getClass().getName().contains("StressTestController")) {
            refreshBtn.setVisible(false);
            selectIpByImageBox.setVisible(false);
            selectIpByImageBtn.setVisible(false);
            selectIPsBtn.setVisible(false);
            clockInfoTable.getColumns().forEach(timeClockTableColumn -> {
                String id = timeClockTableColumn.getId();
                if (!id.contains("mac") && !id.contains("ip")) {
                    timeClockTableColumn.setVisible(false);
                } else {
                    timeClockTableColumn.setPrefWidth(150);
                    timeClockTableColumn.setVisible(true);
                }
                if (id.contains("reason"))
                {
                    timeClockTableColumn.setVisible(true);
                }

            });

        } else if (callingController.getClass().getName().contains("MainController"))
        {
            refreshBtn.setVisible(true);
            selectIpByImageBox.setVisible(true);
            selectIpByImageBtn.setVisible(true);
            selectIPsBtn.setVisible(true);
            clockInfoTable.getColumns().forEach(timeClockTableColumn -> {
                String id = timeClockTableColumn.getId();
                if (id.contains("reason")) {
                    timeClockTableColumn.setVisible(false);
                } else {
                    timeClockTableColumn.setVisible(true);
                }
                if (id.contains("mac"))
                {
                    timeClockTableColumn.setPrefWidth(136);
                } else if (id.contains("ip"))
                {
                    timeClockTableColumn.setPrefWidth(109);
                }

            });
        }
    }

    @FXML
    private void selectIPsByImage(ActionEvent event)
    {
        if(selectIpByImageBox.getValue()!= null)
        {
            mainController.ipTextArea.clear();
            String image = selectIpByImageBox.getValue();
            ObservableList<TimeClock> itemsByImage = clockInfoTable.getItems();
            itemsByImage.forEach((clock -> {
                if (clock.getImage().equals(image))
                mainController.setSelectedIps(clock.getIpAddress().split("\\.")[3]);
            }));
        }
    }

    @FXML
    private void selectIPs(ActionEvent event)
    {
        mainController.ipTextArea.clear();
        ObservableList<TimeClock> selectedItems = clockInfoTable.getSelectionModel().getSelectedItems();
        selectedItems.forEach((n->{
            mainController.setSelectedIps(n.getIpAddress().split("\\.")[3]);
        }));
    }

    public void refresh( ObservableList<TimeClock> clocks )
    {



        clockInfoTable.setItems(clocks);
        if(clocks!=null)
        {
        clocks.forEach(clock->{
            if (clock.getImage()!=null && !clock.getImage().equals(""))
            imageSet.add(clock.getImage());
        });
        }
        imageSet.forEach((image ->{
            selectIpByImageBox.getItems().add(image);
        }));
        clockInfoTable.setVisible(false);
        clockInfoTable.setVisible(true);
        clockInfoTable.getColumns().forEach(n->{
            n.setVisible(false);
            n.setVisible(true);
        });
    }

    @FXML private void buttonRefresh(ActionEvent event)
    {
        System.out.println(clockInfoTable.getScene().getUserData());
        selectIpByImageBox.getItems().clear();
        Context.getInstance().getClockInfoClocks().forEach(clock->{
            if (clock.getImage()!=null && !clock.getImage().equals(""))
                imageSet.add(clock.getImage());
        });
        imageSet.forEach((image ->{
            selectIpByImageBox.getItems().add(image);
        }));
        clockInfoTable.setVisible(false);
        clockInfoTable.setVisible(true);
        clockInfoTable.getColumns().forEach(n->{
            n.setVisible(false);
            n.setVisible(true);
        });
    }

    public void addToImageSet(String image)
    {
        int size = imageSet.size();
        imageSet.add(image);
        if (imageSet.size() > size)
        {
            selectIpByImageBox.getItems().clear();
            imageSet.forEach(n->{
                selectIpByImageBox.getItems().add(n);
            });
        }
    }

    public void setCallingController(AbstractController callingController) {
        this.callingController = callingController;
    }

    public void setStage(Stage stage)
    {
        this.currentStage = stage;
        this.currentScene = stage.getScene();

    }

    public void setClocks()
    {
        if (callingController.getClass().getName().contains("StressTestController")) {
            clockInfoTable.setItems(Context.getInstance().getFailedClocks());
        } else if (callingController.getClass().getName().contains("MainController")) {
        clockInfoTable.setItems(Context.getInstance().getClockInfoClocks());
    }
    }
}
