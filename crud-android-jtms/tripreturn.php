<?php
error_reporting(0);
ini_set('display_errors', 0);
date_default_timezone_set('Asia/Manila');

$host = 'localhost';
$db = 'jtms_db';
$user = 'root';
$pass = '';

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo "failed to connect";
    exit();
}

$trip_id = $_POST['trip_id'] ?? '';
$jeepney_id = $_POST['jeepney_id'] ?? '';
$returned_by = $_POST['returned_by'] ?? 'unknown';

if (empty($trip_id) || empty($jeepney_id)) {
    echo "missing params";
    exit();
}

// FIX: Use full datetime format (Y-m-d H:i:s) so MySQL DATETIME column
// stores correctly. Previously only H:i:s was used which caused 0000-00-00 00:00:00.
$return_time = date('Y-m-d H:i:s');

$stmt = $conn->prepare(
    "UPDATE jeepney_trips SET return_time = ?, returned_by = ?
     WHERE trip_id = ?"
);
$stmt->bind_param("ssi", $return_time, $returned_by, $trip_id);

if ($stmt->execute()) {
    // Update status + increment total_trips + total_fare
    $stmt2 = $conn->prepare(
        "UPDATE jeepney_status
         SET status        = 'IN TERMINAL',
             last_activity = ?,
             total_trips   = total_trips + 1,
             total_fare    = total_fare +
                             (SELECT fare_price FROM fare_settings LIMIT 1) *
                             (SELECT capacity FROM jeepney WHERE jeepney_id = ?)
         WHERE jeepney_id  = ?"
    );
    $last_activity = date('h:i A') . ' - Returned';
    $stmt2->bind_param("sss", $last_activity, $jeepney_id, $jeepney_id);
    $stmt2->execute();
    $stmt2->close();

    echo "success";
} else {
    echo "failed";
}

$stmt->close();
$conn->close();
?>