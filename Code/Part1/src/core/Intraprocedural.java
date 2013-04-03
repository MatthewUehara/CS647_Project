package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the callgraph generated by LLVM.
 */
public class Intraprocedural implements StaticAnalysis {
	// REGEX pattern for function name
	public static final Pattern nodePattern = Pattern
			.compile("Call graph node for function: '(.*?)'<<(.*?)>>  #uses=(\\d*).*");
	// REGEX pattern for callsite
	public static final Pattern callsitePattern = Pattern
			.compile("\\s*CS<(.*?)> calls function '(.*?)'.*");

	/**
	 * I don't think we need to overcomplicate things. Java can just perform
	 * Runtime.getRuntime().exec()
	 * 
	 * @param fileName
	 *            Filename of bitcode file that will be anlyized.
	 * @param thresholdSupport
	 *            Minimum amount of support for the relationship.
	 * @param thresholdConfidence
	 *            Confidence of bug necessary in decimal format < 1 (eg.
	 *            thresholdConfidence=0.85 means 85%)
	 */
	public void callgraph(String fileName, int thresholdSupport,
			double thresholdConfidence) {
		try {
			// multi-threads resolve process deadlock problem
			final Process process = Runtime.getRuntime().exec(
					"opt -print-callgraph " + fileName);
			new Thread() {
				public void run() {
					InputStream isStdout = process.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(isStdout, Charset.forName("UTF-8")));
					try {
						while (reader.readLine() != null)
							;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			InputStream isError = process.getErrorStream();
			BufferedReader reader2 = new BufferedReader(new InputStreamReader(
					isError, Charset.forName("UTF-8")));
			
			// Parse BufferedReader output of callgraph
			HashMap<String, TreeSet<String>> fm = this.parse(reader2);
			
			// Evaluated the parsed format
			this.eval(fm, thresholdSupport, thresholdConfidence);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public HashMap<String, TreeSet<String>> parse(BufferedReader br) {
		
		// functionMapIntra (function, caller set) is initialed
		HashMap<String, TreeSet<String>> functionMapIntra = new HashMap<String, TreeSet<String>>();
		String currentLine = null;
		String currentNode = null;

		// update
		String key = "";
		boolean check = false;

		try {
			while ((currentLine = br.readLine()) != null) {

				// We're at a new node
				Matcher nodeMatcher = nodePattern.matcher(currentLine);
				if (nodeMatcher.find()) {
					currentNode = nodeMatcher.group(1);

					// update
					key = currentNode;
					check = true;
				}

				// We're at a callsite within currentNode
				Matcher callsiteMatcher = callsitePattern.matcher(currentLine);
				// First node in callgraph is a null function

				// update
				if (check == false && callsiteMatcher.find()) {
					String callee = callsiteMatcher.group(2);
					functionMapIntra.put(callee, new TreeSet<String>());
				}

				if (callsiteMatcher.find() && currentNode != null) {

					// update
					String callee = callsiteMatcher.group(2);
					if (functionMapIntra.get(callee) == null) {
						functionMapIntra.put(callee, new TreeSet<String>());
					}
					
					// add (function, caller set) in the map
					functionMapIntra.get(callee).add(key);
				}

				// System.out.println(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		return functionMapIntra;
	}
	
	public void eval(HashMap<String, TreeSet<String>> functionMap, int thresholdSupport, double thresholdConfidence) {
		/*
		 * Please see PairConfidence.java for details. It contains function,
		 * function pair, support, and confidence. PairCofidence is a key used
		 * in TreeMap, and the value is a TreeSet which has the functions with
		 * bugs The complexity of my current algorithm is still n square. May
		 * need some optimization. THe current run time of test3 is only 2 to 3
		 * sec. The order of our output is not important. It will be sorted
		 * before comparing with gold file.
		 */
		TreeMap<PairConfidence, TreeSet<String>> pairs = new TreeMap<PairConfidence, TreeSet<String>>();
		ArrayList<String> functions = new ArrayList<String>();
		functions.addAll(functionMap.keySet());
		for (int i = 0; i < functions.size(); i++) {
			String function1 = functions.get(i);
			TreeSet<String> callerList = functionMap.get(functions.get(i));
			int support = functionMap.get(functions.get(i)).size();
			if (support == 0) {
				continue;
			}
			
			/*
			 * for each individual function rather than if self
			 * compare the caller sets of both functions
			 * the support of pair is the number of common elements in both sets
			 * the support of function is the number of elements in the function's caller sets
			 */
			for (int j = 0; j < functions.size(); j++) {
				if (i == j) {
					continue;
				}
				
				// compare two caller sets and get the number of common elements as the support
				String function2 = functions.get(j);
				TreeSet<String> tmp = new TreeSet<String>();
				tmp.addAll(callerList);
				TreeSet<String> remain = new TreeSet<String>();
				
				// the remain elements in the caller set are the bug locations
				remain.addAll(tmp);
				tmp.retainAll(functionMap.get(functions.get(j)));
				remain.removeAll(tmp);
				
				// filter the result by the support level and confidence level
				int supportPair = tmp.size();
				if (supportPair < thresholdSupport) {
					continue;
				}
				double confidence = ((double) supportPair) / ((double) support);

				if (confidence < thresholdConfidence) {
					continue;
				}

				// create the PairConfidence and bug location list
				// create result maps
				String pair = "";
				if (function1.compareTo(function2) < 0) {
					pair = "(" + function1 + " " + function2 + ") ";
				} else {
					pair = "(" + function2 + " " + function1 + ") ";
				}
				PairConfidence pc = new PairConfidence(function1, pair,
						supportPair, confidence);
				pairs.put(pc, remain);
			}
		}

		// System.out.println("RESULTS:");
		// System.out.println("--------");
		NumberFormat numf = NumberFormat.getNumberInstance();
		numf.setMaximumFractionDigits(2);
		numf.setRoundingMode(RoundingMode.HALF_EVEN);

		// only for local test. The actual output on ECE machine will be
		// sorted automatically.
		TreeMap<String, String> display = new TreeMap<String, String>();

		for (Map.Entry entry : pairs.entrySet()) {
			String function = ((PairConfidence) entry.getKey()).getFunction();
			String header = "bug: " + function + " in ";
			for (String s : pairs.get(entry.getKey())) {
				String message = header + s
						+ ((PairConfidence) entry.getKey()).toString();
				// System.out.println(message); // will be used on ECE
				// machine.

				// only for local test. The actual output on ECE machine
				// will be sorted automatically.
				display.put(message.replaceAll("_", "").replaceAll(" ", ""),
						message);
			}
		}

		// only for local test. The actual output on ECE machine will be
		// sorted automatically.
		for (Map.Entry entry : display.entrySet()) {
			System.out.println((String) entry.getValue());
		}

		System.exit(0);

	}
}
