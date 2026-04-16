package com.example.jtms30032026;

public class StaffModel {
    private String staff_id;
    private String staff_fname;
    private String staff_lname;
    private String staff_username;
    private String staff_password;

    public StaffModel(String staff_id, String staff_fname, String staff_lname,
                      String staff_username, String staff_password) {
        this.staff_id = staff_id;
        this.staff_fname = staff_fname;
        this.staff_lname = staff_lname;
        this.staff_username = staff_username;
        this.staff_password = staff_password;
    }

    public String getStaff_id() { return staff_id; }
    public String getStaff_fname() { return staff_fname; }
    public String getStaff_lname() { return staff_lname; }
    public String getStaff_username() { return staff_username; }
    public String getStaff_password() { return staff_password; }
}