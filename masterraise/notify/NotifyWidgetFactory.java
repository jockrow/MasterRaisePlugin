/*
 * InspectionWidgetFactory.java - register inspection if wish to cancel
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2019 Richard Martinez
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package masterraise.notify;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import masterraise.notify.Notify.Detail;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.statusbar.StatusWidgetFactory;
import org.gjt.sp.jedit.gui.statusbar.Widget;

public class NotifyWidgetFactory implements StatusWidgetFactory{
	private final static int COL_INSPECT = 0;
	private final static int COL_LOCATION = 1;
	private final static int COL_DATE = 2;
	private final static int COL_TYPE = 3;
	
	@Override
	public Widget getWidget(View view){
		Widget inspectionWidget = new InspectionWidget(view);
		return inspectionWidget;
	}
	
	private static class InspectionWidget implements Widget{
		private final InspectionHighlight inspectionHighlight;
		
		InspectionWidget(View view){
			inspectionHighlight = new InspectionHighlight(view);
		}

		@Override
		public JComponent getComponent(){
			return inspectionHighlight;
		}

		@Override
		public void update(){
			inspectionHighlight.update();
		}

		@Override
		public void propertiesChanged(){
		}
	}

	private static class InspectionHighlight extends JLabel implements ActionListener{
		private static final long serialVersionUID = -6779257819489239933L;
		private Timer timer;

		InspectionHighlight(View view){
			addMouseListener(new MyMouseAdapter(view));
		}

		@Override
		public void addNotify(){
			super.addNotify();
			update();
			int millisecondsPerMinute = 1000;

			timer = new Timer(millisecondsPerMinute, this);
			timer.start();
		}

		@Override
		public void removeNotify(){
			timer.stop();
			ToolTipManager.sharedInstance().unregisterComponent(this);
			super.removeNotify();
		}

		@Override
		public Point getToolTipLocation(MouseEvent event){
			return new Point(event.getX(), -20);
		}

		@Override
		public void actionPerformed(ActionEvent e){
			update();
		}

		private void update(){
			int numInspections = Notify.list.size();
			if(numInspections > 0){
				String detail = "";
				
				for(Iterator<Detail> di = Notify.list.iterator(); di.hasNext();){
					Detail row = di.next();
					
					if(row.getType().equals("ALARM")){
						if(row.getMinutes() == 0){
							row.setMinutes(row.getTimer().getInitialDelay() / 60000);
						}
						String message = "Alarm: " + row.getLocation() + " will finish in ";
						
						row.setSeconds(row.getSeconds() - 1);
						if(row.getSeconds() <= 0){
							row.setMinutes(row.getMinutes() - 1);
							if(row.getMinutes() == 0){
								row.setSeconds(0);
							}
							else{
								row.setSeconds(60);
							}
						}

						if(row.getMinutes() <= 1){
							message += row.getSeconds() + " seconds";
						}
						else{
							message += row.getMinutes() + " minutes";
						}
						detail += ", " + message;
					}
					else{
						detail += ", " + row.getInspect();
					}
				}
				
				setText(numInspections + " Inspections Running:" + detail.replaceAll("^, ", ""));
				setToolTipText("Double Click to Show Inpections");
			}
			else{
				setText(null);
				setToolTipText(null);
			}
		}

		private class MyMouseAdapter extends MouseAdapter
		{
			private final View view;

			MyMouseAdapter(View view){
				this.view = view;
			}

			@Override
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount() == 2){
					new InspectionDialog(view);
				}
			}
		}
	}
	
	private static class InspectionDialog extends EnhancedDialog{
		private static final long serialVersionUID = -8939500116032119507L;
		private final JButton removeThisInspection;
		private final JButton removeAllInspection;
		private static JTable table = null;
		
		@SuppressWarnings("serial")
		private InspectionDialog(Frame view){
			super(view, "Notification to Cancel", false);
			table = new JTable(){

			public String getToolTipText(MouseEvent e) {
					String tip = null;
					java.awt.Point p = e.getPoint();
					int rowIndex = rowAtPoint(p);

					try {
						String type = getValueAt(rowIndex, COL_TYPE).toString();
						switch(type){
						case "FILE":
							tip = getValueAt(rowIndex, COL_LOCATION) + ": double click to show file";
							break;
						case "PIXEL":
							tip = getValueAt(rowIndex, COL_LOCATION) + ": double click to move pointer";
							break;
						}
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}

					return tip;
				}
			};
			
			table.setModel(new DefaultTableModel(null, new String[]{"Notify for", "Location", "Start Hour", "type"}));
			table.getColumnModel().getColumn(COL_INSPECT).setPreferredWidth(25);
			table.getColumnModel().getColumn(COL_LOCATION).setPreferredWidth(150);
			table.getColumnModel().getColumn(COL_DATE).setPreferredWidth(25);
			table.getColumnModel().getColumn(COL_TYPE).setMinWidth(0);
			table.getColumnModel().getColumn(COL_TYPE).setMaxWidth(0);
			table.getColumnModel().getColumn(COL_TYPE).setWidth(0);

			//set table as ReadOnly
			table.setDefaultEditor(Object.class, null);
			
			//Enable cancel current when selected at least one inspection
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				@Override
		        public void valueChanged(ListSelectionEvent event) {
		        	removeThisInspection.setEnabled(true);
		        }
		    });

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			for(Iterator<Detail> di = Notify.list.iterator(); di.hasNext();){
				Detail row = di.next();
				
				model.addRow(new Object[]{row.getInspect(), row.getLocation(), Notify.dateFormat.format(row.getDateHour()), row.getType()});
				table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
					@Override
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,int column) {
						Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						String type = table.getModel().getValueAt(row,COL_TYPE).toString();
						if (column == 0 && type.equals("PIXEL")) {
							Color col = Color.decode(((Detail)Notify.list.get(row)).getInspect()); 
							c.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue()));
							c.setForeground(new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue()));
						} else {
							c.setBackground(null);
							c.setForeground(null);
						}
						return this;
					}
				});

				table.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						JTable target = (JTable)e.getSource();
						String type = table.getModel().getValueAt(target.getSelectedRow(), COL_TYPE).toString();
						if (e.getClickCount() == 2) {
							switch(type){
							case "FILE":
								jEdit.openFile(jEdit.getActiveView(),table.getModel().getValueAt(target.getSelectedRow(), COL_LOCATION).toString());
								break;
							case "PIXEL":
								try {
									int xCoord = Integer.parseInt(table.getModel().getValueAt(target.getSelectedRow(), COL_LOCATION).toString().replaceAll("Position x:| y:.*", ""));
									int yCoord = Integer.parseInt(table.getModel().getValueAt(target.getSelectedRow(), COL_LOCATION).toString().replaceAll(".*y:", ""));
									new Robot().mouseMove(xCoord, yCoord);
								} catch (AWTException e1) {
									e1.printStackTrace();
								}
								break;
							}
						}
					}
				});
			}
			
			getContentPane().add(new JScrollPane(table));

			Box buttons = new Box(BoxLayout.X_AXIS);
			buttons.add(Box.createGlue());

			buttons.add(removeThisInspection = new JButton("Cancel Current"));
			buttons.add(Box.createHorizontalStrut(6));
			buttons.add(removeAllInspection = new JButton("Cancel All"));
			removeThisInspection.setEnabled(false);

			ActionListener actionListener = new MyActionListener();
			removeThisInspection.addActionListener(actionListener);
			removeAllInspection.addActionListener(actionListener);
			buttons.add(Box.createGlue());
			
			getContentPane().add(buttons, BorderLayout.SOUTH);
			pack();
			setSize(618, 322);
			GUIUtilities.loadGeometry(this,"status.inspection.widget");
			setVisible(true);
		}
		
		@Override
		public void dispose(){
			GUIUtilities.saveGeometry(this, "status.inspection.widget");
			super.dispose();
		}
		
		@Override
		public void ok(){
			dispose();
		}
		
		@Override
		public void cancel(){
			dispose();
		}
		
		private class MyActionListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e){
				Object source = e.getSource();
				if(source == removeThisInspection){
					int i = Notify.unregisterInspect(table.getModel().getValueAt(table.getSelectedRow(), COL_LOCATION).toString());
					((DefaultTableModel)table.getModel()).removeRow(i);
					removeThisInspection.setEnabled(false);
				}
				else if(source == removeAllInspection){
					if(Macros.confirm(jEdit.getActiveView(), "Do you want stop all Inspects?", JOptionPane.YES_NO_OPTION)==0){
						for(Iterator<Detail> di = Notify.list.iterator(); di.hasNext();){
							Detail row = di.next();
							row.getTimer().stop();
							di.remove();
						}
					}
				}
				if(Notify.list.size() <= 0){
					dispose();
				}
				jEdit.getActiveView().getStatus().setMessage("");
			}
		}
	}
}
