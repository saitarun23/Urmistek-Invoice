package backend.com.service;

import java.math.BigDecimal;

/**
 * Converts a numeric amount into words, e.g. 12450.00 -> "Twelve Thousand
 * Four Hundred Fifty Rupees". Matches the "Amount In Words" row on the
 * sample invoice. Supports values up to 99,99,999 (Indian numbering).
 */
public final class AmountToWordsUtil {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private AmountToWordsUtil() {
    }

    public static String convert(BigDecimal amount) {
        long rupees = amount.longValue();
        if (rupees == 0) return "Zero";

        StringBuilder result = new StringBuilder();

        long crore = rupees / 10000000;
        rupees %= 10000000;
        long lakh = rupees / 100000;
        rupees %= 100000;
        long thousand = rupees / 1000;
        rupees %= 1000;
        long hundred = rupees / 100;
        long remainder = rupees % 100;

        if (crore > 0) result.append(twoDigit(crore)).append(" Crore ");
        if (lakh > 0) result.append(twoDigit(lakh)).append(" Lakh ");
        if (thousand > 0) result.append(twoDigit(thousand)).append(" Thousand ");
        if (hundred > 0) result.append(ONES[(int) hundred]).append(" Hundred ");
        if (remainder > 0) result.append(twoDigit(remainder));

        return result.toString().trim();
    }

    private static String twoDigit(long n) {
        if (n < 20) return ONES[(int) n];
        return (TENS[(int) (n / 10)] + " " + ONES[(int) (n % 10)]).trim();
    }
}
