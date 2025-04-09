package com.example.quanlynhahang.nhap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VNPayUtils {

    public static String generateSecureHash(Map<String, String> params, String hashSecret) {
        try {
            // Step 1: Sort parameters alphabetically by key
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            // Step 2: Create hash data string
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                if (params.get(fieldName) != null && !params.get(fieldName).isEmpty()) {
                    hashData.append(fieldName).append("=").append(params.get(fieldName)).append("&");
                }
            }

            // Remove last '&'
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }

            // Step 3: Create SHA512 hash
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512_HMAC.init(secretKey);

            byte[] hashBytes = sha512_HMAC.doFinal(hashData.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Helper method to get VNPay error message
    public static String getVNPayMessage(String responseCode) {
        Map<String, String> vnpayResponseCodes = new HashMap<>();
        vnpayResponseCodes.put("00", "Giao dịch thành công");
        vnpayResponseCodes.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).");
        vnpayResponseCodes.put("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.");
        vnpayResponseCodes.put("10", "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        vnpayResponseCodes.put("11", "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.");
        vnpayResponseCodes.put("12", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.");
        vnpayResponseCodes.put("13", "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.");
        vnpayResponseCodes.put("24", "Giao dịch không thành công do: Khách hàng hủy giao dịch");
        vnpayResponseCodes.put("51", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.");
        vnpayResponseCodes.put("65", "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.");
        vnpayResponseCodes.put("75", "Ngân hàng thanh toán đang bảo trì.");
        vnpayResponseCodes.put("79", "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch");
        vnpayResponseCodes.put("99", "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)");

        return vnpayResponseCodes.getOrDefault(responseCode, "Lỗi không xác định: " + responseCode);
    }
}