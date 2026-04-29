<?php
$staff_fname = array();
$con = mysqli_connect('localhost', 'root', '', 'jtms_db');
if ($con) {
    $sql = "select * from crud_admin";
    $result = mysqli_query($con, $sql);
    if (mysqli_num_rows($result) != 0) {
        $i = 0;
        while ($row = mysqli_fetch_assoc($result)) {
            $staff_fname[$i] = $row;
            $i++;
        }
        echo json_encode($staff_fname);
    } else {
        echo "no data found";
    }
}
?>