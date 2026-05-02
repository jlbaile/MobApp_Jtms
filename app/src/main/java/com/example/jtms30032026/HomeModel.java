package com.example.jtms30032026;

public class HomeModel {

    private String jeepney_id;
    private String driver_name;
    private String plate_number;
    private String capacity;
    private String status;
    private String last_activity;
    private String total_trips;
    private String total_fare;
    private String active_trip_id;
    private String departed_by;     // username of staff who issued depart

    public HomeModel(String jeepney_id, String driver_name, String plate_number,
                     String capacity, String status, String last_activity,
                     String total_trips, String total_fare,
                     String active_trip_id, String departed_by) {
        this.jeepney_id     = jeepney_id;
        this.driver_name    = driver_name;
        this.plate_number   = plate_number;
        this.capacity       = capacity;
        this.status         = status;
        this.last_activity  = last_activity;
        this.total_trips    = total_trips;
        this.total_fare     = total_fare;
        this.active_trip_id = active_trip_id;
        this.departed_by    = departed_by;
    }

    public String getJeepney_id()     { return jeepney_id; }
    public String getDriver_name()    { return driver_name; }
    public String getPlate_number()   { return plate_number; }
    public String getCapacity()       { return capacity; }
    public String getStatus()         { return status; }
    public String getLast_activity()  { return last_activity; }
    public String getTotal_trips()    { return total_trips; }
    public String getTotal_fare()     { return total_fare; }
    public String getActive_trip_id() { return active_trip_id; }
    public String getDeparted_by()    { return departed_by; }
}