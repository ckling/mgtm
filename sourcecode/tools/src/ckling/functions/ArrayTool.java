package ckling.functions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class ArrayElement implements Comparable<Object> {
	private int index;
	private double value;

	ArrayElement(int index, double value) {
		this.index = index;
		this.value = value;
	}

	public double getValue() {
		return value;
	}
	public int getIndex() {
		return index;
	}

	public int compareTo(Object other) {
		if (this.getValue() == ((ArrayElement) other).getValue()) {
			return 0;
		} else if (this.getValue() > ((ArrayElement) other).getValue()) {
			return 1;
		} else {
			return -1;
		}
	}

}

class ArrayElementInt implements Comparable<Object> {
	private int index;
	private int value;

	ArrayElementInt(int index, int value) {
		this.index = index;
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	public int getIndex() {
		return index;
	}

	public int compareTo(Object other) {
		if (this.getValue() == ((ArrayElementInt) other).getValue()) {
			return 0;
		} else if (this.getValue() > ((ArrayElementInt) other).getValue()) {
			return 1;
		} else {
			return -1;
		}
	}

}

public class ArrayTool  {

	public static String[] getDistinct(String[] input) {

		Set<String> distinct = new HashSet<String>();
		for(String element : input) {
			distinct.add(element);
		}

		return distinct.toArray(new String[0]);
	}
	public static int[] getDistinct(int[] input) {

		Set<Integer> distinct = new HashSet<Integer>();
		for(int element : input) {
			distinct.add(element);
		}

		int[] ret = new int [distinct.size()];

		int i=0;
		Iterator<Integer> iterator = distinct.iterator();
		while (iterator.hasNext()) {
			ret[i++]=iterator.next();
		}

		return ret;
	}

	public static int[] sortArray(double[] array) {


		int[] index = new int[array.length];
		ArrayElement[] arrayElement = new ArrayElement[array.length];
		for (int i = 0; i < array.length;i++) {
			index[i]=i;
			arrayElement[i] = new ArrayElement(i,array[i]);			  
		}
		Arrays.sort(arrayElement);
		for (int i = 0; i < array.length;i++) {
			index[i]=arrayElement[i].getIndex();
			array[i]=arrayElement[i].getValue();
		}

		int[] result = index;

		return(result);

	}

	public static int[] sortArray(int[] array) {


		int[] index = new int[array.length];
		ArrayElementInt[] arrayElementInt = new ArrayElementInt[array.length];
		for (int i = 0; i < array.length;i++) {
			index[i]=i;
			arrayElementInt[i] = new ArrayElementInt(i,array[i]);			  
		}
		Arrays.sort(arrayElementInt);
		for (int i = 0; i < array.length;i++) {
			index[i]=arrayElementInt[i].getIndex();
			array[i]=arrayElementInt[i].getValue();
		}

		int[] result = index;

		return(result);

	}


}
