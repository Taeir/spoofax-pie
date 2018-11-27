package mb.spoofax.api.module;

import java.util.Collection;

/**
 * Exception wrapping multiple exceptions.
 */
public class MultiException extends Exception {
    private Exception[] exceptions;

    public MultiException(Exception... exceptions) {
        this.exceptions = exceptions;
    }

    public MultiException(Collection<? extends Exception> exceptions) {
        this.exceptions = exceptions.toArray(new Exception[0]);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("MultiIOException:\n");
        for (Exception ex : exceptions) {
            sb.append("  ").append(ex.getMessage()).append("\n");
        }

        return sb.toString();
    }

    public Exception[] getExceptions() {
        return exceptions;
    }
}
