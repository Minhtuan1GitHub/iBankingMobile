package com.example.ibankingapp.Constant;

public class AppInfo {
    // Thông tin cấu hình VNPay Sandbox (Môi trường Test)
    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_TmnCode = "24JRSKQZ"; // Mã Website (Terminal ID) Test
    public static final String vnp_HashSecret = "NVXWBVUDKGFWTRWJMIGBBAVPSIBJOFFL"; // Secret Key Test
    public static final String vnp_ReturnUrl = "ibanking://result"; // Phải khớp với Scheme trong AndroidManifest
}