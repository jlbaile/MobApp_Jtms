<?php
if (!empty($_POST['staff_username']) && !empty($_POST['staff_password'])) {
    $staff_username = $_POST['staff_username'];
    $staff_password = $_POST['staff_password'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = "SELECT * FROM jeepney_staff WHERE staff_username='$staff_username' AND staff_password='$staff_password'";
        $result = mysqli_query($con, $sql);
        if (mysqli_num_rows($result) > 0) {
            echo "success";
        } else {
            echo "invalid";
        }
    } else {
        echo "failed to connect to database";
    }
}
?>