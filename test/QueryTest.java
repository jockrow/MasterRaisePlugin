package test;

import static org.junit.Assert.assertEquals;

import java.util.ConcurrentModificationException;

import org.gjt.sp.jedit.jEdit;
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
	private static final String PATH = TEST_PATH + "convert/";
	String convertion = "";

	@Test
	public void convertSelectToUpdate() {
		setVars("SELECT", "UPDATE");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertSelectToInsert() {
		setVars("SELECT", "INSERT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertInsertToSelect() {
		setVars("INSERT", "SELECT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertInsertToUpdate() {
		setVars("INSERT", "UPDATE");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertInsertToCsv() {
		setVars("INSERT", "CSV");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertUpdateToInsert() {
		setVars("UPDATE", "INSERT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertUpdateToSelect() {
		setVars("UPDATE", "SELECT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertUpdateToCsv() {
		setVars("UPDATE", "CSV");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertCsvToInsert() {
		setVars("CSV", "INSERT");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertCsvToSelect() {
		setVars("CSV", "SELECT JOIN");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	@Test
	public void convertCsvToUpdate() {
		setVars("CSV", "UPDATE");
		assertEquals(contentTo.trim(), convertedQuery.trim());
	}

	private void executeAgain(String query1, String query2) {
		switch(convertion) {
		case "SELECT_UPDATE":
			convertSelectToUpdate();
			break;
		case "SELECT_INSERT":
			convertSelectToInsert();
			break;
		case "INSERT_SELECT":
			convertInsertToSelect();
			break;
		case "INSERT_UPDATE":
			convertInsertToUpdate();
			break;
		case "INSERT_CSV":
			convertInsertToCsv();
			break;
		case "UPDATE_INSERT":
			convertUpdateToInsert();
			break;
		case "UPDATE_SELECT":
			convertUpdateToSelect();
			break;
		case "UPDATE_CSV":
			convertUpdateToCsv();
			break;
		case "CSV_INSERT":
			convertCsvToInsert();
			break;
		case "CSV_SELECT":
			convertCsvToSelect();
			break;
		case "CSV_UPDATE":
			convertCsvToUpdate();
			break;
		}
	}

	private void setVars(String query1, String query2) {
		String fromFolder = PATH + "from/" + query1 + ".sql";
		String convertedFolder = PATH + "converted/" + query1 + "_" + query2 + ".sql";
		contentTo = new MrFile().readFile(convertedFolder);
		convertion = query1 + "_" + query2;

		if(convertion.equals("CSV_SELECT JOIN")) {
			fromFolder = PATH + "from/CSV_SELECT_JOIN.sql";
		}

		try {
			jEdit.openFile(view, fromFolder);
			ConvertQuery cq = new Query().new ConvertQuery(query1, query2);
			convertedQuery = cq.processText();
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
			executeAgain(query1, query2); 
		} catch (Exception e) {
			e.printStackTrace();
			executeAgain(query1, query2);
		}
	}
}
