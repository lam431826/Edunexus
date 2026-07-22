package com.edunexus.web;

import com.edunexus.domain.Payment;
import com.edunexus.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * VNPay callback endpoints (API-06). /return is the browser redirect - informational only, never
 * trusted to grant access (VNPay itself warns this can be tampered with client-side). /ipn is the
 * real server-to-server confirmation that PaymentService.handleCallback verifies and processes.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/vnpay")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/return")
    public String returnPage(@RequestParam(name = "vnp_TxnRef", required = false) String reference, Model model) {
        if (reference == null) {
            model.addAttribute("status", "UNKNOWN");
            return "student/payment-return";
        }
        try {
            Payment payment = paymentService.getByReference(reference);
            model.addAttribute("status", payment.getStatus().name());
            model.addAttribute("payment", payment);
        } catch (RuntimeException ex) {
            model.addAttribute("status", "UNKNOWN");
        }
        return "student/payment-return";
    }

    @GetMapping("/ipn")
    @ResponseBody
    public Map<String, String> ipn(@RequestParam Map<String, String> allParams) {
        boolean granted = paymentService.handleCallback(allParams);
        Map<String, String> response = new HashMap<>();
        if (granted) {
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature or transaction");
        }
        return response;
    }
}
