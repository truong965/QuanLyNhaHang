package com.example.quanlynhahang.nhap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlynhahang.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class testVNPay extends AppCompatActivity {
    private WebView webView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_vnpay);

        webView = findViewById(R.id.webView);
        setupWebView();
        showLoading();
        // Load URL thanh toán
        String paymentUrl = buildVNPayUrl();
        webView.loadUrl(paymentUrl);
    }
    private void setupWebView() {
        // Force software rendering in emulator
        if (Build.FINGERPRINT.contains("generic")) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new VNPayWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
    private String generateTransactionRef() {
        // Generate unique transaction reference (VNPay requires unique for each transaction)
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        String dateString = formatter.format(new Date());

        // Add random 4 digits to ensure uniqueness
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000; // 1000-9999

        return "ORDER_" + dateString + "_" + randomNum;
    }
    private String buildVNPayUrl() {
        // Xây dựng URL với các tham số bắt buộc
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "EVFBZEQ2");
        params.put("vnp_Amount", "1000000"); // 10,000 VND
        params.put("vnp_BankCode", ""); // Let user select bank on VNPay page
        params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", getIPAddress()); // Helper method below
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toán đơn hàng");
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", "yourapp://vnpay/result");
        params.put("vnp_TxnRef", generateTransactionRef());
        String secureHash = VNPayUtils.generateSecureHash(params, "OTEZU8O5B6ZTC325SRR6ONEM026D189I"); // Get from VNPay dashboard
        // Build URL
        Uri.Builder builder = Uri.parse("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html").buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        builder.appendQueryParameter("vnp_SecureHashType", "SHA256");
        builder.appendQueryParameter("vnp_SecureHash", secureHash);

        return builder.build().toString();
    }
    private String getIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "192.168.1.1"; // Fallback IP
    }
    private void handlePaymentResult(String redirectUrl) {
        Uri uri = Uri.parse(redirectUrl);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");

        Intent resultIntent = new Intent();
        if ("00".equals(responseCode)) {
            resultIntent.putExtra("result", "SUCCESS");
            resultIntent.putExtra("transaction_ref", uri.getQueryParameter("vnp_TxnRef"));
        } else {
            resultIntent.putExtra("result", "FAILED");
            resultIntent.putExtra("message", VNPayUtils.getVNPayMessage(responseCode));
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kết nối đến VNPay...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    // 3. Class xử lý chuyển hướng
    class VNPayWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUrlLoading(request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrlLoading(url);
        }

        private boolean handleUrlLoading(String url) {
            if (url.startsWith("yourapp://vnpay")) {
                handlePaymentResult(url);
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            // Có thể thêm xử lý khi bắt đầu load trang
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideLoading();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            hideLoading();
            showErrorDialog("Thanh toán thất bại: " + error, false);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (request.isForMainFrame()) {
                hideLoading();
                showErrorDialog("Không thể kết nối đến máy chủ VNPay", true);
            }
        }
        private void showErrorDialog(String errorMessage, boolean allowRetry) {
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(testVNPay.this);

                // Custom dialog layout
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_error, null);
                builder.setView(dialogView);

                TextView tvMessage = dialogView.findViewById(R.id.tv_error_message);
                Button btnRetry = dialogView.findViewById(R.id.btn_retry);
                Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

                tvMessage.setText(errorMessage != null ? errorMessage :
                        "Đã xảy ra lỗi trong quá trình thanh toán. Vui lòng thử lại sau.");

                if (allowRetry) {
                    btnRetry.setOnClickListener(v -> {
                        webView.reload();
                        showLoading();
                    });
                } else {
                    btnRetry.setVisibility(View.GONE);
                }

                btnCancel.setOnClickListener(v -> {
                    setResult(RESULT_CANCELED);
                    finish();
                });

                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                // Custom animation
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
            });
        }
    }
}



//    public static String createVnpayUrl() throws Exception {
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", "2.1.0");
//        vnp_Params.put("vnp_Command", "pay");
//        vnp_Params.put("vnp_TmnCode", "EVFBZEQ2");
//        vnp_Params.put("vnp_Amount", "1000000"); // 10,000 VNĐ (đã nhân 100)
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_TxnRef", String.valueOf(System.currentTimeMillis()));
//        vnp_Params.put("vnp_OrderInfo", "Test VNPAY");
//        vnp_Params.put("vnp_OrderType", "other");
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", "https://example.com/vnpay_return");
//        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
//        vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//        // Thêm tham số ExpireDate (15 phút sau)
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, 15);
//        vnp_Params.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime()));
//
//        // 1. Sắp xếp tham số tăng dần theo key
//        SortedMap<String, String> sortedParams = new TreeMap<>(vnp_Params);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        boolean isFirstParam = true;
//        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            // Xây dựng hashData (không encode)
//            if (!isFirstParam) {
//                hashData.append("&");
//            }
//            hashData.append(key).append("=").append(value);
//
//            // Xây dựng query (có encode)
//            query.append(URLEncoder.encode(key, "UTF-8"))
//                    .append("=")
//                    .append(URLEncoder.encode(value, "UTF-8"))
//                    .append("&");
//
//            isFirstParam = false;
//        }
//
//        // 2. Ký dữ liệu
//        String secretKey = "OTEZU8O5B6ZTC325SRR6ONEM026D189I";
//        String secureHash = hmacSHA256(secretKey, hashData.toString());
//        Log.i("hashData", hashData.toString());
//        Log.i("secureHash: " , secureHash);
//
//        query.append("vnp_SecureHashType=SHA256&vnp_SecureHash=").append(secureHash);
//
//        // 4. Trả về URL hoàn chỉnh
//        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + query.toString();
//    }