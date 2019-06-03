package es.gob.fire.server.admin.tool;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;

import es.gob.fire.server.admin.conf.ConfigManager;




public class SendMail {

	/**
	 *
	 * Atributo que representa el objeto que administra el registro de la clase.
	 * @throws UnsupportedEncodingException
	 */
	private static final Logger LOGGER = Logger.getLogger(SendMail.class.getName());

    public static void sendMail(final List<String> receivers, final String subject, final String body)
    		throws NoSuchProviderException,SendFailedException,ParseException,
    				MessagingException,NullPointerException, UnsupportedEncodingException {

        // Cree un objeto Propiedades para contener informacion de configuracion de conexion.
    	final Properties props = new Properties();
    	props.put("mail.smtp.host", ConfigManager.getMailHost()); //$NON-NLS-1$
    	props.put("mail.smtp.port", ConfigManager.getMailPort());  //$NON-NLS-1$
    	props.put("mail.smtp.username", ConfigManager.getMailUsername());  //$NON-NLS-1$
    	props.put("mail.smtp.password", ConfigManager.getMailPassword()); //$NON-NLS-1$
    	props.put("mail.smtp.starttls.enable",ConfigManager.getMailStarttls()); //$NON-NLS-1$
    	props.put("mail.smtp.auth", ConfigManager.getMailAuth()); //$NON-NLS-1$

        // Cree un objeto Session para representar una sesion de correo con las propiedades especificadas.
    	 final Session session = Session.getInstance(props,
                 new javax.mail.Authenticator() {
                     @Override
					protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication(ConfigManager.getMailUsername(), ConfigManager.getMailPassword());
                     }
                 });

        // Crea un mensaje con la informacion especificada.
        final MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(ConfigManager.getMailFrom(), ConfigManager.getMailFromName()));


     // Modificamos los destinatarios
        final InternetAddress[] addresses = new InternetAddress[receivers.size()];
        for (int i = 0; i < receivers.size(); i++) {
        	addresses[i] = new InternetAddress(receivers.get(i));
        }

        msg.setRecipients(Message.RecipientType.TO, addresses);
        msg.setSubject(subject);
        msg.setContent(body,"text/html"); //$NON-NLS-1$

        // Agregar un encabezado de conjunto de configuracion. Comenta o borra el
        // siguiente linea si no está utilizando un conjunto de configuracion
      //  msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET); //$NON-NLS-1$

        // Crear un transporte
        final Transport transport = session.getTransport();

        // Enviar el mensaje.
        try
        {
        	LOGGER.info("Sending..."); //$NON-NLS-1$

            // Conectese utilizando el nombre de usuario y la contrasena de SMTP que especifico anteriormente.
            transport.connect(ConfigManager.getMailHost(),ConfigManager.getMailUsername(), ConfigManager.getMailPassword());

            // Enviar el correo electronico.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!"); //$NON-NLS-1$
        }
        catch (final MessagingException e) {
        	 LOGGER.severe("El correo no ha sido enviado: " + e); //$NON-NLS-1$
             throw e;
		}
        finally
        {
            // Cierre y termine la conexion.
            transport.close();
        }
    }
   // public static void main(final String[] args) throws NoSuchProviderException, SendFailedException, ParseException, NullPointerException, MessagingException, UnsupportedEncodingException {

   // 	final List<String> receiver = new ArrayList<>();
    //	receiver.add("carlosjavierrabosoaranda@gmail.com"); //$NON-NLS-1$

    //	final String subject = "Titulo"; //$NON-NLS-1$
    	//final String body = "Cuerpo del correo"; //$NON-NLS-1$

		//sendMail(receiver, subject, body);
//	}
}
