<?php
if (!empty($_POST['fare_price'])) {
    $fare_price = $_POST['fare_price'];
    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = "UPDATE fare_settings SET fare_price='$fare_price'";
        if (mysqli_query($con, $sql)) {
            echo "success";
        } else {
            echo "failed";
        }
    } else {
        echo "failed to connect";
    }
}
?>