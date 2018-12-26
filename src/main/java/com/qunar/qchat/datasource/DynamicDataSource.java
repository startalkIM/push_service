package com.qunar.qchat.datasource;

import com.qunar.qchat.aop.routingdatasource.DataSourceKeyHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by qitmac000378 on 17/5/23.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
//    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    protected Object determineCurrentLookupKey() {
        return DataSourceKeyHolder.getCurrentKey();
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        if (DataSourceKeyHolder.getCurrentKey() != null) {
//            logger.info("Datasource route to {}, key={}", connection, DataSourceKeyHolder.getCurrentKey());
        }
        return connection;
    }
}
