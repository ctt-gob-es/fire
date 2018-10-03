/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.clavefirma.trustManagerClassName;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @author mario
 * Clase encargada del trust manager. Deja pasar cualquier certificado para que sea la aplicación quien decida si es v&aacute;lido o no
 */
public class TrustManagerClass implements X509TrustManager
{

  /**
   * Constructor vac&iacute;o
   */
  public TrustManagerClass(){
	  // vacio
  }

  @Override
public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
	  // Aceptamos todas las conexiones
  }

  @Override
public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
	  // Aceptamos todas las conexiones
  }

  @Override
public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
