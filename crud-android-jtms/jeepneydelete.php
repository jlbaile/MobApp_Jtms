<?php
date_default_timezone_set('Asia/Manila');
header('Content-Type: application/json');

$host = 'localhost';
$db = 'jtms_db';
$user = 'root';
$pass = '';

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo "error: " . $conn->connect_error;
    exit();
}

$jeepney_id = $_POST['jeepney_id'] ?? '';

if (empty($jeepney_id)) {
    echo "error: missing jeepney_id";
    exit();
}

// Delete related jeepney_status rows first (foreign key safety)
$stmt1 = $conn->prepare("DELETE FROM jeepney_status WHERE jeepney_id = ?");
$stmt1->bind_param("s", $jeepney_id);
$stmt1->execute();
$stmt1->close();

// Delete related jeepney_trips rows
$stmt2 = $conn->prepare("DELETE FROM jeepney_trips WHERE jeepney_id = ?");
$stmt2->bind_param("s", $jeepney_id);
$stmt2->execute();
$stmt2->close();

// Delete the jeepney itself
$stmt3 = $conn->prepare("DELETE FROM jeepney WHERE jeepney_id = ?");
$stmt3->bind_param("s", $jeepney_id);

if ($stmt3->execute()) {
    echo "success";
} else {
    echo "error: " . $stmt3->error;
}

$stmt3->close();
$conn->close();
?>