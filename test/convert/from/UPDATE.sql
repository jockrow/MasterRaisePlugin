	UPDATE SICA.SC_FINCA
	/*
	comentario1
	*/
	SET CODIGO_SICA = '1700100378'
	, ESTADO_FINCA = 'ACT'
	, CN_USUARIO_MODIFICACION = 'SICA_USER'
	--comentario2
	, FECHA_MODIFICACION = SYSDATE
	, Fecha_Inactivacion = TO_DATE('31/01/2016','dd/mm/yyyy')
	, Area_Cultivo = 6,24
	WHERE CODIGO_SICA = '2529300114';