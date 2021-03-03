package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.customcomponent.EditingCell;
import com.darkzalgo.model.TimeClock;
import com.darkzalgo.model.TimeClockStringProperties;
import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.SSHHandler;
import com.jcraft.jsch.JSchException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
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
    @FXML TableColumn modelColumn, imageColumn, ipColumn, macColumn, kernelVersionColumn, uptimeColumn, dateColumn, rebootColumn;

    @FXML TableView clockInfoTable;

    @FXML ChoiceBox selectIpByImageBox;

    private SSHHandler sshHandler = new SSHHandler();

    MainController mainController = Context.getInstance().getMainController();

    private static final Logger logger = LoggerFactory.getLogger(TableViewController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        ArrayList<TimeClock> clocks = Context.getInstance().currentClocks();
        ObservableList<TimeClockStringProperties> timeClockStrings = FXCollections.observableArrayList();

        clockInfoTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        clockInfoTable.setEditable(true);

        Set<String> imageSet = new HashSet<String>();

        modelColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("model"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("image"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("ipAddress"));
        macColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("macAddress"));
        kernelVersionColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("kernelVersion"));
        uptimeColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("uptime"));
        rebootColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("rebootCount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<TimeClock, String>("date"));

        Callback<TableColumn<TimeClockStringProperties, String>,
                TableCell<TimeClockStringProperties, String>> cellFactory
                = (TableColumn<TimeClockStringProperties, String> p) -> new EditingCell();
        macColumn.setCellFactory(cellFactory);

        macColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<TimeClockStringProperties, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<TimeClockStringProperties, String> cell) {
                TimeClockStringProperties currentCellProperties = cell.getTableView().getItems().get(cell.getTablePosition().getRow());
                TimeClock clock = new TimeClock();
                clock.setHost(currentCellProperties.getIpAddress());
                try
                {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("MAC Change Confirmation");
                    alert.setHeaderText("Are you sure you want to change MAC Address?");
                    alert.setContentText("From\n" +cell.getOldValue() + "To\n" + cell.getNewValue() + "\nOn IP " + currentCellProperties.getIpAddress());
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);


                    Optional<ButtonType> res = alert.showAndWait();
                    if (res.get() == ButtonType.OK)
                    {

                        logger.info("Changing MAC to "+ cell.getNewValue());
                        sshHandler.sendCmd("echo " + cell.getNewValue() + " > /etc/mac.txt ; reboot", clock);
                        currentCellProperties.setMacAddress(cell.getNewValue());
                    }
                    else
                        {
                            currentCellProperties.setMacAddress(cell.getOldValue());
                        }
                } catch (JSchException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        });


        ipColumn.setComparator(new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
               return Integer.parseInt(o.toString().split("\\.")[3]) -  Integer.parseInt(t1.toString().split("\\.")[3]);

            }
        });
        kernelVersionColumn.setComparator(new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
                Date firstDate = null;
                Date secondDate = null;
                try {
                    firstDate = format.parse(o.toString());
                    secondDate = format.parse(t1.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return firstDate.compareTo(secondDate);
            }
        });



        clocks.stream().forEach((n -> {
            if(n.canConnect())
            {
                timeClockStrings.add(new TimeClockStringProperties(
                        n.getModel(),
                        n.getImage(),
                        n.getHost(),
                        n.getMac(),
                        n.getKernelVersion(),
                        n.getUptime(),
                        n.getRebootCount(),
                        n.getDate()
                ));
                imageSet.add(n.getImage());
            }
        }));

       
        clockInfoTable.setItems(timeClockStrings);
        imageSet.forEach((image ->{
            selectIpByImageBox.getItems().add(image);
        }));
    }

    @FXML
    private void selectIPsByImage(ActionEvent event)
    {
        if(selectIpByImageBox.getValue()!= null)
        {
            mainController.ipTextArea.clear();
            String image = (String) selectIpByImageBox.getValue();
            ObservableList<TimeClockStringProperties> itemsByImage = clockInfoTable.getItems();
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
        ObservableList<TimeClockStringProperties> selectedItems = clockInfoTable.getSelectionModel().getSelectedItems();
        selectedItems.forEach((n->{
            mainController.setSelectedIps(n.getIpAddress().split("\\.")[3]);
        }));
    }
}
