package core;

/**
 * TODO Refactor so we can switch between intra- and inter-procedural analysis
 * 
 * @param args
 *            arg[0] is filename, arg[1] is support, arg[2] is confidence %,
 *            arg[3] is parse type ("intra", "inter", "output_txt")
 */
public class Main {
	// Default support and confidence parameters
	public static final int T_SUPPORT_DEFAULT = 3;
	public static final double T_CONFIDENCE_DEFAULT = 65;
	public static final String INTRA_PARSE_TYPE = "intra";
	public static final String INTER_PARSE_TYPE = "inter";
	public static final String OUTPUT_TXT_PARSE_TYPE = "output_txt";

	public static void main(String[] args) {
		StaticAnalysis staticAnalysis;
		String fileName = "";
		int thresholdSupport = T_SUPPORT_DEFAULT;
		double thresholdConfidence = T_CONFIDENCE_DEFAULT / 100;
		String parseType = INTRA_PARSE_TYPE;

		if (args.length > 0) {
			int i = 0;
			while (i < args.length) {
				switch (i) {
				case 0:
					fileName = args[0];
					break;
				case 1:
					thresholdSupport = Integer.parseInt(args[1]);
					break;
				case 2:
					thresholdConfidence = Double.parseDouble(args[2]) / 100;
					break;
				case 3:
					parseType = args[3];
				}
				i++;
			}

			// Set static analysis type based on parseType
			if (parseType.equals(INTER_PARSE_TYPE)) {
				staticAnalysis = new Interprocedural();
			} else if (parseType.equals(OUTPUT_TXT_PARSE_TYPE)) {
				staticAnalysis = new OutputTxt();
			} else {
				// Default is intraprocedural
				staticAnalysis = new Intraprocedural();
			}

			// Run static analsysis parse
			staticAnalysis.parse(fileName, thresholdSupport,
					thresholdConfidence);

		} else {
			System.out
					.println("Minimum argument is at least the BitCode filename");
		}
	}
}
