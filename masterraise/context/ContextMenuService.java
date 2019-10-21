/*
Copyright (c) 2011, Richard Martinez
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package masterraise.context;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DynamicContextMenuService;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import masterraise.Constants;

/**
 * This is the implementation of DynamicContextMenuService that lets
 * this plugin add items to the text area context menu.  This is way
 * better than the kludge I'd been doing previously, which conflicted
 * with at least the ContextMenu plugin, and maybe others.  To have jEdit
 * add the SVN context menu, this class is declared in the services.xml file.
 */
public class ContextMenuService extends DynamicContextMenuService {
	// cache menu for quicker response
	private HashMap<View, JMenu> encloseMenus = new HashMap<View, JMenu>();
	private HashMap<View, JMenu> textObjectsMenus = new HashMap<View, JMenu>();

	// context menu is per View
	public JMenuItem[] createMenu(JEditTextArea textArea, MouseEvent mouseEvent) {
		View view = textArea == null ? jEdit.getFirstView() : textArea.getView();

		JMenu enclosesMenu = encloseMenus.get(view);
		if (enclosesMenu == null) {
			enclosesMenu = new JMenu("Encloses");
			Constants.enclosesMenu(enclosesMenu);
			encloseMenus.put(view, enclosesMenu);
		}

		JMenu textObjectsMenu = textObjectsMenus.get(view);
		if (textObjectsMenu == null) {
			textObjectsMenu = new JMenu("Text Object");
			Constants.textObjectMenu(textObjectsMenu);
			textObjectsMenus.put(view, textObjectsMenu);
		}

		return new JMenuItem[]{enclosesMenu, textObjectsMenu};
	}
}