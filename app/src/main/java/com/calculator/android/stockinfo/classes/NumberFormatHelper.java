package com.calculator.android.stockinfo.classes;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by Braxton on 5/7/2016.
 */
public class NumberFormatHelper {
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(String number) {
        if(number.contains(".")){
            return formatFloat(number);
        }
        return formatInteger(Integer.parseInt(number));
    }

    private static String formatFloat(String number) {
        String result = "";

        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '.') {
                // pull next 2 chars from number
                result += "." + next(++i, number) +
                        next(++i, number);
                break;
            }
            else {
                result += number.charAt(i) + "";
            }
        }
        return result;
    }

    private static String next(int i, String number) {
        if(i >= number.length())
            return "";
        else {
            return number.charAt(i) + "";
        }
    }

    public static String formatInteger(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatInteger(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatInteger(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
