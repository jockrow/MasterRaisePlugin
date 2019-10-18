package masterraise.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.InternationalFormatter;

import masterraise.Text;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;

public class SpreadSheet extends Text{
	private final View view = jEdit.getActiveView();
	private final TextArea textArea = view.getTextArea();
	
	private String LBL_NUMBERS = "Number to Char";
	private String COPY_NUMBER = "Copy Number Column";
	private String COPY_LETTER = "Copy Letter Column";
	private String LBL_TEXT = "Char to Number";

	private JDialog dialog = null;
	private JPanel content = new JPanel(new BorderLayout());
	private JPanel fieldPanel = new JPanel(new GridLayout(3, 3, 2, 2));
	private JTextArea lblValidate = new JTextArea("");
	private JPanel validationPanel = new JPanel();
	private JRadioButton rbNumbers = null;
	private JPanel buttonPanel = new JPanel();
	private JButton cancel = new JButton("Cancel");
	private JButton copy = null;
	private InternationalFormatter formatNumber = new InternationalFormatter();
	private JFormattedTextField txtNumberColumn = new JFormattedTextField(formatNumber);
	private JFormattedTextField txtAddColumns = new JFormattedTextField(formatNumber);
	private JTextField txtLetterColumn = null;
	private JTextField txtResult = new JTextField("B");
	private String dialogType = "";
	
	private KeyListener kl = new KeyListener(){
		@Override
		public void keyReleased(KeyEvent keyEvent){
			if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE){
				dialog.dispose();
			}
			else{
				validation();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {}
	};

	public boolean isMatchColumns(int iniLine){
		boolean match = true;
		int maxLines = textArea.getLineCount();
		int numTabsPrev = 0;
		int numTabsCurrent = 0;

		textArea.setCaretPosition(textArea.getLineStartOffset(iniLine-1));
		for(int i=0; i<maxLines; i++){
			textArea.selectLine();
			numTabsPrev = replaceSelection("(\\p{Print}*)(\\t)(\\p{Print}*)", "$1$2$3", "ir");
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

	private Integer letterToNumber(String column){
		int retVal = 0;
		String col = column.toUpperCase();
		for(int iChar = col.length() - 1; iChar >= 0; iChar--){
			char colPiece = col.charAt(iChar);
			int colNum = colPiece - 64;
			retVal = retVal + colNum * (int)Math.pow(26, col.length() - (iChar + 1));
		}
		return retVal;
	}

	private String numberToLetter(int column){
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

	private String increaseColumn(String currentLetter, int diffColumn){
		int numLetter = letterToNumber(currentLetter);
		String newColumn = numberToLetter(numLetter + diffColumn);
		return newColumn;
	}
	
	//TODO:ES EL MÉTODO QUE OPTIMIZA increaseColumn y Value_From_Column
	private void setFields(){
		System.out.println("...setFields.dialogType:" + dialogType);
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		dialog.setContentPane(content);
		formatNumber.setMinimum(new Integer(1));
		lblValidate.setEditable(false);
		lblValidate.setBackground(content.getBackground());

		validationPanel.add(lblValidate);
		validationPanel.setBorder(new EmptyBorder(0, 30, 20, 30));
		content.add(validationPanel, "Center");
		
		txtLetterColumn = new JTextField("A");
		txtLetterColumn.addKeyListener(kl);
		fieldPanel.add(new JLabel("Letter Column"));
		fieldPanel.add(txtLetterColumn);
	
		content.add(fieldPanel, "North");
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(12, 30, 0, 30));
		
		dialog.getRootPane().setDefaultButton(copy);
		buttonPanel.add(Box.createHorizontalStrut(6));
		buttonPanel.add(copy);
		buttonPanel.add(cancel);
		content.add(buttonPanel, "South");

		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
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
	public void transposeMatrix(){
		if(textArea.getSelectedText() == null){
			textArea.selectAll();
		}

		String t = iniSelectedText();
		if(!java.util.regex.Pattern.compile("\\A(\\p{Print})+\\t").matcher(t).find()){
			Macros.message(view, "The Selection or Text must Separated by TABS");
			return;
		}

		String transposeText = "";
		String[] lines = t.replaceAll("(?m)\\t$", "\t\"\"").replaceAll("(?m)(^[ \\t]*|[ ]*$)", "").split("\\r?\\n");
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

		t = transposeText.replaceAll("(?m)(\\n$|[ \\t]*$)", "");
		endSelectedText(t);
	}
	
	/**
	* Get the letter or Number from column:
	* @example
	*	11 To: K
	*	or 731 To: ABC
	*/
/*	public void valueFromColumn(){
		System.out.println("...valueFromColumn");
		dialogType = "valueFromColumn";
		dialog = new JDialog(view, "Get Value From Column", false);
		
		ActionListener al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals(LBL_NUMBERS) || e.getActionCommand().equals(LBL_TEXT)){
					if(e.getActionCommand().equals(LBL_NUMBERS)){
						txtNumberColumn.setEditable(true);
						txtLetterColumn.setEditable(false);
						copy.setText(COPY_LETTER);
					}
					else{
						txtNumberColumn.setEditable(false);
						txtLetterColumn.setEditable(true);
						copy.setText(COPY_NUMBER);
					}

					validation();
				}
				else{
					if(e.getSource() != cancel){
						if(e.getSource() == copy){
							if(rbNumbers.isSelected()){
								Registers.setRegister('$', txtLetterColumn.getText());
							}
							else{
								Registers.setRegister('$', txtNumberColumn.getText());
							}
						}
					}
					else{
						dialog.dispose();
					}
				}
			}
		};

		txtNumberColumn.setValue(1);
		txtNumberColumn.addKeyListener(kl);
		fieldPanel.add(new JLabel("Number Column"));
		fieldPanel.add(txtNumberColumn);
		
		txtLetterColumn.setEditable(false);

		rbNumbers = new JRadioButton(LBL_NUMBERS);
		rbNumbers.setActionCommand(LBL_NUMBERS);
		rbNumbers.addActionListener(al);
		rbNumbers.setSelected(true);

		JRadioButton rbText = new JRadioButton(LBL_TEXT);
		rbText.setActionCommand(LBL_TEXT);
		rbText.addActionListener(al);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rbText);
		group.add(rbNumbers);
		fieldPanel.add(rbNumbers);
		fieldPanel.add(rbText);
		
		copy = new JButton(COPY_LETTER);
		cancel.addActionListener(al);
		copy.addActionListener(al);
		
		setFields();
	}
	*/

	public void valueFromColumn(){
		dialogType = "valueFromColumn";
		dialog = new JDialog(view, "Get Value From Column", false);
		
		ActionListener al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals(LBL_NUMBERS) || e.getActionCommand().equals(LBL_TEXT)){
					if(e.getActionCommand().equals(LBL_NUMBERS)){
						txtNumberColumn.setEditable(true);
						txtLetterColumn.setEditable(false);
						copy.setText(COPY_LETTER);
					}
					else{
						txtNumberColumn.setEditable(false);
						txtLetterColumn.setEditable(true);
						copy.setText(COPY_NUMBER);
					}

					validation();
				}
				else{
					if(e.getSource() != cancel){
						if(e.getSource() == copy){
							if(rbNumbers.isSelected()){
								Registers.setRegister('$', txtLetterColumn.getText());
							}
							else{
								Registers.setRegister('$', txtNumberColumn.getText());
							}
						}
					}
					else{
						dialog.dispose();
					}
				}
			}
		};

		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		dialog.setContentPane(content);

		formatNumber.setMinimum(new Integer(1));
		
		lblValidate.setEditable(false);
		lblValidate.setBackground(content.getBackground());

		validationPanel.add(lblValidate);
		validationPanel.setBorder(new EmptyBorder(0, 30, 20, 30));
		content.add(validationPanel, "Center");
		
		txtNumberColumn.setValue(1);
		txtNumberColumn.addKeyListener(kl);
		fieldPanel.add(new JLabel("Number Column"));
		fieldPanel.add(txtNumberColumn);
		
		txtLetterColumn = new JTextField("A");
		txtLetterColumn.setEditable(false);
		txtLetterColumn.addKeyListener(kl);
		fieldPanel.add(new JLabel("Letter Column"));
		fieldPanel.add(txtLetterColumn);
		

		rbNumbers = new JRadioButton(LBL_NUMBERS);
		rbNumbers.setActionCommand(LBL_NUMBERS);
		rbNumbers.addActionListener(al);
		rbNumbers.setSelected(true);

		JRadioButton rbText = new JRadioButton(LBL_TEXT);
		rbText.setActionCommand(LBL_TEXT);
		rbText.addActionListener(al);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rbText);
		group.add(rbNumbers);
		fieldPanel.add(rbNumbers);
		fieldPanel.add(rbText);
		content.add(fieldPanel, "North");
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(12, 30, 0, 30));
		copy = new JButton(COPY_LETTER);
		dialog.getRootPane().setDefaultButton(copy);
		buttonPanel.add(Box.createHorizontalStrut(6));
		buttonPanel.add(copy);
		buttonPanel.add(cancel);
		content.add(buttonPanel, "South");

		cancel.addActionListener(al);
		copy.addActionListener(al);

		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}
	
	/**
	* Get value for Increase or Decrease in Column
	* @example
	*	Z + 1 = AA
	*/
/*	public void increaseColumn(){
		dialogType = "increaseColumn";
		
		ActionListener al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				validation();
				if(e.getSource() != cancel){
					if(e.getSource() == copy && txtResult.getText().trim() != ""){
						Registers.setRegister('$', txtResult.getText());
					}
				}
				else{
					dialog.dispose();
				}
			}
		};

		dialog = new JDialog(view, "Increase Column", false);
		content = new JPanel(new BorderLayout());
		fieldPanel = new JPanel(new GridLayout(3, 3, 2, 2));

		lblValidate = new JTextArea("");

		validationPanel = new JPanel();
		
		txtAddColumns.setValue(1);
		txtAddColumns.addKeyListener(kl);
		fieldPanel.add(new JLabel("Additional Columns"));
		fieldPanel.add(txtAddColumns);
		
		txtResult.setEditable(false);
		fieldPanel.add(new JLabel("Result:"));
		fieldPanel.add(txtResult);
		
		copy = new JButton("Copy new Column");

		cancel.addActionListener(al);
		copy.addActionListener(al);

		setFields();
	}*/

	public void increaseColumn(){
		dialogType = "increaseColumn";
		
		ActionListener al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				validation();
				if(e.getSource() != cancel){
					if(e.getSource() == copy && txtResult.getText().trim() != ""){
						Registers.setRegister('$', txtResult.getText());
					}
				}
				else{
					dialog.dispose();
				}
			}
		};

		dialog = new JDialog(view, "Get Value From Column", false);
		content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		dialog.setContentPane(content);
		fieldPanel = new JPanel(new GridLayout(3, 3, 2, 2));

		InternationalFormatter formatNumber = new InternationalFormatter();
		formatNumber.setMinimum(new Integer(1));
		
		lblValidate = new JTextArea("");
		lblValidate.setEditable(false);
		lblValidate.setBackground(content.getBackground());

		validationPanel = new JPanel();
		validationPanel.add(lblValidate);
		validationPanel.setBorder(new EmptyBorder(0, 30, 20, 30));
		content.add(validationPanel, "Center");
		
		txtLetterColumn = new JTextField("A");
		txtLetterColumn.addKeyListener(kl);
		fieldPanel.add(new JLabel("Letter Column"));
		fieldPanel.add(txtLetterColumn);
		
		txtAddColumns.setValue(1);
		txtAddColumns.addKeyListener(kl);
		fieldPanel.add(new JLabel("Additional Columns"));
		fieldPanel.add(txtAddColumns);
		
		txtResult.setEditable(false);
		fieldPanel.add(new JLabel("Result:"));
		fieldPanel.add(txtResult);
		
		content.add(fieldPanel, "North");
		
//		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(12, 30, 0, 30));
		cancel = new JButton("Cancel");
		copy = new JButton("Copy new Column");
		dialog.getRootPane().setDefaultButton(copy);
		buttonPanel.add(Box.createHorizontalStrut(6));
		buttonPanel.add(copy);
		buttonPanel.add(cancel);
		content.add(buttonPanel, "South");

		cancel.addActionListener(al);
		copy.addActionListener(al);

		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}
	
	private boolean displayError(String message){
		lblValidate.setForeground(Color.red);
		lblValidate.setText(message);
		
		if(dialogType.equals("valueFromColumn")){
			if(rbNumbers.isSelected()){
				txtLetterColumn.setText("");
			}
			else{
				txtNumberColumn.setText("");
			}			
		}
		else{
			txtResult.setText("");
		}
		
		return false;
	}
	
	private boolean validation(){
		String ERROR_NUMBERS = "This field must have only Numbers";
		String ERROR_LETTER = "This field must have only Letters";
		String NOT_ZERO = "Not less Zero Values";
		try{
			Pattern p = Pattern.compile(".*[0-9].*");
			boolean isErrorLetter = txtLetterColumn == null ? false : p.matcher(txtLetterColumn.getText()).find();
			
			if(dialogType.equals("valueFromColumn")){
				if(rbNumbers.isSelected()){
					int numberColumn = Integer.valueOf(txtNumberColumn.getText());
					if(numberColumn<=0){
						return displayError(NOT_ZERO);
					}
					
					isErrorLetter = false;
				}
			}
			
			if(isErrorLetter){
				return displayError(ERROR_LETTER);
			}

			lblValidate.setForeground(Color.black);
			lblValidate.setText("");
			processText();
		}
		catch(NumberFormatException ex){
			displayError(ERROR_NUMBERS);
		}
		processText();
		return true;
	}
	
	private void processText(){
		String textColumn = txtLetterColumn == null ? "" : txtLetterColumn.getText();
		
		if(dialogType.equals("valueFromColumn")){
			if(rbNumbers.isSelected()){
				txtLetterColumn.setText(numberToLetter(Integer.valueOf(txtNumberColumn.getText())));
			}
			else{
				txtNumberColumn.setText(letterToNumber(textColumn).toString());
			}
		}
		else{
			int addColumns = Integer.valueOf(txtAddColumns.getText());
			txtResult.setText(increaseColumn(textColumn, addColumns));
		}
	}
}
