package masterraise.tools;

import masterraise.Edit;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;

public class SpreadSheet {
	private final View view = jEdit.getActiveView();
	private final TextArea textArea = view.getTextArea();

	boolean isMatchColumns(int iniLine){
		boolean match = true;
		int maxLines = textArea.getLineCount();
		int numTabsPrev = 0;
		int numTabsCurrent = 0;

		textArea.setCaretPosition(textArea.getLineStartOffset(iniLine-1));
		for(int i=0; i<maxLines; i++){
			textArea.selectLine();
			numTabsPrev = new Edit().replaceSelection("(\\p{Print}*)(\\t)(\\p{Print}*)", "$1$2$3", "ir");
			if(i > 0 && numTabsCurrent != numTabsPrev){
				match = false;
				break;
			}
			numTabsCurrent = numTabsPrev;
			textArea.goToNextLine(false);
		}
		//TODO:QUITAR ESTO SI POR UN BUEN MOMENTO FUNCIONA
		//textArea.goToBufferStart(false);
		return match;
	}

	int letterToNumber(String column){
		int retVal = 0;
		String col = column.toUpperCase();
		for(int iChar = col.length() - 1; iChar >= 0; iChar--){
			char colPiece = col.charAt(iChar);
			int colNum = colPiece - 64;
			retVal = retVal + colNum * (int)Math.pow(26, col.length() - (iChar + 1));
		}
		return retVal;
	}

	String numberToLetter(int column){
		String columnString = "";
		float columnNumber = column;
		while(columnNumber > 0){
			float currentLetterNumber = (columnNumber - 1) % 26;
			char currentLetter = (char)(currentLetterNumber + 65);
			columnString = currentLetter + columnString;
			columnNumber = (columnNumber - (currentLetterNumber + 1)) / 26;
		}
		return columnString;
	}

	String increaseColumn(String currentLetter, int diffColumn){
		int numLetter = letterToNumber(currentLetter);
		String newColumn = numberToLetter(numLetter + diffColumn);
		return newColumn;
	}

	/**
	* Transpose the grid for this way:
	* @example
	11	12	13
	21	22	23
	31	32	33
	41	42	43

	To:

	11	21	31	41
	12	22	32	42
	13	23	33	43
	*/
	boolean transposeMatrix(){
		if(textArea.getSelectedText() == null){
			textArea.selectAll();
		}

		String text = textArea.getSelectedText();
		if(!java.util.regex.Pattern.compile("\\A(\\p{Print})+\\t").matcher(text).find()){
			Macros.message(view, "The Selection or Text must Separated by TABS");
			return false;
		}

		String transposeText = "";
		String[] lines = text.replaceAll("(?m)\\t$", "\t\"\"").replaceAll("(?m)(^[ \\t]*|[ ]*$)", "").split("\\r?\\n");
		String[][] matrix = new String[lines.length][];
		for(int l = 0; l < lines.length; l++){
			matrix[l] = lines[l].split("\\t");
		}

		for(int c=0; c<matrix[0].length; c++){
			for(int r=0; r<matrix.length; r++){
				transposeText += matrix[r][c] + "\t";
			}
			transposeText += "\n";
		}

		textArea.setSelectedText(transposeText.replaceAll("(?m)(\\n$|[ \\t]*$)", ""));
		return true;
	}
}
