
function cargaAppAfirma(pathToSignFolder) {
	var userUrl = window.location.href;
	userUrl = userUrl.substring(0, userUrl.lastIndexOf('/'));	
	urlBaseAfirma = userUrl + "/"+pathToSignFolder;
	
	if(typeof AutoScript == 'undefined'){
		alert('Cargando componentes...');
	}
	AutoScript.setForceWSMode(false);
	AutoScript.cargarAppAfirma(urlBaseAfirma+'../static/js/miniapplet/');
	AutoScript.setServlets(urlBaseAfirma+"/StorageService", urlBaseAfirma+"/RetrieveService");
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
	setTimeout(function(){unblockScreen();},10000);	
}

function saveSignatureCallback(signatureB64) {
	var signature = signatureB64;
	eval("autenticar(signature);");
}


function showLogCallback(errorType, errorMessage) {
	showLog("Type: " + errorType + "\nMessage: " + errorMessage);
}

function authenticate(sucessCallback, filterCert, randomString) {	
	try{		
		var algorithm = "SHA256withRSA";
		var format = "CAdES";
		var params = 	"mode=implicit\n";
		
		if (filterCert != null && filterCert != "") {
			params += filterCert;
		}				

		var dataToSignBase64 = AutoScript.getBase64FromText(randomString);
		
		AutoScript.sign(dataToSignBase64, algorithm, format, params, saveSignatureCallback,showErrorCallback);
	} catch(e) {
		showErrorCallback(AutoScript.getErrorType(), AutoScript.getErrorMessage());
		setTimeout(function(){unblockScreen();},10000);
	}
}


