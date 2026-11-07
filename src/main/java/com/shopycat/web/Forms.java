package com.shopycat.web;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Turns optional form text into values. Blank means "not given". */
final class Forms {

    private Forms() {
    }

    static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    static String trimOrNull(String s) {
        return blank(s) ? null : s.trim();
    }

    static Double toDouble(String s) {
        return blank(s) ? null : Double.valueOf(s.trim());
    }

    static Integer toInt(String s) {
        return blank(s) ? null : Integer.valueOf(s.trim());
    }

    static BigDecimal toMoney(String s) {
        return blank(s) ? null : new BigDecimal(s.trim());
    }

    static LocalDate toDate(String s) {
        return blank(s) ? null : LocalDate.parse(s.trim());
    }

    static boolean validLatLng(Double lat, Double lng) {
        return lat != null && lng != null && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
