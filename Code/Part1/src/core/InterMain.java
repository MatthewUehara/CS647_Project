package core;

/**
 * TODO Refactor so we can switch between intra- and inter-procedural
 * analysis
 * 
 * @param args
 *            arg[0] is filename, arg[1] is support, arg[2] is confidence %
 */
public class InterMain {
	// Default support and confidence parameters
	public static final int T_SUPPORT_DEFAULT = 3;
	public static final double T_CONFIDENCE_DEFAULT = 65;
	
	public static void main(String[] args) {
		ParseCallgraph parseCallgraph = new ParseCallgraph();
		String filePart = "";
		int thresholdSupport = T_SUPPORT_DEFAULT;
		double thresholdConfidence = T_CONFIDENCE_DEFAULT / 100;
		
		if (args.length > 0) {
			int i = 0;
			while (i < args.length) {
				switch (i) {
					case 0 :
						String fileName = args[0];
						int extIndex = fileName.indexOf(".bc");
						filePart = fileName.substring(0, extIndex);
						break;
					case 1 :
						thresholdSupport = Integer.parseInt(args[1]);
						break;
					case 2 :
						thresholdConfidence = Double.parseDouble(args[2]) / 100;
						break;
				}
				i++;
			}
			
			// Run bug analysis
			//parseCallgraph.parse(filePart, thresholdSupport, thresholdConfidence);
			
			
			//update
			ParseInter parseInter = new ParseInter();
			parseInter.parse(filePart, thresholdSupport, thresholdConfidence);
			
		} else {
			System.out.println("Minimum argument is at least the BitCode filename");
		}
	}
}
