/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.en.us;

import com.sun.speech.freetts.util.Utilities;
import java.util.List;
import java.util.LinkedList;

/**
 * Expands Strings containing digits characters into
 * a list of words representing those digits.
 *
 * It translates the following code from flite:
 * <code>lang/usEnglish/us_expand.c</code>
 */
public class NumberExpander {
    
    private static final String[] digit2num = {
	"zero",
	"one",
	"two",
	"three",
	"four",
	"five",
	"six",
	"seven",
	"eight",
	"nine" };
    
    private static final String[] digit2teen = {
	"ten",  /* shouldn't get called */
	"eleven",
	"twelve",
	"thirteen",
	"fourteen",
	"fifteen",
	"sixteen",
	"seventeen",
	"eighteen",
	"nineteen" };
    
    private static final String[] digit2enty = {
	"zero",  /* shouldn't get called */
	"ten",
	"twenty",
	"thirty",
	"forty",
	"fifty",
	"sixty",
	"seventy",
	"eighty",
	"ninety" };
    
    private static final String[] ord2num = {
	"zeroth",
	"first",
	"second",
	"third",
	"fourth",
	"fifth",
	"sixth",
	"seventh",
	"eighth",
	"ninth" };
    
    private static final String[] ord2teen = {
	"tenth",  /* shouldn't get called */
	"eleventh",
	"twelfth",
	"thirteenth",
	"fourteenth",
	"fifteenth",
	"sixteenth",
	"seventeenth",
	"eighteenth",
	"nineteenth" };
    
    private static final String[] ord2enty = {
	"zeroth",  /* shouldn't get called */
	"tenth",
	"twentieth",
	"thirtieth",
	"fortieth",
	"fiftieth",
	"sixtieth",
	"seventieth",
	"eightieth",
	"ninetieth" };


    /**
     * Unconstructable
     */
    private NumberExpander() {
    }


    /**
     * Expands a digit string into a list of English words of those digits.
     * For example, "1234" expands to "one two three four"
     *
     * @param  numberString  the digit string to expand.
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */ 
    public static List expandNumber(String numberString, List numberList) {
	int numDigits = numberString.length();
	
	if (numDigits == 0) {
	    // numberList = null;
	} else if (numDigits == 1) {
	    expandDigits(numberString, numberList);
	} else if (numDigits == 2) {
	    expand2DigitNumber(numberString, numberList);
	} else if (numDigits == 3) {
	    expand3DigitNumber(numberString, numberList);
	} else if (numDigits < 7) {
	    expandBelow7DigitNumber(numberString, numberList);
	} else if (numDigits < 10) {
	    expandBelow10DigitNumber(numberString, numberList);
	} else if (numDigits < 13) {
	    expandBelow13DigitNumber(numberString, numberList);
	} else {
	    expandDigits(numberString, numberList);
	}
	
	return numberList;
    }


    /**
     * Expands a two-digit string into a list of English words.
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expand2DigitNumber(String numberString,
					   List numberList) {
	if (numberString.charAt(0) == '0') {
	    // numberString is "0X"
	    if (numberString.charAt(1) == '0') {
		// numberString is "00"
		return null;
	    } else {
		// numberString is "01", "02" ...
		String number = digit2num[numberString.charAt(1)-'0'];
		numberList.add(number);
	    }
	} else if (numberString.charAt(1) == '0') {
	    // numberString is "10", "20", ...
	    String number = digit2enty[numberString.charAt(0)-'0'];
	    numberList.add(number);
	} else if (numberString.charAt(0) == '1') {
	    // numberString is "11", "12", ..., "19"
	    String number = digit2teen[numberString.charAt(1)-'0'];
	    numberList.add(number);
	} else {
	    // numberString is "2X", "3X", ...
	    String enty = digit2enty[numberString.charAt(0)-'0'];
	    numberList.add(enty);
	    expandDigits(numberString.substring(1,numberString.length()),
			 numberList);
	}

	return numberList;
    }

    
    /**
     * Expands a three-digit string into a list of English words.
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expand3DigitNumber(String numberString,
					   List numberList) {
	if (numberString.charAt(0) == '0') {
	    expandNumberAt(numberString, 1, numberList);
	} else {
	    String hundredDigit = digit2num[numberString.charAt(0)-'0'];
	    numberList.add(hundredDigit);
	    numberList.add("hundred");
	    expandNumberAt(numberString, 1, numberList);
	}

	return numberList;
    }


    /**
     * Expands a string that is a 4 to 6 digits number into a list
     * of English words. For example, "333000" into "three hundred
     * and thirty-three thousand".
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expandBelow7DigitNumber(String numberString,
						List numberList) {
	return expandLargeNumber(numberString, "thousand", 3, numberList);
    }
    

    /**
     * Expands a string that is a 7 to 9 digits number into a list
     * of English words. For example, "19000000" into nineteen million.
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expandBelow10DigitNumber(String numberString,
						 List numberList) {
	return expandLargeNumber(numberString, "million", 6, numberList);
    }


    /**
     * Expands a string that is a 10 to 12 digits number into a list
     * of English words. For example, "27000000000" into twenty-seven
     * billion.
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expandBelow13DigitNumber(String numberString,
						 List numberList) {
	return expandLargeNumber(numberString, "billion", 9, numberList);
    }


    /**
     * Expands a string that is a number longer than 3 digits into a list
     * of English words. For example, "1000" into one thousand.
     *
     * @param numberString the string which is the number to expand
     * @param order either "thousand", "million", or "billion"
     * @param numberZeroes the number of zeroes, depending on the order, so
     *        its either 3, 6, or 9
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expandLargeNumber(String numberString,
					  String order,
					  int numberZeroes,
					  List numberList) {
	int numberDigits = numberString.length();
		
	// parse out the prefix, e.g., "113" in "113,000"
	int i = numberDigits - numberZeroes;
	String part = numberString.substring(0, i);
		
	// get how many thousands/millions/billions
	int oldLength = numberList.size();
	
	expandNumber(part, numberList);

	if (numberList.size() == oldLength) {
	    expandNumberAt(numberString, i, numberList);
	} else {
	    numberList.add(order);
	    expandNumberAt(numberString, i, numberList);
	}

	return numberList;
    }


    /**
     * Returns the number string list of the given string starting at
     * the given index. E.g., expandNumberAt("1100", 1) gives "one hundred"
     *
     * @param numberString the string which is the number to expand
     * @param startIndex the starting position
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    private static List expandNumberAt(String numberString,
				       int startIndex,
				       List numberList) {
	return expandNumber(numberString.substring(startIndex,
						   numberString.length()),
			    numberList);
    }
    

    /**
     * Expands given token to list of words pronouncing it as digits
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    public static List expandDigits(String numberString, List numberList) {
	int numberDigits = numberString.length();
	for (int i = 0; i < numberDigits; i++) {
	    char digit = numberString.charAt(i);
	    if (isDigit(digit)) {
		numberList.add(digit2num[numberString.charAt(i)-'0']);
	    } else {
		numberList.add("umpty");
	    }
	}
	return numberList;
    }
    

    /**
     * Returns the digit string of an ordinal number.
     *
     * @param rawNumberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    public static List expandOrdinal(String rawNumberString, List numberList) {
	String ordinal = null;
	String lastNumber;
	int i;
			
	// remove all ','s from the raw number string
	String numberString = Utilities.deleteChar(rawNumberString, ',');
	
	expandNumber(numberString, numberList);

	// get the last in the list of number strings
	int listSize = numberList.size();
	if (listSize > 0) {
	    lastNumber = (String) numberList.get(listSize - 1);
	    
	    ordinal = findMatchInArray(lastNumber, digit2num, ord2num);
	    if (ordinal == null) {
		ordinal = findMatchInArray(lastNumber, digit2teen, ord2teen);
	    }
	    if (ordinal == null) {
		ordinal = findMatchInArray(lastNumber, digit2enty, ord2enty);
	    }

	    if (lastNumber.equals("hundred")) {
		ordinal = "hundredth";
	    } else if (lastNumber.equals("thousand")) {
		ordinal = "thousandth";
	    } else if (lastNumber.equals("billion")) {
		ordinal = "billionth";
	    }

	    // if there was an ordinal, set the last element of the list
	    // to that ordinal; otherwise, don't do anything
	    if (ordinal != null) {
		numberList.set(listSize - 1, ordinal);
	    }
	}
	
	return numberList;
    }


    /**
     * Finds a match of the given string in the given array,
     * and returns the element at the same index in the returnInArray
     *
     * @param strToMatch the string to match
     * @param matchInArray the source array
     * @param returnInArray the return array
     *
     * @return an element in returnInArray, or <code>null</code> 
     *   if a match is not found
     */
    private static String findMatchInArray(String strToMatch,
					   String[] matchInArray,
					   String[] returnInArray) {
	for (int i = 0; i < matchInArray.length; i++) {
	    if (strToMatch.equals(matchInArray[i])) {
		if (i < returnInArray.length) {
		    return returnInArray[i];
		} else {
		    return null;
		}
	    }
	}
	return null;
    }
    

    /**
     * Expands the given number string as pairs as in years or IDs
     *
     * @param numberString the string which is the number to expand
     * @param  numberList  words are added to this list
     *
     * @return a list of English words
     */
    public static List expandID(String numberString, List numberList) {
	
	int numberDigits = numberString.length();
	
	if ((numberDigits == 2) && (numberString.charAt(0) == '0')) {
	    numberList.add("oh");
	    expandDigits(numberString.substring(1,2), numberList);
	} else if ((numberDigits == 4 &&
		    numberString.charAt(1) == '0') ||
		   numberDigits < 3) {
	    expandNumber(numberString, numberList);
	} else if (numberDigits % 2 == 1) {
	    String firstDigit = digit2num[numberString.charAt(0)-'0'];
	    numberList.add(firstDigit);
	    expandID(numberString.substring(1,numberDigits), numberList);
	} else {
	    expandNumber(numberString.substring(0,2), numberList);
	    expandID(numberString.substring(2,numberDigits), numberList);
	}
	return numberList;
    }


    /**
     * Expands the given number string as a real number.
     *
     * @param numberString the string which is the real number to expand
     * @param numberList words are added to this list
     *
     * @return a list of English words
     */
    public static List expandReal(String numberString, List numberList) {

	int stringLength = numberString.length();
	int position;

	if (numberString.charAt(0) == '-') {
	    // negative real numbers
	    numberList.add("minus");
	    expandReal(numberString.substring(1, stringLength), numberList);
	} else if (numberString.charAt(0) == '+') {
	    // prefixed with a '+'
	    numberList.add("plus");
	    expandReal(numberString.substring(1, stringLength), numberList);
	} else if ((position = numberString.indexOf('e')) != -1 ||
		   (position = numberString.indexOf('E')) != -1) {
	    // numbers with 'E' or 'e'
	    expandReal(numberString.substring(0, position), numberList);
	    numberList.add("e");
	    expandReal(numberString.substring(position + 1), numberList);
	} else if ((position = numberString.indexOf('.')) != -1) {
	    // numbers with '.'
	    expandReal(numberString.substring(0, position), numberList);
	    numberList.add("point");
	    expandReal(numberString.substring(position + 1), numberList);
	} else {
	    // everything else
	    expandNumber(numberString, numberList);
	}

	return numberList;
    }
    

    /**
     * Returns the given string of letters as a list of single char symbols.
     *
     * @param letters the string of letters to expand
     * @param letterList words are added to this list
     *
     * @return a list of single char symbols
     */
    public static List expandLetters(String letters, List letterList) {
	letters = letters.toLowerCase();
	char c;
			
	for (int i = 0; i < letters.length(); i++) {
	    // if this is a number
	    c = letters.charAt(i);

	    if (isDigit(c)) {
		letterList.add(digit2num[c-'0']);
	    } else if (letters.equals("a")) {
		letterList.add("_a");
	    } else {
		letterList.add(String.valueOf(c));
	    }
	}

	return letterList;
    }
    
    
    /**
     * Returns the integer value of the given string of Roman numerals.
     *
     * @param roman the string of Roman numbers
     *
     * @return the integer value
     */
    public static int expandRoman(String roman) {
	int value = 0;

	for (int p = 0; p < roman.length(); p++) {
	    char c = roman.charAt(p);
	    if (c == 'X') {
		value += 10;
	    } else if (c == 'V') {
		value += 5;
	    } else if (c == 'I') {
		if (p+1 < roman.length()) {
		    char p1 = roman.charAt(p+1);
		    if (p1 == 'V') {
			value += 4;
			p++;
		    } else if (p1 == 'X') {
			value += 9;
			p++;
		    } else {
			value += 1;
		    }
		} else {
		    value += 1;
		}
	    }
	}
	return value;
    }


    /**
     * Returns true if the given character is a digit (0-9 only).
     *
     * @param ch the character to test
     *
     * @return true or false
     */
    public static boolean isDigit(char ch) {
	return ('0' <= ch && ch <= '9');
    }
}
