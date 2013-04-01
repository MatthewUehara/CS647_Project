package core;

/**
 * Interface that defines the methods that all static analysis operations should
 * have in common.
 */
public interface StaticAnalysis {
	public void parse(String filePart, int thresholdSupport,
			double thresholdConfidence);
}
