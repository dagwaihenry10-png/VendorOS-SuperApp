<?php
require_once __DIR__ . '/config.php';
header('Content-Type: application/json');

$uid = $_GET['uid'] ?? '';
$mysqli = getDbConnection();
$payments = [];

if ($mysqli && !empty($uid)) {
    $stmt = $mysqli->prepare("SELECT payment_id, uid, plan, amount, reference, status, created_at FROM vendoros_payments WHERE uid = ? ORDER BY created_at DESC LIMIT 50");
    if ($stmt) {
        $stmt->bind_param("s", $uid);
        $stmt->execute();
        $res = $stmt->get_result();
        while ($row = $res->fetch_assoc()) {
            $payments[] = $row;
        }
        $stmt->close();
    }
}

echo json_encode([
    'success' => true,
    'masked_account' => MASKED_OPAY_ACCOUNT,
    'count' => count($payments),
    'payments' => $payments
]);
?>
