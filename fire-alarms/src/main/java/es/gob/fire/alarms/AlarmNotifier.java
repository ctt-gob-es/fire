package es.gob.fire.alarms;

import java.util.Properties;

/**
 * Notificador de alarmas del sistema. Las implementaciones de esta
 * interfaz pueden configurarse para enviar alarmas a un sistema de
 * notificaciones o capaz de gestionar alarmas.
 */
public interface AlarmNotifier {

	/**
	 * Configura el notificador con los valores necesarios para el env&iacute;o
	 * de las alarmas. La configuraci&oacute;n proporcionada sera obtenido por
	 * el sistema a trav&eacute;s de un mecanismo est&aacute;ndar para todas
	 * las implementaciones, por lo que no debe hacerlo el propio notificador.
	 * @param config Conjunto de propiedades configuradas para el notificador.
	 */
	void configure(Properties config);

	/**
	 * Notifica una alarma.
	 * @param module Nombre del m&oacute;dulo o fuente que genera la alarma.
	 * @param level Nivel de la alarma.
	 * @param alarm Tipo de alarma.
	 * @param source Origen de la alarma. Por ejemplo, ante un problema de
	 * conexi&oacute;n, ser&iacute;a el destino al que no se ha podido conectar.
	 */
	void notify(String module, AlarmLevel level, Alarm alarm, String source);
}
