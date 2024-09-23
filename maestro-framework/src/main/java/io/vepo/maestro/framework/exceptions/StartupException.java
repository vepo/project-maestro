package io.vepo.maestro.framework.exceptions;

import org.jboss.weld.exceptions.IllegalStateException;

/**
 * StartupException is thrown when the application fails to start.
 * 
 * A MaestroApplication is a CDI Application that requires
 * a <code>META-INF/beans.xml</code> file.
 * 
 * @see <a href="https://jakarta.ee/learn/docs/jakartaee-tutorial/current/cdi/cdi-basic/cdi-basic.html#_configuring_a_cdi_application">Configuring a CDI Application</a>
 */
public class StartupException extends IllegalStateException {

    public StartupException(String message) {
        super(message);
    }

    public StartupException(String message, Exception cause) {
        super(message, cause);
    }
}
