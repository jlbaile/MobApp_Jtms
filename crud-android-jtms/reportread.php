<?php
error_reporting(0);
ini_set('display_errors', 0);

date_default_timezone_set('Asia/Manila');
header('Content-Type: application/json');

$host = "localhost";
$user = "root";
$pass = "";
$db = "jtms_db";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Connection failed: " . $conn->connect_error]);
    exit();
}

// ── Input ─────────────────────────────────────────────────────────────────────
$start_date = isset($_POST['start_date']) ? $_POST['start_date'] : date('Y-m-01');
$end_date = isset($_POST['end_date']) ? $_POST['end_date'] : date('Y-m-d');
$jeepney_ids = isset($_POST['jeepney_ids']) ? $_POST['jeepney_ids'] : [];

$start_date = $conn->real_escape_string($start_date);
$end_date = $conn->real_escape_string($end_date);

// ── Jeepney filter ────────────────────────────────────────────────────────────
$jeepney_filter = "";
$jeepney_param = "";
if (!empty($jeepney_ids)) {
    $safe_ids = array_map(fn($id) => (int) $id, $jeepney_ids);
    $id_list = implode(",", $safe_ids);
    $jeepney_filter = "AND t.jeepney_id IN ($id_list)";
    $jeepney_param = "AND j.jeepney_id IN ($id_list)";
}

// ── Fare price ────────────────────────────────────────────────────────────────
$fare_result = $conn->query("SELECT fare_price FROM fare_settings ORDER BY id DESC LIMIT 1");
$fare_price = 0;
if ($fare_result && $fare_result->num_rows > 0) {
    $fare_price = (float) $fare_result->fetch_assoc()['fare_price'];
}

// ── Summary totals ────────────────────────────────────────────────────────────
$summary_sql = "
    SELECT
        COUNT(t.trip_id)                              AS total_trips,
        SUM(j.capacity)                               AS total_passengers,
        COUNT(t.trip_id) * AVG(j.capacity) * ?        AS total_revenue
    FROM jeepney_trips t
    JOIN jeepney j ON t.jeepney_id = j.jeepney_id
    WHERE t.trip_date BETWEEN ? AND ?
      AND t.return_time IS NOT NULL
      $jeepney_filter
";

$stmt = $conn->prepare($summary_sql);
$stmt->bind_param("dss", $fare_price, $start_date, $end_date);
$stmt->execute();
$summary_row = $stmt->get_result()->fetch_assoc();
$stmt->close();

$total_trips = (int) ($summary_row['total_trips'] ?? 0);
$total_passengers = (int) ($summary_row['total_passengers'] ?? 0);
$total_revenue = round((float) ($summary_row['total_revenue'] ?? 0), 2);

// ── Per-jeepney breakdown ─────────────────────────────────────────────────────
$breakdown_sql = "
    SELECT
        j.jeepney_id,
        j.plate_number,
        j.driver_name,
        j.capacity,
        COUNT(t.trip_id)                   AS trip_count,
        COUNT(t.trip_id) * j.capacity      AS est_passengers,
        COUNT(t.trip_id) * j.capacity * ?  AS revenue
    FROM jeepney j
    LEFT JOIN jeepney_trips t
        ON j.jeepney_id = t.jeepney_id
        AND t.trip_date BETWEEN ? AND ?
        AND t.return_time IS NOT NULL
    WHERE 1=1 $jeepney_param
    GROUP BY j.jeepney_id, j.plate_number, j.driver_name, j.capacity
    ORDER BY trip_count DESC
";

$stmt2 = $conn->prepare($breakdown_sql);
$stmt2->bind_param("dss", $fare_price, $start_date, $end_date);
$stmt2->execute();
$result2 = $stmt2->get_result();

$breakdown = [];
while ($row = $result2->fetch_assoc()) {
    $breakdown[] = [
        "jeepney_id" => $row['jeepney_id'],
        "plate_number" => $row['plate_number'],
        "driver_name" => $row['driver_name'],
        "capacity" => (int) $row['capacity'],
        "trip_count" => (int) $row['trip_count'],
        "est_passengers" => (int) $row['est_passengers'],
        "revenue" => round((float) $row['revenue'], 2)
    ];
}
$stmt2->close();
$conn->close();

echo json_encode([
    "success" => true,
    "start_date" => $start_date,
    "end_date" => $end_date,
    "fare_price" => $fare_price,
    "total_trips" => $total_trips,
    "total_passengers" => $total_passengers,
    "total_revenue" => $total_revenue,
    "breakdown" => $breakdown
]);
?>