package com.example.ibankingapp.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class StringUtils {

    public static String formatCurrency(double amount) {
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(amount);
        } catch (Exception e) {
            return amount + " VND";
        }
    }
}