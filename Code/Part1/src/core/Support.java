package core;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * <p>
 * Support is the number of times a pair of functions appears together. Ordering
 * of functions does not matter.
 * </p>
 * <p>
 * eg. Either A() then B() or B() then A().
 * </p>
 */
public class Support implements Comparable<Support> {
	private TreeSet<String> functions;
	private int count;

	public Support(TreeSet<String> functionPair, int count) {
		this.functions = functionPair;
		this.count = count;
	}

	public void incrementCount() {
		this.count = this.count + 1;
	}

	/**
	 * Create an overview toString.
	 * 
	 * TODO Adjust this to the format required by project guidelines
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if (this.functions.size() > 1) {
			sb.append("Functions ");
		} else {
			sb.append("Function ");
		}

		Iterator functionsIter = this.functions.iterator();
		while (functionsIter.hasNext()) {
			sb.append(functionsIter.next());
			if (functionsIter.hasNext()) {
				sb.append(" + ");
			} else {
				sb.append(", ");
			}
		}
		sb.append("Support = " + count);
		return sb.toString();
	}

	/**
	 * Overriden equals(...) function does not consider count for a given
	 * Support object but only evaluates that the functions in the Support
	 * object are the same.
	 */
	@Override
	public boolean equals(Object obj) {
		// Check that they're the same class
		if (obj instanceof Support) {
			Support s2 = (Support) obj;

			// Check that they're the same size
			if (this.functions.size() == s2.getFunctions().size()) {

				// Everything in this.functions exists in s2.getFunctions()
				for (String f : this.functions) {
					if (!s2.getFunctions().contains(f)) {
						return false;
					}
				}

				// All prior checks passed, must be equal
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Sorted alphabetically by first function in functions TreeSet.
	 */
	@Override
	public int compareTo(Support s2) {
		int compareResult = this.functions.first().compareTo(
				s2.getFunctions().first());

		// If first element is the same
		if (compareResult == 0) {
			// Sort single function Support before paired functions' Support
			compareResult = this.functions.size() - s2.getFunctions().size();
		} else {
			return compareResult;
		}

		// If both are paired functions' Support
		if (compareResult == 0) {
			// Iterate through functions TreeSets together until difference
			Iterator<String> thisFunctionsIter = this.functions.iterator();
			for (String s2Function : s2.getFunctions()) {
				if (thisFunctionsIter.hasNext()) {
					compareResult = thisFunctionsIter.next().compareTo(
							s2Function);
					if (compareResult != 0) {
						return compareResult;
					}
				} else {
					// TODO This method may need to be more robust
					break;
				}
			}

			// Somehow they're equal, should not be reached.
			return 0;
		} else {
			return compareResult;
		}
	}

	public TreeSet<String> getFunctions() {
		return functions;
	}

	public void setFunctions(TreeSet<String> functions) {
		this.functions = functions;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
