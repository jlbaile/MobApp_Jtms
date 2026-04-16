package com.example.jtms30032026;

public class JeepneyModel {
    private String jeepney_id;
    private String driver_name;
    private String plate_number;
    private String capacity;

    public JeepneyModel(String jeepney_id, String driver_name, String plate_number, String capacity) {
        this.jeepney_id = jeepney_id;
        this.driver_name = driver_name;
        this.plate_number = plate_number;
        this.capacity = capacity;
    }

    public String getJeepney_id() { return jeepney_id; }
    public String getDriver_name() { return driver_name; }
    public String getPlate_number() { return plate_number; }
    public String getCapacity() { return capacity; }
}