package org.openlca.core.model.mapping.content;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

@Entity
public class ES1FlowContent extends MappingContent {

	@Transient
	private String category;

	@PrePersist
	@PreUpdate
	protected void toJson() {
		// setContent(buildJson)
	}

	@PostLoad
	protected void fromJson() {
		// JSON = getContent(); parse
	}

}
