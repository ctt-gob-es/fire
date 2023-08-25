package es.gob.fire.statistics.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import es.gob.fire.statistics.config.DBConnectionException;
import es.gob.fire.statistics.config.DbManager;
import es.gob.fire.statistics.entity.AuditSignatureCube;

public class AuditSignaturesDAO {
	
	/** SQL para insertar una peticion. */
	private static final String ST_INSERT_AUDIT_SIGN = "INSERT INTO TB_AUDIT_FIRMAS " //$NON-NLS-1$
			+ "(id_transaccion, id_int_lote, operacion_criptografica, formato, formato_actualizado, tamanno, resultado, error_detalle) " //$NON-NLS-1$
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?)"; //$NON-NLS-1$
	
	/**
	 * Inserta una configuraci&oacute;n de operaci&oacute;n de firma en base de datos indicando
	 * cuantas veces se dio esta configuraci&oacute;n un d&iacute;a concreto.
	 * @param signature Configuraci&oacute;n de la operaci&oacute;n de firma.
	 * @return {@code true} si la configuraci&oacute;n se inserto correctamente. {@code false}
	 * en caso contrario.
	 * @throws SQLException Cuando se produce un error al insertar los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static boolean insertAuditSignature( final AuditSignatureCube signature, final long total)
			throws SQLException, DBConnectionException {

		boolean inserted = false;
		
		try (final Connection conn = DbManager.getInstance().getConnection(true);) {
			insertAuditSignature(signature, total, conn);
		}
		
		return inserted;
	}
	
	/**
	 * Inserta una configuraci&oacute;n de operaci&oacute;n de firma en base de datos indicando
	 * cuantas veces se dio esta configuraci&oacute;n un d&iacute;a concreto.
	 * @param signature Configuraci&oacute;n de la operaci&oacute;n de firma.
	 * @return {@code true} si la configuraci&oacute;n se inserto correctamente. {@code false}
	 * en caso contrario.
	 * @throws SQLException Cuando se produce un error al insertar los datos.
	 * @throws DBConnectionException Cuando se produce un error de conexi&oacute;n con la base de datos.
	 */
	public static boolean insertAuditSignature( final AuditSignatureCube signature, final long total,
			final Connection conn)
			throws SQLException, DBConnectionException {

		try (final PreparedStatement st = conn.prepareStatement(ST_INSERT_AUDIT_SIGN)) {
			st.setString(1, signature.getIdTransaction());
			st.setString(2, signature.getIdIntLote());
			st.setString(3, signature.getCryptoOperation());
			st.setString(4, signature.getFormat());
			st.setString(5, signature.getImprovedFormat());
			st.setLong(6, signature.getDataSize());
			st.setBoolean(7, signature.isResult());
			st.setString(8, signature.getErrorDetail());
			if (st.executeUpdate() < 1) {
				return false;
			}
		}
		return true;
	}

}
