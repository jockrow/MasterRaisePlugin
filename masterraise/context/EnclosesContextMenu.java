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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import masterraise.Text;

/**
 * Subversion context menu to add to the jEdit text area context menu.
 */
@SuppressWarnings("serial")
public class EnclosesContextMenu extends JMenu {
//	private View view = null;
	private final View view = jEdit.getActiveView();
	private final Text text = new Text();
	

	public EnclosesContextMenu( View view ) {
//		super( "Encloses" );
//		this.view = view;
		
		JMenuItem item = new JMenuItem("Enc Admiration i !");
		item.addActionListener( encAdmiration() );
		add( item );

		item = new JMenuItem("Enc Curly Bracket { }");
		item.addActionListener( encCurlyBracket() );
		add( item );

		item = new JMenuItem("Enc Double Quote '' ''");
		item.addActionListener( encDoubleQuote() );
		add( item );

		item = new JMenuItem("Enc LtGt \u2039 \u203A");
		item.addActionListener( encLtGt() );
		add( item );

		item = new JMenuItem("Enc Percent % %");
		item.addActionListener( encPercent() );
		add( item );

		item = new JMenuItem("Enc Question \u00BF ?");
		item.addActionListener( encQuestion() );
		add( item );
		
		item = new JMenuItem("Enc Quote ` `");
		item.addActionListener( encQuote() );
		add( item );

		item = new JMenuItem("Enc Round Bracket ( )");
		item.addActionListener( encRoundBracket() );
		add( item );

		item = new JMenuItem("Enc Single Quote ' '");
		item.addActionListener( encSingleQuote() );
		add( item );

		item = new JMenuItem("Enc Square Bracket [ ]");
		item.addActionListener( encSquareBracket() );
		add( item );
	}

	private ActionListener encAdmiration() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Admiration_i_!").invoke(view);
			}
		};
	}
	
	private ActionListener encCurlyBracket() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Curly_Bracket_{_}").invoke(view);
			}
		};
	}

	private ActionListener encDoubleQuote() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Double_Quote_''_''").invoke(view);
			}
		};
	}

	private ActionListener encLtGt() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_LtGt_Tag").invoke(view);
			}
		};
	}

	private ActionListener encPercent() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Percent_%_%").invoke(view);
			}
		};
	}
	
	private ActionListener encQuestion() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Question_\u00BF").invoke(view);
			}
		};
	}

	private ActionListener encQuote() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Quote_`_`").invoke(view);
			}
		};
	}

	private ActionListener encRoundBracket() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Round_Bracket_(_)").invoke(view);
			}
		};
	}

	private ActionListener encSingleQuote() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Single_Quote_'_'").invoke(view);
			}
		};
	}

	private ActionListener encSquareBracket() {
		return new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				Macros.getMacro("Text/Encloses/Enc_Square_Bracket_[_]").invoke(view);
			}
		};
	}
}