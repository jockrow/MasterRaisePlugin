package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import masterraise.tools.Language;

public class LanguageTest extends Tester{
	Language l = new Language();

	@Test
	public void printDebugVariables() {
		setVars("printDebugVariables");
		actual = l.printDebugVariables();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void generateUrlString() {
		setVars("generateUrlString");
		actual = l.generateUrlString();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void codeToString() {
		setVars("codeToString");
		actual = l.codeToString();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void stringToVars() {
		setVars("stringToVars");
		actual = l.stringToVars();
		assertEquals(expected.trim(), actual.trim());
	}
}
