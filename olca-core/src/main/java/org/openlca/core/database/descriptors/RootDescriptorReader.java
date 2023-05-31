package org.openlca.core.database.descriptors;

import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;

public record RootDescriptorReader<T extends RootDescriptor>(
		RootEntityDao<?, T> dao
) implements DescriptorReader<T> {

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
				   	d.library,
				   	d.tags from
				""" + table + " d";
	}

	@Override
	public T getDescriptor(ResultSet r) {
		try {
			var d = dao.getDescriptorType()
					.getDeclaredConstructor()
					.newInstance();
			d.id = getId(r);
			d.refId = getRefIf(r);
			d.name = getName(r);
			d.version = getVersion(r);
			d.lastChange = getLastChange(r);
			d.category = getCategory(r);
			d.library = getLibrary(r);
			d.tags = getTags(r);
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
