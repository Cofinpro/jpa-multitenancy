package de.cofinpro.multitenant.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.util.config.ConfigurationException;
import org.hibernate.service.spi.InjectService;
import org.hibernate.service.spi.Startable;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

/**
 * @author Gregor Tudan, Cofinpro AG
 */
public class TenantConnectionProvider extends AbstractMultiTenantConnectionProvider implements Startable {

    private final Logger log = Logger.getLogger(getClass());
    private final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
    private ConfigurationService configurationService;

    @Override
    public void start() {
        DataSource dataSource = configurationService.getSetting(AvailableSettings.DATASOURCE, DataSource.class, null);
        if (dataSource == null) {
            throw new ConfigurationException("No Datasource configured.");
        }
        connectionProvider.configure(Collections.singletonMap(Environment.DATASOURCE, dataSource));
        initDatasource(dataSource);
    }

    private void initDatasource(DataSource dataSource) {
        log.info("Initializing database schema");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("RUNSCRIPT FROM 'classpath:sql/create.sql'");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @InjectService
    public void setConfigurationProvider(ConfigurationService configurationService) {
        this.configurationService = configurationService;
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
        return getAnyConnectionProvider().getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        getAnyConnectionProvider().closeConnection(connection);
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.debugf("Requesting connection for tenant %s", tenantIdentifier);

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
        selectConnectionProvider(tenantIdentifier).closeConnection(connection);
    }
}
