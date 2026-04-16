package com.example.jtms30032026;

public class TripLogItem {
    public String plateNumber, driverName, departTime, returnTime, fare;
    public int capacity;

    public TripLogItem(String plateNumber, String driverName, String departTime,
                       String returnTime, int capacity, String fare) {
        this.plateNumber = plateNumber;
        this.driverName  = driverName;
        this.departTime  = departTime;
        this.returnTime  = returnTime;
        this.capacity    = capacity;
        this.fare        = fare;
    }
}
