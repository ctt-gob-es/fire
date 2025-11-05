
function cargaAppAfirma() {
	AutoScript.cargarAppAfirma();
}



///////////////////////
//// AUTENTICACION ////
///////////////////////

function authenticate(randomString, filterCert, sucessCallback, errorCallback) {
	var algorithm = "SHA256withRSA";
	var format = "CAdES";
	var params = 	"mode=implicit\n";
	
	if (filterCert != null && filterCert != "") {
		params += filterCert;
	}				
	
	var dataToSignBase64 = AutoScript.getBase64FromText(randomString);
	
	AutoScript.sign(dataToSignBase64, algorithm, format, params, sucessCallback, errorCallback);
}

