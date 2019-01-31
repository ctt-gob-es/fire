/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.gob.fire.logs;

import java.io.PrintStream;

/**
 * Caches {@link System#out STDOUT} and {@link System#err STDERR} early.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */

public class StandardOutputStreams {

	/** Salida est&aacute;ndar. */
    public static final PrintStream STDOUT = System.out;

    /** Entrada est&aacute;ndar. */
    public static final PrintStream STDERR = System.err;

    /** Prints an error messages to {@link #STDERR STDERR}.
     * @param msg the message to print */
    public static void printError(final String msg) {
        STDERR.println(msg);
    }

    /**
     * Prints an error messages to {@link #STDERR STDERR}.
     *
     * @param format the {@link java.util.Formatter format}
     * @param args   the arguments for the format
     */
    public static void printError(final String format, final Object... args) {
        STDERR.printf(format, args);
    }

    /**
     * Prints an error messages to {@link #STDERR STDERR}.
     *
     * @param cause the cause of the error, if not {@code null} the {@link Throwable#printStackTrace(PrintStream)}
     *              writes to {@link #STDERR STDERR}
     * @param msg   the message to print
     */
    public static void printError(final Throwable cause, final String msg) {
        STDERR.println(msg);
        if (cause != null) {
            cause.printStackTrace(STDERR);
        }
    }

    /**
     * Prints an error messages to {@link #STDERR STDERR}.
     *
     * @param cause  the cause of the error, if not {@code null} the {@link Throwable#printStackTrace(PrintStream)}
     *               writes to {@link #STDERR STDERR}
     * @param format the {@link java.util.Formatter format}
     * @param args   the arguments for the format
     */
    public static void printError(final Throwable cause, final String format, final Object... args) {
        STDERR.printf(format, args);
        if (cause != null) {
            cause.printStackTrace(STDERR);
        }
    }
}
