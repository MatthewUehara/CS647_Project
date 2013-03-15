package core;

public class PairConfidence implements Comparable<PairConfidence>{
	
	private String function;
	private String s;
	private int support;
	private double confidence;
	
	public PairConfidence(String function, String s, int support, double confidence) {
		this.function = function;
		this.s = s;
		this.support = support;
		this.confidence = confidence;
	}
	
	public String getFunction() {
		return function;
	}
	
	public String getPair() {
		return s;
	}
	
	public int getSupport() {
		return support;
	}
	
	public double getConf() {
		return confidence;
	}
	
	public int compareTo(PairConfidence pc) {
		if (this.function.compareTo(pc.getFunction()) == 0) {
			String p1 = s;
			String p2 = pc.getPair();
			return p1.compareTo(p2);
		}
		return this.function.compareTo(pc.getFunction());
	}
	
	public String toString() {
		return " pair: " + s + "support: " + support + " confidence: " + String.format("%.2f", confidence*100.0) + "%";
	}
}