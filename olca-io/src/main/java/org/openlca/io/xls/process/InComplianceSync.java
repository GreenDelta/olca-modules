package org.openlca.io.xls.process;

import java.util.List;

import org.openlca.commons.Strings;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;

class InComplianceSync {

	private final InConfig config;
	private final List<ComplianceDeclaration> decs;

	private InComplianceSync(InConfig config) {
		this.config = config;
		var process = config.process();
		if (process.documentation == null) {
			process.documentation = new ProcessDoc();
		}
		this.decs = process.documentation.complianceDeclarations;
	}

	static void sync(InConfig config) {
		new InComplianceSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.COMPLIANCE_DECLARATIONS);
		if (sheet == null)
			return;
		ComplianceDeclaration dec = null;
		var it = sheet.sheetObject().rowIterator();
		while (it.hasNext()) {
			var row = it.next();
			var head = In.stringOf(row, 0);

			// match section
			if (Section.COMPLIANCE_DECLARATION.matches(head)) {
				dec = new ComplianceDeclaration();
				decs.add(dec);
				continue;
			}
			if (dec == null)
				continue;

			// match fields
			if (Field.COMPLIANCE_SYSTEM.matches(head)) {
				dec.system = config.index().get(
					Source.class, In.stringOf(row, 1));
				continue;
			}
			if (Field.COMMENT.matches(head)) {
				dec.comment = In.stringOf(row, 1);
				continue;
			}

			// aspect section
			if (Section.COMPLIANCE_DETAILS.matches(head)) {
				for (var iRow : sheet.eachBlockRowAfter(row)) {
					var aspect = In.stringOf(iRow, 0);
					if (Strings.isBlank(aspect))
						break;
					var value = In.stringOf(iRow, 1);
					if (Strings.isNotBlank(value)) {
						dec.aspects.put(aspect, value);
					}
				}
				continue;
			}
		}
	}
}
