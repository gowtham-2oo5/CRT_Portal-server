package com.crt.server.util;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;

        int len1 = s1.length();
        int len2 = s2.length();
        int i1 = 0, i2 = 0;

        while (i1 < len1 && i2 < len2) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);

            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int num1 = 0, num2 = 0;

                while (i1 < len1 && Character.isDigit(s1.charAt(i1))) {
                    num1 = num1 * 10 + (s1.charAt(i1) - '0');
                    i1++;
                }

                while (i2 < len2 && Character.isDigit(s2.charAt(i2))) {
                    num2 = num2 * 10 + (s2.charAt(i2) - '0');
                    i2++;
                }

                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                int result = Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                if (result != 0) {
                    return result;
                }
                i1++;
                i2++;
            }
        }

        return Integer.compare(len1, len2);
    }
}
