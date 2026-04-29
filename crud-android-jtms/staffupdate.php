<?php
date_default_timezone_set('Asia/Manila');

if (!empty($_POST['staff_id']) && !empty($_POST['staff_fname']) && !empty($_POST['staff_lname']) && !empty($_POST['staff_username']) && !empty($_POST['staff_password'])) {
    $staff_id = $_POST['staff_id'];
    $staff_fname = $_POST['staff_fname'];
    $staff_lname = $_POST['staff_lname'];
    $staff_username = $_POST['staff_username'];
    $staff_password = $_POST['staff_password'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = "UPDATE jeepney_staff SET 
                    staff_fname='$staff_fname', 
                    staff_lname='$staff_lname', 
                    staff_username='$staff_username', 
                    staff_password='$staff_password' 
                WHERE staff_id='$staff_id'";
        if (mysqli_query($con, $sql)) {
            echo "success";
        } else {
            echo "failed: " . mysqli_error($con);
        }
    } else {
        echo "failed to connect";
    }
} else {
    echo "empty params";
}
?>