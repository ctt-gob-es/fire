/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package es.gob.fire.logs.formatters;

import java.io.Writer;

final class StringBuilderWriter extends Writer {

    private final StringBuilder builder;

    StringBuilderWriter() {
        this(new StringBuilder());
    }

    public StringBuilderWriter(final StringBuilder builder) {
        this.builder = builder;
    }

    /**
     * Clears the builder used for the writer.
     *
     * @see StringBuilder#setLength(int)
     */
    void clear() {
        this.builder.setLength(0);
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) {
        this.builder.append(cbuf, off, len);
    }

    @Override
    public void write(final int c) {
        this.builder.append((char) c);
    }

    @Override
    public void write(final char[] cbuf) {
        this.builder.append(cbuf);
    }

    @Override
    public void write(final String str) {
        this.builder.append(str);
    }

    @Override
    public void write(final String str, final int off, final int len) {
        this.builder.append(str, off, len);
    }

    @Override
    public Writer append(final CharSequence csq) {
        this.builder.append(csq);
        return this;
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) {
        this.builder.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(final char c) {
        this.builder.append(c);
        return this;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
