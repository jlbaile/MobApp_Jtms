<?php
$con = mysqli_connect('localhost', 'root', '', 'jtms_db');
if ($con) {
    $result = mysqli_query($con, "SELECT fare_price FROM fare_settings LIMIT 1");
    $row = mysqli_fetch_assoc($result);
    echo $row['fare_price'];
} else {
    echo "failed to connect";
}
?>