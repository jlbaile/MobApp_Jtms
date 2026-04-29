<?php
$con = mysqli_connect('localhost', 'root', '', 'jtms_db');
if ($con) {
    $sql = "SELECT * FROM jeepney";
    $result = mysqli_query($con, $sql);
    if (mysqli_num_rows($result) != 0) {
        $jeepneys = array();
        $i = 0;
        while ($row = mysqli_fetch_assoc($result)) {
            $jeepneys[$i] = $row;
            $i++;
        }
        echo json_encode($jeepneys);
    } else {
        echo "no data found";
    }
} else {
    echo "failed to connect to database";
}
?>