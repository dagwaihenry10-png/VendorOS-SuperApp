<?php
/**
 * VendorOS Secure Payment Gateway Configuration
 * Server-Side Configuration File
 * NOTE: The real OPay account is kept strictly on this server and NEVER sent to client app displays.
 */
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, X-Secure-Token, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

define('REAL_OPAY_ACCOUNT', '7081022844');
define('REAL_OPAY_BANK', 'OPay');
define('REAL_OPAY_NAME', 'VendorOS');
define('MASKED_OPAY_ACCOUNT', '7081****44');

define('DB_HOST', 'localhost');
define('DB_USER', 'YOUR_CPANEL_USER');
define('DB_PASS', 'YOUR_CPANEL_PASS');
define('DB_NAME', 'YOUR_DB_NAME');
define('SECURE_SALT', 'VendorOS_Secure_2024_Salt_!@#');

function getDbConnection() {
    $mysqli = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    if ($mysqli->connect_error) {
        // Return null for serverless / file-mock fallback if database is not configured
        return null;
    }
    return $mysqli;
}
?>
