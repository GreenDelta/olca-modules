package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_compliance_declarations")
public class ComplianceDeclaration extends AbstractEntity
		implements Copyable<ComplianceDeclaration> {

	/**
	 * The source that describes and references the compliance system.
	 */
	@OneToOne
	@JoinColumn(name = "f_source")
	public Source source;

	/**
	 * Description of compliance details.
	 */
	@Column(name = "details")
	@Lob
	public String details;

	@Override
	public ComplianceDeclaration copy() {
		var copy = new ComplianceDeclaration();
		copy.source = source;
		copy.details = details;
		return copy;
	}
}
