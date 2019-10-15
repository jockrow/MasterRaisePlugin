/**********************************************
*     @author Richard Martínez 2018/11/29     *
***********************************************/
/**
 * Start a message every each time
 */
package masterraise.notify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.InternationalFormatter;

import org.gjt.sp.jedit.View;

public class Alarm{
	InternationalFormatter formatNumber = new InternationalFormatter();
	JFormattedTextField txtMinutes = new JFormattedTextField(formatNumber);
	JTextField txtMessage = new JTextField("");
	JTextArea lblValidate = new JTextArea("");
	JButton ok = null;
	JDialog dialog = null;
	
	Timer t = new Timer(0, new ActionListener(){
		public void actionPerformed(ActionEvent evt){
			String command = txtMessage.getText();
			
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) { }
			Notify.displayMessage(command);
		}
	});

	public void start(View view){
		KeyListener ka = new KeyListener(){
			public void keyReleased(KeyEvent ke){
				if (ke.getKeyCode() == KeyEvent.VK_ESCAPE){
					dialog.dispose();
				}
				else{
					validation();
				}
			}
			public void keyPressed(KeyEvent ke){}
			public void keyTyped(KeyEvent ke){}
		};

		ActionListener al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				switch(e.getActionCommand()){
				case "OK":
					startAlarm();
					break;
				case "Cancel":
					dialog.dispose();
					break;
				default:
					validation();
					break;
				}
			}
		};

		dialog = new JDialog(view, "Alarm", true);
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		dialog.setContentPane(content);

		lblValidate.setEditable(false);
		lblValidate.setBackground(content.getBackground());

		JPanel validationPanel = new JPanel();
		validationPanel.add(lblValidate);
		content.add(validationPanel, "Center");

		formatNumber.setMinimum(new Integer(1));
		txtMinutes.setValue("1");
		txtMinutes.addKeyListener(ka);

		txtMinutes.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						txtMinutes.selectAll();
					}
				});
			}
			
			@Override
			public void focusLost(FocusEvent e){
				if(txtMinutes.getText().trim().equals("") || txtMinutes.getText().trim().equals("0")){
					txtMinutes.setText("1");
				}
				validation();
			}
		});

		JPanel fieldPanel = new JPanel(new GridLayout(0,2));
		fieldPanel.add(new JLabel("Minutes:"));
		fieldPanel.add(txtMinutes);

		fieldPanel.add(new JLabel("Reminder Message:"));
		txtMessage.addKeyListener(ka);
		fieldPanel.add(txtMessage);

		content.add(fieldPanel, "North");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(12, 50, 0, 50));
		JButton cancel = new JButton("Cancel");
		ok = new JButton("OK");
		ok.setPreferredSize(cancel.getPreferredSize());
		dialog.getRootPane().setDefaultButton(ok);
		buttonPanel.add(ok);
		buttonPanel.add(Box.createHorizontalStrut(6));
		buttonPanel.add(cancel);
		content.add(buttonPanel, "South");

		ok.addActionListener(al);
		cancel.addActionListener(al);

		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		validation();
		dialog.setVisible(true);
	}

	private boolean displayError(String message){
		lblValidate.setForeground(Color.red);
		lblValidate.setText(message);
		ok.setEnabled(false);
		return false;
	}

	private boolean validation(){
		if(txtMinutes.getText().trim().equals("") || txtMessage.getText().trim().equals("")){
			return displayError("Fields NOT must have Empty Values");
		}
		if(txtMinutes.getText().equals("0")){
			return displayError("Not Zero Value");
		}

		lblValidate.setForeground(Color.black);
		lblValidate.setText("");
		ok.setEnabled(true);
		return true;
	}

	void startAlarm(){
		int delay = Integer.valueOf(txtMinutes.getText()) * 60000;
		Notify ins = new Notify();
		
		dialog.dispose();
		ins.registerInspect(txtMinutes.getText()
				, txtMessage.getText()
				, t
				, "ALARM");
		
		t.setInitialDelay(delay);
		t.setRepeats(false);
		t.start();
	}
}
