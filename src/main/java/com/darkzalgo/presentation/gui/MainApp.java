package com.darkzalgo.presentation.gui;

import com.darkzalgo.presentation.controllers.MainController;
import com.darkzalgo.presentation.controllers.StressTestController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application
{

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("/mainViewWindow.fxml"));
        primaryStage.setTitle("Multi Utility 03.14.2021");
        primaryStage.setScene(new Scene(root, 700, 500));

        MainController mainController = Context.getInstance().getMainController();

        EventHandler<WindowEvent> eventHandle = new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                mainController.setCurrentStage(primaryStage);
                mainController.setStages();

                primaryStage.removeEventHandler(WindowEvent.WINDOW_SHOWING, this);
            }
        };

        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWING, eventHandle);

        Context.getInstance().setMainViewStage(primaryStage);


        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
