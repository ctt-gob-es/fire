# FIRe
FIRe es una solución que simplifica la integración de procesos de firma electrónica de usuario en trámites web al concentrar en un solo componente todos los requisitos de creación de firmas basadas tanto en certificados locales como en certificados en la nube. 

## Licencia

FIRe es software libre y se publica con licencia [GPL 2+](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html) y [EUPL 1.1](http://ec.europa.eu/idabc/servlets/Docb4f4.pdf). Puede consular más información sobre el producto en el  [Portal de Administración electrónica](https://administracionelectronica.gob.es/ctt/fire).

## Notas de construcción

* La versión aquí publicada de FIRe se distribuye con versiones del MiniApplet @firma 1.6 y AutoFirma WebStart 1.6 firmadas con certificados de prueba. Un despliegue en producción de FIRe debería publicarse con versiones de estos JAR firmadas con certificados de confianza.

* Las bibliotecas de conexión con el sistema de custodia de Cl@ve Firma no se encuentran en repositorios públicos ni se atienen a la licencia de este producto. Para poder realizar un despliegue de FIRe que tenga acceso a Cl@ve Firma será necesario ponerse en contacto con los responsables del proyecto para dar autorización a su aplicación y obtener las bibliotecas necesarias. La dependencia Maven necesaria es:
 * `com.openlandsw.rss:gateway-api:2.4.05`

## Arquitectura

El sistema FIRe está compuesto principalmente por dos elementos:

1. Componente central

   Es el servidor en el que las aplicaciones delegan todas las labores de firma. Está a su vez compuesto por varios subcomponentes que se despliegan en un contenedor de aplicaciones java
   
    - Componente de firma **(fire-signature)**
    - Simulador de Cl@veFirma **(clavefirma-test-services)**
    - Aplicación de administración **(fire-admin-jsp)**
   
2. Cliente distruido

   Está formado por las librerías de integración **(fire-client)** que utilizan las aplicaciones para conectarse al componente central. Las librerías se proporcionan en los lenguajes Java, .Net y PHP.
   
3. Aplicación de prueba

   Aplicación de prueba en lenguaje java **(fire-test-jsp)** que permite demostrar las funcionalidades del sistema y que puede servir de base para desarrollos propios.
   
   
## Release notes

### v2.1

#### Componente central
  
 - [RFE] Se introduce la posibilidad de introducir y configurar un gestor de documentos que le indique a las operaciones de firma y lotes de donde tomar y donde guardar los datos en lugar de recibirlos directamente.
 - [RFE] Se adminte el uso del formato "NONE" que permite realizar una firma digital PKCS#1 sobre los datos indicados.
 - [RFE] Se actualiza el MiniApplet y AutoFirma WebStart a las versiones 1.6.
 - [RFE] Actualización de los servicios trifásicos con los cambios del Cliente @firma 1.6.
 - [RFE] Se incluye un nuevo tipo de error para detectar cuando se produce un fallo después de llamar a la pasarela de autorización o emisión de certificado.
 - [RFE] Se crea un mecanismo para la gestión compartida de sesiones a través de disco por varios nodos balanceados (necesario cuando se despliega el componente central en varios servidores balanceados sin clúster).
 - [RFE] Mejora en la gestión del borrado de temporales.
 - [RFE] Se comprueba el formato longevo al que se actualizado una firma para identificar si la mejora no fue completa con respecto a lo que se solicitó.
 - [BUG] Corrección del error en el tratamiento de los nombres y título de los documentos enviados a firmar cuando tienen caracteres no ANSI básicos (tíldes, eñes...). Se hace obligatorio el uso de la codificación UTF-8.
 - [BUG] La página de carga del Cliente @firma ignora las mayúsculas en la selección del método de firma (sign, cosign o countersign). Esto resuelve el error de selección de la operación con el método de firma con enumerados.
 
#### Cliente distribuido Java

 - [RFE] Se introduce un nuevo API que permite proporcionar directamente la configuración de la conexión con el componente central.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 
#### Cliente distribuido .NET

 - [RFE] Se introduce un nuevo API que permite proporcionar directamente la configuración de la conexión con el componente central.
 - [RFE] Se agregan nuevas excepciones para describir errores al agregar documentos a un lote.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 - [RFE] e modifica el formato de devolución de los datos de firma para que se puedan obtener como binarios.
 - [BUG] Se agrega al método de recuperación de firma el parámetro necesario para permitir su actualización a formatos longevos.
 - [BUG] Corrección en la carga de la configuración del registro de la conexión SSL.
 - [BUG] Se corrige el envío de los parámetros extra para la configuración de las firmas.
 - [BUG] Se corrige el envío de la configuración del comportamiento de la firma de lote cuando se encuentra un error. Siempre se continuaba con el resto de firmas.
 
#### Cliente distribuido PHP

 - [RFE] Se agregan nuevas excepciones para describir errores al agregar documentos a un lote.
 - [RFE] Se agregan nuevas excepciones para describir errores durante la recuperación de la firma de un lote.
 - [BUG] Se corrige la declaración de excepciones para eliminar algunas que nunca se lanzaban.
 - [BUG] Se agrega al método de recuperación de firma el parámetro necesario para permitir su actualización a formatos longevos.
 
#### Aplicación de pruebas

 - [RFE] Se modifica para hacer uso del nuevo componente distribuido Java.

----------

### v2.0.1

#### Componente central

 - [RFE] Se sustituye el MiniApplet 1.5 incluida por la misma versión firmada por el MinHAFP.
 - [RFE] Se modifica la configuración por defecto para que se utilice AutoFirma nativo para las firmas con certificado local.

----------

### v2.0

Se cambia el nombre del proyecto a FIRe

#### Componente central

 - [RFE] Se integra la opción de incluir la selección de certificado como parte del proceso de firma.
 - [RFE] Se integra el despliegue del MiniApplet @firma y AutoFirma vía JNLP para su uso como parte del proceso de firma.
 - [RFE] Se integran las funciones de firma de lotes para la firma de múltiples documentos.
 - [RFE] Se integran páginas web para la selección del origen del certificado, selección del certificado en la nube, expedición de certificados y firma con certificado cliente.

#### Componente distribuido

 - [RFE] Se agrega un nuevo API desde el que invocar las nuevas funciones de firma (simples y de lotes), independiente del origen del certificado.
 - [RFE] Se agrega un método para obtener los errores internos producidos en servidor durante la ejecución de la transacción.
 - [RFE] [java] Se elimina la dependencia con el módulo core del Cliente @firma (afirma-core.jar).

#### Aplicación de pruebas

 - [RFE] Aplicación de pruebas;		Uso del nuevo componente distribuido Java.
 - [RFE] Aplicación de pruebas;		Uso del modelo de selección de certificado integrada en el proceso de firma.
 - [RFE] Aplicación de pruebas;		Selección del origen del certificado (ClaveFirma o certificado local) automático y mediante configuración.
 - [RFE] Aplicación de pruebas;		Carga de los datos a firmar en forma de fichero.

----------

### v1.0

Versión inicial con Cl@veFirma