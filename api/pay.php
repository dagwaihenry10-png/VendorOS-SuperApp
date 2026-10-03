<?php
require_once __DIR__ . '/config.php';

$ref = htmlspecialchars($_GET['ref'] ?? 'VOS-' . strtoupper(substr(md5(time()), 0, 8)));
$amount = intval($_GET['amount'] ?? 2000);
$plan = htmlspecialchars($_GET['plan'] ?? 'pro');
$token = htmlspecialchars($_GET['token'] ?? '');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VendorOS Secure Payment Gateway</title>
    <style>
        * { box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        body { background: #0f172a; color: #f8fafc; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px; }
        .card { background: #1e293b; border-radius: 20px; padding: 28px; width: 100%; max-width: 440px; box-shadow: 0 20px 40px rgba(0,0,0,0.5); border: 1px solid #334155; }
        .badge { background: #059669; color: #fff; padding: 6px 14px; border-radius: 9999px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; display: inline-block; }
        .title { font-size: 24px; font-weight: 800; margin: 16px 0 8px; color: #fff; }
        .subtitle { color: #94a3b8; font-size: 14px; margin-bottom: 24px; }
        .account-box { background: #0f172a; border: 1px solid #10b981; border-radius: 16px; padding: 20px; margin-bottom: 20px; }
        .label { font-size: 12px; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px; }
        .value { font-size: 26px; font-weight: 800; color: #10b981; letter-spacing: 2px; margin-bottom: 12px; }
        .meta-row { display: flex; justify-content: space-between; margin-top: 10px; font-size: 14px; color: #cbd5e1; }
        .btn { display: block; width: 100%; padding: 14px; border-radius: 12px; text-align: center; text-decoration: none; font-weight: 700; font-size: 15px; margin-top: 12px; cursor: pointer; border: none; }
        .btn-copy { background: #2563eb; color: #fff; }
        .btn-whatsapp { background: #25d366; color: #fff; }
        .btn-done { background: #10b981; color: #042f2e; }
        .notice { font-size: 12px; color: #64748b; margin-top: 18px; text-align: center; line-height: 1.5; }
    </style>
</head>
<body>
    <div class="card">
        <span class="badge">Official Gateway</span>
        <div class="title">VendorOS Secure Pay</div>
        <div class="subtitle">Transfer the exact amount below to activate your plan</div>

        <div class="account-box">
            <div class="label">Bank Name</div>
            <div style="font-size: 18px; font-weight: 700; color: #fff; margin-bottom: 12px;"><?php echo REAL_OPAY_BANK; ?></div>

            <div class="label">Account Number</div>
            <div class="value" id="accountNumber"><?php echo REAL_OPAY_ACCOUNT; ?></div>

            <div class="label">Account Name</div>
            <div style="font-size: 16px; font-weight: 600; color: #fff;"><?php echo REAL_OPAY_NAME; ?></div>

            <div class="meta-row">
                <span>Amount:</span>
                <strong style="color: #38bdf8;">₦<?php echo number_format($amount); ?></strong>
            </div>
            <div class="meta-row">
                <span>Narration/Ref:</span>
                <strong style="color: #fbbf24;"><?php echo $ref; ?></strong>
            </div>
        </div>

        <button class="btn btn-copy" onclick="copyAccount()">Copy Account Number</button>

        <a class="btn btn-whatsapp" href="https://wa.me/2347081022844?text=<?php echo urlencode("Hi VendorOS, I just paid N$amount for $plan. Reference: $ref"); ?>" target="_blank">
            Chat On WhatsApp (Proof)
        </a>

        <a class="btn btn-done" href="https://wa.me/2347081022844?text=<?php echo urlencode("I Don Pay! Kindly confirm my subscription. Ref: $ref"); ?>" target="_blank">
            I Don Pay (Instant Activation)
        </a>

        <div class="notice">
            🔒 Bank transfers are verified in real time. Please include <strong><?php echo $ref; ?></strong> as payment description/narration.
        </div>
    </div>

    <script>
        function copyAccount() {
            navigator.clipboard.writeText("<?php echo REAL_OPAY_ACCOUNT; ?>").then(() => {
                alert("Account Number Copied: <?php echo REAL_OPAY_ACCOUNT; ?>");
            });
        }
    </script>
</body>
</html>
