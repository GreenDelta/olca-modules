package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemUseSearch implements
		IUseSearch<ProductSystemDescriptor> {

	private IDatabase db;

	public ProductSystemUseSearch(IDatabase db) {
		this.db = db;
	}

	public List<BaseDescriptor> findUses(ProductSystem system) {
		return findUses(Descriptors.toDescriptor(system));
	}

	@Override
	public List<BaseDescriptor> findUses(ProductSystemDescriptor system) {
		if (system == null)
			return Collections.emptyList();
		String sql = "select f_project from tbl_project_variants where " +
				"f_product_system = " + system.getId();
		try {
			return doProjectQuery(sql);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to find uses of " + system, e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> doProjectQuery(String sql) throws Exception {
		final HashSet<Long> ids = new HashSet<>();
		NativeSql.on(db).query(sql, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				ids.add(result.getLong(1));
				return true;
			}
		});
		ProjectDao dao = new ProjectDao(db);
		List<ProjectDescriptor> list = dao.getDescriptors(ids);
		return new ArrayList<BaseDescriptor>(list);
	}
}
