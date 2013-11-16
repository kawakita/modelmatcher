package com.CornellTech.ModelMatcher;

public class Tray {
    private final String mName;
    private final double mDatetime;
    private final int mNumber;
    public Tray(
            String name,
            double datetime,
            int n) {
        // Set the instance fields from the constructor

        // An identifier for the tray
        this.mName = name;

        this.mDatetime = datetime;
        this.mNumber = n;
    }

    public String getName() {
        return mName;
    }    

    public double getDatetime() {
        return mDatetime;
    }    
    
    public double getN() {
        return mNumber;
    }     
}
