package com.example.jtms30032026;

public class HomeModel {
    private String jeepney_id;
    private String driver_name;
    private String plate_number;
    private String capacity; // Add this
    private String status;
    private String last_activity;
    private String total_trips;
    private String active_trip_id;
    private String total_fare;

    public HomeModel(String jeepney_id, String driver_name, String plate_number,
                     String capacity, String status, String last_activity,  // Add capacity here
                     String total_trips, String active_trip_id, String total_fare) {
        this.jeepney_id = jeepney_id;
        this.driver_name = driver_name;
        this.plate_number = plate_number;
        this.capacity = capacity; // Add this
        this.status = status;
        this.last_activity = last_activity;
        this.total_trips = total_trips;
        this.active_trip_id = active_trip_id;
        this.total_fare = total_fare;
    }

    public String getJeepney_id() { return jeepney_id; }
    public String getDriver_name() { return driver_name; }
    public String getPlate_number() { return plate_number; }
    public String getCapacity() { return capacity; } // Add this
    public String getStatus() { return status; }
    public String getLast_activity() { return last_activity; }
    public String getTotal_trips() { return total_trips; }
    public String getActive_trip_id() { return active_trip_id; }
    public String getTotal_fare() { return total_fare; }
    public void setStatus(String status) { this.status = status; }
    public void setActive_trip_id(String active_trip_id) { this.active_trip_id = active_trip_id; }
}