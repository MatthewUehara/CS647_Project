

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Interface that defines the methods that all static analysis operations should
 * have in common.
 */
public interface StaticAnalysis {
	public void callgraph(String filePart, int thresholdSupport,
			double thresholdConfidence);
	
	public HashMap<String, TreeSet<String>> parse(BufferedReader reader);
	
	public void eval(HashMap<String, TreeSet<String>> functionMap, int thresholdSupport, double thresholdConfidence);
}
