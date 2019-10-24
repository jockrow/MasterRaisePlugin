package masterraise.tools;

import masterraise.Text;

/**
 * 
 * @author Richard Martínez 2013/06/12
 *
 */
//TODO: optimice with t.replaceAll...
public class MyGeneration extends Text{
	private String prefix = "            <Column p=\"";
	private String suffix = "\">\n              <Properties>\n                <Property k=\"required\" v=\"true\" />\n              </Properties>\n            </Column>";
	private String t = iniSelectedText();
	
	/**
	 * format in My Generation format column xml to My Generation columns properties
	@Sample:
	<Column p="FECHA_FSP" n="Fecha FSP" />
	<Column p="COSTOS_EST_REQUERIMIENTO" n="Costos Estimados para el Requerimiento" />
	<Column p="FECHA_ENTREGA_FSP_EVALUACION" n="Fecha Entrega FSP Evaluación" />

	Change to:
            <Column p="FECHA_FSP" n="Fecha FSP">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
            <Column p="COSTOS_EST_REQUERIMIENTO" n="Costos Estimados para el Requerimiento">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
            <Column p="FECHA_ENTREGA_FSP_EVALUACION" n="Fecha Entrega FSP Evaluación">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
	 */
	public void columnsToProperies(){
		prefix = "            ";
		t=t.replaceAll("(?m)(^)(.*.)(\" />$)", prefix + "$2" + suffix);
		endSelectedText(t);
	}
	
	/**
	 * format csv to My Generation columns properties
	 *@Sample
	 FECHA_EST_COMITE	Fecha Estimada Comité
	 FECHA_REAL_COMITE	Fecha Real Comité
	 FECHA_INI_EST_PRUEBAS_USU	Fecha Inicio Estimada Pruebas de Usuario
	 Change to
            <Column p="FECHA_EST_COMITE" n="Fecha Estimada Comité">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
            <Column p="FECHA_REAL_COMITE" n="Fecha Real Comité">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
            <Column p="FECHA_INI_EST_PRUEBAS_USU" n="Fecha Inicio Estimada Pruebas de Usuario">
              <Properties>
                <Property k="required" v="true" />
              </Properties>
            </Column>
	 **/
	public void csvToProperies(){
		t=t.replaceAll("(?m)(^)(.*.)($)", prefix + "$2" + suffix);
		t=t.replaceAll("\t", "\" n=\"");
		endSelectedText(t);
	}
}
