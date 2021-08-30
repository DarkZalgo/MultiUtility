package com.darkzalgo.presentation.gui;


import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.controllers.ConfigUtilController;
import com.darkzalgo.presentation.controllers.MainController;
import com.darkzalgo.presentation.controllers.StressTestController;
import com.darkzalgo.presentation.controllers.TableViewController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context
{
    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private final static Context instance = new Context();

    public static synchronized Context getInstance()
    {
        return instance;
    }

    private boolean hasListener = true;

    private ObservableList<TimeClock> clockInfoClocks = FXCollections.observableArrayList();

    private ObservableList<TimeClock> failedClocks = FXCollections.observableArrayList();

    private MainController controller;

    private TableViewController tableViewController;

    private ConfigUtilController configController;

    private StressTestController stressTestController;

    private Stage mainViewStage, tableViewStage, configUtilStage, stressTestStage;

    public ObservableList<TimeClock> getClockInfoClocks()
    {
        if (!hasListener)
        {
            clockInfoClocks.addListener(new ListChangeListener<TimeClock>() {
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
            });
            hasListener = true;
            logger.info("Added Listener to clocks object");
        }
        return clockInfoClocks;
    }
    public ObservableList<TimeClock> getFailedClocks()
    {
        if (!hasListener)
        {
            failedClocks.addListener(new ListChangeListener<TimeClock>() {
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
            });
            hasListener = true;
            logger.info("Added Listener to clocks object");
        }
        return failedClocks;
    }


    public void setClocks(ObservableList<TimeClock> clocks)
    {
        this.clockInfoClocks = clocks;
    }

    public MainController getMainController() {
        return controller;
    }

    public void setMainController(MainController controller) {
        this.controller = controller;
    }

    public TableViewController getTableViewController() {
        return tableViewController;
    }

    public void setTableViewController(TableViewController tableViewController) {
        this.tableViewController = tableViewController;
    }

    public ConfigUtilController getConfigController() {
        return configController;
    }

    public void setConfigController(ConfigUtilController configController) {
        this.configController = configController;
    }

    public StressTestController getStressTestController() {
        return stressTestController;
    }

    public void setStressTestController(StressTestController stressTestController) {
        this.stressTestController = stressTestController;
    }

    public Stage getMainViewStage() {
        return mainViewStage;
    }

    public void setMainViewStage(Stage mainViewStage) {
        this.mainViewStage = mainViewStage;
    }

    public Stage getTableViewStage() {
        return tableViewStage;
    }

    public void setTableViewStage(Stage tableViewStage) {
        this.tableViewStage = tableViewStage;
    }

    public Stage getConfigUtilStage() {
        return configUtilStage;
    }

    public void setConfigUtilStage(Stage configUtilStage) {
        this.configUtilStage = configUtilStage;
    }

    public Stage getStressTestStage() {
        return stressTestStage;
    }

    public void setStressTestStage(Stage stressTestStage) {
        this.stressTestStage = stressTestStage;
    }
}
