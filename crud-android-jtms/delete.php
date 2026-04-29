<?php
if (!empty($_POST['staff_id'])) {
    $staff_id = $_POST['staff_id'];
    $con = mysqli_connect('localhost', 'root', '', 'jtms_db');
    if (con) {
        $sql = "delete from crud_admin where staff_id = $staff_id";
        if (mysqli_query($con, $sql)) {
            echo "data deleted successfully";
        } else
            echo "failed to delete data";
    } else
        echo "failed to connect to database";
}
?>