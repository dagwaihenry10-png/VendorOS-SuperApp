<?php
require_once __DIR__ . '/config.php';
header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    echo json_encode([
        'success' => false,
        'message' => 'Invalid JSON input',
        'masked_account' => MASKED_OPAY_ACCOUNT
    ]);
    exit();
}

$paymentId = $input['paymentId'] ?? $input['payment_id'] ?? ('PAY-' . uniqid());
$uid = $input['uid'] ?? 'guest';
$email = $input['email'] ?? '';
$plan = $input['plan'] ?? 'pro';
$amount = intval($input['amount'] ?? 2000);
$reference = $input['reference'] ?? '';
$secureLink = $input['secureLink'] ?? $input['secure_link'] ?? '';

$mysqli = getDbConnection();
if ($mysqli) {
    $mysqli->query("CREATE TABLE IF NOT EXISTS vendoros_payments (
        id INT AUTO_INCREMENT PRIMARY KEY,
        payment_id VARCHAR(100) UNIQUE,
        uid VARCHAR(100),
        email VARCHAR(100),
        plan VARCHAR(50),
        amount INT,
        reference VARCHAR(150),
        secure_link TEXT,
        real_account VARCHAR(20) DEFAULT '" . REAL_OPAY_ACCOUNT . "',
        status VARCHAR(20) DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_uid (uid),
        INDEX idx_ref (reference)
    )");

    $stmt = $mysqli->prepare("INSERT INTO vendoros_payments (payment_id, uid, email, plan, amount, reference, secure_link, real_account, status) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending') 
        ON DUPLICATE KEY UPDATE amount = VALUES(amount), plan = VALUES(plan), status = VALUES(status)");
    $realAcc = REAL_OPAY_ACCOUNT;
    if ($stmt) {
        $stmt->bind_param("ssssisss", $paymentId, $uid, $email, $plan, $amount, $reference, $secureLink, $realAcc);
        $stmt->execute();
        $stmt->close();
    }
}

echo json_encode([
    'success' => true,
    'secure' => true,
    'message' => 'Synced securely',
    'payment_id' => $paymentId,
    'reference' => $reference,
    'masked_account' => MASKED_OPAY_ACCOUNT
]);
?>
