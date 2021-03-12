package com.darkzalgo.presentation.gui;


import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.controllers.MainController;
import com.darkzalgo.presentation.controllers.TableViewController;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;
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

    private ObservableList<TimeClock> clocks = FXCollections.observableArrayList();/*(new Callback<TimeClock, Observable[]>() {
        @Override
        public Observable[] call(TimeClock timeClock) {
            logger.info("\n\n\n\n\nCLOCKS OBSERVABLE CALL\n\n\n\n");
            if (timeClock.rebootCountProperty()==null)
            {
                return new Observable[0];
            }
            logger.info("Adding New Observable for " +timeClock.getIpAddress());
            Observable[] res = new Observable[] {
                    timeClock.modelProperty(),
                    timeClock.imageProperty(),
                    timeClock.ipAddressProperty(),
                    timeClock.macAddressProperty(),
                    timeClock.kernelVersionProperty(),
                    timeClock.uptimeProperty(),
                    timeClock.rebootCountProperty(),
                    timeClock.dateProperty()
            };

            return res;
        }
    });*/


    private MainController controller;

    private TableViewController tableViewController;

    public ObservableList<TimeClock> currentClocks()
    {
        if (!hasListener)
        {
            clocks.addListener(new ListChangeListener<TimeClock>() {
                @Override
                public void onChanged(Change<? extends TimeClock> change)
                {
                    while (change.next())
                    {
                        if (tableViewController!=null)
                        {
                            logger.info("\n\n\nCALLING REFRESH FROM CONTEXT");
                            tableViewController.refresh((ObservableList<TimeClock>) change.getList());
                        }
                    }
//                if (tableViewController!=null)
//                {
//                    tableViewController.refresh(clockList);
//                }
                }
            });
            hasListener = true;
            logger.info("Added Listener to clocks object");
        }
        return clocks;
    }

    public void addObservableListener (ObservableList<TimeClock> clockList)
    {
        clockList.addListener(new ListChangeListener<TimeClock>() {
            @Override
            public void onChanged(Change<? extends TimeClock> change)
            {
                while (change.next())
                {
                    if (tableViewController!=null)
                    {
                        logger.info("\n\n\nCALLING REFRESH FROM CONTEXT");
                        tableViewController.refresh((ObservableList<TimeClock>) change.getList());
                    }
                }
//                if (tableViewController!=null)
//                {
//                    tableViewController.refresh(clockList);
//                }
            }
        });
    }

    public void setClocks(ObservableList<TimeClock> clocks)
    {
        this.clocks = clocks;
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
}
