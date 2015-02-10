package org.openlca.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.openlca.util.BinUtils;

/**
 * A set of process groups that can be stored in the database. The groups are
 * stored in a binary format:
 * 
 * gzip([number_of_groups:n]([group_name][group_size:s]([process_id])*s)*n)
 * 
 */
@Entity
@Table(name = "tbl_process_group_sets")
public class ProcessGroupSet extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@Lob
	@Column(name = "groups_blob")
	private byte[] groupsBlob;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the groups of this set by converting it to an internal byte array
	 * presentation.
	 */
	public void setGroups(List<ProcessGroup> groups) throws IOException {
		if (groups == null || groups.isEmpty()) {
			groupsBlob = null;
			return;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BinUtils.writeInt(out, groups.size());
		for (ProcessGroup group : groups) {
			BinUtils.writeString(out, group.getName());
			List<String> ids = group.getProcessIds();
			BinUtils.writeInt(out, ids.size());
			for (String id : ids) {
				BinUtils.writeString(out, id);
			}
		}
		out.flush();
		groupsBlob = BinUtils.gzip(out.toByteArray());
	}

	/** Get the process groups from the internal byte array presentation. */
	public List<ProcessGroup> getGroups() throws Exception {
		if (groupsBlob == null || groupsBlob.length == 0)
			return Collections.emptyList();
		byte[] bytes = BinUtils.gunzip(groupsBlob);
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		List<ProcessGroup> groups = new ArrayList<>();
		int groupCount = BinUtils.readInt(bin);
		for (int i = 0; i < groupCount; i++) {
			String name = BinUtils.readString(bin);
			ProcessGroup group = new ProcessGroup();
			groups.add(group);
			group.setName(name);
			int idCount = BinUtils.readInt(bin);
			for (int k = 0; k < idCount; k++) {
				String id = BinUtils.readString(bin);
				group.getProcessIds().add(id);
			}
		}
		return groups;
	}

}
