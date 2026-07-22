package com.edunexus.service.payment;

import com.edunexus.domain.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * VNPay sandbox integration (https://sandbox.vnpayment.vn). Requires a real sandbox merchant
 * account - the operator must supply vnpay.tmn-code/vnpay.hash-secret (see application.yml); with
 * those left blank, checkout still starts but redirect/callback verification will simply fail
 * cleanly rather than crash (GBR-12: an unsigned/unverifiable callback must never grant access).
 */
@Component
public class VnPayPaymentGateway implements PaymentGateway {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.currency}")
    private String currency;

    @Override
    public String createPaymentUrl(Payment payment, HttpServletRequest request) {
        if (tmnCode.isBlank() || hashSecret.isBlank()) {
            throw new IllegalStateException(
                    "Payment is not available yet: VNPay sandbox credentials are not configured.");
        }
        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        // VNPay expects the amount multiplied by 100 (no decimal places).
        params.put("vnp_Amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", currency);
        params.put("vnp_TxnRef", payment.getReference());
        params.put("vnp_OrderInfo", "EduNexus payment " + payment.getReference());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp(request));
        params.put("vnp_CreateDate", LocalDateTime.now().format(DATE_FORMAT));

        String query = buildQueryString(params, true);
        String hashData = buildQueryString(params, false);
        String secureHash = hmacSha512(hashSecret, hashData);
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public GatewayCallbackResult verifyCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || tmnCode.isBlank() || hashSecret.isBlank()) {
            return new GatewayCallbackResult(false, false, params.get("vnp_TxnRef"), null);
        }

        SortedMap<String, String> signable = new TreeMap<>(params);
        signable.remove("vnp_SecureHash");
        signable.remove("vnp_SecureHashType");
        String hashData = buildQueryString(signable, false);
        String computedHash = hmacSha512(hashSecret, hashData);

        boolean signatureValid = computedHash.equalsIgnoreCase(receivedHash);
        boolean success = signatureValid && "00".equals(params.get("vnp_ResponseCode"));
        return new GatewayCallbackResult(signatureValid, success, params.get("vnp_TxnRef"), params.get("vnp_TransactionNo"));
    }

    private String buildQueryString(Map<String, String> params, boolean urlEncodeValue) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=');
            sb.append(urlEncodeValue
                    ? URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII)
                    : entry.getValue());
        }
        return sb.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            hmac512.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to compute VNPay signature", e);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
}
