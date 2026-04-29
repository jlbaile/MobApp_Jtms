<?php
if (!empty($_POST['stafflname']) && !empty($_POST['staff_id'])) {
    $staff_id = $_POST['staff_id'];
    $staff_fname = $_POST['stafffname'];
    $staff_lname = $_POST['stafflname'];
    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if (con) {
        $sql = "update crud_admin set staff_fname = '$staff_fname', staff_lname = '$staff_lname' where staff_id = $staff_id";
        if (mysqli_query($con, $sql)) {
            echo "data updated successfully";
        } else
            echo "failed to update data";
    } else
        echo "failed to connect to database";
}
?>