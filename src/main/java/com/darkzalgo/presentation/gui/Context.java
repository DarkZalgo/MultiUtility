package com.darkzalgo.presentation.gui;


import com.darkzalgo.model.TimeClock;
import com.darkzalgo.presentation.controllers.MainController;

import java.util.ArrayList;

public class Context
{

    private final static Context instance = new Context();

    public static Context getInstance()
    {
        return instance;
    }

    private ArrayList<TimeClock> clocks = new ArrayList<>();

    private MainController controller;

    public  ArrayList<TimeClock> currentClocks()
    {
        return clocks;
    }

    public void setClocks(ArrayList<TimeClock> clocks)
    {
        this.clocks = clocks;
    }

    public MainController getMainController() {
        return controller;
    }

    public void setMainController(MainController controller) {
        this.controller = controller;
    }
}
