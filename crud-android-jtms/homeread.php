<?php
date_default_timezone_set('Asia/Manila');

$host = 'localhost';
$db = 'jtms_db';
$user = 'root';
$pass = '';

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["error" => "failed to connect"]);
    exit();
}

// FIX: The active trip subquery now checks for BOTH:
//   - return_time IS NULL       (clean new records)
//   - return_time = '0000-00-00 00:00:00'  (legacy bad records from the H:i:s bug)
// This ensures jeepneys with old bad records still resolve correctly.
$sql = "
    SELECT
        j.jeepney_id,
        j.driver_name,
        j.plate_number,
        j.capacity,
        s.status,
        s.last_activity,
        s.total_trips,
        s.total_fare,
        (
            SELECT t.trip_id
            FROM jeepney_trips t
            WHERE t.jeepney_id = j.jeepney_id
              AND (t.return_time IS NULL OR t.return_time = '0000-00-00 00:00:00')
            ORDER BY t.trip_id DESC
            LIMIT 1
        ) AS active_trip_id,
        COALESCE((
            SELECT t.departed_by
            FROM jeepney_trips t
            WHERE t.jeepney_id = j.jeepney_id
              AND (t.return_time IS NULL OR t.return_time = '0000-00-00 00:00:00')
            ORDER BY t.trip_id DESC
            LIMIT 1
        ), '') AS departed_by
    FROM jeepney j
    LEFT JOIN jeepney_status s ON j.jeepney_id = s.jeepney_id
    ORDER BY j.jeepney_id ASC
";

$result = $conn->query($sql);
$data = [];

while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
$conn->close();
?>