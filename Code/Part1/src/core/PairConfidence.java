package core;

import java.util.Iterator;
import java.util.TreeSet;

public class PairConfidence implements Comparable<PairConfidence>{
	
	private String function;
	private TreeSet<String> s;
	private int support;
	private double confidence;
	
	public PairConfidence(String function, TreeSet<String> s, int support, double confidence) {
		this.function = function;
		this.s = s;
		this.support = support;
		this.confidence = confidence;
	}
	
	public String getFunction() {
		return function;
	}
	
	public TreeSet<String> getSet() {
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
			Iterator<String> i1 = this.s.iterator();
			Iterator<String> i2 = pc.getSet().iterator();
			String p1 = "";
			String p2 = "";
			while (i1.hasNext()) {
				String tmp = i1.next();
				if (!tmp.equals(function)) {
					p1 = tmp;
					break;
				}
			}
			while (i2.hasNext()) {
				String tmp = i2.next();
				if (!tmp.equals(function)) {
					p2 = tmp;
					break;
				}
			}
			return p1.compareTo(p2);
		}
		return this.function.compareTo(pc.getFunction());
	}
}
