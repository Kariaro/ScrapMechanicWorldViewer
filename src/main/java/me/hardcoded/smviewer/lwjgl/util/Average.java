package me.hardcoded.smviewer.lwjgl.util;

public class Average {
	private double[] test;
	private int index;
	
	public Average(int count) {
		test = new double[count];
		index = 0;
	}
	
	public void add(double value) {
		test[(index++) % test.length] = value;
	}
	
	public double getAverage() {
		double total = 0;
		for (int i = 0; i < test.length; i++) {
			total += test[i];
		}
		
		return total / (double)test.length;
	}
}
