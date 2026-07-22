package com.edunexus.service.payment;

import com.edunexus.domain.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentGateway {

    /** Builds the URL the Student is redirected to in order to pay. */
    String createPaymentUrl(Payment payment, HttpServletRequest request);

    /** Verifies a gateway callback's signature and outcome. Never trust an unverified callback. */
    GatewayCallbackResult verifyCallback(Map<String, String> params);

    record GatewayCallbackResult(boolean signatureValid, boolean paymentSuccessful,
                                  String reference, String gatewayTransactionId) {
    }
}
