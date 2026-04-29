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

$staff_id = $_POST['staff_id'] ?? '';

if (empty($staff_id)) {
    echo "error: missing staff_id";
    exit();
}

$stmt = $conn->prepare("DELETE FROM jeepney_staff WHERE staff_id = ?");
$stmt->bind_param("s", $staff_id);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>