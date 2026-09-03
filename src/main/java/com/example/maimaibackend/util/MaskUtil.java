package com.example.maimaibackend.util;

public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskCertificateNo(String certificateNo) {
        if (certificateNo == null || certificateNo.length() <= 8) {
            return certificateNo;
        }
        return certificateNo.substring(0, 4) + "********" + certificateNo.substring(certificateNo.length() - 4);
    }
}
