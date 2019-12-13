package masterraise;

import javax.swing.JMenu;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.menu.DynamicMenuProvider;

public class MasterRaiseMenuProvider implements DynamicMenuProvider{
	private JMenu menu = null; 

	private void createSubMenu(String title){
		menu.add(GUIUtilities.loadMenuItem(jEdit.getAction(title), false));
	}

	@Override
	public void update(JMenu superMenu) {
		menu = new JMenu("Bulk");
		createSubMenu("insert-selection");
		//		createSubMenu("invocation-massive");
		superMenu.add(menu);

		menu = new JMenu("Encloses");
		Constants.enclosesMenu(menu);
		superMenu.add(menu);

		menu = new JMenu("Files");
		createSubMenu("join-buffers");
		createSubMenu("open-selection");
		superMenu.add(menu);

		menu = new JMenu("HTML");
		createSubMenu("html-fields-list");
		createSubMenu("html-entities-name");
		createSubMenu("html-entities-table");
		createSubMenu("html-options2csv");
		superMenu.add(menu);

		menu = new JMenu("Java");
		createSubMenu("java-default-icons");
		createSubMenu("java-fields-to-java-properties");
		createSubMenu("java-gen-get-set");
		superMenu.add(menu);

		menu = new JMenu("Language");
		createSubMenu("language-code-to-string");
		createSubMenu("language-generate-url-string");
		createSubMenu("language-print-debug-variables");
		createSubMenu("language-string-to-vars");
		superMenu.add(menu);

		menu = new JMenu("MyGeneration");
		createSubMenu("mygeneration-columns-to-properies");
		createSubMenu("mygeneration-csv-to-properies");
		superMenu.add(menu);

		menu = new JMenu("Notify");
		createSubMenu("notify-file");
		createSubMenu("notify-pixel");
		createSubMenu("notify-alarm");
		superMenu.add(menu);

		menu = new JMenu("PHP");
		createSubMenu("get-php-vars-from-html");
		superMenu.add(menu);

		menu = new JMenu("Query");
		createSubMenu("query-beauty");
		createSubMenu("query-convert");
		createSubMenu("query-to-language");
		createSubMenu("query-convert-to-sqllite");
		createSubMenu("query-format-in");
		createSubMenu("query-oracle-ldr-to-bat-rename-images");
		createSubMenu("query-sqlserver-get-temp-tables");
		createSubMenu("query-sqlserver-set-variables-sp");
		superMenu.add(menu);

		menu = new JMenu("SpreadSheet");
		createSubMenu("spreadsheet-increase-column");
		createSubMenu("spreadsheet-transpose-matrix");
		createSubMenu("spreadsheet-value-column");
		superMenu.add(menu);

		menu = new JMenu("Text");
		createSubMenu("text-first-upper-case");
		createSubMenu("text-search-back");
		createSubMenu("text-replace-accent");
		superMenu.add(menu);
	}

	@Override
	public boolean updateEveryTime() {
		return false;
	}
}
