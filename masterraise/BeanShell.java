package masterraise;

import java.io.IOException;

import org.gjt.sp.jedit.bsh.EvalError;
import org.gjt.sp.jedit.bsh.Interpreter;

public class BeanShell {
	private String code = "";
	
	BeanShell(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public void execute() throws EvalError{
//		Interpreter interpreter = new Interpreter();
//		StringBuffer sb = new StringBuffer("import org.gjt.sp.jedit.jEdit;");
//		sb.append("import org.gjt.sp.jedit.Macros;");
//		sb.append("import org.gjt.sp.jedit.Buffer;");
//		sb.append("import org.gjt.sp.jedit.textarea.TextArea;");
//		sb.append("import org.gjt.sp.jedit.Macros;");
//		sb.append(code);
//		interpreter.eval(sb.toString());
		
//		new Interpreter().eval("Runtime.getRuntime().exec(" + code + ");");
		try {
			Runtime.getRuntime().exec(code);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
