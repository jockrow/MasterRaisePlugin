package masterraise;

import javax.swing.JMenu;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.menu.DynamicMenuProvider;

public class MasterRaiseMenuProvider implements DynamicMenuProvider{

	@Override
	public void update(JMenu superMenu) {
		JMenu menu = new JMenu("SpreadSheet");
		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction("spreadsheet-transpose-matrix"), false));
//		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction("project-compile"), false));
//		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction("project-run"), false));
////		menu.addSeparator();
//		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction("chdir-pv-root"), false));
//		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction("chdir-pv-selected"), false));
		superMenu.add(menu);
////		superMenu.addSeparator();
//		
		menu = new JMenu("SQL");
//		EditAction[] commands = MasterRaisePlugin.getCommandoCommands();
//		for(int i = 0; i < commands.length; i++)
//		{
//			menu.add(GUIUtilities.loadMenuItem(commands[i], false));
//		}
		superMenu.add(menu);
//		menu = new JMenu("Shells");
//		commands = MasterRaisePlugin.getSwitchActions();
//		for(int i = 0; i < commands.length; i++)
//		{
//			menu.add(GUIUtilities.loadMenuItem(commands[i], false));
//		}
		superMenu.add(menu);
	}

	@Override
	public boolean updateEveryTime() {
		return false;
	}
}
