<?php
date_default_timezone_set('Asia/Manila');

if (!empty($_POST['jeepney_id']) && !empty($_POST['driver_name']) && !empty($_POST['plate_number']) && !empty($_POST['capacity'])) {
    $jeepney_id = $_POST['jeepney_id'];
    $driver_name = $_POST['driver_name'];
    $plate_number = $_POST['plate_number'];
    $capacity = $_POST['capacity'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = "UPDATE jeepney SET driver_name='$driver_name', plate_number='$plate_number', capacity='$capacity' WHERE jeepney_id='$jeepney_id'";
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