package com.skirlez.fabricatedexchange.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
/*
    SuperNumber: TODO I want cool ASCII art here of the name
-has two bigintegers: numerator and denominator
-can represent any real number if you have enough memory 
- (well, except for irrational numbers, but technically if you had infinite memory...)
-used for emc values
-really cool name
-"super" for "super slow"

*/
import java.text.DecimalFormat;

public class SuperNumber {
    private BigInteger numerator;
    private BigInteger denominator;

    public static final SuperNumber ZERO = new SuperNumber(BigInteger.ZERO);
    public static final SuperNumber ONE = new SuperNumber(BigInteger.ONE);
    public static final SuperNumber INTEGER_LIMIT = new SuperNumber(Integer.MAX_VALUE);

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

    /** Takes in a String formatted like "%numerator%/%denominator%". If there is no slash, it will set the denominator to 1.
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

    /** Rounds the SuperNumber to the nearest whole number smaller than itself */
    public void floor() {
        BigInteger mod = numerator.mod(denominator);
        numerator.subtract(mod);
        simplify();
    }

    /** Rounds the SuperNumber to the nearest whole number greater than itself */
    public void ceil() {
        BigInteger mod = numerator.mod(denominator);
        numerator.add(denominator.subtract(mod));
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
            numerator = numerator.multiply(other.denominator);
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

    /** Swaps the numerator and denominator. This instance will become the inverse of the original number. */
    public void inversify() {
        BigInteger temp = numerator;
        numerator = denominator;
        denominator = temp;
    }

    /** A comparison between this and another SuperNumber. 
     * Returns -1 for if this is smaller than other, 
     * 0 for if this equals to other, 
     * and 1 for if this is bigger than other. */
    public int compareTo(SuperNumber other) {
        if (denominator.equals(BigInteger.ONE) && other.denominator.equals(BigInteger.ONE))
            return numerator.compareTo(other.numerator);

        SuperNumber thisCopy = new SuperNumber(this);
        SuperNumber otherCopy = new SuperNumber(other);
        thisCopy.numerator = thisCopy.numerator.multiply(other.denominator);
        otherCopy.numerator = otherCopy.numerator.multiply(denominator);
        return thisCopy.numerator.compareTo(otherCopy.numerator);
    }

    /** Returns the smaller of the two SuperNumbers. */
    public static SuperNumber min(SuperNumber a, SuperNumber b) {
        return (a.compareTo(b) == -1) ? a : b;
    }


    // TODO i don't like the next 3 functions improve them
    /** Returns a representation of the number as a String, formatted with commas,
    * and will also return scientific notation if the value is greater than 1,000,000,000. */
    public String toString() {
        if (this.equalsZero())
            return "0";
        BigDecimal a = new BigDecimal(numerator);
        a = a.divide(new BigDecimal(denominator), 10, RoundingMode.HALF_EVEN);
        if (a.compareTo(new BigDecimal("1000000000")) == 1)
            return scientificNotation(a);
        return normalNotation(a);
    }
    private String scientificNotation(BigDecimal a) {
        DecimalFormat format = new DecimalFormat("0.#####E0");
        return format.format(a);
    }
    private String normalNotation(BigDecimal a) {
        DecimalFormat formatter = new DecimalFormat("#,###.000");
        String s = formatter.format(a);
        while (s.endsWith("0")) 
            s = s.substring(0, s.length() - 1);
        if (s.endsWith("."))
            s = s.substring(0, s.length() - 1);
        if (s.startsWith("."))
            s = "0" + s;
        return s;
    }

    /** Returns the SuperNumber as a string, formatted as %numerator%/%denominator%. If the denominator is 1, it will simply return the numerator as a String. Useful for serialization.
     * @see SuperNumber#SuperNumber(String)
     */
    public String divisionString() {
        return numerator.toString() + ((denominator.toString().equals("1")) ? "" : "/" + denominator.toString());
    }

    /** divides the fraction it's most simplified form. (3/6) -> (1/2) */
    private void simplify() {
        if (denominator.equals(BigInteger.ONE) || numerator.equals(BigInteger.ONE))
            return;
        BigInteger gcd = numerator.gcd(denominator);
        if (!gcd.equals(BigInteger.ONE)) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
    }
}