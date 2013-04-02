package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class OutputTxtInter extends Interprocedural {
	@Override
	public void callgraph(String fileName, int thresholdSupport,
			double thresholdConfidence) {
		try {
			FileReader input = new FileReader(fileName);
			BufferedReader reader = new BufferedReader(input);				
			
			// Parse BufferedReader output of callgraph
			HashMap<String, TreeSet<String>> fm = super.parse(reader);
			
			// Evaluated the parsed format
			super.eval(fm, thresholdSupport, thresholdConfidence);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
