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

$jeepney_id = $_POST['jeepney_id'] ?? '';
$departed_by = $_POST['departed_by'] ?? 'unknown';

if (empty($jeepney_id)) {
    echo "failed";
    exit();
}

// ── RACE CONDITION FIX ────────────────────────────────────────────────────────
// Use a transaction + FOR UPDATE lock so that if two devices tap Depart at the
// same time, only the first one wins. The second will see status = 'ON ROAD'
// and be rejected before any INSERT happens.
$conn->begin_transaction();

$lock_stmt = $conn->prepare(
    "SELECT status FROM jeepney_status WHERE jeepney_id = ? FOR UPDATE"
);
$lock_stmt->bind_param("s", $jeepney_id);
$lock_stmt->execute();
$lock_result = $lock_stmt->get_result();
$lock_row = $lock_result->fetch_assoc();
$lock_stmt->close();

if (!$lock_row || $lock_row['status'] !== 'IN TERMINAL') {
    $conn->rollback();
    echo "already_departed";
    exit();
}
// ── END RACE CONDITION FIX ────────────────────────────────────────────────────

$depart_time = date('Y-m-d H:i:s');
$trip_date = date('Y-m-d');

$stmt = $conn->prepare(
    "INSERT INTO jeepney_trips (jeepney_id, depart_time, trip_date, departed_by)
     VALUES (?, ?, ?, ?)"
);
$stmt->bind_param("ssss", $jeepney_id, $depart_time, $trip_date, $departed_by);

if ($stmt->execute()) {
    $trip_id = $conn->insert_id;

    $last_activity = date('h:i A') . ' - Departed';
    $stmt2 = $conn->prepare(
        "UPDATE jeepney_status SET status = 'ON ROAD', last_activity = ?
         WHERE jeepney_id = ?"
    );
    $stmt2->bind_param("ss", $last_activity, $jeepney_id);
    $stmt2->execute();
    $stmt2->close();

    $conn->commit();
    echo $trip_id;
} else {
    $conn->rollback();
    echo "failed";
}

$stmt->close();
$conn->close();
?>