package de.example.multitenant.hibernate;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gregor Tudan, Cofinpro AG
 */
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        final HttpServletRequest httpServletRequest = CDI.current().select(HttpServletRequest.class).get();
        return httpServletRequest.getParameter("tenant");
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
