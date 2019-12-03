package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import masterraise.tools.Html;

public class HtmlTest extends Tester{
	@Test
	public void getFieldsList() {
		//TODO:REEMPLAZAR POR setVars("getFieldsList");
		setVars(NO_PARAMS, "getFieldsList");
		actual = new Html().getFieldsList();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void options2Csv() {
		setVars(NO_PARAMS, "options2Csv");
		actual = new Html().options2Csv();
		expected = decodeUtf8(expected.trim());
		assertEquals(expected, actual.trim());
	}
}
