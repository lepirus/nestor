package com.amplifino.jdbc.pools;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.metatype.annotations.Designate;

@Component(configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=DataSourceConfiguration.class, factory=true)
public class DataSourceProvider {

	@Reference
	private DataSourceFactory dataSourceFactory;
	private PoolDataSource dataSource;
	private ServiceRegistration<DataSource> registration;
	
	@Activate
	public void activate(BundleContext context, DataSourceConfiguration configuration) throws SQLException {
		Properties props = new Properties();
		props.put(DataSourceFactory.JDBC_URL, configuration.url());
		props.put(DataSourceFactory.JDBC_USER, configuration.user());
		props.put(DataSourceFactory.JDBC_PASSWORD, configuration._password());
		ConnectionPoolDataSource connectionPoolDataSource = dataSourceFactory.createConnectionPoolDataSource(props);
		PoolDataSource.Builder builder = PoolDataSource.builder(connectionPoolDataSource)
			.name(configuration.dataSourceName())
			.useIsValid(configuration.useConnectionIsValid())
			.initialSize(configuration.initialPoolSize());
		if (configuration.maxPoolSize() > 0) {
			builder.maxSize(configuration.maxPoolSize());
		}
		if (configuration.minPoolSize() > 0) {
			builder.maxIdle(configuration.minPoolSize());
		}
		if (configuration.maxIdleTime() > 0) {
			builder.maxIdleTime(configuration.maxIdleTime(), TimeUnit.SECONDS);
		}
		dataSource = builder.build();
		Dictionary<String, Object> dictionary = new Hashtable<>();
		dictionary.put(DataSourceFactory.JDBC_DATABASE_NAME, configuration.dataSourceName());	
		dictionary.put("application", configuration.application());
		registration = context.registerService(DataSource.class, dataSource,  dictionary);
	}
	
	@Deactivate 
	public void deactivate() {
		dataSource.close();
		registration.unregister();
	}
}
