<?php
if (!empty($_POST['driver_name']) && !empty($_POST['plate_number']) && !empty($_POST['capacity'])) {
    $driver_name = $_POST['driver_name'];
    $plate_number = $_POST['plate_number'];
    $capacity = $_POST['capacity'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        // Insert jeepney
        $sql = "INSERT INTO jeepney (driver_name, plate_number, capacity) VALUES ('$driver_name', '$plate_number', '$capacity')";
        if (mysqli_query($con, $sql)) {
            $new_jeepney_id = mysqli_insert_id($con);

            // Auto insert status row for new jeepney
            $sqlStatus = "INSERT INTO jeepney_status (jeepney_id, status, last_activity, total_trips) VALUES ('$new_jeepney_id', 'IN TERMINAL', 'No activity yet', 0)";
            mysqli_query($con, $sqlStatus);

            echo "success";
        } else {
            echo "failed";
        }
    } else {
        echo "failed to connect to database";
    }
}
?>