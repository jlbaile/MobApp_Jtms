<?php
if (!empty($_POST['staff_fname'])) {
    $staff_fname = $_POST['staff_fname'];

    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if ($con) {
        $sql = " insert into crud_admin (staff_fname) values ('$staff_fname')";
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