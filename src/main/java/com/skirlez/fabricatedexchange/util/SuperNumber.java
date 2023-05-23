package com.skirlez.fabricatedexchange.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
/*
    SuperNumber:
-has two bigintegers: numerator and denominator
-can represent any number if you have enough memory
-used for emc values
-really cool name
-"super" for "super slow"

*/
import java.text.DecimalFormat;

import com.skirlez.fabricatedexchange.FabricatedExchange;


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
    public SuperNumber(String divisionString) {
        String[] parts = divisionString.split("/");
        
        this.numerator = new BigInteger(parts[0]);
        this.denominator = (parts.length > 1) ? new BigInteger(parts[1]) : BigInteger.ONE;
    }
    public SuperNumber(SuperNumber other) {
        this.numerator = other.numerator;
        this.denominator = other.denominator;
        // don't need to simplify since we know for sure other was simplified
    }


    public int toInt() {
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

    public void floor() {
        denominator = BigInteger.ONE;
    }


    public void add(BigInteger other) {
        numerator = numerator.add(other.multiply(denominator));
        simplify();
    }

    public void add(SuperNumber other) {
        numerator = numerator.multiply(other.denominator);
        numerator = numerator.add(other.numerator.multiply(denominator));
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    public void subtract(SuperNumber other) {
        numerator = numerator.multiply(other.denominator);
        numerator = numerator.subtract(other.numerator.multiply(denominator));
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    public void multiply(SuperNumber other) {
        numerator = numerator.multiply(other.numerator);
        denominator = denominator.multiply(other.denominator);
        simplify();
    }

    public void multiply(int other) {
        numerator = numerator.multiply(BigInteger.valueOf(other));
        simplify();
    }

    public void divide(SuperNumber other) {
        numerator = numerator.multiply(other.denominator);
        denominator = denominator.multiply(other.numerator);
        simplify();
    }

    public void divide(int other) {
        denominator = denominator.multiply(BigInteger.valueOf(other));
        simplify();
    }


    public int compareTo(SuperNumber other) {
        if (denominator.equals(BigInteger.ONE) && other.denominator.equals(BigInteger.ONE))
            return numerator.compareTo(other.numerator);

        SuperNumber thisCopy = new SuperNumber(this);
        SuperNumber otherCopy = new SuperNumber(other);
        thisCopy.numerator = thisCopy.numerator.multiply(other.denominator);
        otherCopy.numerator = otherCopy.numerator.multiply(denominator);
        return thisCopy.numerator.compareTo(otherCopy.numerator);
    }

    public static SuperNumber min(SuperNumber a, SuperNumber b) {
        return (a.compareTo(b) == -1) ? a : b;
    }


    public String toString() {
        BigDecimal a = new BigDecimal(numerator);
        a = a.divide(new BigDecimal(denominator), 10, RoundingMode.HALF_EVEN);
        return a.toString();
    }

    public String toString(int digitsAfterPoint) {
        BigDecimal a = new BigDecimal(numerator);
        a = a.divide(new BigDecimal(denominator), digitsAfterPoint, RoundingMode.HALF_EVEN);
        return a.toString();
    }

    public String scientificNotation() {
        BigDecimal a = new BigDecimal(numerator);
        DecimalFormat format = new DecimalFormat("0.####E0");
        return format.format(a);
    }

    public String scientificNotation(int digits) {
        BigDecimal a = new BigDecimal(numerator);
        String pattern = "";
        for (int i = 0; i < digits; i++) {
            pattern += "#";
        }
        DecimalFormat format = new DecimalFormat("0." + pattern + "E0");
        return format.format(a);
    }
    public String divisonString() {
        return numerator.toString() + "/" + denominator.toString();
    }


    // divides the fraction to the most simplified form
    private void simplify() {
        if (denominator.equals(BigInteger.ONE))
            return;
        BigInteger gcd = gcd(numerator, denominator);
        numerator = numerator.divide(gcd);
        denominator = denominator.divide(gcd);
    }


    private BigInteger gcd(BigInteger a, BigInteger b) {
        while (!b.equals(BigInteger.ZERO)) {
            BigInteger temp = b;
            b = a.mod(b);
            a = temp;
        }
        return a;
    }
}