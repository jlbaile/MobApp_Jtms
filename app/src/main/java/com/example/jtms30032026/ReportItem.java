package com.example.jtms30032026;

public class ReportItem {
    private String jeepneyId;
    private String plateNumber;
    private String driverName;
    private int capacity;
    private int tripCount;
    private int estPassengers;
    private double revenue;

    public ReportItem(String jeepneyId, String plateNumber, String driverName,
                      int capacity, int tripCount, int estPassengers, double revenue) {
        this.jeepneyId    = jeepneyId;
        this.plateNumber  = plateNumber;
        this.driverName   = driverName;
        this.capacity     = capacity;
        this.tripCount    = tripCount;
        this.estPassengers = estPassengers;
        this.revenue      = revenue;
    }

    public String getJeepneyId()    { return jeepneyId; }
    public String getPlateNumber()  { return plateNumber; }
    public String getDriverName()   { return driverName; }
    public int    getCapacity()     { return capacity; }
    public int    getTripCount()    { return tripCount; }
    public int    getEstPassengers(){ return estPassengers; }
    public double getRevenue()      { return revenue; }
}