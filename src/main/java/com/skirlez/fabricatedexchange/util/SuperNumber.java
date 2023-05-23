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