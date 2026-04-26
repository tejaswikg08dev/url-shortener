package com.urlshortener.common.util;

public final class Base62Encoder {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int BASE = 62;

    public static final int KEY_LENGTH = 7;

    private Base62Encoder() {}

    public static String encode(long number) {

        if(number < 0) throw new IllegalArgumentException("Number must be non-negative");

        StringBuilder sb = new StringBuilder();

        if(number == 0) {
            sb.append(ALPHABET.charAt(0));
        }

        while(number > 0) {
            sb.append(ALPHABET.charAt((int)(number % BASE)));
            number /= BASE;
        }

        while(sb.length() < KEY_LENGTH) {
            sb.append(ALPHABET.charAt(0));
        }

        return sb.reverse().toString();

    }

    public static long decode(String encoded) {
        long result = 0;
        for(char c : encoded.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if(index < 0) throw new IllegalArgumentException("Invalid base62 character: " + c);
            result = result * BASE + index;
        }
        return result;
    }
}