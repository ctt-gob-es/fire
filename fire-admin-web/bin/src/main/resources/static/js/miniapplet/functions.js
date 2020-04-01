
function cargaMiniApplet(pathToSignFolder) {
	var userUrl = window.location.href;
	userUrl = userUrl.substring(0, userUrl.lastIndexOf('/'));	
	urlBaseMonitoriza = userUrl + "/"+pathToSignFolder;
	
	if(typeof MiniApplet == 'undefined'){
		alert('Cargando componentes...');
	}
	MiniApplet.setForceWSMode(false);
	MiniApplet.cargarMiniApplet(urlBaseMonitoriza+'../static/js/miniapplet/');
	MiniApplet.setServlets(urlBaseMonitoriza+"/StorageService", urlBaseMonitoriza+"/RetrieveService");
}



///////////////////////
//// AUTENTICACION ////
///////////////////////


function showErrorCallback (errorType, errorMessage) {
	if(errorType=='es.gob.afirma.core.AOCancelledOperationException'){
		
	} else if (errorType=='java.io.FileNotFoundException') {
		alert('Error: No se ha seleccionado un fichero de datos válido');
	} else if (errorType=='es.gob.afirma.keystores.AOCertificatesNotFoundException') {
		alert('Error: No se ha encontrado ningún certificado de firma válido');
	} else if(errorType=='es.gob.afirma.keystores.main.common.AOKeystoreAlternativeException') {
		alert('La contraseña indicada no es correcta');
	} else if(errorType=='java.lang.IllegalStateException') {
		alert('La contraseña indicada no es correcta');
	} else {
		alert('Error: Se produjo un error durante la operación de firma: ' + errorType + ' - ' + errorMessage);
	}
	setTimeout(function(){unblockScreen();},1000);	
}

function saveSignatureCallback(signatureB64) {
	var certificate = signatureB64;
	eval("autenticar(certificate);");
}


function showLogCallback(errorType, errorMessage) {
	showLog("Type: " + errorType + "\nMessage: " + errorMessage);
}

function authenticate(sucessCallback, filterCert) {	
	try{		
		var algorithm = "SHA1withRSA";
		var format = "CAdES";
		var params = 	"mode=implicit\n";
		
		if (filterCert != null && filterCert != "") {
			params += filterCert;
		}
		
		var dataToSignBase64 = MiniApplet.getBase64FromText("session");
		
		MiniApplet.sign(dataToSignBase64, algorithm, format, params, saveSignatureCallback,showErrorCallback);
	} catch(e) {
		showErrorCallback(MiniApplet.getErrorType(), MiniApplet.getErrorMessage());
		setTimeout(function(){unblockScreen();},1000);
	}
}
