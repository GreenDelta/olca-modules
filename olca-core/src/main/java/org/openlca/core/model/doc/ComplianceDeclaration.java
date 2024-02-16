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
import org.openlca.core.model.Copyable;
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

	/**
	 * Description of compliance details.
	 */
	@Lob
	@Column(name = "details")
	public String details;

	@Lob
	@Mutable
	@Column(name = "aspects")
	@Convert(converter = AspectMapConverter.class)
	public final AspectMap aspects = new AspectMap();

	@Override
	public ComplianceDeclaration copy() {
		var copy = new ComplianceDeclaration();
		copy.system = system;
		copy.details = details;
		copy.aspects.putAll(aspects);
		return copy;
	}
}
