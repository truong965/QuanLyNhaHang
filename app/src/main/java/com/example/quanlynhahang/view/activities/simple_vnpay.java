package com.example.quanlynhahang.view.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlynhahang.model.Order;
import com.example.quanlynhahang.data.local.ManagmentCart;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.utils.CustomToast;
import com.example.quanlynhahang.utils.VnPayHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class simple_vnpay extends AppCompatActivity {
//    Ngân hàng	NCB
//Số thẻ	9704198526191432198
//    Tên chủ thẻ	NGUYEN VAN A
//    Ngày phát hành	07/15
//    Mật khẩu OTP	123456
        private WebView webView;
        private CustomToast customToast;
        private Order order;
        private ManagmentCart managmentCart;
        private ProgressBar loading;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_test_vnpay);
            managmentCart = new ManagmentCart(this);
            loading = findViewById(R.id.progressBar3);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) order = (Order) bundle.getParcelable("order");
            Log.i("orderId vnpay: ", order.getOrderId());
            init();
        }
        private void showFailDialog(String message) {
            customToast.showToast(message,false);
        }
        private void init(){
            customToast = new CustomToast(this);
            webView = findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
//            webView.getSettings().setDomStorageEnabled(true);
//            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    loading.setVisibility(View.VISIBLE); // Hiện loading khi bắt đầu load
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    loading.setVisibility(View.GONE); // Ẩn loading khi load xong
                    super.onPageFinished(view, url);
                }
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    loading.setVisibility(View.GONE); // Ẩn loading nếu load bị lỗi
                    super.onReceivedError(view, request, error);
                }
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    Uri uri = request.getUrl();
                    if (uri.toString().startsWith("myapp://vnpay_return")) {
                        // Đây là lúc VNPay trả về kết quả
                        // Bạn có thể lấy các tham số từ URL như:
                        String responseCode = uri.getQueryParameter("vnp_ResponseCode"); // 00 là thành công
                        String transactionRef = uri.getQueryParameter("vnp_TxnRef");

//                        Log.d("VNPAY", "Response Code: " + responseCode);
//                        Log.d("VNPAY", "Order ID: " + transactionRef);

                        if (responseCode != null && transactionRef != null) {
                            if ("00".equals(responseCode)) {
                                if (order.getOrderId() != null) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    if(auth.getCurrentUser().isAnonymous()){
                                        managmentCart.clearCart();
                                        customToast.showToast("✅ Đặt hàng thành công", true);
                                        goToHome();
                                        return true;
                                    }
                                    String userId = auth.getCurrentUser().getUid();
                                    DatabaseReference dbRef = database.getReference("Orders").child(userId);
                                    dbRef.child(order.getOrderId()).setValue(order)
                                            .addOnSuccessListener(aVoid -> {
                                                managmentCart.clearCart();
                                                customToast.showToast("✅ Đặt hàng thành công", true);
                                                goToHome();
                                            })
                                            .addOnFailureListener(e -> customToast.showToast("❌ Đặt hàng thất bại", false));
                                }
                            } else {
                                // Thất bại
                                showFailDialog("Giao dịch "+transactionRef + " không thành công: ");
                                goToHome();
                            }
                        } else {
                            showFailDialog("Không nhận được dữ liệu thanh toán từ VNPay.");
                        }
                        return true; // Ngăn WebView load trang này
                    }

                    return false; // Cho phép WebView load bình thường
                }
            });
            try {
                double totalPrice = order.getFoodList().stream().mapToDouble(food -> food.getPrice() * food.getNumberInCart()).sum();
                // Chuyển đổi double to long
//                Log.i("Truonggggg", "Total Price: " + (long) (totalPrice*10000));
//                Log.i("Truonggggg", "Total 222: " + order.getOrderId());
                String paymentUrl = VnPayHelper.createVnpayUrl(order.getOrderId(),(long)(totalPrice*1000));
//                String paymentUrl = VnPayHelper.createVnpayUrl("lmsci", 10000);
//                Log.i("Truonggggg", "Payment URL: " + paymentUrl);
                webView.loadUrl(paymentUrl);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("VNPAY", "Lỗi khi tạo URL thanh toán: " + e.getMessage());
                goToHome();
            }

        }
    private void goToHome(){
        Intent intent = new Intent(simple_vnpay.this, Home.class);
        intent.putExtra("targetFragment", "OrderFragment");
         startActivity(intent);
        finish();
    }
}