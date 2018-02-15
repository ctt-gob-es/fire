/**
 * 
 */

$(document)
		.ready(
				function() {
					
					
					var totalRecords;
					var recordsPerPage = 5;
					var recordsToFetch = recordsPerPage;
					var totalPages;
					var currentPage = 1;
					var currentIndex = 0;

					//Consultar los registros
					$.get("processCertRequest.jsp?requestType="
							+ requestTypeCount, function(data) {
						var JSONData = JSON.parse(data);
						totalRecords = JSONData.count;

						if (totalRecords > 0) {
							$("#data").html("");
						} else {
							$("#navDataTable").css({display : 'none'});
							$("#nav_page").hide();
							return;
						}
						totalPages = Math.floor(totalRecords / recordsPerPage);

						if (totalRecords % recordsPerPage != 0) {
							totalPages++;
						}

						if (totalRecords < recordsPerPage) {
							recordsToFetch = totalRecords % recordsPerPage;
						} else {
							recordsToFetch = recordsPerPage;
						}

						$("#page").html("Página " + currentPage + " de " + totalPages);
						
						//En caso de tener registros pintar la tabla
						$.get("processCertRequest.jsp?requestType=" + requestType
								+ "&currentIndex=" + currentIndex
								+ "&recordsToFetch=" + recordsToFetch,
								function(data) {
									var JSONData = JSON.parse(data);

									if (totalRecords > 0) {
										if (requestType == "getRecordsCert") {
											printCertificateTable(JSONData,recordsToFetch);
										} else {
											printApplicationsByCertificateTable(JSONData, recordsToFetch);
										}
									}

									if (currentPage == totalPages) {
										$("#next").hide();
									} else {
										$("#next").show();
									}

									if (currentPage == 1) {
										$("#back").hide();
									} else {
										$("#back").show();
									}

								});
						
						

					});

					

					$("#next").click(function() {
						$("#data").html("");
						currentPage++;

						if (currentPage == totalPages) {
							$("#next").hide();
							if (totalRecords % recordsPerPage != 0) {
								recordsToFetch = totalRecords% recordsPerPage;
							} else {
								recordsToFetch = recordsPerPage;
							}
						} else {
							$("#next").show();
							recordsToFetch = recordsPerPage;
						}

										if (currentPage == 1) {
											$("#back").hide();
										} else {
											$("#back").show();
										}

										$.get("processCertRequest.jsp?requestType="+ requestType+ "&currentIndex="+ currentIndex+ "&recordsToFetch="+ recordsToFetch,
												function(data) {
													var JSONData = JSON.parse(data);
													if (totalRecords > 0) {
														if (requestType == "getRecordsCert") {
															printCertificateTable(JSONData,recordsToFetch);
														} else {
															printApplicationsByCertificateTable(JSONData,recordsToFetch);
														}
													}
										});
										$("#page").html("Página " + currentPage
														+ " de " + totalPages);

									});

					$("#back")
							.click(
									function() {
										$("#data").html("");
										currentPage--;
										currentIndex = currentIndex
												- recordsToFetch
												- recordsPerPage;

										if (currentPage == totalPages) {
											$("#next").hide();
											recordsToFetch = totalRecords
													% recordsPerPage;
										} else {
											$("#next").show();
											recordsToFetch = recordsPerPage;
										}

										if (currentPage == 1) {
											$("#back").hide();
										} else {
											$("#back").show();
										}

										$
												.get(
														"processCertRequest.jsp?requestType="
																+ requestType
																+ "&currentIndex="
																+ currentIndex
																+ "&recordsToFetch="
																+ recordsToFetch,
														function(data) {
															var JSONData = JSON
																	.parse(data);
															if (totalRecords > 0) {
																if (requestType == "getRecordsCert") {
																	printCertificateTable(
																			JSONData,
																			recordsToFetch);
																} else {
																	printApplicationsByCertificateTable(
																			JSONData,
																			recordsToFetch);
																}
															}
														});

										$("#page").html(
												"Página " + currentPage
														+ " de " + totalPages);

									});

					function printCertificateTable(JSONData, recordsToFetch) {

						var htmlTableHead = "<table class='admin-table'>"
								+ "<thead>" + "<tr>" + "<td>Nombre</td>"
								+ "<td>Certificado 1</td>"
								+ "<td>Certificado 2</td>"
								+ "<td>Fecha Alta</td>" + "<td>Acciones</td>"
								+ "</tr>" + "</thead><tbody>";
						var htmlTableBody = "";
						var htmlTableFoot = "</tbody></table>";

						for (i = 0; i < recordsToFetch; ++i) {
							htmlTableBody = htmlTableBody
									+ "<tr><td>"
									+ dataUndefined(JSONData.CertList[i].nombre_cert)
									+ "</td>";
							htmlTableBody = htmlTableBody
									+ "<td>"
									+ dataUndefined(JSONData.CertList[i].cert_principal)
									+ "</td>";
							htmlTableBody = htmlTableBody
									+ "<td>"
									+ dataUndefined(JSONData.CertList[i].cert_backup)
									+ "</td>";

							fecAlta = new Date(JSONData.CertList[i].fec_alta);
							htmlTableBody = htmlTableBody + "<td>"
									+ convertDateFormat(fecAlta) + "</td>";

							htmlTableBody = htmlTableBody + "<td>";
							htmlTableBody = htmlTableBody
									+ "<a href='NewCertificate.jsp?id-cert="
									+ JSONData.CertList[i].id_certificado
									+ "&op=0&nombre-cert="
									+ JSONData.CertList[i].nombre_cert
									+ "'><img src='../resources/img/details_icon.png'/></a>";
							htmlTableBody = htmlTableBody
									+ "<a href='NewCertificate.jsp?id-cert="
									+ JSONData.CertList[i].id_certificado
									+ "&op=2&nombre-cert="
									+ JSONData.CertList[i].nombre_cert
									+ "'><img src='../resources/img/editar_icon.png'/></a>";
							htmlTableBody = htmlTableBody
									+ "<a href='#'><img src='../resources/img/delete_icon.png' onclick='return confirmar(\""
									+ JSONData.CertList[i].nombre_cert+"\","+JSONData.CertList[i].id_certificado
									+ ",\"../deleteCert?id-cert=\")'/></a>";
							htmlTableBody = htmlTableBody + "</td></tr>";

							currentIndex++;
						}
						$("#data").append(htmlTableHead + htmlTableBody + htmlTableFoot);

					}

					function printApplicationsByCertificateTable(JSONData,
							recordsToFetch) {

						var htmlTableHead = "<table class='admin-table'>" +
								"<thead><tr>" +
									"<td>Aplicaci&oacute;n</td>" +
									"<td>ID</td>" +
									"<td>Responsable</td>" +
									"<td>Fecha Alta</td>" +
								"</thead><tbody>";
						var htmlTableBody = "";
						var htmlTableFoot = "</tbody></table>";

						for (i = 0; i < recordsToFetch; ++i) {
							htmlTableBody = htmlTableBody
									+ "<tr><td>"
									+ dataUndefined(JSONData.AppList[i].nombre)
									+ "</td><td>"
									+ dataUndefined(JSONData.AppList[i].id)
									+ "</td><td>"
									+ dataUndefined(JSONData.AppList[i].responsable)
									+ "<br>";

							if (JSONData.AppList[i].correo != null
									&& dataUndefined(JSONData.AppList[i].correo) != ""
									&& JSONData.AppList[i].correo != "") {
								htmlTableBody = htmlTableBody
										+ "<a href='mailto://"
										+ JSONData.AppList[i].correo + "'>"
										+ JSONData.AppList[i].correo + "</a>";
							}
							if (JSONData.AppList[i].telefono != null
									&& dataUndefined(JSONData.AppList[i].telefono) != ""
									&& JSONData.AppList[i].telefono != "") {
								htmlTableBody = htmlTableBody
										+ "(<a href='tel://"
										+ JSONData.AppList[i].telefono + "'>"
										+ JSONData.AppList[i].telefono
										+ "</a>)";
							}
							fecAlta = new Date(JSONData.AppList[i].alta);
							htmlTableBody = htmlTableBody + "</td><td>"
									+ convertDateFormat(fecAlta) + "</td>";
							htmlTableBody = htmlTableBody + "</tr>";

							currentIndex++;
						}
						$("#data").append(htmlTableHead + htmlTableBody + htmlTableFoot);

					}

					function convertDateFormat(date) {
						var dd = date.getDate();
						var mm = date.getMonth() + 1;
						var yyyy = date.getFullYear();
						if (dd < 10) {
							dd = '0' + dd;
						}
						if (mm < 10) {
							mm = '0' + mm;
						}
						return dd + "/" + mm + "/" + yyyy;
					}

					function dataUndefined(data) {
						if (typeof data === "undefined") {
							return "";
						} else {
							return data;
						}
					}
				});

	function confirmar(nombreCert, idCert, url) {	
	
		$.get("processCertRequest.jsp?requestType=countRecordsCertApp&id-cert="+idCert ,function(data){			
			var JSONData = JSON.parse(data);
			totalRecords = JSONData.count;

			if (totalRecords > 0) {			
				alert("Error al dar de baja el certificado '"+nombreCert+"', tiene asociadas aplicaciones.");
			} else {
				if (confirm('¿Está seguro de eliminar el certificado '+ nombreCert + '?')) {
					document.location.href=url+idCert;
					return true;
				}
			}
		});		
	}
