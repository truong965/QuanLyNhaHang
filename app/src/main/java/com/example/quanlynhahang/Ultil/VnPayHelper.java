package com.example.quanlynhahang.Ultil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
public class VnPayHelper {
    private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNPAY_TMN_CODE = "EVFBZEQ2";
    private static final String VNPAY_HASH_SECRET = "OTEZU8O5B6ZTC325SRR6ONEM026D189I";
    public static String createVnpayUrl(String orderId, long amount) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = VNPAY_TMN_CODE;
        String vnp_TxnRef = orderId;
        String vnp_OrderInfo = "pay " + orderId;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = "myapp://vnpay_return";
        String vnp_IpAddr = "127.0.0.1";

        long vnp_Amount = amount * 100; // Số tiền nhân 100 theo yêu cầu VNPay

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName).append("=")
                        .append(URLEncoder.encode(fieldValue, "UTF-8"))
                        .append("&");

                query.append(fieldName).append("=")
                        .append(URLEncoder.encode(fieldValue, "UTF-8"))
                        .append("&");
            }
        }
        String queryUrl = query.substring(0, query.length() - 1);
        String secureHash = hmacSHA512(VNPAY_HASH_SECRET, hashData.toString().substring(0, hashData.length() - 1));
        queryUrl += "&vnp_SecureHash=" + secureHash;

        return VNPAY_URL + "?" + queryUrl;
    }
    private static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte aByte : bytes) {
                hash.append(String.format("%02x", aByte));
            }
            return hash.toString();
        } catch (Exception e) {
            return "";
        }
    }
}