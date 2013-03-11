package test;

import static org.junit.Assert.*;

import java.util.TreeSet;

import org.junit.Test;

import core.Support;

public class TestSupport {

	@Test
	public void testEquals() {
		// TreeSet of function names
		TreeSet<String> ts1 = new TreeSet<String>();
		ts1.add("foo");
		ts1.add("bar");

		// Second TreeSet of function names, ordering maintained by TreeSet
		TreeSet<String> ts2 = new TreeSet<String>();
		ts2.add("bar");
		ts2.add("foo");

		// Equals should not evaluate count as part of equality
		Support s1 = new Support(ts1, 1);
		Support s2 = new Support(ts2, 2);

		// They should be equal
		assertEquals(s1, s2);
	}

	@Test
	public void testCompareTo() {
		TreeSet<Support> supports = new TreeSet<Support>();

		// Should be sorted before ts3
		TreeSet<String> ts1 = new TreeSet<String>();
		ts1.add("b");
		ts1.add("c");
		Support s1 = new Support(ts1, 1);
		supports.add(s1);

		// Should be sorted before ts1
		TreeSet<String> ts2 = new TreeSet<String>();
		ts2.add("a");
		ts2.add("b");
		Support s2 = new Support(ts2, 2);
		supports.add(s2);

		// Should be sorted last
		TreeSet<String> ts3 = new TreeSet<String>();
		ts3.add("f");
		Support s3 = new Support(ts3, 3);
		supports.add(s3);
		
		// Should be sorted first
		TreeSet<String> ts4 = new TreeSet<String>();
		ts4.add("a");
		Support s4 = new Support(ts4, 4);
		supports.add(s4);

		int i = 1;
		for (Support s : supports) {
			switch (i) {
			case 1:
				assertEquals(s, s4);
				break;
			case 2:
				assertEquals(s, s2);
				break;
			case 3:
				assertEquals(s, s1);
				break;
			case 4:
				assertEquals(s, s3);
				break;
			default:
				assertTrue(false); // Should never be reached
			}
			
			i++;
		}
	}
}
