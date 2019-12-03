package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import masterraise.tools.Language;

public class LanguageTest extends Tester{
	@Test
	public void printDebugVariables() {
		setVars("printDebugVariables");
		actual = new Language().printDebugVariables();
		assertEquals(expected.trim(), actual.trim());
	}
	
//	@Test //TODO:
//	public void generateUrlString() {
//		setVars("generateUrlString");
//		actual = new Language().generateUrlString();
//		assertEquals(expected.trim(), actual.trim());
//	}
	
//	@Test
//	public void codeToString() {
//		setVars("codeToString");
//		actual = new Language().codeToString();
//		assertEquals(expected.trim(), actual.trim());
//	}
	
//	@Test
//	public void stringToVars() {
//		setVars("stringToVars");
//		actual = new Language().stringToVars();
//		assertEquals(expected.trim(), actual.trim());
//	}
}
