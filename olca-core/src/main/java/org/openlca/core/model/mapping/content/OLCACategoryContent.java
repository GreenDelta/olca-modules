package org.openlca.core.model.mapping.content;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class OLCACategoryContent extends MappingContent {

	@Override
	@PrePersist
	@PreUpdate
	protected void toJson() {
		// TODO Auto-generated method stub
		
	}

	@Override
	@PostLoad
	protected void fromJson() {
		// TODO Auto-generated method stub
		
	}

}
