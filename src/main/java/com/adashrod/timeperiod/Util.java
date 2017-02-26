package com.adashrod.timeperiod;

public class Util {
    /**
     * Returns the number as a string, with a minimum length of desiredLength. If the number as a string is greater than
     * or equal to desiredLength, it is returned without any padding or truncation.
     * E.g.
     *      padWithZeroes(5, 3) == "005"
     *      padWithZeroes(123, 2) == "123"
     * @param number the number to convert to a string
     * @param desiredLength the minimum string length of the result
     * @return a string representation of number, at least desiredLength chars long
     */
    public static String padWithZeroes(final long number, final int desiredLength) {
        final String numString = Long.toString(number);
        if (numString.length() >= desiredLength) {
            return numString;
        } else {
            final int numZeroes = desiredLength - numString.length();
            final StringBuilder leadingZeroes = new StringBuilder();
            for (int i = 0; i < numZeroes; i++) {
                leadingZeroes.append("0");
            }
            return leadingZeroes.append(numString).toString();
        }
    }
}
