package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import masterraise.tools.Java;

public class JavaTest extends Tester {
	@Test
	public void fields2javaProperties() {
		setVars("fields2javaProperties");
		actual = new Java().fields2javaProperties();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void genGetSet() {
		setVars("genGetSet");
		actual = new Java().genGetSet();
		assertEquals(expected.trim(), actual.trim());
	}
}
