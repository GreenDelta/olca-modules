package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Objects;

class RootDescriptorReader<T extends RootDescriptor>
		implements DescriptorReader<T> {

	private final RootEntityDao<?, T> dao;

	RootDescriptorReader(RootEntityDao<?, T> dao) {
		this.dao = Objects.requireNonNull(dao);
	}

	@Override
	public IDatabase db() {
		return dao.getDatabase();
	}

	@Override
	public String query() {
		var table = dao.getEntityTable();
		return """
					select
						d.id,
				   	d.ref_id,
				   	d.name,
				   	d.version,
				   	d.last_change,
				   	d.f_category,
				   	d.data_package,
				   	d.tags from
				""" + table + " d";
	}

	@Override
	public T getDescriptor(ResultSet r) {
		try {
			var d = dao.getDescriptorType()
					.getDeclaredConstructor()
					.newInstance();
			Util.fill(d, this, r);
			return d;
		} catch (
				NoSuchMethodException
				| SecurityException
				| InstantiationException
				| IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException("failed to create descriptor instance", e);
		}
	}
}
