package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import masterraise.tools.Html;

public class HtmlTest extends Tester{
	@Test
	public void getFieldsList() {
		setVars("getFieldsList");
		actual = new Html().getFieldsList();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertHtmlEntities() {
		setVars("convertHtmlEntities");
		actual = new Html().convertHtmlEntities(true);
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void options2Csv() {
		setVars("options2Csv");
		actual = new Html().options2Csv();
		expected = decodeUtf8(expected.trim());
		assertEquals(expected, actual.trim());
	}
}
