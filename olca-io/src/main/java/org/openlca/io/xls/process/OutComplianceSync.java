package org.openlca.io.xls.process;

import org.openlca.core.model.doc.ComplianceDeclaration;

class OutComplianceSync {

	private final OutConfig config;

	private OutComplianceSync(OutConfig config) {
		this.config = config;
	}

	static void sync(OutConfig config) {
		new OutComplianceSync(config).sync();
	}

	private void sync() {
		var p = config.process();
		if (p.documentation == null)
			return;
		var cs = p.documentation.complianceDeclarations;
		if (cs.isEmpty())
			return;
		var sheet = config.createSheet(Tab.COMPLIANCE_DECLARATIONS)
			.withColumnWidths(3, 40);
		for (var c : cs) {
			write(c, sheet);
		}
	}

	private void write(ComplianceDeclaration c, SheetWriter sheet) {
		sheet.next(Section.COMPLIANCE_DECLARATION)
			.next(Field.COMPLIANCE_SYSTEM, c.system)
			.next(Field.COMMENT, c.comment)
			.next();

		if (!c.aspects.isEmpty()) {
			sheet.next(Section.COMPLIANCE_DETAILS);
			c.aspects.each(
				(aspect, value) -> sheet.next(
					row -> row.next(aspect).next(value)));
			sheet.next();
		}
	}
}
