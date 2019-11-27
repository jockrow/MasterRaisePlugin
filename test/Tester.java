package test;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import masterraise.Constants;

public class Tester extends Constants{
	public final View view = jEdit.getActiveView();
	
	public String contentTo = "";
	public String convertedQuery = "";
	
	public static final long MILISECONDS = 0;
	public final static String TEST_PATH = System.getProperty("user.dir") + "/test/";
//	public final static String TEST_PATH = "D:\\myProjects\\MasterRaisePlugin/test/";
	public final static String ERR_TEST_AGAIN = "Sorry :( Error Testing, please try Again";
}
