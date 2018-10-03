<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
        
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Prueba gestion de logs</title>
</head>
<body>
	<p>Listar Ficheros</p>
	<form  method="POST" action="Logservice?op=3">
		<input id="submit-btn" type="submit" value="Listar Ficheros Logs"/>
	</form>
	<br>
	<p>Abrir fichero Log</p>
	<form  method="POST" action="Logservice?op=4">
		<input type="text" id="fname" name="fname" value="logging_api.log" />
		<input id="submit-btn" type="submit" value="Abrir fichero"/>
	</form>
	<br>
	<p>Visualizar las n últimas líneas</p>
	<form  method="POST" action="Logservice?op=6">
		<input type="text" id="fname" name="fname" value="logging_api.log" />
		<input type="number" id="nlines" name="nlines" value="20" />
		<input id="submit-btn" type="submit" value="Tail"/>
	</form>
		<br>
	<p>Visualizar las n  líneas</p>
	<form  method="POST" action="Logservice?op=7">
		<input type="text" id="fname" name="fname" value="logging_api.log" />
		<input type="number" id="nlines" name="nlines" value="20" />
		<input id="submit-btn" type="submit" value="More"/>
	</form>
	
	<p>Buscar texto n  líneas</p>
	<form  method="POST" action="Logservice?op=8">
		<input type="text" id="fname" name="fname" value="logging_api.log" />
		<input type="number" id="nlines" name="nlines" value="20" />
		<input type="text" id="search_txt" name="search_txt"  />
		<input type="text" id="search_date" name="search_date"  />
		<input id="submit-btn" type="submit" value="More"/>
	</form>
</body>
</html>