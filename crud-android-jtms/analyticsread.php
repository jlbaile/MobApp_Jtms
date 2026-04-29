<?php
error_reporting(0);
ini_set('display_errors', 0);
date_default_timezone_set('Asia/Manila');
header('Content-Type: application/json');

$conn = new mysqli("localhost", "root", "", "jtms_db");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed"]);
    exit();
}

$today = date('Y-m-d');

// ── Total trips today ─────────────────────────────────────────────────────────
$tripsResult = $conn->query(
    "SELECT COUNT(*) AS total FROM jeepney_trips WHERE trip_date = '$today'"
);
$tripsToday = (int) $tripsResult->fetch_assoc()['total'];

// ── Est. passengers today ─────────────────────────────────────────────────────
$passResult = $conn->query("
    SELECT SUM(j.capacity) AS est_passengers
    FROM jeepney_trips jt
    JOIN jeepney j ON jt.jeepney_id = j.jeepney_id
    WHERE jt.trip_date = '$today'
");
$estPassengers = (int) ($passResult->fetch_assoc()['est_passengers'] ?? 0);

// ── Fare price ────────────────────────────────────────────────────────────────
$fareResult = $conn->query("SELECT fare_price FROM fare_settings LIMIT 1");
$farePrice = (float) ($fareResult->fetch_assoc()['fare_price'] ?? 0);

// ── Projected revenue ─────────────────────────────────────────────────────────
$projectedRevenue = $estPassengers * $farePrice;

// ── Yesterday's trips for % change ───────────────────────────────────────────
$yesterdayResult = $conn->query("
    SELECT COUNT(*) AS total
    FROM jeepney_trips
    WHERE trip_date = DATE_SUB('$today', INTERVAL 1 DAY)
");
$tripsYesterday = (int) $yesterdayResult->fetch_assoc()['total'];
$percentChange = 0;
if ($tripsYesterday > 0) {
    $percentChange = round((($tripsToday - $tripsYesterday) / $tripsYesterday) * 100);
}

// ── Today's completed trips list ──────────────────────────────────────────────
$tripsListResult = $conn->query("
    SELECT
        jt.trip_id,
        jt.depart_time,
        jt.return_time,
        j.plate_number,
        j.driver_name,
        j.capacity,
        (j.capacity * fs.fare_price) AS fare
    FROM jeepney_trips jt
    JOIN jeepney       j  ON jt.jeepney_id = j.jeepney_id
    JOIN fare_settings fs ON fs.id = 1
    WHERE jt.trip_date   = '$today'
      AND jt.return_time IS NOT NULL
    ORDER BY jt.return_time DESC
");

$tripsList = [];
while ($row = $tripsListResult->fetch_assoc()) {

    // Format depart_time: stored as H:i:s → display as h:i A (e.g. 10:06 AM)
    $depart_display = '—';
    if (!empty($row['depart_time']) && $row['depart_time'] !== '00:00:00') {
        $depart_display = date('h:i A', strtotime($row['depart_time']));
    }

    // Format return_time: stored as H:i:s → display as h:i A
    $return_display = '—';
    if (!empty($row['return_time']) && $row['return_time'] !== '00:00:00') {
        $return_display = date('h:i A', strtotime($row['return_time']));
    }

    $tripsList[] = [
        "trip_id" => $row['trip_id'],
        "plate_number" => $row['plate_number'],
        "driver_name" => $row['driver_name'],
        "capacity" => $row['capacity'],
        "depart_time" => $depart_display,
        "return_time" => $return_display,
        "fare" => number_format((float) $row['fare'], 2)
    ];
}

$conn->close();

echo json_encode([
    "success" => true,
    "trips_today" => $tripsToday,
    "percent_change" => $percentChange,
    "est_passengers" => $estPassengers,
    "projected_revenue" => $projectedRevenue,
    "fare_price" => $farePrice,
    "trips_list" => $tripsList
]);
?>