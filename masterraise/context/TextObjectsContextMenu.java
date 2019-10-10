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

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * Subversion context menu to add to the jEdit text area context menu.
 */
@SuppressWarnings("serial")
public class TextObjectsContextMenu extends JMenu {

	private View view = null;

	public TextObjectsContextMenu( View view ) {
		super(jEdit.getProperty("plugin.textobjects.TextObjectsPlugin.name"));
		this.view = view;

		JMenuItem item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-word.label" ) );
		item.addActionListener(selectAWord());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-word.label" ) );
		item.addActionListener(selectInWord());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-brace.label" ) );
		item.addActionListener(selectABrace());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-brace.label" ) );
		item.addActionListener(selectInBrace());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-bracket.label" ) );
		item.addActionListener(selectABracket());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-bracket.label" ) );
		item.addActionListener(selectInBracket());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-paren.label" ) );
		item.addActionListener(selectAParen());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-paren.label" ) );
		item.addActionListener(selectInParen());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-angle.label" ) );
		item.addActionListener(selectAAngle());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-angle.label" ) );
		item.addActionListener(selectInAngle());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-quote.label" ) );
		item.addActionListener(selectAQuote());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-quote.label" ) );
		item.addActionListener(selectInQuote());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-tick.label" ) );
		item.addActionListener(selectATick());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-tick.label" ) );
		item.addActionListener(selectInTick());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-back-tick.label" ) );
		item.addActionListener(selectABackTick());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-back-tick.label" ) );
		item.addActionListener(selectInBackTick());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-paragraph.label" ) );
		item.addActionListener(selectAParagraph());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-paragraph.label" ) );
		item.addActionListener(selectInParagraph());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-comment.label" ) );
		item.addActionListener(selectAComment());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-comment.label" ) );
		item.addActionListener(selectInComment());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-sentence.label" ) );
		item.addActionListener(selectASentence());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-sentence.label" ) );
		item.addActionListener(selectInSentence());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-a-indent.label" ) );
		item.addActionListener(selectAIndent());
		add( item );

		item = new JMenuItem( jEdit.getProperty( "textobjects.select-in-indent.label" ) );
		item.addActionListener(selectInIndent());
		add( item );
	}

	private ActionListener selectAWord() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.word(view.getTextArea(), view.getTextArea().getCaretPosition(), true));
				   }
			   };
	}

	private ActionListener selectInWord() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.word(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectABrace() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), true, "{}"));
				   }
			   };
	}

	private ActionListener selectInBrace() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), false, "{}"));
				   }
			   };
	}

	private ActionListener selectABracket() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), true, "[]"));
				   }
			   };
	}

	private ActionListener selectInBracket() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), false, "[]"));
				   }
			   };
	}

	private ActionListener selectAParen() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), true, "()"));
				   }
			   };
	}

	private ActionListener selectInParen() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), false, "()"));
				   }
			   };
	}

	private ActionListener selectAAngle() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.quote(view.getTextArea(), view.getTextArea().getCaretPosition(), true, '"'));
				   }
			   };
	}

	private ActionListener selectInAngle() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.quote(view.getTextArea(), view.getTextArea().getCaretPosition(), true, '\''));
				   }
			   };
	}

	private ActionListener selectAQuote() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.quote(view.getTextArea(), view.getTextArea().getCaretPosition(), false, '\''));
				   }
			   };
	}

	private ActionListener selectInQuote() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.quote(view.getTextArea(), view.getTextArea().getCaretPosition(), true, '`'));
				   }
			   };
	}

	private ActionListener selectATick() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.quote(view.getTextArea(), view.getTextArea().getCaretPosition(), false, '`'));
				   }
			   };
	}

	private ActionListener selectInTick() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.paragraph(view.getTextArea(), view.getTextArea().getCaretPosition(), true));
				   }
			   };
	}

	private ActionListener selectABackTick() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.paragraph(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectInBackTick() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), true, "&lt;&gt;"));
				   }
			   };
	}

	private ActionListener selectAParagraph() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.block(view.getTextArea(), view.getTextArea().getCaretPosition(), false, "&lt;&gt;"));
				   }
			   };
	}

	private ActionListener selectInParagraph() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.comment(view.getTextArea(), view.getTextArea().getCaretPosition(), true));
				   }
			   };
	}

	private ActionListener selectAComment() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.sentence(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectInComment() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.indent(view.getTextArea(), view.getTextArea().getCaretPosition(), true));
				   }
			   };
	}

	private ActionListener selectASentence() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.indent(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectInSentence() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.comment(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectAIndent() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.comment(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}

	private ActionListener selectInIndent() {
		return new ActionListener() {
				   public void actionPerformed( ActionEvent ae ) {
					   view.getTextArea().setSelection(textobjects.TextObjectsPlugin.comment(view.getTextArea(), view.getTextArea().getCaretPosition(), false));
				   }
			   };
	}
}