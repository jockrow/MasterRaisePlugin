package test;

import static org.junit.Assert.assertEquals;

import org.gjt.sp.jedit.jEdit;
import org.junit.Test;

import masterraise.files.MrFile;
import masterraise.tools.Query;
import masterraise.tools.Query.BeautyQuery;
import masterraise.tools.Query.ConvertQuery;
/*
	SELECT_UPDATE	SELECT_INSERT	INSERT_SELECT	INSERT_UPDATE	INSERT_CSV	UPDATE_INSERT	UPDATE_SELECT	UPDATE_CSV	CSV_INSERT	CSV_SELECT	CSV_UPDATE
comments	ok	ok	ok	ok	ok	ok	ok	ok	-	-	-
convert	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok
beauty	ok	ok	ok	ok	-	ok	ok	-	ok	ok	ok
putWhere	ok		-	-	-	-	ok	-	-	-	-
whereIn			-	-	-	-	x	-	-	-	-

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
public class QueryTest extends Tester {
	@Test
	public void convertSelectToUpdate() {
		setVars(CONVERT, "SELECT", "UPDATE");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertSelectToInsert() {
		setVars(CONVERT, "SELECT", "INSERT");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertInsertToSelect() {
		setVars(CONVERT, "INSERT", "SELECT");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertInsertToUpdate() {
		setVars(CONVERT, "INSERT", "UPDATE");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertInsertToCsv() {
		setVars(CONVERT, "INSERT", "CSV");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertUpdateToInsert() {
		setVars(CONVERT, "UPDATE", "INSERT");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertUpdateToSelect() {
		setVars(CONVERT, "UPDATE", "SELECT");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertUpdateToCsv() {
		setVars(CONVERT, "UPDATE", "CSV");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertCsvToInsert() {
		setVars(CONVERT, "CSV", "INSERT");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertCsvToSelect() {
		setVars(CONVERT, "CSV", "SELECT JOIN");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void convertCsvToUpdate() {
		setVars(CONVERT, "CSV", "UPDATE");
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void sqlServerGetTmpTables() {
		setVars("sqlServerGetTmpTables");
		actual = new Query().sqlServerGetTmpTables();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void sqlServerSetVariablesSp() {
		setVars("sqlServerSetVariablesSp");
		actual = new Query().sqlServerSetVariablesSp();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void beautyQuery() {
		setVars("beautyQuery");
		BeautyQuery bt = new Query(). new BeautyQuery("");
		actual = bt.processText();
		assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void queryToLanguage() {
		setVars("queryToLanguage");
		actual = new Query().queryToLanguage();
		assertEquals(expected.trim(), actual.trim());
	}

	private void setVars(String convertType, String query1, String query2) {
		String path = TEST_PATH + convertType + "/";
		String fileOriginal = path + "from/" + query1 + ".sql";
		String fileConverted = path + "converted/" + query1 + "_" + query2 + ".sql";
		expected = new MrFile().readFile(fileConverted);
		String convertion = query1 + "_" + query2;

		if(convertion.equals("CSV_SELECT JOIN")) {
			fileOriginal = path + "from/CSV_SELECT_JOIN.sql";
		}

		try {
			jEdit.openFile(view, fileOriginal);
			ConvertQuery cq = new Query().new ConvertQuery(query1, query2);
			actual = cq.processText();
		} catch (Exception e) {
			e.printStackTrace();
			setVars(convertType, query1, query2);
		}
	}
}
