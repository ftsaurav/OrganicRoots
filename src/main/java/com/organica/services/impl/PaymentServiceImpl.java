package com.organica.services.impl;

import com.organica.payload.PaymentDetails;
import com.organica.services.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKey;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    @Override
    public PaymentDetails CreateOrder(Double amount) {

        try {
            // Convert amount to paise: 100 Rs → 10000 paise
            int amountInPaise = (int)(amount * 100);

            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);  // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("payment_capture", 1);

            Order order = client.orders.create(orderRequest);

            PaymentDetails response = new PaymentDetails();
            response.setOrderId(order.get("id"));
            response.setAmount(amount);
            response.setCurrency("INR");
            response.setRazorpayKey(razorpayKey);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }
}
