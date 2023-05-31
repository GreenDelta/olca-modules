package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.openlca.core.database.descriptors.Util.ex;

public interface DescriptorReader<T extends RootDescriptor> {

	IDatabase db();

	String query();

	default List<T> getAll() {
		var list = new ArrayList<T>();
		NativeSql.on(db()).query(query(), r -> {
			var d = getDescriptor(r);
			list.add(d);
			return true;
		});
		return list;
	}

	default List<T> getAll(Predicate<ResultSet> fn) {
		if (fn == null)
			return List.of();
		var list = new ArrayList<T>();
		NativeSql.on(db()).query(query(), r -> {
			if (fn.test(r)) {
				var d = getDescriptor(r);
				list.add(d);
			}
			return true;
		});
		return list;
	}

	default long getId(ResultSet r) {
		try {
			return r.getLong(1);
		} catch (SQLException e) {
			throw ex("failed to read field 'id'", e);
		}
	}

	default String getRefIf(ResultSet r) {
		try {
			return r.getString(2);
		} catch (SQLException e) {
			throw ex("failed to read field 'ref-id'", e);
		}
	}

	default String getName(ResultSet r) {
		try {
			return r.getString(3);
		} catch (SQLException e) {
			throw ex("failed to read field 'name'", e);
		}
	}

	default long getVersion(ResultSet r) {
		try {
			return r.getLong(4);
		} catch (SQLException e) {
			throw ex("failed to read field 'version'", e);
		}
	}

	default long getLastChange(ResultSet r) {
		try {
			return r.getLong(5);
		} catch (SQLException e) {
			throw ex("failed to read field 'last-change'", e);
		}
	}

	default Long getCategory(ResultSet r) {
		try {
			var id = r.getLong(6);
			return r.wasNull() ? null : id;
		} catch (SQLException e) {
			throw ex("failed to read field 'category'", e);
		}
	}

	default String getLibrary(ResultSet r) {
		try {
			return r.getString(7);
		} catch (SQLException e) {
			throw ex("failed to read field 'library'", e);
		}
	}

	default String getTags(ResultSet r) {
		try {
			return r.getString(8);
		} catch (SQLException e) {
			throw ex("failed to read field 'tags'", e);
		}
	}

	T getDescriptor(ResultSet r);

}
