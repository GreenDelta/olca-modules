package org.openlca.core.model.doc;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.Mutable;
import org.openlca.core.model.AbstractEntity;
import org.openlca.commons.Copyable;
import org.openlca.core.model.Source;

@Entity
@Table(name = "tbl_compliance_declarations")
public class ComplianceDeclaration extends AbstractEntity
		implements Copyable<ComplianceDeclaration> {

	/**
	 * The source that describes and references the compliance system.
	 */
	@OneToOne
	@JoinColumn(name = "f_system")
	public Source system;

	@Lob
	@Column(name = "comment")
	public String comment;

	@Lob
	@Mutable
	@Column(name = "aspects")
	@Convert(converter = AspectMapConverter.class)
	public final AspectMap aspects = new AspectMap();

	@Override
	public ComplianceDeclaration copy() {
		var copy = new ComplianceDeclaration();
		copy.system = system;
		copy.comment = comment;
		copy.aspects.putAll(aspects);
		return copy;
	}
}
