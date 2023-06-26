package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;
import java.math.RoundingMode;
import com.google.common.math.BigIntegerMath;

/** SuperNumber
*<p> TODO I want cool ASCII art here of the name
*<p> SuperNumber has two BigIntegers: a numerator and denominator.
* It can represent any real number if you have enough memory!
* (well, except for irrational numbers, but technically if you had infinite memory...)
*<p> It is/was:
*<p>-Used for emc values
*<p>-Really cool name
*<p>-"Super" for "super slow"
*<p>-Given to me by God
*@author Jesus H. Christ
*@see BigInteger
**/

public class SuperNumber {
    private BigInteger numerator;
    private BigInteger denominator;

    public static final SuperNumber ZERO = new SuperNumber(BigInteger.ZERO);
    public static final SuperNumber ONE = new SuperNumber(BigInteger.ONE);
    public static final SuperNumber INTEGER_LIMIT = new SuperNumber(Integer.MAX_VALUE);

    private static char[] metricPrefixes = {'k', 'M', 'G', 'T', 'P'}; 

    // constructor heaven
    public SuperNumber(int numerator) {
        this.numerator = BigInteger.valueOf(numerator);
        this.denominator = BigInteger.ONE;
    }   
    public SuperNumber(int numerator, int denominator) {
        this.numerator = BigInteger.valueOf(numerator);
        this.denominator = BigInteger.valueOf(denominator);
        simplify();
    }   
    public SuperNumber(BigInteger numerator) {
        this.numerator = numerator;
        this.denominator = BigInteger.ONE;
        simplify();
    }
    public SuperNumber(BigInteger numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = BigInteger.valueOf(denominator);
        simplify();
    }

    /** Takes in a String formatted like "{numerator}/{denominator}". If there is no slash, it will set the denominator to 1.
     *  @see SuperNumber#divisionString() */
    public SuperNumber(String divisionString) {
        String[] parts = divisionString.split("/");
        
        this.numerator = new BigInteger(parts[0]);
        if (parts.length > 1) {
            this.denominator = new BigInteger(parts[1]);
            simplify();
            return;
        }
        this.denominator = BigInteger.ONE;
    }
    public SuperNumber(SuperNumber other) {
        this.numerator = other.numerator;
        this.denominator = other.denominator;
        // don't need to simplify since we know for sure other was simplified
    }

    public int toInt() { // TODO: this is dumb
        if (this.compareTo(INTEGER_LIMIT) == 1 || numerator.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
            return 0;
        }
        return (numerator.intValue()/denominator.intValue());
    }
    
    public static SuperNumber Zero() {
        return new SuperNumber(BigInteger.ZERO);
    }
    public static SuperNumber One() {
        return new SuperNumber(BigInteger.ZERO);
    }

    public boolean equalsZero() {
        return numerator.equals(BigInteger.ZERO);
    }
    public boolean equalsOne() {
        return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    /** Rounds the SuperNumber to the nearest whole number smaller than itself */
    public void floor() {
        if (numerator.equals(BigInteger.ZERO))
            return;
        BigInteger mod = numerator.mod(denominator);
        numerator = numerator.subtract(mod);
        simplify();
    }

    /** Rounds the SuperNumber to the nearest whole number greater than itself */
    public void ceil() {
        if (numerator.equals(BigInteger.ZERO))
            return;
        BigInteger mod = numerator.mod(denominator);
        numerator = numerator.add(denominator.subtract(mod));
        simplify();
    }
    
    // addition methods
    public void add(BigInteger other) {
        if (denominator.equals(BigInteger.ONE)) {
            numerator = numerator.add(other);
            return;
        }
        numerator = numerator.add(other.multiply(denominator));
        simplify();
    }

    public void add(SuperNumber other) {
        if (denominator.equals(BigInteger.ONE)) {
            if (other.denominator.equals(BigInteger.ONE)) {
                numerator = numerator.add(other.numerator);
                return;
            }
            numerator = numerator.multiply(other.denominator).add(other.numerator);
            denominator = other.denominator;
            simplify();
            return;
        }
        numerator = numerator.multiply(other.denominator);
        numerator = numerator.add(other.numerator.multiply(denominator));
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    // subtraction methods

    public void subtract(BigInteger other) {
        if (denominator.equals(BigInteger.ONE)) {
            numerator = numerator.subtract(other);
            return;
        }
        numerator = numerator.subtract(other.multiply(denominator));
        simplify();
    }

    public void subtract(SuperNumber other) {
        if (denominator.equals(BigInteger.ONE)) {
            if (other.denominator.equals(BigInteger.ONE)) {
                numerator = numerator.subtract(other.numerator);
                return;
            }
            numerator = numerator.multiply(other.denominator).subtract(other.numerator);
            denominator = other.denominator;
            simplify();
            return;
        }
        numerator = numerator.multiply(other.denominator);
        numerator = numerator.subtract(other.numerator.multiply(denominator));
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    // multiplication methods

    public void multiply(int other) {
        numerator = numerator.multiply(BigInteger.valueOf(other));
        simplify();
    }

    public void multiply(SuperNumber other) {
        if (denominator.equals(BigInteger.ONE)) {
            if (other.denominator.equals(BigInteger.ONE)) {
                numerator = numerator.multiply(other.numerator); // no need to simplify here
                return;
            }
            numerator = numerator.multiply(other.numerator);
            denominator = other.denominator;
            simplify();
            return;
        }
        numerator = numerator.multiply(other.numerator);
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    // division methods

    public void divide(int other) {
        if (denominator.equals(BigInteger.ONE)) {
            denominator = BigInteger.valueOf(other);
            simplify();
            return;
        }
        denominator = denominator.multiply(BigInteger.valueOf(other));
        simplify();
    }

    public void divide(SuperNumber other) {
        if (other.equalsZero())
            throw new ArithmeticException("SuperNumber: division by zero");
        
        if (denominator.equals(BigInteger.ONE)) {
            if (other.denominator.equals(BigInteger.ONE)) {
                denominator = other.numerator;
                simplify();
                return;
            }
            numerator = numerator.multiply(other.denominator);
            denominator = other.numerator;
            simplify();
            return;
        }
        numerator = numerator.multiply(other.denominator);
        denominator = denominator.multiply(other.numerator);
        simplify();
    }


    /** Swaps the numerator and denominator. This instance will become the inverse of the original number. 
     * <p> (0.5 = 1/2) -> (2 = 2/1)
     * <p> (0.4 = 2/5) -> (2.5 = 5/2)
    */
    public void inversify() {
        BigInteger temp = numerator;
        numerator = denominator;
        denominator = temp;
    }

    /** @return Whether the value of the SuperNumbers is equal. */
    public boolean equalTo(SuperNumber other) {
        return numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }

    /** A comparison between this and another SuperNumber. 
     * @return -1 for if this is smaller than other
     * <p> 0 for if this equals to other
     * <p> 1 for if this is bigger than other */
    public int compareTo(SuperNumber other) {
        if (denominator.equals(other.denominator))
            return numerator.compareTo(other.numerator);

        SuperNumber thisCopy = new SuperNumber(this);
        SuperNumber otherCopy = new SuperNumber(other);
        thisCopy.numerator = thisCopy.numerator.multiply(other.denominator);
        otherCopy.numerator = otherCopy.numerator.multiply(denominator);
        return thisCopy.numerator.compareTo(otherCopy.numerator);
    }

    /** @return the smaller of the two SuperNumbers. */
    public static SuperNumber min(SuperNumber a, SuperNumber b) {
        return (a.compareTo(b) == -1) ? a : b;
    }

    /** Copies the value of another SuperNumber to this SuperNumber. */
    public void copyValueOf(SuperNumber other) {
        this.denominator = other.denominator;
        this.numerator = other. numerator;
    }

    /** @return a representation of the number as a String, formatted with commas and a point */
    public String toString() {
        if (equalsZero())
            return "0";
        StringBuilder newStr = new StringBuilder();
        
        if (numerator.abs().compareTo(denominator) == -1) { // This will fail for numbers who's decimal representation is longer than the 32 bit integer limit due to zeroLen. Too bad!
            /* Our desired output for numbers smaller than zero is as follows:
                1. if there are any zeros before the first real digit, get ALL of them
                2. afterwards get a maximum of 3 digits
                We can actually get the number of zeros after the point by using Log10! Except it's off by one
                for any number that's a power of 10 multiplied by the numerator. It's easy to see why 
                if you graph it in software like Desmos. So we explicitly check for that case.
                after that, we can get the next 3 digits by multiplying the numerator by 
                10^(number of zeros + 3) and dividing it by the denominator.
            */
            if (numerator.compareTo(BigInteger.ZERO) == -1)
                newStr.append("-");
            newStr.append("0.");
            BigInteger division = denominator.divide(numerator);
            String divisionString = division.toString();
            String numeratorString = numerator.toString();
            int zeroLen;
            boolean isPower;
            if (divisionString.startsWith(numeratorString)) { 
                isPower = true;
                for (int i = numeratorString.length(); i < divisionString.length(); i++) {
                    if (divisionString.charAt(i) != '0') {
                        isPower = false;
                        break;
                    }
                }
            }
            else
                isPower = false;
            if (!isPower)
                zeroLen = BigIntegerMath.log10(division.abs(), RoundingMode.FLOOR);
            else
                zeroLen = divisionString.length() - 2;
            for (int i = 0; i < zeroLen; i++)
                newStr.append("0");
            
            String str = numerator.multiply(BigInteger.TEN.pow(Math.max(zeroLen + 3, 3))).divide(denominator).toString();
            
            newStr.append(str);

            int len = newStr.length();
            while (newStr.charAt(len - 1) == '0') {
                newStr.deleteCharAt(len - 1);
                len--;
            }
            
            return newStr.toString();
        }


        BigInteger temp = numerator.multiply(BigInteger.valueOf(1000)).divide(denominator);
        String str = temp.toString();
        int i;
        
        for (i = 0; i < 3; i++) {
            if (!str.endsWith("0"))
                break;
            str = str.substring(0, str.length() - 1);
        }
        int commaCount = (3 - ((str.length() + i) % 3)) % 3;
        for (int j = 0; j < str.length(); j++) {
            int o = str.length() - (4 - i);
            newStr.append(str.charAt(j));
            if (j == o && o != str.length() - 1)
                newStr.append('.');
            if (j < o) {
                commaCount++;
                if (commaCount >= 3) {
                    commaCount = 0;
                    newStr.append(',');
                }
            }
        }
        return newStr.toString();
    }

    /** @return a representation of the number as a String, guaranteed to be <=16 in length. either formatted with commas and a point,
     * with SI (metric) prefixes, or in scientific notation, depending on the size of the number.
     */
    public String shortString() {
        String str = toString();
        if (str.length() <= 15)
            return str; // string with fraction

        if (str.startsWith("0")) {
            int ind = 2;
            while (ind < str.length()) {
                if (str.charAt(ind) != '0')
                    break;
                ind++; 
            }
            int length = 0;
            for (int i = ind; i < str.length(); i++) {
                if (str.charAt(i) == '0')
                    break;
                length++; 
            }

            int offset = 0;
            if (str.length() - ind > 6)
                offset = str.length() - ind - 6;
            str = str.substring(ind, str.length() - offset);
            
            return str + "e-" + (ind + length - 2); // string with scientific notation (negative exponent)
        }

        int diff = str.length() - 15;
        while (str.contains(".")) {
            str = str.substring(0, str.length() - 1);
            diff--;
        }
        if (str.length() <= 15)
            return str; // string without fraction
        if (diff > 17) { 
            str = str.replaceAll(",", "");
            int offset = str.length() - 7;
            str = str.charAt(0) + "." + str.substring(1, str.length());
            str = str.substring(0, str.length() - offset);
            return str + "e" + (offset + str.length() - 2); // string with scientific notation
        }
        diff /= 3;
        if (diff == 0)
            diff = 1;
        char c = metricPrefixes[diff - 1];
        str = str.substring(0, str.length() - diff * 4) + c;
        return str; // string with metric prefix
    }


    /** @return the SuperNumber as a string, formatted as {numerator}/{denominator}. If the denominator is 1, it will simply return the numerator as a String. Useful for serialization.
     * @see SuperNumber#SuperNumber(String)
     */
    public String divisionString() {
        return numerator.toString() + ((denominator.equals(BigInteger.ONE)) ? "" : "/" + denominator.toString());
    }


    /** @return the SuperNumber as a double. 
     * Will return 0 if the value of the SuperNumber is not representable with a double. */
    public double toDouble() {        
        double result = (numerator.doubleValue() / denominator.doubleValue());
        if (Double.isNaN(result))
            return 0;
        return result;
    }


    /** divides the fraction it's most simplified form. Example: (3/6) -> (1/2) */
    private void simplify() {
        if (denominator.equals(BigInteger.ONE) || numerator.equals(BigInteger.ONE) || equalsZero())
            return;
        BigInteger gcd = numerator.gcd(denominator);
        if (!gcd.equals(BigInteger.ONE)) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
    }
}