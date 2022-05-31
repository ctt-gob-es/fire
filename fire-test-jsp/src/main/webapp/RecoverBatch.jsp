<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Prueba FIRe</title>
		<link rel="shortcut icon" href="img/cert.png">
		<link rel="stylesheet" href="styles/styles.css"/>
		<script type="text/javascript">
		
			var loaded = false;
		
			/** Recupera el resultado de la operaci&oacute;n de firma. */
			function recoverResult() {
				
				// Solicitamos de forma asincrona la carga de los datos
				var httpRequest = getHttpRequest();
				httpRequest.open("GET", "RecoverBatchService", true);
				httpRequest.onreadystatechange = function() {
					if (httpRequest.readyState == 4 && httpRequest.status == 200) {
						// Marcamos la pagina como cargada para detener las consultas de estado
						loaded = true;
						// Agregamos los datos a la tabla de resultados
						showResults(JSON.parse(httpRequest.responseText));
						// Eliminamos el dialogo de espera si se esta mostrando
						hideProgress();
					}
				}
				httpRequest.onerror = function(e) {
					window.location = window.location.substring(0, window.location.indexOf("RecoverBatch.jsp")) +
							"ErrorPage.jsp?msg=" + encodeURIComponent(e);
				}
				httpRequest.send();
			}
			
			/** Recupera el progreso del lote de firma. */
			function recoverResultState() {
				// Si se termino la carga, ocultamos el dialogo de progreso
				if (loaded) {
					hideProgress();
					return;
				}

				// Llamamos a la funcion de recuperacion de estado
				var httpRequest = getHttpRequest();
				httpRequest.open("GET", "RecoverBatchStateService", true);
				httpRequest.onreadystatechange = function() {
					if (httpRequest.readyState == 4 && httpRequest.status == 200) {
						if (!loaded) {
							// Actualizamos el dialogo de progreso
							showProgress(httpRequest.responseText);
							// Repetimos la consulta con un cierto retardo
							setTimeout(recoverResultState, 2000);
						}
					}
				}
				httpRequest.onerror = function(e) {
					showProgress("Error: " + e);
				}
				httpRequest.send();
			}
			
			/** Muestra y actualiza el dialogo de progreso. */
			function showProgress(progress) {
				document.getElementById("progressText").innerHTML = "Ejecutando firma: " + progress + "%"; 
				document.getElementById("progressDialog").style.display = "block";
			}

			/** Oculta el dialogo de progreso. */
			function hideProgress() {
				document.getElementById("progressDialog").style.display = "none";
			}
			
			/** Agrega a la tabla de resultados los resultados del lote. */
			function showResults(results) {
				
				var htmlResult = "";
				for (var i = 0; i < results.batch.length; i++) {
					htmlResult += "<tr>";
					htmlResult += "<td>" + results.batch[i].id + "</td>";
					htmlResult += "<td>" + results.batch[i].ok + "</td>";
					htmlResult += "<td>";
					if (results.batch[i].gp) {
						htmlResult += "ID periodo de gracia: " + results.batch[i].gp.id +
									  "<br>Fecha estimada: " + new Date(parseInt(results.batch[i].gp.dt));	
					}
					else {
						htmlResult += results.batch[i].dt;
					}
					htmlResult += "</td>";
					htmlResult += "<td>";
					if (results.batch[i].ok == "true" && !results.batch[i].gp) {
						htmlResult += "<a target=\"_blank\" href=\"RecoverBatchSign.jsp?docid=" + results.batch[i].id + "\">Recuperar firma</a>";
					}
					htmlResult += "</td>";
					htmlResult += "</tr>";
				}
				
				document.getElementById("table-results-body").innerHTML = htmlResult;
			}
			
			/** Cargamos el objeto con el que hacer las peticiones remotas. */
			function getHttpRequest() {
	            var xmlHttp = null;
	            if (typeof XMLHttpRequest != "undefined") {	// Navegadores actuales
	            	xmlHttp = new window.XMLHttpRequest;
	            } else if (typeof window.ActiveXObject != "undefined") {	// Internet Explorer antiguos
	                try {
	                    xmlHttp = new ActiveXObject("Msxml2.XMLHTTP.4.0");
	                } catch (e) {
	                    try {
	                        xmlHttp = new ActiveXObject("MSXML2.XMLHTTP");
	                    } catch (e) {
	                        try {
	                            xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
	                        } catch (e) {
	                            xmlHttp = null;
	                        }
	                    }
	                }
	            }
	            return xmlHttp;
			}
			
		</script>
	</head>
	<body style=" font-weight: 300;">

		<%
			if (session.getAttribute("user") == null) { //$NON-NLS-1$
				response.sendRedirect("Login.jsp"); //$NON-NLS-1$
				return;
			}
		%>

		<div id="menubar">
			<div id="bar-txt"><b>Prueba FIRe</b></div>
		</div>

		<div id="sign-container">
			<h1 style="color:#303030;">RESULTADO DE LA FIRMA DEL LOTE</h1>

			<div style="display:inline-block;"></div>

			<noscript>
				<div>Su navegador web tiene JavaScript desactivado. Esta pagina no funcionara correctamente</div>
				<div style="display:inline-block;"></div>
			</noscript>

			<table class="batch-results">
				<tr>
					<th class="doc-column">Documento</th>
					<th class="result-column">Resultado</th>
					<th class="details-column">Detalle</th>
					<th class="recover-button-column">Firma</th>
				</tr>
				<tbody id="table-results-body">
				
				</tbody>
			</table>

			<form method="POST" action="Login.jsp">
				<div style="margin-top:30px;text-align: center; ">
					<label for="submit-btn">Pulse el bot&oacute;n para realizar una nueva firma</label><br><br>
					<input  id="submit-btn"  type="submit" value="NUEVA FIRMA">
				</div>
			</form>
		</div>
	
	<div id="progressDialog" style="display: none;background-color: rgba(50,50,50,0.3); width: 100%; height: 100%; z-index: 9990; position: fixed; left: 0; top: 0; ">
		<div style="background-color: white; position: fixed; top: 30%; left: 35%; width: 30%; padding: 20px; border: 2px solid black; border-radius: 15px;">
			<span style="text-align: center; font-weight: bold; font-size: 20pt;" id="progressText"></span>
		</div>
	</div>
	
	<script type="text/javascript">
		// Mostramos el dialogo de carga
		showProgress("0");
		// Iniciamos de inmediato el proceso de recuperacion del resultado
		recoverResult();
		// Al cabo de 1 segundo, consultamos el estado de la operacion
		setTimeout(recoverResultState, 1000);
	</script>
	</body>
</html>