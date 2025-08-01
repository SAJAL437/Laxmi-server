package com.app.helper;

import java.util.Random;

public class OtpGenerator {
    public static String generateOtp() {
        int otp = new Random().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}