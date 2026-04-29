<?php
if (!empty($_POST['staff_fname']) && !empty($_POST['staff_lname']) && !empty($_POST['staff_username']) && !empty($_POST['staff_password'])) {
    $staff_fname = $_POST['staff_fname'];
    $staff_lname = $_POST['staff_lname'];
    $staff_username = $_POST['staff_username'];
    $staff_password = $_POST['staff_password'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = "INSERT INTO jeepney_staff (staff_fname, staff_lname, staff_username, staff_password) VALUES ('$staff_fname', '$staff_lname', '$staff_username', '$staff_password')";
        if (mysqli_query($con, $sql)) {
            echo "success";
        } else {
            echo "failed";
        }
    } else {
        echo "failed to connect to database";
    }
}
?>