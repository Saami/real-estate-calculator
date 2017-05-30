package com.saami.realestate.util;

import java.text.NumberFormat;
import java.util.Locale;

public class RealEstateUtils {

    public static String formatPrice(Double price) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        return n.format(price);
    }

}
