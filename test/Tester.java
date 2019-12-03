package test;

import java.nio.charset.Charset;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import masterraise.Constants;
import masterraise.files.MrFile;

public class Tester extends Constants{
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final Charset ISO = Charset.forName("Windows-1252");

	public final static String TEST_PATH = System.getProperty("user.dir") + "/test/";
	public final static String ERR_TEST_AGAIN = "Sorry :( Error Testing, please try Again";
	public static final String CONVERT = "convert";
	public static final String NO_PARAMS = "no_params";

	public final View view = jEdit.getActiveView();
	public String expected = "";
	public String actual = "";

	public String decodeUtf8(String text) {
		return new String(text.getBytes(ISO),UTF_8);
	}

	//TODO:QUITAR
	public void setVars(String convertType, String fileTesting) {
		String path = TEST_PATH + convertType + "/";
		String fileOriginal = path + fileTesting + ".txt";
		String fileConverted = path + fileTesting + "_Converted.txt";
		expected = new MrFile().readFile(fileConverted);

		try {
			jEdit.openFile(view, fileOriginal);
		} catch (Exception e) {
			e.printStackTrace();
			setVars(convertType, fileTesting);
		}
	}
	
	public void setVars(String fileTesting) {
		String path = TEST_PATH + NO_PARAMS + "/";
		String fileOriginal = path + fileTesting + ".txt";
		String fileConverted = path + fileTesting + "_Converted.txt";
		expected = new MrFile().readFile(fileConverted);

		try {
			jEdit.openFile(view, fileOriginal);
		} catch (Exception e) {
			e.printStackTrace();
			setVars(NO_PARAMS, fileTesting);
		}
	}
}
