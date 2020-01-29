/**********************************************/
/*      Develop by Richard Martinez 2011      */
/**********************************************/
/**
 * Start menu filter and change Master Raise icons
 */
package masterraise;

import java.awt.EventQueue;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.menu.EnhancedMenu;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.ViewUpdate;

public class MasterRaisePlugin extends EBPlugin{
	private static final String MENU_FAVORITES_ACTIVATE = "masterraise-plugin.menu.favorites.activate";
	private static final int M_PLUGIN = 8;
	boolean waitFor1stBuff = true;
	private static final String MACRO_PATH = "/bsh";
	
	public void start()
	{
		BeanShell.getNameSpace().addCommandPath(MACRO_PATH, getClass());
	}

	@EBHandler
	public void handleViewUpdate(ViewUpdate vu){
		if (vu.getWhat() == ViewUpdate.CREATED){
			loadFavorites(vu.getView());
		}
	}

	public void handleMessage(EBMessage message){
		if (message instanceof EditPaneUpdate){
			EditPaneUpdate epu = (EditPaneUpdate) message;
			if (epu.getWhat().equals(EditPaneUpdate.CREATED)){
				GUIUtilities.setIconPath("jeditresource:/MasterRaise.jar!/icons/");
				setIcons();
//				setDarkTheme();
//				final SessionSwitcher switcher = new CustomSession(view, true);
//				new CustomSession(jEdit.getActiveView(), true);
			}
		}
		
		if (message instanceof BufferUpdate){
			BufferUpdate epu = (BufferUpdate) message;

			if (epu.getWhat().equals(BufferUpdate.PROPERTIES_CHANGED) 
					&& jEdit.isStartupDone()
					&& epu.getBuffer().equals(jEdit.getBuffers()[0])){
				
				loadFavorites(jEdit.getActiveView());
			}
		}
	}
	
	public void loadFavorites(View view){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				EnhancedMenu menu = (EnhancedMenu)view.getJMenuBar().getMenu(M_PLUGIN);
				menu.init();

				if(checkFavorites()){
					hideMenus(view);			
				}
			}
		});
	}

	private void setIcons(){
		UIManager.put("Tree.collapsedIcon", GUIUtilities.loadIcon("16x16/actions/group-expand.png"));
		UIManager.put("Tree.expandedIcon", GUIUtilities.loadIcon("16x16/actions/group-collapse.png"));
		
		UIManager.put("Tree.openIcon", GUIUtilities.loadIcon("16x16/status/folder-open.png"));
		UIManager.put("Tree.closedIcon", GUIUtilities.loadIcon("16x16/places/folder.png"));
		
		UIManager.put("FileChooser.newFolderIcon", GUIUtilities.loadIcon("16x16/actions/folder-open.png"));
		UIManager.put("OptionPane.questionIcon", GUIUtilities.loadIcon("22x22/apps/help-browser.png"));
		UIManager.put("OptionPane.errorIcon", GUIUtilities.loadIcon("22x22/actions/document-close.png"));
		UIManager.put("OptionPane.informationIcon", GUIUtilities.loadIcon("22x22/apps/information.png"));
		UIManager.put("OptionPane.warningIcon", GUIUtilities.loadIcon("22x22/apps/warning.png"));
		
//		UIManager.put("FileView.floppyDriveIcon", GUIUtilities.loadIcon("22x22/actions/document-save.png"));
	}
	
//	private void setMasterProperty(String property){
//		jEdit.setProperty(property, jEdit.getProperty("masterraise." + property));		
//	}
	
//	private void setDarkTheme(){
//		setMasterProperty("error-list.errorColor");
//		setMasterProperty("error-list.warningColor");
//		setMasterProperty("hypersearch.results.highlight");
//		setMasterProperty("metalcolor.backgroundcolor");
//		setMasterProperty("metalcolor.basecolor");
//		setMasterProperty("metalcolor.scrollbarcolor");
//		setMasterProperty("metalcolor.textcolor");
//		setMasterProperty("view.bgColor");
//		setMasterProperty("view.caretColor");
//		setMasterProperty("view.eolMarkerColor");
//		setMasterProperty("view.fgColor");
//		setMasterProperty("view.gutter.bgColor");
//		setMasterProperty("view.gutter.currentLineColor");
//		setMasterProperty("view.gutter.fgColor");
//		setMasterProperty("view.gutter.focusBorderColor");
//		setMasterProperty("view.gutter.markerColor");
//		setMasterProperty("view.gutter.noFocusBorderColor");
//		setMasterProperty("view.gutter.selectionAreaBgColor");
//		setMasterProperty("view.gutter.structureHighlightColor");
//		setMasterProperty("view.lineHighlightColor");
//		setMasterProperty("view.multipleSelectionColor");
//		setMasterProperty("view.selectionColor");
//		setMasterProperty("view.selectionFgColor");
//		setMasterProperty("view.status.background");
//		setMasterProperty("view.status.foreground");
//		setMasterProperty("view.status.memory.background");
//		setMasterProperty("view.status.memory.foreground");
//		setMasterProperty("view.structureHighlightColor");
//		setMasterProperty("view.style.comment1");
//		setMasterProperty("view.style.comment3");
//		setMasterProperty("view.style.digit");
//		setMasterProperty("view.style.keyword1");
//		setMasterProperty("view.style.keyword2");
//		setMasterProperty("view.style.keyword3");
//		setMasterProperty("view.style.literal1");
//		setMasterProperty("view.style.function");
//		setMasterProperty("view.style.operator");
//		setMasterProperty("view.wrapGuideColor");
//		setMasterProperty("vfs.browser.colors.8.color");
//		setMasterProperty("white-space.block-color");
//		setMasterProperty("white-space.fold-color");
//		setMasterProperty("white-space.space-color");
//		setMasterProperty("white-space.tab-color");
//		setMasterProperty("white-space.whitespace-color");
//		setMasterProperty("console.bgColor");
//		setMasterProperty("console.caretColor");
//		setMasterProperty("console.errorColor");
//		setMasterProperty("console.infoColor");
//		setMasterProperty("console.plainColor");
//		setMasterProperty("console.warningColor");
//	}

	private static void hideMenus(View view){
		JMenu menu = view.getJMenuBar().getMenu(M_PLUGIN);
		String strHidePlugins = jEdit.getProperty("masterraise-plugin.menu.favorites.visible");

		for (int i=2; i<menu.getMenuComponentCount()-1; i++){
			JMenuItem c = (JMenuItem)menu.getMenuComponent((i+1));
			if(!c.getText().matches(strHidePlugins) && !c.getText().equals(jEdit.getProperty("plugin.masterraise.MasterRaisePlugin.name"))){
				c.setVisible(false);
			}
		}
	}

	public static void showMenus(View view){
		JMenu menu = view.getJMenuBar().getMenu(M_PLUGIN);

		for (int i=3; i<menu.getMenuComponentCount(); i++){
			JMenuItem c = (JMenuItem)menu.getMenuComponent((i+1));
			c.setVisible(true);
		}
	}

	public static void toggleFavorites(View view){
		if(checkFavorites()){
			jEdit.setProperty(MENU_FAVORITES_ACTIVATE, "false");
			showMenus(view);
		}
		else{
			jEdit.setProperty(MENU_FAVORITES_ACTIVATE, "true");
			hideMenus(view);
		}
	}

	public static boolean checkFavorites(){
		return jEdit.getProperty(MENU_FAVORITES_ACTIVATE).equals("true");
	}
}