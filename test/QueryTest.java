package test;

import static org.junit.Assert.assertEquals;

import java.util.ConcurrentModificationException;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.junit.Before;
import org.junit.Test;

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
public class QueryTest extends Tester{
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
	public void convertSelectToUpdate() {
		setVars("SELECT", "UPDATE");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

//	@Test //TODO
//	public void convertSelectToInsert() {
//		setVars("SELECT", "INSERT");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertInsertToSelect() {
//		setVars("INSERT", "SELECT");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertInsertToUpdate() {
//		setVars("INSERT", "UPDATE");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertInsertToCsv() {
//		setVars("INSERT", "CSV");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertUpdateToInsert() {
//		setVars("UPDATE", "INSERT");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}

	@Test //DONE
	public void convertUpdateToSelect() {
		setVars("UPDATE", "SELECT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}
	
//	@Test //TODO
//	public void convertUpdateToCsv() {
//		setVars("UPDATE", "CSV");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertCsvToInsert() {
//		setVars("CSV", "INSERT");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertCsvToSelect() {
//		setVars("CSV", "SELECT");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}
//
//	@Test //TODO
//	public void convertCsvToUpdate() {
//		setVars("CSV", "UPDATE");
//		assertEquals(contentTo.trim(), convertedQuery.trim());
//	}

	private void setVars(String query1, String query2) {
		String fromFolder = PATH + "from/" + query1 + ".sql";
		String convertedFolder = PATH + "converted/" + query1 + "_" + query2 + ".sql";
		contentTo = new MrFile().readFile(convertedFolder);
		
		try {
			view.setFocusable(true);
			view.getMousePosition(true);
			jEdit.openFile(view, fromFolder);
			ConvertQuery cq = new Query().new ConvertQuery(query1, query2);
			view.repaint();
			convertedQuery = cq.processText();
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
			Macros.error(view, MSG_ERROR);
		}
	}
}
