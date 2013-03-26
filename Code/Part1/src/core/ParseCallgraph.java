package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.math.RoundingMode;
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
public class ParseCallgraph {
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
	 * @param filePart
	 *            Part of the filename that matches directory and file naming
	 *            format (eg. filePart="test3" will be mapped to
	 *            ../test3/test3.bc)
	 * @param thresholdSupport
	 *            Minimum amount of support for the relationship.
	 * @param thresholdConfidence
	 *            Confidence of bug necessary in decimal format < 1 (eg.
	 *            thresholdConfidence=0.85 means 85%)
	 */
	public void intra(String filePart, int thresholdSupport,
			double thresholdConfidence) {
		String currentLine = null;
		String currentNode = null;

		// used in inter-processing
		HashMap<String, TreeSet<String>> functionMap = new HashMap<String, TreeSet<String>>();
		HashMap<String, TreeSet<String>> functionMapIntra = new HashMap<String, TreeSet<String>>();

		try {
			// multi-threads resolve process deadlock problem
			final Process process = Runtime.getRuntime().exec(
					"opt -print-callgraph ../" + filePart + "/main.bc");
			new Thread() {
				public void run() {
					InputStream stdout = process.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(stdout));
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
					isError));

			// update
			String key = "";
			boolean check = false;

			while ((currentLine = reader2.readLine()) != null) {

				// We're at a new node
				Matcher nodeMatcher = nodePattern.matcher(currentLine);
				if (nodeMatcher.find()) {
					currentNode = nodeMatcher.group(1);

					// update
					key = currentNode;
					check = true;
					functionMap.put(key, new TreeSet<String>());
				}

				// We're at a callsite within currentNode
				Matcher callsiteMatcher = callsitePattern.matcher(currentLine);
				// First node in callgraph is a null function
				// TODO Do we need to evaluate it? TA's tutorial was unclear.

				// update
				if (check == false && callsiteMatcher.find()) {
					String callee = callsiteMatcher.group(2);
					functionMapIntra.put(callee, new TreeSet<String>());
				}

				if (callsiteMatcher.find() && currentNode != null) {

					// update
					String callee = callsiteMatcher.group(2);
					functionMap.get(key).add(callee);
					if (functionMapIntra.get(callee) == null) {
						functionMapIntra.put(callee, new TreeSet<String>());
					}
					functionMapIntra.get(callee).add(key);
				}

				System.out.println(currentLine);
			}

			// update
			/*
			 * Please see PairConfidence.java for details. It contains function,
			 * function pair, support, and confidence. PairCofidence is a key
			 * used in TreeMap, and the value is a TreeSet which has the
			 * functions with bugs The complexity of my current algorithm is
			 * still n square. May need some optimization. THe current run time
			 * of test3 is only 2 to 3 sec. The order of our output is not
			 * important. It will be sorted before comparing with gold file.
			 */
			TreeMap<PairConfidence, TreeSet<String>> pairs = new TreeMap<PairConfidence, TreeSet<String>>();
			ArrayList<String> functions = new ArrayList<String>();
			functions.addAll(functionMapIntra.keySet());
			for (int i = 0; i < functions.size(); i++) {
				String function1 = functions.get(i);
				TreeSet<String> callerList = functionMapIntra.get(functions
						.get(i));
				int support = functionMapIntra.get(functions.get(i)).size();
				if (support == 0) {
					continue;
				}
				for (int j = 0; j < functions.size(); j++) {
					if (i == j) {
						continue;
					}
					String function2 = functions.get(j);
					TreeSet<String> tmp = new TreeSet<String>();
					tmp.addAll(callerList);
					TreeSet<String> remain = new TreeSet<String>();
					remain.addAll(tmp);
					tmp.retainAll(functionMapIntra.get(functions.get(j)));
					remain.removeAll(tmp);
					int supportPair = tmp.size();
					if (supportPair < thresholdSupport) {
						continue;
					}
					double confidence = ((double) supportPair)
							/ ((double) support);

					if (confidence < thresholdConfidence) {
						continue;
					}

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

			System.out.println("RESULTS:");
			System.out.println("--------");
			NumberFormat numf = NumberFormat.getNumberInstance();
			numf.setMaximumFractionDigits(2);
			numf.setRoundingMode(RoundingMode.HALF_EVEN);

			// only for local test. The actual output on ECE machine will be
			// sorted automatically.
			TreeMap<String, String> display = new TreeMap<String, String>();

			for (Map.Entry entry : pairs.entrySet()) {
				String function = ((PairConfidence) entry.getKey())
						.getFunction();
				String header = "bug: " + function + " in ";
				for (String s : pairs.get(entry.getKey())) {
					String message = header + s
							+ ((PairConfidence) entry.getKey()).toString();
					// System.out.println(message); // will be used on ECE
					// machine.

					// only for local test. The actual output on ECE machine
					// will be sorted automatically.
					display.put(
							message.replaceAll("_", "").replaceAll(" ", ""),
							message);
				}
			}

			// only for local test. The actual output on ECE machine will be
			// sorted automatically.
			for (Map.Entry entry : display.entrySet()) {
				System.out.println((String) entry.getValue());
			}

			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
