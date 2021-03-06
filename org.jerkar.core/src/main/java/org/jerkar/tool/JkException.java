package org.jerkar.tool;

/**
 * Exception launched to break the build. Throw this exception when you want to
 * break the build for whatever reason (compilation error, test failures, ...).
 *
 * @author Jerome Angibaud
 */
public final class JkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@link JkException} with the specified message.
     */
    public JkException(String message) {
        super(message);
    }

}
