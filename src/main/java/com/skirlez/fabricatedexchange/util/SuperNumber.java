package com.skirlez.fabricatedexchange.util;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/** SuperNumber
*<p> TODO I want cool ASCII art here of the name
*<p> SuperNumber has two BigIntegers: a numerator and denominator.
* It can represent any real number if you have enough memory!
* (well, except for irrational numbers, but technically if you had infinite memory...)
*<p> It:
*<p>-is used for emc values
*<p>-has a really cool name
*<p>-is "super" for "super slow"
*<p>-was given to me by God
*@author Jesus H. Christ
*@see BigInteger
**/
public class SuperNumber implements Comparable<SuperNumber> {
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
	public SuperNumber(BigInteger numerator) {
		this.numerator = numerator;
		this.denominator = BigInteger.ONE;
		simplify();
	}
	public SuperNumber(int numerator, int denominator) {
		this.numerator = BigInteger.valueOf(numerator);
		this.denominator = BigInteger.valueOf(denominator);
		simplify();
	}
	public SuperNumber(BigInteger numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = BigInteger.valueOf(denominator);
		simplify();
	}
	public SuperNumber(SuperNumber other) {
		this.numerator = other.numerator;
		this.denominator = other.denominator;
		// don't need to simplify since we know for sure other was simplified
	}
	/** Takes in two byte representations of the numerator and denominator
	 * @see SuperNumber#toByteArrays()
	 */
	public SuperNumber(byte[] numeratorBytes, byte[] denominatorBytes) {
		numerator = new BigInteger(numeratorBytes);
		denominator = new BigInteger(denominatorBytes);
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

	/** Convert the SuperNumber to an integer. If above the integer limit, return the failsafe parameter. */
	public int toInt(int failsafe) {
		BigInteger mod = numerator.remainder(denominator);
		BigInteger flooredValue = numerator.subtract(mod).divide(denominator);
		try {
			return (flooredValue.intValueExact());
		}
		catch (ArithmeticException thanks) {
			return failsafe;
		}
	}


	/** Convert the SuperNumber to an long. If above the long limit, return the failsafe parameter. */
	public long toLong(long failsafe) {
		BigInteger mod = numerator.remainder(denominator);
		BigInteger flooredValue = numerator.subtract(mod).divide(denominator);
		try {
			return (flooredValue.longValueExact());
		}
		catch (ArithmeticException thanks) {
			return failsafe;
		}
	}

	public static SuperNumber NegativeOne() {
		return new SuperNumber(BigInteger.ONE.negate());
	}
	public static SuperNumber Zero() {
		return new SuperNumber(BigInteger.ZERO);
	}
	public static SuperNumber One() {
		return new SuperNumber(BigInteger.ONE);
	}

	public boolean isPositive() {
		return numerator.compareTo(BigInteger.ZERO) > 0;
	}
	public boolean isNegative() {
		return numerator.compareTo(BigInteger.ZERO) < 0;
	}
	public boolean equalsZero() {
		return numerator.equals(BigInteger.ZERO);
	}
	public boolean equalsOne() {
		return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
	}
	public boolean isRound() {
		return denominator.equals(BigInteger.ONE);
	}

	/** Rounds the SuperNumber to the closest whole number.
	 * If it's the same distance from two whole numbers, it'll choose the one greater than itself. */
	public void round() {
		if (numerator.equals(BigInteger.ZERO) || denominator.equals(BigInteger.ONE))
			return;
		if (numerator.multiply(BigInteger.TWO).compareTo(denominator) >= 0)
			ceil();
		else
			round();
	}


	/** Rounds the SuperNumber to the closest whole number smaller than itself */
	public void floor() {
		if (denominator.equals(BigInteger.ONE))
			return;
		BigInteger mod = numerator.remainder(denominator);
		numerator = numerator.subtract(mod);

		numerator = numerator.divide(denominator);
		denominator = BigInteger.ONE;
	}

	/** Rounds the SuperNumber to the closest whole number greater than itself */
	public void ceil() {
		if (denominator.equals(BigInteger.ONE))
			return;
		BigInteger mod = numerator.mod(denominator);
		numerator = numerator.add(denominator.subtract(mod));

		numerator = numerator.divide(denominator);
		denominator = BigInteger.ONE;
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

	/** This method will "steal" an amount from the other SuperNumber and add it's value to this.
	 * If other has less than amount, then this SuperNumber will steal all of other. */
	public void stealFrom(SuperNumber other, SuperNumber amount) {
		if (other.compareTo(amount) >= 0) {
			other.subtract(amount);
			this.add(amount);
			return;
		}
		this.add(other);
		other.copyValueOf(SuperNumber.ZERO);
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
		if (other == 0)
			throw new ArithmeticException("SuperNumber: division by zero");
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

	/** Raises the SuperNumber to the power of 2.*/
	public void square() {
		numerator = numerator.multiply(numerator);
		denominator = denominator.multiply(denominator);
		// There is no need to simplify
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


	/** Negates the SuperNumber
	 * <p> 1 -> -1
	 * <p> 3/4 -> -3/4
	*/
	public void negate() {
		numerator = numerator.negate();
	}

	/** Copies the value of another SuperNumber to this SuperNumber. */
	public void copyValueOf(SuperNumber other) {
		this.denominator = other.denominator;
		this.numerator = other. numerator;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SuperNumber other)
			return numerator.equals(other.numerator) && denominator.equals(other.denominator);
		return false;
	}

	/** A comparison between this and another SuperNumber.
	 * @return -1 for if this is smaller than other
	 * <p> 0 for if this equals to other
	 * <p> 1 for if this is bigger than other */
	@Override
	public int compareTo(@NotNull SuperNumber other) {
		if (denominator.equals(other.denominator))
			return numerator.compareTo(other.numerator);

		SuperNumber thisCopy = new SuperNumber(this);
		SuperNumber otherCopy = new SuperNumber(other);
		thisCopy.numerator = thisCopy.numerator.multiply(other.denominator);
		otherCopy.numerator = otherCopy.numerator.multiply(denominator);
		return thisCopy.numerator.compareTo(otherCopy.numerator);
	}


	private static final BigInteger ONE_THOUSAND = BigInteger.valueOf(1000);
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	/** @return a representation of the number as a String, formatted with commas and a point */
	// TODO: This function is stupidily written and slow
	public String toString() {
		if (equalsZero())
			return "0";
		String whole = numerator.divide(denominator).toString();
		DecimalFormat decimalFormat = ((DecimalFormat) NumberFormat.getInstance());

		// Group the whole part (like 1234 to 1,234)
		if (decimalFormat.isGroupingUsed() && whole.length() > decimalFormat.getGroupingSize()) {
			StringBuilder builder = new StringBuilder(whole);
			int groupSize = decimalFormat.getGroupingSize();

			int end = (numerator.signum() == -1) ? 1 : 0; // End earlier when negative because of the - sign

			for (int i = whole.length() - groupSize; i > end; i -= groupSize) {
				builder.insert(i, symbols.getGroupingSeparator());
			}
			whole = builder.toString();
		}
		if (denominator.equals(BigInteger.ONE))
			return whole;

		BigInteger remainderNumerator = numerator.abs().mod(denominator);
		// Multiply by 1000 to get at least 3 digits
		String fraction = remainderNumerator.multiply(ONE_THOUSAND).divide(denominator).toString();

		// Remove any trailing zeros
		int lastNonZeroIndex = 0;
		for (int i = 0; i < fraction.length(); i++) {
			if (fraction.charAt(i) != '0')
				lastNonZeroIndex = i;
		}
		fraction = fraction.substring(0, lastNonZeroIndex + 1);
		return whole + symbols.getDecimalSeparator() + fraction;
	}


	// TODO: this function isn't general enough to be justified to be here
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

	/** Returns a two-dimensional byte array representing the SuperNumber. The array at index 0 represents the numerator,
	 * and the array at index 1 represents the denominator.
	 * @see SuperNumber#SuperNumber(byte[], byte[])
	 */
	public byte[][] toByteArrays() {
		byte[] numeratorBytes = numerator.toByteArray();
		byte[] denominatorBytes = denominator.toByteArray();

		byte[][] bytes = new byte[2][];
		bytes[0] = numeratorBytes;
		bytes[1] = denominatorBytes;
		return bytes;
	}


	/** Simplifies the fraction to its most simplified form. Example: (3/6) -> (1/2) */
	private void simplify() {
		if (denominator.signum() == -1) {
			denominator = denominator.negate();
			numerator = numerator.negate();
		}
		if (denominator.equals(BigInteger.ONE) || numerator.equals(BigInteger.ONE))
			return;
		if (equalsZero())
			denominator = BigInteger.ONE;
		BigInteger gcd = numerator.gcd(denominator);
		if (!gcd.equals(BigInteger.ONE)) {
			numerator = numerator.divide(gcd);
			denominator = denominator.divide(gcd);
		}
	}

	/** @return the smaller of the two SuperNumbers. */
	public static SuperNumber min(SuperNumber a, SuperNumber b) {
		return (a.compareTo(b) < 0) ? a : b;
	}
	/** @return the bigger of the two SuperNumbers. */
	public static SuperNumber max(SuperNumber a, SuperNumber b) {
		return (a.compareTo(b) > 0) ? a : b;
	}

	/** @return true if the number is valid for use in SuperNumber(String divisionString)
	 * @see SuperNumber#SuperNumber(String divisionString) */
	public static boolean isValidNumberString(String number) {
		int slashPos = -1;
		for (int i = 0; i < number.length(); i++) {
			char c = number.charAt(i);
			if (c == '/') {
				if (slashPos != -1)
					return false;
				slashPos = i;
			}
			else if (!Character.isDigit(c))
				return false;
		}
		return slashPos != 0 && slashPos != number.length() - 1;
	}



}


