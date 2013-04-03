package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the callgraph generated by LLVM.
 */
public class Interprocedural implements StaticAnalysis {
	// REGEX pattern for function name
	public static final Pattern nodePattern = Pattern
			.compile("Call graph node for function: '(.*?)'<<(.*?)>>  #uses=(\\d*).*");
	// REGEX pattern for callsite
	public static final Pattern callsitePattern = Pattern
			.compile("\\s*CS<(.*?)> calls function '(.*?)'.*");

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
		String currentLine = null;
		String currentNode = null;
		
		// functionMap is a <function, callee list> map. We build this one for testing purpose
		HashMap<String, TreeSet<String>> functionMap = new HashMap<String, TreeSet<String>>();
		// functionMapInter is the <function, ancestors list> used in our interprocedural implementation
		HashMap<String, TreeSet<String>> functionMapInter = new HashMap<String, TreeSet<String>>();

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
					functionMap.put(key, new TreeSet<String>());
				}

				// We're at a callsite within currentNode
				Matcher callsiteMatcher = callsitePattern.matcher(currentLine);
				// First node in callgraph is a null function
				// TODO Do we need to evaluate it? TA's tutorial was unclear.

				// update
				if (check == false && callsiteMatcher.find()) {
					String callee = callsiteMatcher.group(2);
					
					// generate initial inter map which is the same as intra map
					functionMapInter.put(callee, new TreeSet<String>());
				}

				if (callsiteMatcher.find() && currentNode != null) {

					// update
					String callee = callsiteMatcher.group(2);
					functionMap.get(key).add(callee);
					
					// update key(function) and value (caller list)
					if (functionMapInter.get(callee) == null) {
						functionMapInter.put(callee, new TreeSet<String>());
					}
					functionMapInter.get(callee).add(key);
				}

				//System.out.println(currentLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// inter-process
		/* For each function in our functionMapInter map, we call the interPro() method to return a list of all the callers of this function
		 * Those callers include direct callers and also the callers of direct callers
		 * If we consider the call graph as a tree, then this interPro will return all the nodes from root which can reach the function
		 * The list may include the function itself if there is a cycle.
		 * We'll remove the cycle at the end.
		 */
		for (Map.Entry entry : functionMapInter.entrySet()) {
			//System.out.print(entry.getKey() + ": ");
			String func = (String) entry.getKey();
			TreeSet<String> callerSet = functionMapInter.get(func);

			callerSet.addAll(interPro(functionMapInter, new TreeSet<String>(),
					func));
			
			// remove self-call of each function
			if (callerSet.contains(entry.getKey())) {
				callerSet.remove(entry.getKey());
			}
			/*
			for (String s : callerSet) {
				System.out.print(s + " ");
			}
			System.out.println("");
			*/
		}

		return functionMapInter;
	}

	public void eval(HashMap<String, TreeSet<String>> functionMapInter,
			int thresholdSupport, double thresholdConfidence) {
		/*
		 * Please see PairConfidence.java for details. It contains function,
		 * function pair, support, and confidence. PairCofidence is a key used
		 * in TreeMap, and the value is a TreeSet which has the functions with
		 * bugs. The order of our output is not important. It will be sorted
		 * before comparing with gold file.
		 */

		TreeMap<PairConfidence, TreeSet<String>> pairs = new TreeMap<PairConfidence, TreeSet<String>>();
		ArrayList<String> functions = new ArrayList<String>();
		functions.addAll(functionMapInter.keySet());
		for (int i = 0; i < functions.size(); i++) {
			
			// get the caller set of this function
			String function1 = functions.get(i);
			TreeSet<String> callerList = functionMapInter.get(functions.get(i));
			int support = functionMapInter.get(functions.get(i)).size();
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
				remain.addAll(tmp);
				tmp.retainAll(functionMapInter.get(functions.get(j)));
				
				// the remain elements in the caller set are the bug locations
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
				// create result map
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

		//System.out.println("RESULTS:");
		//System.out.println("--------");
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

	// interPro - the inter-procedural happens here
	/*
	 * We use bottom-up approach: Start with a function, return all the nodes starting from root, which is an ancient of that function
	 * F : <all the ancients of F>
	 * 
	 * For each function, we have a master set which is the final result.
	 * For each function, we first get its caller set and add to our master set
	 * Then for each individual caller, we recursively call interPro to get the caller set of that caller.
	 * We merge the caller's caller set with the master set
	 * Since we use tree set, there will be no duplicate.
	 * 
	 * We continue merging caller set until for an individual caller, our master set contains all the ancients of this caller
	 * In this case, we move to the next caller. 
	 * 
	 * We return our master set as the set of all the ancients of that function.
	 */
	private TreeSet<String> interPro(
			HashMap<String, TreeSet<String>> functionMapIntra,
			TreeSet<String> functions, String s) {
		TreeSet<String> callerSet = functionMapIntra.get(s);
		functions.addAll(callerSet);
		for (String caller : callerSet) {
			TreeSet<String> callers = functionMapIntra.get(caller);
			if (callers.size() == 0 || functions.containsAll(callers)) {
				continue;
			}
			functions.addAll(interPro(functionMapIntra, functions, caller));
		}
		return functions;
	}
}
