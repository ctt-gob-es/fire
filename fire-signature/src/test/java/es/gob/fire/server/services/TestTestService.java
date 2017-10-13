/* Copyright (C) 2017 [Gobierno de Espana]
 * This file is part of FIRe.
 * FIRe is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 08/09/2017
 * You may contact the copyright holder at: soporte.afirma@correo.gob.es
 */
package es.gob.fire.server.services;

import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.fire.signature.LoadResult;
import es.gob.fire.signature.connector.FIReConnector;
import es.gob.fire.signature.connector.test.TestConnector;

/** Pruebas del servicio de pruebas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestTestService {

	private static final String TRIPHASE_DATA_SAMPLE =
		"<xml>\r\n" +  //$NON-NLS-1$
		" <firmas>\r\n" +  //$NON-NLS-1$
		"  <firma Id=\"bcadaaec-0066-4f59-b336-eb50d938e5ec\">\r\n" +  //$NON-NLS-1$
		"   <param n=\"PK1\">MeCgx4ZTIrhoRLgKWsCjsC20sm/puN/Wxrltr452N3VrvfJjsDoL/vZfL0OSpeffsIRbEfkY7tFyV5RgaPsM9jQILHnKND6ykNtTSP/5zfj+CVlsvnGhjTa6icYMdten0oUDTX7zh3V2o7hKYFjHA8tdKfgyKNLiJjJi/OAlKU8=</param>\r\n" +  //$NON-NLS-1$
		"   <param n=\"BASE\">PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGZlOkZhY3R1cmFlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczpmZT0iaHR0cDovL3d3dy5mYWN0dXJhZS5lcy9GYWN0dXJhZS8yMDA3L3YzLjEvRmFjdHVyYWUiPjxGaWxlSGVhZGVyPjxTY2hlbWFWZXJzaW9uPjMuMTwvU2NoZW1hVmVyc2lvbj48TW9kYWxpdHk+STwvTW9kYWxpdHk+PEludm9pY2VJc3N1ZXJUeXBlPlJFPC9JbnZvaWNlSXNzdWVyVHlwZT48QmF0Y2g+PEJhdGNoSWRlbnRpZmllcj5BMjg4NTUyNjAxMDA8L0JhdGNoSWRlbnRpZmllcj48SW52b2ljZXNDb3VudD4xPC9JbnZvaWNlc0NvdW50PjxUb3RhbEludm9pY2VzQW1vdW50PjxUb3RhbEFtb3VudD40NzIwLjAwPC9Ub3RhbEFtb3VudD48L1RvdGFsSW52b2ljZXNBbW91bnQ+PFRvdGFsT3V0c3RhbmRpbmdBbW91bnQ+PFRvdGFsQW1vdW50PjQ3MjAuMDA8L1RvdGFsQW1vdW50PjwvVG90YWxPdXRzdGFuZGluZ0Ftb3VudD48VG90YWxFeGVjdXRhYmxlQW1vdW50PjxUb3RhbEFtb3VudD40NzIwLjAwPC9Ub3RhbEFtb3VudD48L1RvdGFsRXhlY3V0YWJsZUFtb3VudD48SW52b2ljZUN1cnJlbmN5Q29kZT5FVVI8L0ludm9pY2VDdXJyZW5jeUNvZGU+PC9CYXRjaD48L0ZpbGVIZWFkZXI+PFBhcnRpZXM+PFNlbGxlclBhcnR5PjxUYXhJZGVudGlmaWNhdGlvbj48UGVyc29uVHlwZUNvZGU+SjwvUGVyc29uVHlwZUNvZGU+PFJlc2lkZW5jZVR5cGVDb2RlPlI8L1Jlc2lkZW5jZVR5cGVDb2RlPjxUYXhJZGVudGlmaWNhdGlvbk51bWJlcj5BMjg4NTUyNjA8L1RheElkZW50aWZpY2F0aW9uTnVtYmVyPjwvVGF4SWRlbnRpZmljYXRpb24+PExlZ2FsRW50aXR5PjxDb3Jwb3JhdGVOYW1lPkFUT1MgUy5BLjwvQ29ycG9yYXRlTmFtZT48QWRkcmVzc0luU3BhaW4+PEFkZHJlc3M+Qy8gQW1pbyBuwroxMTQ8L0FkZHJlc3M+PFBvc3RDb2RlPjEyMzEyPC9Qb3N0Q29kZT48VG93bj5TYW50aWFnbzwvVG93bj48UHJvdmluY2U+QSBDb3J1w7FhPC9Qcm92aW5jZT48Q291bnRyeUNvZGU+RVNQPC9Db3VudHJ5Q29kZT48L0FkZHJlc3NJblNwYWluPjwvTGVnYWxFbnRpdHk+PC9TZWxsZXJQYXJ0eT48QnV5ZXJQYXJ0eT48VGF4SWRlbnRpZmljYXRpb24+PFBlcnNvblR5cGVDb2RlPko8L1BlcnNvblR5cGVDb2RlPjxSZXNpZGVuY2VUeXBlQ29kZT5SPC9SZXNpZGVuY2VUeXBlQ29kZT48VGF4SWRlbnRpZmljYXRpb25OdW1iZXI+UzE1MTEwMDFIPC9UYXhJZGVudGlmaWNhdGlvbk51bWJlcj48L1RheElkZW50aWZpY2F0aW9uPjxMZWdhbEVudGl0eT48Q29ycG9yYXRlTmFtZT5YVU5UQSBERSBHQUxJQ0lBPC9Db3Jwb3JhdGVOYW1lPjxBZGRyZXNzSW5TcGFpbj48QWRkcmVzcz5TYW4gQ2FldGFubzwvQWRkcmVzcz48UG9zdENvZGU+MTU3MDQ8L1Bvc3RDb2RlPjxUb3duPlNhbnRpYWdvIGRlIENvbXBvc3RlbGE8L1Rvd24+PFByb3ZpbmNlPkEgQ29ydcOxYTwvUHJvdmluY2U+PENvdW50cnlDb2RlPkVTUDwvQ291bnRyeUNvZGU+PC9BZGRyZXNzSW5TcGFpbj48L0xlZ2FsRW50aXR5PjwvQnV5ZXJQYXJ0eT48L1BhcnRpZXM+PEludm9pY2VzPjxJbnZvaWNlPjxJbnZvaWNlSGVhZGVyPjxJbnZvaWNlTnVtYmVyPjEwMDwvSW52b2ljZU51bWJlcj48SW52b2ljZVNlcmllc0NvZGU+QTIwMTI8L0ludm9pY2VTZXJpZXNDb2RlPjxJbnZvaWNlRG9jdW1lbnRUeXBlPkZDPC9JbnZvaWNlRG9jdW1lbnRUeXBlPjxJbnZvaWNlQ2xhc3M+T088L0ludm9pY2VDbGFzcz48L0ludm9pY2VIZWFkZXI+PEludm9pY2VJc3N1ZURhdGE+PElzc3VlRGF0ZT4yMDEyLTAyLTIwPC9Jc3N1ZURhdGU+PEludm9pY2VDdXJyZW5jeUNvZGU+RVVSPC9JbnZvaWNlQ3VycmVuY3lDb2RlPjxUYXhDdXJyZW5jeUNvZGU+RVVSPC9UYXhDdXJyZW5jeUNvZGU+PExhbmd1YWdlTmFtZT5lczwvTGFuZ3VhZ2VOYW1lPjwvSW52b2ljZUlzc3VlRGF0YT48VGF4ZXNPdXRwdXRzPjxUYXg+PFRheFR5cGVDb2RlPjAxPC9UYXhUeXBlQ29kZT48VGF4UmF0ZT4xOC4wMDwvVGF4UmF0ZT48VGF4YWJsZUJhc2U+PFRvdGFsQW1vdW50PjQwMDAuMDA8L1RvdGFsQW1vdW50PjwvVGF4YWJsZUJhc2U+PFRheEFtb3VudD48VG90YWxBbW91bnQ+NzIwLjAwPC9Ub3RhbEFtb3VudD48L1RheEFtb3VudD48L1RheD48L1RheGVzT3V0cHV0cz48SW52b2ljZVRvdGFscz48VG90YWxHcm9zc0Ftb3VudD40MDAwLjAwPC9Ub3RhbEdyb3NzQW1vdW50PjxUb3RhbEdyb3NzQW1vdW50QmVmb3JlVGF4ZXM+NDAwMC4wMDwvVG90YWxHcm9zc0Ftb3VudEJlZm9yZVRheGVzPjxUb3RhbFRheE91dHB1dHM+NzIwLjAwPC9Ub3RhbFRheE91dHB1dHM+PFRvdGFsVGF4ZXNXaXRoaGVsZD4wLjAwPC9Ub3RhbFRheGVzV2l0aGhlbGQ+PEludm9pY2VUb3RhbD40NzIwLjAwPC9JbnZvaWNlVG90YWw+PFRvdGFsT3V0c3RhbmRpbmdBbW91bnQ+NDcyMC4wMDwvVG90YWxPdXRzdGFuZGluZ0Ftb3VudD48VG90YWxFeGVjdXRhYmxlQW1vdW50PjQ3MjAuMDA8L1RvdGFsRXhlY3V0YWJsZUFtb3VudD48L0ludm9pY2VUb3RhbHM+PEl0ZW1zPjxJbnZvaWNlTGluZT48SXRlbURlc2NyaXB0aW9uPmzDrW5lYSAxIGRlbCBjb25jZXB0byAgPC9JdGVtRGVzY3JpcHRpb24+PFF1YW50aXR5PjEuMDwvUXVhbnRpdHk+PFVuaXRPZk1lYXN1cmU+MDE8L1VuaXRPZk1lYXN1cmU+PFVuaXRQcmljZVdpdGhvdXRUYXg+MTAwMC4wMDAwMDA8L1VuaXRQcmljZVdpdGhvdXRUYXg+PFRvdGFsQ29zdD4xMDAwLjAwPC9Ub3RhbENvc3Q+PEdyb3NzQW1vdW50PjEwMDAuMDA8L0dyb3NzQW1vdW50PjxUYXhlc091dHB1dHM+PFRheD48VGF4VHlwZUNvZGU+MDE8L1RheFR5cGVDb2RlPjxUYXhSYXRlPjE4LjAwPC9UYXhSYXRlPjxUYXhhYmxlQmFzZT48VG90YWxBbW91bnQ+MTAwMC4wMDwvVG90YWxBbW91bnQ+PC9UYXhhYmxlQmFzZT48VGF4QW1vdW50PjxUb3RhbEFtb3VudD4xODAuMDA8L1RvdGFsQW1vdW50PjwvVGF4QW1vdW50PjwvVGF4PjwvVGF4ZXNPdXRwdXRzPjwvSW52b2ljZUxpbmU+PEludm9pY2VMaW5lPjxJdGVtRGVzY3JpcHRpb24+bMOtbmVhIDIgZGVsIGNvbmNlcHRvIDwvSXRlbURlc2NyaXB0aW9uPjxRdWFudGl0eT4yLjA8L1F1YW50aXR5PjxVbml0T2ZNZWFzdXJlPjAxPC9Vbml0T2ZNZWFzdXJlPjxVbml0UHJpY2VXaXRob3V0VGF4PjE1MDAuMDAwMDAwPC9Vbml0UHJpY2VXaXRob3V0VGF4PjxUb3RhbENvc3Q+MzAwMC4wMDwvVG90YWxDb3N0PjxHcm9zc0Ftb3VudD4zMDAwLjAwPC9Hcm9zc0Ftb3VudD48VGF4ZXNPdXRwdXRzPjxUYXg+PFRheFR5cGVDb2RlPjAxPC9UYXhUeXBlQ29kZT48VGF4UmF0ZT4xOC4wMDwvVGF4UmF0ZT48VGF4YWJsZUJhc2U+PFRvdGFsQW1vdW50PjMwMDAuMDA8L1RvdGFsQW1vdW50PjwvVGF4YWJsZUJhc2U+PFRheEFtb3VudD48VG90YWxBbW91bnQ+NTQwLjAwPC9Ub3RhbEFtb3VudD48L1RheEFtb3VudD48L1RheD48L1RheGVzT3V0cHV0cz48L0ludm9pY2VMaW5lPjwvSXRlbXM+PC9JbnZvaWNlPjwvSW52b2ljZXM+PGRzOlNpZ25hdHVyZSBJZD0iU2lnbmF0dXJlLWY0Y2IyMGZmLTE2ZGItNDdlNC1iMTIwLWQ5OTIwZDhhNDQxZi1TaWduYXR1cmUiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnL1RSLzIwMDEvUkVDLXhtbC1jMTRuLTIwMDEwMzE1Ii8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPjxkczpSZWZlcmVuY2UgSWQ9IlJlZmVyZW5jZS1kNjFiZTYzMy1lOGM4LTRjMTMtOTJkYi01OGU4NjgxZGFmODkiIFR5cGU9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNPYmplY3QiIFVSST0iIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48L2RzOlRyYW5zZm9ybXM+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTUxMiIvPjxkczpEaWdlc3RWYWx1ZT5DSXhVWTJrSXB2Z1NTRTY0VkpqZGUySU5wL0piaW9GbFNTSTd6NHBGUkFIZnk4bHVuYk5wYWx1bHJSeGNqY0J3OHgrK3FZZjZVUlFRCkIvcHdCSWJ2c1E9PTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PGRzOlJlZmVyZW5jZSBUeXBlPSJodHRwOi8vdXJpLmV0c2kub3JnLzAxOTAzI1NpZ25lZFByb3BlcnRpZXMiIFVSST0iI1NpZ25hdHVyZS1mNGNiMjBmZi0xNmRiLTQ3ZTQtYjEyMC1kOTkyMGQ4YTQ0MWYtU2lnbmVkUHJvcGVydGllcyI+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTUxMiIvPjxkczpEaWdlc3RWYWx1ZT5pSUVSSTdSQ1dRTDBHZTVjYWZ1VUl1RGpMeWV5VUgxUk93STE4Q0ZoaDhST2JCeFFBT3htbCs1M1F6VkxlK3dtMTBEQ3JiKzJkbkJ4ClpCWER2cjRtK2c9PTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PGRzOlJlZmVyZW5jZSBVUkk9IiNTaWduYXR1cmUtZjRjYjIwZmYtMTZkYi00N2U0LWIxMjAtZDk5MjBkOGE0NDFmLUtleUluZm8iPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGE1MTIiLz48ZHM6RGlnZXN0VmFsdWU+SjlzNWxRMnEzdGZEQllTZ0E1U09lOE4zYmV3cTJKbzlKaHZzR2M1QUZzWmpKU3UwR2NJSnlGd2pjb2FDdTJMT2t1Wnc1WVdtb3hJMApSM3kwdTA5S3d3PT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWUgSWQ9IlNpZ25hdHVyZS1mNGNiMjBmZi0xNmRiLTQ3ZTQtYjEyMC1kOTkyMGQ4YTQ0MWYtU2lnbmF0dXJlVmFsdWUiPiUlUkVQTEFDRU1FXzAlJTwvZHM6U2lnbmF0dXJlVmFsdWU+PGRzOktleUluZm8gSWQ9IlNpZ25hdHVyZS1mNGNiMjBmZi0xNmRiLTQ3ZTQtYjEyMC1kOTkyMGQ4YTQ0MWYtS2V5SW5mbyI+ZjkwZDQ2OWItOGNlMS00NGVlLWIyM2ItOThhNmYxOTg3Njc2PC9kczpLZXlJbmZvPjxkczpPYmplY3Q+PHhhZGVzOlF1YWxpZnlpbmdQcm9wZXJ0aWVzIElkPSJTaWduYXR1cmUtZjRjYjIwZmYtMTZkYi00N2U0LWIxMjAtZDk5MjBkOGE0NDFmLVF1YWxpZnlpbmdQcm9wZXJ0aWVzIiBUYXJnZXQ9IiNTaWduYXR1cmUtZjRjYjIwZmYtMTZkYi00N2U0LWIxMjAtZDk5MjBkOGE0NDFmLVNpZ25hdHVyZSIgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiIHhtbG5zOnhhZGVzPSJodHRwOi8vdXJpLmV0c2kub3JnLzAxOTAzL3YxLjMuMiMiPjx4YWRlczpTaWduZWRQcm9wZXJ0aWVzIElkPSJTaWduYXR1cmUtZjRjYjIwZmYtMTZkYi00N2U0LWIxMjAtZDk5MjBkOGE0NDFmLVNpZ25lZFByb3BlcnRpZXMiPjx4YWRlczpTaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPjx4YWRlczpTaWduaW5nVGltZT4yMDE1LTExLTE3VDE5OjE5OjAzKzAxOjAwPC94YWRlczpTaWduaW5nVGltZT48eGFkZXM6U2lnbmluZ0NlcnRpZmljYXRlPjx4YWRlczpDZXJ0Pjx4YWRlczpDZXJ0RGlnZXN0PjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGE1MTIiLz48ZHM6RGlnZXN0VmFsdWU+cGtCOXorWVZ3YnBtWUMvTGxmdTkrSmN5MG1IZ2NNN2xHdGk5cGxaTk9aQVE0UC9WRm5jN24xYnQwWXVGVElFc3k2UWttQ2cybDMwcklZczJmRDVUSHc9PTwvZHM6RGlnZXN0VmFsdWU+PC94YWRlczpDZXJ0RGlnZXN0Pjx4YWRlczpJc3N1ZXJTZXJpYWw+PGRzOlg1MDlJc3N1ZXJOYW1lPkNOPUFORiBBc3N1cmVkIElEIENBMSwgU0VSSUFMTlVNQkVSPUc2MzI4NzUxMCwgRU1BSUxBRERSRVNTPWluZm9AYW5mLmVzLCBPVT1BTkYgQXV0b3JpZGFkIEludGVybWVkaWEgZGUgSWRlbnRpZGFkLCBPPUFORiBBdXRvcmlkYWQgZGUgQ2VydGlmaWNhY2lvbiwgTD1CYXJjZWxvbmEgKHNlZSBjdXJyZW50IGFkZHJlc3MgYXQgaHR0cDovL3d3dy5hbmYuZXMvZXMvYWRkcmVzcy1kaXJlY2Npb24uaHRtbCApLCBTVD1CYXJjZWxvbmEsIEM9RVM8L2RzOlg1MDlJc3N1ZXJOYW1lPjxkczpYNTA5U2VyaWFsTnVtYmVyPjM2NzcwMzkwOTMyMjQ0MzMyODwvZHM6WDUwOVNlcmlhbE51bWJlcj48L3hhZGVzOklzc3VlclNlcmlhbD48L3hhZGVzOkNlcnQ+PC94YWRlczpTaWduaW5nQ2VydGlmaWNhdGU+PHhhZGVzOlNpZ25lclJvbGU+PHhhZGVzOkNsYWltZWRSb2xlcz48eGFkZXM6Q2xhaW1lZFJvbGU+ZW1pc29yPC94YWRlczpDbGFpbWVkUm9sZT48L3hhZGVzOkNsYWltZWRSb2xlcz48L3hhZGVzOlNpZ25lclJvbGU+PC94YWRlczpTaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPjx4YWRlczpTaWduZWREYXRhT2JqZWN0UHJvcGVydGllcz48eGFkZXM6RGF0YU9iamVjdEZvcm1hdCBPYmplY3RSZWZlcmVuY2U9IiNSZWZlcmVuY2UtZDYxYmU2MzMtZThjOC00YzEzLTkyZGItNThlODY4MWRhZjg5Ij48eGFkZXM6RGVzY3JpcHRpb24vPjx4YWRlczpPYmplY3RJZGVudGlmaWVyPjx4YWRlczpJZGVudGlmaWVyIFF1YWxpZmllcj0iT0lEQXNVUk4iPnVybjpvaWQ6MS4yLjg0MC4xMDAwMy41LjEwOS4xMDwveGFkZXM6SWRlbnRpZmllcj48eGFkZXM6RGVzY3JpcHRpb24vPjwveGFkZXM6T2JqZWN0SWRlbnRpZmllcj48eGFkZXM6TWltZVR5cGU+dGV4dC94bWw8L3hhZGVzOk1pbWVUeXBlPjx4YWRlczpFbmNvZGluZz5VVEYtODwveGFkZXM6RW5jb2Rpbmc+PC94YWRlczpEYXRhT2JqZWN0Rm9ybWF0PjwveGFkZXM6U2lnbmVkRGF0YU9iamVjdFByb3BlcnRpZXM+PC94YWRlczpTaWduZWRQcm9wZXJ0aWVzPjwveGFkZXM6UXVhbGlmeWluZ1Byb3BlcnRpZXM+PC9kczpPYmplY3Q+PC9kczpTaWduYXR1cmU+PC9mZTpGYWN0dXJhZT4=</param>\r\n" +  //$NON-NLS-1$
		"  </firma>\r\n" +  //$NON-NLS-1$
		" </firmas>\r\n" +  //$NON-NLS-1$
		"</xml>"; //$NON-NLS-1$

	/** Prueba de carga de datos.
	 * @throws Exception */
	@SuppressWarnings("static-method")
	@Test
	public void testLoadData() throws Exception {
		final FIReConnector testService = new TestConnector();
		System.out.println(
			testService.loadDataToSign(
				"00001", //$NON-NLS-1$
				"SHA256withRSA", //$NON-NLS-1$
				TriphaseData.parser(TRIPHASE_DATA_SAMPLE.getBytes()),
				CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					TestTestService.class.getResourceAsStream("/ANF_USUARIO_ACTIVO.cer") //$NON-NLS-1$
				)
			)
		);
	}

	/** prueba de obtenci&oacute;n de certificados.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testGetCertificates() throws Exception {
		final FIReConnector testService = new TestConnector();
		final X509Certificate[] certs = testService.getCertificates("00001"); //$NON-NLS-1$
		for (final X509Certificate cert : certs) {
			System.out.println(AOUtil.getCN(cert));
		}
	}

	/** Prueba de transacci&oacute;n completa de firma.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testSignData() throws Exception {

		final FIReConnector nbh = new TestConnector();

		final Properties config = new Properties();
		config.setProperty("redirectOkUrl", "http://www.google.com"); //$NON-NLS-1$ //$NON-NLS-2$
		config.setProperty("redirectErrorUrl", "http://www.ibm.com"); //$NON-NLS-1$ //$NON-NLS-2$
		nbh.init(config);

		final X509Certificate cert = nbh.getCertificates("00001")[0]; //$NON-NLS-1$

		System.out.println();
		System.out.println(AOUtil.getCN(cert));
		System.out.println();

		final Properties extraParams = new Properties();
		extraParams.setProperty("mode", "implicit"); //$NON-NLS-1$ //$NON-NLS-2$

		final TriphaseData td = FIReTriHelper.getPreSign(
			"sign", //$NON-NLS-1$
			"XAdES", //$NON-NLS-1$
			"SHA1withRSA", //$NON-NLS-1$
			extraParams,
			cert,
			"Hola mundo!".getBytes() //$NON-NLS-1$
		);

		System.out.println();
		System.out.println(td);
		System.out.println();

		final LoadResult res = nbh.loadDataToSign(
			"00001", //$NON-NLS-1$
			"SHA1withRSA", //$NON-NLS-1$
			td,
			cert
		);

		System.out.println(res);
		System.out.println();

		// HAY QUE PARAR AQUI LA EJECUCION Y ENTRAR MANUALMENTE EN LA URL QUE SE HA MOSTRADO
		// COMO REDIRECCION EN EL PRINTLN

		final Map<String, byte[]> ret;
		try {
			ret = nbh.sign(res.getTransactionId());
		}
		catch (final Throwable t) {
			t.printStackTrace();
			return;
		}

		// Insertamos los PKCS#1 en la sesion trifasica
		final Set<String> keys = ret.keySet();
		for (final String key : keys) {
			System.out.println("Firma " + key + " = " + AOUtil.hexify(ret.get(key), false)); //$NON-NLS-1$ //$NON-NLS-2$
			FIReTriHelper.addPkcs1ToTriSign(ret.get(key), key, td);
		}

		System.out.println("TripaseData:\n" + td.toString()); //$NON-NLS-1$

		// Ya con el TriphaseData relleno, hacemos la postfirma
		final byte[] signature = FIReTriHelper.getPostSign(
				"sign", //$NON-NLS-1$
				"CAdES", //$NON-NLS-1$
				"SHA1withRSA", //$NON-NLS-1$
				extraParams,
				cert,
				"Hola mundo!".getBytes(), //$NON-NLS-1$
				td
			);

		// El resultado obtenido es la firma completa
		final FileOutputStream fos = new FileOutputStream(File.createTempFile("ClaveFirma-XSIG_", ".xml")); //$NON-NLS-1$ //$NON-NLS-2$
		fos.write(signature);
		fos.close();

		System.out.println("OK"); //$NON-NLS-1$
	}

}
