package org.fsarmiento.invoicing.entities;

import java.net.URL;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.dbunit.database.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class AbstractHibernateDaoTest.
 * 
 * @author Florencio Sarmiento
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/test-main-context.xml",
		"/spring/hibernate-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public abstract class AbstractHibernateDaoTest<T extends AbstractEntity> {

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	@PostConstruct
	public void dataSetup() throws Exception {
		IDatabaseConnection conn = new DatabaseConnection(
				dataSource.getConnection());

		IDataSet dataSet = getDataSet();

		if (dataSet == null) {
			return;
		}

		try {
			DatabaseOperation.CLEAN_INSERT.execute(conn, dataSet);
		} finally {
			conn.close();
		}
	}

	private IDataSet getDataSet() throws Exception {

		List<String> dataSetLocations = getDataSetLocations();

		if (dataSetLocations == null || dataSetLocations.isEmpty()) {
			return null;
		}

		List<IDataSet> dataSets = new ArrayList<IDataSet>();

		for (String dataSetLocation : dataSetLocations) {
			URL url = null;

			if (dataSetLocation.startsWith("/")) {
				Resource resource = new ClassPathResource(dataSetLocation);
				url = resource.getURL();

			} else {
				url = getClass().getResource(dataSetLocation);
			}

			IDataSet dataSet = new FlatXmlDataSetBuilder().build(url);
			dataSets.add(dataSet);
		}

		return new CompositeDataSet(
				(IDataSet[]) dataSets.toArray(new IDataSet[dataSets.size()]));
	}

	protected abstract List<String> getDataSetLocations();
}
