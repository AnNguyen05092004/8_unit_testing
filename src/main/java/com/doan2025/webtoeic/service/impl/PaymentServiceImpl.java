//package com.doan2025.webtoeic.service.impl;
//
//import com.doan2025.webtoeic.constants.Constants;
//import com.doan2025.webtoeic.constants.enums.EStatusOrder;
//import com.doan2025.webtoeic.constants.enums.ResponseCode;
//import com.doan2025.webtoeic.constants.enums.ResponseObject;
//import com.doan2025.webtoeic.domain.Enrollment;
//import com.doan2025.webtoeic.domain.OrderDetail;
//import com.doan2025.webtoeic.domain.Orders;
//import com.doan2025.webtoeic.domain.User;
//import com.doan2025.webtoeic.dto.response.PaymentResponse;
//import com.doan2025.webtoeic.exception.WebToeicException;
//import com.doan2025.webtoeic.repository.EnrollmentRepository;
//import com.doan2025.webtoeic.repository.OrderDetailRepository;
//import com.doan2025.webtoeic.repository.OrderRepository;
//import com.doan2025.webtoeic.repository.UserRepository;
//import com.doan2025.webtoeic.service.PaymentService;
//import com.doan2025.webtoeic.utils.CommonUtil;
//import com.doan2025.webtoeic.utils.JwtUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.servlet.view.RedirectView;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
//public class PaymentServiceImpl implements PaymentService {
//    private final JwtUtil jwtUtil;
//    private final UserRepository userRepository;
//    private final OrderDetailRepository orderDetailRepository;
//    private final OrderRepository orderRepository;
//    private final EnrollmentRepository enrollmentRepository;
//
//
//    @Value("${url.server.be}")
//    private String BE;
//    @Value("${url.serve.fe}")
//    private String FE;
//    @Value("${payment.secretKey}")
//    private String SECRET_KEY;
//    @Value("${payment.orderType}")
//    private String ORDER_TYPE;
//    @Value("${payment.vnp_Command}")
//    private String VPN_COMMAND;
//    @Value("${payment.vnp_Version}")
//    private String VPN_VERSION;
//    @Value("${payment.vnp_PayUrl}")
//    private String VPN_PAY_URL;
//    @Value("${payment.vnp_TmnCode}")
//    private String VPN_TMN_CODE;
//
//    @Override
//    public PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest request) {
//        String email = "";
//        String bearerToken = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            email = jwtUtil.getEmailFromToken(request);
//        }
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
//        Orders order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));
//
//        if (!user.getEmail().equals(order.getUser().getEmail())) {
//            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
//        }
//
//        OrderDetail detail = orderDetailRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));
//
//        if (order.getStatus().equals(EStatusOrder.COMPLETED)) {
//            throw new WebToeicException(ResponseCode.HAS_PAID, ResponseObject.ORDER);
//        }
//
//        long amount = order.getTotalAmount();
//        if (amount < 0) throw new WebToeicException(ResponseCode.INVALID, ResponseObject.AMOUNT);
//
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(Constants.DATE_FORMAT.TIME_ZONE));
//        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT.YYYY_MM_DD_HH_MM_SS);
//        formatter.setTimeZone(TimeZone.getTimeZone(Constants.DATE_FORMAT.TIME_ZONE));
//        String vnp_CreateDate = formatter.format(cld.getTime());
//
//        String vnp_TxnRef = vnp_CreateDate + "_" + order.getId();
//        String vnp_IpAddr = CommonUtil.getIpAddress(request);
//        String vnp_Amount = String.valueOf(amount * 100);
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put(Constants.PAYMENT.VNP_VERSION, VPN_VERSION);
//        vnp_Params.put(Constants.PAYMENT.VNP_COMMAND, VPN_COMMAND);
//        vnp_Params.put(Constants.PAYMENT.VNP_TMN_CODE, VPN_TMN_CODE);
//        vnp_Params.put(Constants.PAYMENT.VNP_AMOUNT, vnp_Amount);
//        vnp_Params.put(Constants.PAYMENT.VNP_CURR_CODE, Constants.PAYMENT.VND);
//        vnp_Params.put(Constants.PAYMENT.VNP_BANK_CODE, Constants.PAYMENT.NCB);
//
//        vnp_Params.put(Constants.PAYMENT.VNP_TXN_REF, vnp_TxnRef);
//        vnp_Params.put(Constants.PAYMENT.VNP_ORDER_INFO, "Thanh toan don hang:" + detail.getCourse().getTitle());
//        vnp_Params.put(Constants.PAYMENT.VNP_ORDER_TYPE, ORDER_TYPE);
//        vnp_Params.put(Constants.PAYMENT.VNP_LOCALE, Constants.PAYMENT.VN);
//
//        vnp_Params.put(Constants.PAYMENT.VNP_RETURN_URL, BE + "payment/return");
//        vnp_Params.put(Constants.PAYMENT.VNP_IP_ADDR, vnp_IpAddr);
//
//        vnp_Params.put(Constants.PAYMENT.VNP_CREATE_DATE, vnp_CreateDate);
//
//        cld.add(Calendar.MINUTE, 15);
//        String vnp_ExpireDate = formatter.format(cld.getTime());
//        vnp_Params.put(Constants.PAYMENT.VNP_EXPIRE_DATE, vnp_ExpireDate);
//
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        Iterator<String> itr = fieldNames.iterator();
//        while (itr.hasNext()) {
//            String fieldName = itr.next();
//            String fieldValue = vnp_Params.get(fieldName);
//            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
//                //Build hash data
//                hashData.append(fieldName);
//                hashData.append('=');
//                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
//                //Build query
//                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
//                query.append('=');
//                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
//                if (itr.hasNext()) {
//                    query.append('&');
//                    hashData.append('&');
//                }
//            }
//        }
//        String queryUrl = query.toString();
//        try {
//            String vnp_SecureHash = CommonUtil.hmacSHA512(SECRET_KEY, hashData.toString());
//            queryUrl += Constants.PAYMENT.VPN_SECURE_HASH + vnp_SecureHash;
//            String paymentUrl = VPN_PAY_URL + "?" + queryUrl;
//
//            return PaymentResponse.builder()
//                    .status(Constants.SUCCESS)
//                    .message("Create " + ResponseCode.SUCCESS.getMessage()
//                            .replace("{entity}", ResponseObject.PAYMENT.toString()))
//                    .URL(paymentUrl)
//                    .build();
//        } catch (Exception e) {
//            throw new WebToeicException(ResponseCode.NOT_SUCCESS, ResponseObject.PAYMENT);
//        }
//    }
//
//    @Override
//    public RedirectView handleVNPayReturn(HttpServletRequest request) {
//        String txnRef = request.getParameter(Constants.PAYMENT.VNP_TXN_REF);
//        String responseCode = request.getParameter(Constants.PAYMENT.VNP_RESPONSE_CODE);
//        RedirectView redirectView = new RedirectView();
//        String status = responseCode.equals("00") ? Constants.SUCCESS : Constants.FAIL;
//        String url = FE + "/order-status"
//                + "?status=" + status;
//        if (responseCode.equals("00")) {
//            url += Constants.PAYMENT.TXN_REF + txnRef
//                    + Constants.PAYMENT.TRANSACTION_NO + request.getParameter(Constants.PAYMENT.VNP_TRANSACTION_NO)
//                    + Constants.PAYMENT.AMOUNT + request.getParameter(Constants.PAYMENT.VNP_AMOUNT)
//                    + Constants.PAYMENT.PAY_DATE + request.getParameter(Constants.PAYMENT.VPN_PAY_DATE);
//
//            String[] part = txnRef.split("_");
//            String orderId = part[part.length - 1];
//            Orders orders = orderRepository.findById(Long.valueOf(orderId))
//                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));
//            orders.setStatus(EStatusOrder.COMPLETED);
//            orderRepository.save(orders);
//
//            OrderDetail detail = orderDetailRepository.findByOrderId(Long.valueOf(orderId))
//                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));
//            Enrollment enrollment = new Enrollment();
//            enrollment.setUser(orders.getUser());
//            enrollment.setCourse(detail.getCourse());
//            enrollmentRepository.save(enrollment);
//        }
//
//        redirectView.setUrl(url);
//        return redirectView;
//    }
//}




package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Enrollment;
import com.doan2025.webtoeic.domain.OrderDetail;
import com.doan2025.webtoeic.domain.Orders;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.response.PaymentResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.OrderDetailRepository;
import com.doan2025.webtoeic.repository.OrderRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.PaymentService;
import com.doan2025.webtoeic.utils.CommonUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class PaymentServiceImpl implements PaymentService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final EnrollmentRepository enrollmentRepository;


    @Value("${url.server.be}")
    private String BE;
    @Value("${url.serve.fe}")
    private String FE;
    @Value("${payment.secretKey}")
    private String SECRET_KEY;
    @Value("${payment.orderType}")
    private String ORDER_TYPE;
    @Value("${payment.vnp_Command}")
    private String VPN_COMMAND;
    @Value("${payment.vnp_Version}")
    private String VPN_VERSION;
    @Value("${payment.vnp_PayUrl}")
    private String VPN_PAY_URL;
    @Value("${payment.vnp_TmnCode}")
    private String VPN_TMN_CODE;
    @Value("${payment.vnp_ReturnUrl:}")
    private String VNP_RETURN_URL;
    @Value("${payment.mock.enabled:false}")
    private boolean MOCK_PAYMENT_ENABLED;

    @Override
    public PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest request) {
        String email = "";
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(request);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));

        if (!user.getEmail().equals(order.getUser().getEmail())) {
            throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
        }

        OrderDetail detail = orderDetailRepository.findByOrderId(orderId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));

        if (order.getStatus().equals(EStatusOrder.COMPLETED)) {
            throw new WebToeicException(ResponseCode.HAS_PAID, ResponseObject.ORDER);
        }

        long amount = order.getTotalAmount();
        if (amount < 0) throw new WebToeicException(ResponseCode.INVALID, ResponseObject.AMOUNT);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(Constants.DATE_FORMAT.TIME_ZONE));
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT.YYYY_MM_DD_HH_MM_SS);
        formatter.setTimeZone(TimeZone.getTimeZone(Constants.DATE_FORMAT.TIME_ZONE));
        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_TxnRef = vnp_CreateDate + "_" + order.getId();
        String vnp_IpAddr = CommonUtil.getIpAddress(request);
        String vnp_Amount = String.valueOf(amount * 100);

        if (MOCK_PAYMENT_ENABLED) {
            String mockUrl = buildCallbackUrl(vnp_TxnRef, "00", vnp_Amount, vnp_CreateDate, String.valueOf(System.currentTimeMillis()));
            return PaymentResponse.builder()
                    .status(Constants.SUCCESS)
                    .message("Create " + ResponseCode.SUCCESS.getMessage()
                            .replace("{entity}", ResponseObject.PAYMENT.toString()) + " (mock)")
                    .URL(mockUrl)
                    .build();
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put(Constants.PAYMENT.VNP_VERSION, VPN_VERSION);
        vnp_Params.put(Constants.PAYMENT.VNP_COMMAND, VPN_COMMAND);
        vnp_Params.put(Constants.PAYMENT.VNP_TMN_CODE, VPN_TMN_CODE);
        vnp_Params.put(Constants.PAYMENT.VNP_AMOUNT, vnp_Amount);
        vnp_Params.put(Constants.PAYMENT.VNP_CURR_CODE, Constants.PAYMENT.VND);
        vnp_Params.put(Constants.PAYMENT.VNP_BANK_CODE, Constants.PAYMENT.NCB);

        vnp_Params.put(Constants.PAYMENT.VNP_TXN_REF, vnp_TxnRef);
        vnp_Params.put(Constants.PAYMENT.VNP_ORDER_INFO, "Thanh toan don hang:" + detail.getCourse().getTitle());
        vnp_Params.put(Constants.PAYMENT.VNP_ORDER_TYPE, ORDER_TYPE);
        vnp_Params.put(Constants.PAYMENT.VNP_LOCALE, Constants.PAYMENT.VN);

        String returnUrl = StringUtils.hasText(VNP_RETURN_URL) ? VNP_RETURN_URL : BE + "payment/return";
        vnp_Params.put(Constants.PAYMENT.VNP_RETURN_URL, returnUrl);
        vnp_Params.put(Constants.PAYMENT.VNP_IP_ADDR, vnp_IpAddr);

        vnp_Params.put(Constants.PAYMENT.VNP_CREATE_DATE, vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put(Constants.PAYMENT.VNP_EXPIRE_DATE, vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        try {
            String vnp_SecureHash = CommonUtil.hmacSHA512(SECRET_KEY, hashData.toString());
            queryUrl += Constants.PAYMENT.VPN_SECURE_HASH + vnp_SecureHash;
            String paymentUrl = VPN_PAY_URL + "?" + queryUrl;

            return PaymentResponse.builder()
                    .status(Constants.SUCCESS)
                    .message("Create " + ResponseCode.SUCCESS.getMessage()
                            .replace("{entity}", ResponseObject.PAYMENT.toString()))
                    .URL(paymentUrl)
                    .build();
        } catch (Exception e) {
            throw new WebToeicException(ResponseCode.NOT_SUCCESS, ResponseObject.PAYMENT);
        }
    }

    @Override
    public RedirectView handleVNPayReturn(HttpServletRequest request) {
        String txnRef = request.getParameter(Constants.PAYMENT.VNP_TXN_REF);
        String responseCode = request.getParameter(Constants.PAYMENT.VNP_RESPONSE_CODE);
        RedirectView redirectView = new RedirectView();
        boolean isSuccess = "00".equals(responseCode);
        String status = isSuccess ? Constants.SUCCESS : Constants.FAIL;
        String url = FE + "/order-status?status=" + status;
        if (isSuccess) {
            try {
                Long orderId = extractOrderId(txnRef);

                url += Constants.PAYMENT.TXN_REF + txnRef
                        + Constants.PAYMENT.TRANSACTION_NO + request.getParameter(Constants.PAYMENT.VNP_TRANSACTION_NO)
                        + Constants.PAYMENT.AMOUNT + request.getParameter(Constants.PAYMENT.VNP_AMOUNT)
                        + Constants.PAYMENT.PAY_DATE + request.getParameter(Constants.PAYMENT.VPN_PAY_DATE);

                Orders orders = orderRepository.findById(orderId)
                        .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));

                if (!EStatusOrder.COMPLETED.equals(orders.getStatus())) {
                    orders.setStatus(EStatusOrder.COMPLETED);
                    orderRepository.save(orders);
                }

                OrderDetail detail = orderDetailRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER));

                if (!enrollmentRepository.existsByUserAndCourse(orders.getUser(), detail.getCourse())) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUser(orders.getUser());
                    enrollment.setCourse(detail.getCourse());
                    enrollmentRepository.save(enrollment);
                }
            } catch (Exception ex) {
                url = FE + "/order-status?status=" + Constants.FAIL;
            }
        }

        redirectView.setUrl(url);
        return redirectView;
    }

    private Long extractOrderId(String txnRef) {
        if (!StringUtils.hasText(txnRef) || !txnRef.contains("_")) {
            throw new IllegalArgumentException("Invalid txnRef");
        }
        String[] part = txnRef.split("_");
        return Long.valueOf(part[part.length - 1]);
    }

    private String buildCallbackUrl(String txnRef, String responseCode, String amount, String payDate, String transactionNo) {
        return (StringUtils.hasText(VNP_RETURN_URL) ? VNP_RETURN_URL : BE + "payment/return")
                + "?" + Constants.PAYMENT.VNP_RESPONSE_CODE + "=" + responseCode
                + "&" + Constants.PAYMENT.VNP_TXN_REF + "=" + txnRef
                + "&" + Constants.PAYMENT.VNP_TRANSACTION_NO + "=" + transactionNo
                + "&" + Constants.PAYMENT.VNP_AMOUNT + "=" + amount
                + "&" + Constants.PAYMENT.VPN_PAY_DATE + "=" + payDate;
    }
}


