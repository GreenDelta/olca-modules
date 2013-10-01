package org.openlca.core.model.mapping.content;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.openlca.core.model.AbstractEntity;

@Entity
@Table(name = "tbl_mapping_contents")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "discriminator", length = 20)
public abstract class MappingContent extends AbstractEntity{

	@Column(name = "content")
	private String content;

	@PrePersist
	@PreUpdate
	abstract protected void toJson();

	@PostLoad
	abstract protected void fromJson();

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
