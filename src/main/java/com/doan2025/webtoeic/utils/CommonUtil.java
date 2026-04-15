package com.doan2025.webtoeic.utils;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.EGender;
import com.doan2025.webtoeic.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CommonUtil {

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }


    public static String replaceValueResetPassword(User user, Integer otp) {
        return Constants.BODY_REST_PASSWORD
                .replace(Constants.USERNAME, user.getFirstName() + " " + user.getLastName())
                .replace(Constants.OTP_CODE, String.valueOf(otp));
    }

    public static Integer otpGenerator() {
        Random r = new Random();
        return r.nextInt(100_000, 999_999);
    }

    // Hàm parse dob từ String sang Date
    public static Date parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false); // Không chấp nhận định dạng lỏng lẻo
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ, yêu cầu định dạng yyyy-MM-dd: " + date);
        }
    }

    // Hàm chuyển đổi Integer sang EGender
    public static EGender convertIntegerToEGender(Integer num) {
        if (num == null) return null;
        return switch (num) {
            case 1 -> EGender.MALE;
            case 2 -> EGender.FEMALE;
            case 3 -> EGender.OTHER;
            default -> throw new IllegalArgumentException("Giá trị giới tính không hợp lệ: " + num);
        };
    }
}
