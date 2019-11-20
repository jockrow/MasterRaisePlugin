package test;

import static org.junit.Assert.assertEquals;

import java.util.ConcurrentModificationException;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.junit.Before;
import org.junit.Test;

import masterraise.Constants;
import masterraise.files.MrFile;
import masterraise.tools.Query;
import masterraise.tools.Query.ConvertQuery;

/*
	SELECT_UPDATE	SELECT_INSERT	INSERT_SELECT	INSERT_UPDATE	INSERT_CSV	UPDATE_INSERT	UPDATE_SELECT	UPDATE_CSV	CSV_INSERT	CSV_SELECT	CSV_UPDATE
convert	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok
putWhere	ok	ok	-	-	-	-	ok		-	-	-
whereIn	ok	ok	-	-	-				-	-	-
beauty	ok	ok	ok		-
return	-	-	ok	ok	ok
comments	ok	ok

SELECT ESTADO_FINCA, 'ACT'
, CN_USUARIO_MODIFICACION, 'SICA_USER'
, FECHA_MODIFICACION, SYSDATE
, Fecha_Inactivacion, TO_DATE('31/01/2016', 'dd/mm/yyyy')
, Area_Cultivo, 6,24
FROM SICA.SC_FINCA
WHERE CODIGO_SICA ='1700100378'
ORDER BY ESTADO_FINCA
;

SC_FINCA
FIELD	VALUE
CODIGO_SICA	'1700100378'
ESTADO_FINCA	'ACT'
CN_USUARIO_MODIFICACION	'SICA_USER'
FECHA_MODIFICACION	SYSDATE
Fecha_Inactivacion	TO_DATE('31/01/2016','dd/mm/yyyy')
Area_Cultivo	6,24

INSERT INTO SICA.SC_FINCA (CODIGO_SICA, ESTADO_FINCA, CN_USUARIO_MODIFICACION, FECHA_MODIFICACION, Fecha_Inactivacion, Area_Cultivo)
VALUES ('1700100378', 'ACT', 'SICA_USER', SYSDATE, TO_DATE('31/01/2016', 'dd/mm/yyyy'), 6,24);

UPDATE SICA.SC_FINCA
SET CODIGO_SICA = '1700100378'
, ESTADO_FINCA = 'ACT'
, CN_USUARIO_MODIFICACION = 'SICA_USER'
, FECHA_MODIFICACION = SYSDATE
, Fecha_Inactivacion = TO_DATE('31/01/2016','dd/mm/yyyy')
, Area_Cultivo = 6,24
WHERE CODIGO_SICA = '2529300114';
*/
public class QueryTest extends Constants{
	private String contentTo = "";
	private String convertedQuery = "";
	private View view = jEdit.getActiveView();

	@Before
	public void setUp() throws Exception {
//		view = jEdit.getActiveView();
//		textArea = view.getTextArea();
		
//		System.out.println("...setUp.isMainThread:" + jEdit.isMainThread());
//		System.out.println("...setUp.length:" + jEdit.getViews().length);
//		System.out.println("...setUp.view:" + view);
//		System.out.println("...setUp.Count:" + jEdit.getViewCount());
//		System.out.println("...setUp.lastView:" + jEdit.getLastView());
//		System.out.println(String.format("file.encoding: %s", System.getProperty("file.encoding")));
	}

//	@After
//	public void tearDown() throws Exception {
//		System.out.println("...tearDown");
//	}

	@Test //DONE
	public void convertUpdateToSelect() {
		setVars("UPDATE", "SELECT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}
	
//	@Test //TODO
//	public void convertSelectToUpdate() {
//		setVars("SELECT", "UPDATE");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}

	private void setVars(String query1, String query2) {
		String query1Path = PATH + "from/" + query1 + ".sql";
//		String query2Path = PATH + "to/" + query2 + ".sql";
		String query2Path = PATH + "converted/" + query1 + "_" + query2 + ".sql";
		MrFile mf = new MrFile();
		contentTo = mf.readFile(query2Path);
		
		try {
			view.setFocusable(true);
			view.getMousePosition(true);
			jEdit.openFile(view, query1Path);
			ConvertQuery cq = new Query().new ConvertQuery(query1, query2);
			view.repaint();
			convertedQuery = cq.processText();
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
			Macros.error(view, MSG_ERROR);
		}
	}
}
