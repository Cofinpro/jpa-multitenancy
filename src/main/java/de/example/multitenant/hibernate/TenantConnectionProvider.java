package de.example.multitenant.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author Gregor Tudan, Cofinpro AG
 */
public class TenantConnectionProvider extends AbstractMultiTenantConnectionProvider {

    private final ConnectionProvider connectionProvider;

    public TenantConnectionProvider() {
        DatasourceConnectionProviderImpl provider = new DatasourceConnectionProviderImpl();

        try {
            DataSource dataSource = InitialContext.doLookup("java:jboss/datasources/multitenant");
            provider.configure(Collections.singletonMap(Environment.DATASOURCE, dataSource));
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
        this.connectionProvider = provider;
        initDatasource();
    }

    private void initDatasource() {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            connection.createStatement().execute("RUNSCRIPT FROM 'classpath:sql/create.sql'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connectionProvider.closeConnection(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return connectionProvider;
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return connectionProvider;
    }

    public Connection getAnyConnection() throws SQLException {
        return connectionProvider.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connectionProvider.closeConnection(connection);
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try {
                connection.createStatement().execute("USE " + tenantIdentifier);
        } catch (SQLException e) {
            throw new HibernateException(
                    "Could not alter JDBC connection to specified schema [" +
                            tenantIdentifier + "]",
                    e
            );
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            connection.createStatement().execute("USE public");
        } catch (SQLException e) {
            // on error, throw an exception to make sure the connection is not returned to the pool.
            // your requirements may differ
            throw new HibernateException(
                    "Could not alter JDBC connection to specified schema [" +
                            tenantIdentifier + "]", e
            );
        }
        connectionProvider.closeConnection(connection);
    }


}
