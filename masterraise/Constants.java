package masterraise;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

/**
 * Constants settings for Master Rise Plugin
 */
public abstract class Constants {
	public final static String ERR_NOT_MATCH_COLUMN	= "The number columns is not Match";
	public final static String ERR_INVALID_CSV		= "Text must have Tabs";
	public final static String ERR_SELECT_TEXT		= "Must Select a Text";

	public final static String TRIM_UP				= "\\A[\\n$ \\t]*";
	public final static String TRIM_DOWN			= "[\\n$ \\t]*\\z";
	public final static String TRIM_BORDER			= TRIM_UP + "|" + TRIM_DOWN;
	public final static String TRIM_LEFT			= "^[ \\t]*";
	public final static String TRIM_RIGHT			= "[ \\t]*$";
	public final static String TRIM					= TRIM_LEFT + "|" + TRIM_RIGHT;
	public final static String TRIM_TOTAL			= TRIM_BORDER + "|" + TRIM;
	public final static String BLANK_LINE			= "(" + TRIM_UP + ")|(" + TRIM_DOWN + ")|(^[ \\t]*\\n)";
	public final static String BLANK_SPACE			= "(" + TRIM_LEFT + ")|(" + TRIM_RIGHT + ")";
	public final static String LOW_ENIE				= "ñ";
	public final static String DOT					= "_______";
	public final static String ROUND_BRACKET_RIGHT	= "_____";
	public final static String SHARP				= "____";
	public final static String ROUND_BRACKET_LEFT	= "___";
	public final static String COMA					= "__";
	public final static String TRIM_COMA			= "([ ]?)(,)([ ]?)";
	public final static String DOUBLE_SPACES		= "[ \\t]{2,}";
	public final static String TRIM_SPECIAL_CHARS	= "([ ]?)([\\(\\)\\{\\};])([ ]?)";
	public final static String COMMENTS				= "[ \\t]*(--|//).*|/\\*([\\n\\t ]*([#\\w áéíóú]+\\n*)+[\\n\\t ]*)+\\*/";

	public final static String URL						= "((https?|ftp)://)?(\\w+\\.\\w+)+(\\p{Graph})*";
	public final static String HTML_FILTER_FIELDS		= "(select|input|textarea|datalist)";
	public final static String HTML_NOT_FILTER_FIELDS	= "(submit|reset|button)";

	public final static String SQL_OBJECT			= "(\\w+\\.){0,}+\\w+";
	public final static String SQL_VALUES			= "'{0,1}[\\w/]+'{0,1}";
	public final static String SQL_QUOTES_VALUES	= "('\\w+)((,{0,1}[\\w/]){1,})";
	public final static String SQL_FUNCTION			= "(" + SQL_OBJECT + "\\(" + SQL_VALUES + ")((," + SQL_VALUES + ")*)";
	public final static String SQL_ALIAS			= "([a-z] )((AS ){0,1}\\w+)";
	public final static String SQL_RESERVED			= "\\b(insert|into|values|update|set|as|not|like|in|inner|right|left|join|on|select|distinct|convert|case|when|then|else|end|sum|count|max|min|datetime|smallint|int|varchar|dateadd|isnull|null|from|where|and|or|with|nolock|union|group by|order by|having|desc|cast|concat|substr|declare|numeric|default)\\b";
	public final static String SQL_RESERVED_LINE	= "\\b(SET|FROM|WHERE|AND|OR|ORDER|INNER|RIGHT|LEFT)\\b";
	public final static String SQL_NUMBER			= "\\d+,\\d+";
	public final static String CSV_PREFIX			= "Structure table: %s\nFIELD	VALUE\n";

	public final static String[][] ARR_CHARS = {
	{"Á","capital a, acute accent","&Aacute;","&#193;","A"}
	,{"á","small a, acute accent","&aacute;","&#225;","a"}
	,{"É","capital e, acute accent","&Eacute;","&#201;","E"}
	,{"é","small e, acute accent","&eacute;","&#233;","e"}
	,{"Í","capital i, acute accent","&Iacute;","&#205;","I"}
	,{"í","small i, acute accent","&iacute;","&#237;","i"}
	,{"Ó","capital o, acute accent","&Oacute;","&#211;","O"}
	,{"ó","small o, acute accent","&oacute;","&#243;","o"}
	,{"Ú","capital u, acute accent","&Uacute;","&#218;","U"}
	,{"ú","small u, acute accent","&uacute;","&#250;","u"}
	,{"Ñ","capital n, tilde","&Ntilde;","&#209;","N"}
	,{"ñ","small n, tilde","&ntilde;","&#241;","n"}

	,{"À","capital a, grave accent","&Agrave;","&#192;"}
	,{"à","small a, grave accent","&agrave;","&#224;"}
	,{"Â","capital a, circumflex accent","&Acirc;","&#194;"}
	,{"â","small a, circumflex accent","&acirc;","&#226;"}
	,{"Ã","capital a, tilde","&Atilde;","&#195;"}
	,{"ã","small a, tilde","&atilde;","&#227;"}
	,{"Ä","capital a, umlaut mark","&Auml;","&#196;"}
	,{"ä","small a, umlaut mark","&auml;","&#228;"}
	,{"Å","capital a, ring","&Aring;","&#197;"}
	,{"å","small a, ring","&aring;","&#229;"}
	,{"È","capital e, grave accent","&Egrave;","&#200;"}
	,{"è","small e, grave accent","&egrave;","&#232;"}
	,{"Ê","capital e, circumflex accent","&Ecirc;","&#202;"}
	,{"ê","small e, circumflex accent","&ecirc;","&#234;"}
	,{"Ë","capital e, umlaut mark","&Euml;","&#203;"}
	,{"ë","small e, umlaut mark","&euml;","&#235;"}
	,{"Ì","capital i, grave accent","&Igrave;","&#204;"}
	,{"ì","small i, grave accent","&igrave;","&#236;"}
	,{"Î","capital i, circumflex accent","&Icirc;","&#206;"}
	,{"î","small i, circumflex accent","&icirc;","&#238;"}
	,{"Ï","capital i, umlaut mark","&Iuml;","&#207;"}
	,{"ï","small i, umlaut mark","&iuml;","&#239;"}
	,{"Ò","capital o, grave accent","&Ograve;","&#210;"}
	,{"ò","small o, grave accent","&ograve;","&#242;"}
	,{"Ô","capital o, circumflex accent","&Ocirc;","&#212;"}
	,{"ô","small o, circumflex accent","&ocirc;","&#244;"}
	,{"Õ","capital o, tilde","&Otilde;","&#213;"}
	,{"õ","small o, tilde","&otilde;","&#245;"}
	,{"Ö","capital o, umlaut mark","&Ouml;","&#214;"}
	,{"ö","small o, umlaut mark","&ouml;","&#246;"}
	,{"Ù","capital u, grave accent","&Ugrave;","&#217;"}
	,{"ù","small u, grave accent","&ugrave;","&#249;"}
	,{"Û","capital u, circumflex accent","&Ucirc;","&#219;"}
	,{"û","small u, circumflex accent","&ucirc;","&#251;"}
	,{"Ü","capital u, umlaut mark","&Uuml;","&#220;"}
	,{"ü","small u, umlaut mark","&uuml;","&#252;"}

	,{"Ç","capital c, cedilla","&Ccedil;","&#199;"}
	,{"ç","small c, cedilla","&ccedil;","&#231;"}
	,{"Ð","capital eth, Icelandic","&ETH;","&#208;"}
	,{"ð","small eth, Icelandic","&eth;","&#240;"}
	,{"Æ","capital ae","&AElig;","&#198;"}
	,{"æ","small ae","&aelig;","&#230;"}
	,{"Ø","capital o, slash","&Oslash;","&#216;"}
	,{"ø","small o, slash","&oslash;","&#248;"}
	,{"Œ","capital ligature OE","&OElig;","&#338;"}
	,{"œ","small ligature oe","&oelig;","&#339;"}
	,{"Š","capital S with caron","&Scaron;","&#352;"}
	,{"š","small S with caron","&scaron;","&#353;"}
	,{"Ý","capital y, acute accent","&Yacute;","&#221;"}
	,{"ý","small y, acute accent","&yacute;","&#253;"}
	,{"Ÿ","capital Y with diaeres","&Yuml;","&#376;"}
	,{"ÿ","small y, umlaut mark","&yuml;","&#255;"}
	,{"Þ","capital THORN, Icelandic","&THORN;","&#222;"}
	,{"þ","small thorn, Icelandic","&thorn;","&#254;"}

	,{"\"","quotation mark","&quot;","&#34;"}
	,{"'","apostrophe","&apos;","&#39;"}
	,{"&","ampersand","&amp;","&#38"}
	,{"<","less-than","&lt;","&#60;"}
	,{">","greater-than","&gt;","&#62;"}
	,{"&nbsp;","non-breaking space","&nbsp;","&#160;"}
	,{"¡","inverted exclamation mark","&iexcl;","&#161;"}
	,{"¤","currency","&curren;","&#164;"}
	,{"¢","cent","&cent;","&#162;"}
	,{"£","pound","&pound;","&#163;"}

	,{"×","multiplication","&times;","&#215;"}
	,{"÷","division","&divide;","&#247;"}
	,{"±","plus-or-minus&nbsp;","&plusmn;","&#177;"}
	,{"`","spacing grave","&grave;","&#96;"}
	,{"¥","yen","&yen;","&#165;"}
	,{"¦","broken vertical bar","&brvbar;","&#166;"}
	,{"§","section","&sect;","&#167;"}
	,{"¨","spacing diaeresis","&uml;","&#168;"}
	,{"©","copyright","&copy;","&#169;"}
	,{"ª","angle quotation mark (left)","&laquo;","&#171;"}
	,{"ª","feminine ordinal indicator","&ordf;","&#170;"}
	,{"¬","negation","&not;","&#172;"}
	,{"­","soft hyphen","&shy;","&#173;"}
	,{"®","registered trademark","&reg;","&#174;"}
	,{"¯","spacing macron","&macr;","&#175;"}
	,{"°","degree","&deg;","&#176;"}
	,{"²","superscript 2","&sup2;","&#178;"}
	,{"³","superscript 3","&sup3;","&#179;"}
	,{"´","spacing acute","&acute;","&#180;"}
	,{"µ","micro","&micro;","&#181;"}
	,{"¶","paragraph","&para;","&#182;"}
	,{"·","middle dot","&middot;","&#183;"}
	,{"¸","spacing cedilla","&cedil;","&#184;"}
	,{"¹","superscript 1","&sup1;","&#185;"}
	,{"º","masculine ordinal indicator","&ordm;","&#186;"}
	,{"»","angle quotation mark (right)","&raquo;","&#187;"}
	,{"¼","fraction 1/4","&frac14;","&#188;"}
	,{"½","fraction 1/2","&frac12;","&#189;"}
	,{"¾","fraction 3/4","&frac34;","&#190;"}
	,{"¿","inverted question mark","&iquest;","&#191;"}
	,{"ß","small sharp s, German","&szlig;","&#223;"}
	,{"ˆ","modifier letter circumflex accent","&circ;","&#710;"}
	,{"˜","small tilde","&tilde;","&#732;"}
	,{"™","trademark","&trade;","&#8482;"}
	,{"–","en dash","&ndash;","&#8211;"}
	,{"—","em dash","&mdash;","&#8212;"}
	,{"‘","left single quotation mark","&lsquo;","&#8216;"}
	,{"’","right single quotation mark","&rsquo;","&#8217;"}
	,{"‚","single low-9 quotation mark","&sbquo;","&#8218;"}
	,{"“","left double quotation mark","&ldquo;","&#8220;"}
	,{"”","right double quotation mark","&rdquo;","&#8221;"}
	,{"„","double low-9 quotation mark","&bdquo;","&#8222;"}
	,{"‹","single left-pointing angle quotation","&lsaquo;","&#8249;"}
	,{"›","single right-pointing angle quotation","&rsaquo;","&#8250;"}
	,{"†","dagger","&dagger;","&#8224;"}
	,{"‡","double dagger","&Dagger;","&#8225;"}
	,{"…","horizontal ellipsis","&hellip;","&#8230;"}
	,{"‰","per mille&nbsp;","&permil;","&#8240;"}
	,{"€","euro","&euro;","&#8364;"}
	,{" ","en space","&ensp;","&#8194;"}
	,{" ","em space","&emsp;","&#8195;"}
	,{" ","thin space","&thinsp;","&#8201;"}
	,{"‌ ","zero width non-joiner","&zwnj;","&#8204;"}
	,{"‍ ","zero width joiner","&zwj;","&#8205;"}
	,{"‎ ","left-to-right mark","&lrm;","&#8206;"}
	,{"‏ ","right-to-left mark","&rlm;","&#8207;"}
	,{"′","","&prime;"}
	,{"″","","&Prime;"}
	,{"‾","","&oline;"}
	,{"ƒ","","&fnof;"}

	// ISO-8859-2
	,{"Ă","","&Abreve;"}
	,{"ă","","&abreve;"}
	,{"Ą","","&Aogon;"}
	,{"ą","","&aogon;"}
	,{"Ć","","&Cacute;"}
	,{"ć","","&cacute;"}
	,{"Č","","&Ccaron;"}
	,{"č","","&ccaron;"}
	,{"Ď","","&Dcaron;"}
	,{"ď","","&dcaron;"}
	,{"Đ","","&Dstrok;"}
	,{"đ","","&dstrok;"}
	,{"Ę","","&Eogon;"}
	,{"ę","","&eogon;"}
	,{"Ě","","&Ecaron;"}
	,{"ě","","&ecaron;"}
	,{"Ĺ","","&Lacute;"}
	,{"ĺ","","&lacute;"}
	,{"Ľ","","&Lcaron;"}
	,{"ľ","","&lcaron;"}
	,{"Ł","","&Lstrok;"}
	,{"ł","","&lstrok;"}
	,{"Ń","","&Nacute;"}
	,{"ń","","&nacute;"}
	,{"Ň","","&Ncaron;"}
	,{"ň","","&ncaron;"}
	,{"Ő","","&Odblac;"}
	,{"ő","","&odblac;"}
	,{"Ŕ","","&Racute;"}
	,{"ŕ","","&racute;"}
	,{"Ř","","&Rcaron;"}
	,{"ř","","&rcaron;"}
	,{"Ś","","&Sacute;"}
	,{"ś","","&sacute;"}
	,{"Ş","","&Scedil;"}
	,{"ş","","&scedil;"}
	,{"Ţ","","&Tcedil;"}
	,{"ţ","","&tcedil;"}
	,{"Ť","","&Tcaron;"}
	,{"ť","","&tcaron;"}
	,{"Ů","","&Uring;"}
	,{"ů","","&uring;"}
	,{"Ű","","&Udblac;"}
	,{"ű","","&udblac;"}
	,{"Ź","","&Zacute;"}
	,{"ź","","&zacute;"}
	,{"Ż","","&Zdot;"}
	,{"ż","","&zdot;"}
	,{"Ž","","&Zcaron;"}
	,{"ž","","&zcaron;"}
	,{"ˇ","","&caron;"}
	,{"˘","","&breve;"}
	,{"˙","","&dot;"}
	,{"˛","","&ogon;"}
	,{"˝","","&dblac;"}
	};

	public final static void enclosesMenu(JMenu menu) {
		menu.add(createSubMenu("enc-admiration"));
		menu.add(createSubMenu("enc-curly-bracket"));
		menu.add(createSubMenu("enc-double-quote"));
		menu.add(createSubMenu("enc-ltgt-tag"));
		menu.add(createSubMenu("enc-percent"));
		menu.add(createSubMenu("enc-question"));
		menu.add(createSubMenu("enc-quote"));
		menu.add(createSubMenu("enc-round-bracket"));
		menu.add(createSubMenu("enc-single-quote"));
		menu.add(createSubMenu("enc-square-bracket"));
	}

	public final static void textObjectMenu(JMenu menu) {
		menu.add(createSubMenu("textobjects.select-a-word"));
		menu.add(createSubMenu("textobjects.select-in-word"));
		menu.add(createSubMenu("textobjects.select-a-brace"));
		menu.add(createSubMenu("textobjects.select-in-brace"));
		menu.add(createSubMenu("textobjects.select-a-bracket"));
		menu.add(createSubMenu("textobjects.select-in-bracket"));
		menu.add(createSubMenu("textobjects.select-a-paren"));
		menu.add(createSubMenu("textobjects.select-in-paren"));
		menu.add(createSubMenu("textobjects.select-a-quote"));
		menu.add(createSubMenu("textobjects.select-in-quote"));
		menu.add(createSubMenu("textobjects.select-a-tick"));
		menu.add(createSubMenu("textobjects.select-in-tick"));
		menu.add(createSubMenu("textobjects.select-a-back-tick"));
		menu.add(createSubMenu("textobjects.select-in-back-tick"));
		menu.add(createSubMenu("textobjects.select-a-paragraph"));
		menu.add(createSubMenu("textobjects.select-in-paragraph"));
		menu.add(createSubMenu("textobjects.select-a-angle"));
		menu.add(createSubMenu("textobjects.select-in-angle"));
		menu.add(createSubMenu("textobjects.select-a-comment"));
		menu.add(createSubMenu("textobjects.select-in-comment"));
		menu.add(createSubMenu("textobjects.select-a-sentence"));
		menu.add(createSubMenu("textobjects.select-in-sentence"));
		menu.add(createSubMenu("textobjects.select-a-indent"));
		menu.add(createSubMenu("textobjects.select-in-indent"));
	}

	private static JMenuItem createSubMenu(String title){
		return GUIUtilities.loadMenuItem(jEdit.getAction(title), false);
	}
}
