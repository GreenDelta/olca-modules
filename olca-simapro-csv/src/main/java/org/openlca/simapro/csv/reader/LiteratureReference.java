package org.openlca.simapro.csv.reader;

import java.util.Queue;

import org.openlca.simapro.csv.model.refdata.SPLiteratureReference;

final class LiteratureReference {

	static SPLiteratureReference parse(Queue<String> lines) {
		SPLiteratureReference literatureReference = new SPLiteratureReference(
				null, null, null);

		while (!lines.isEmpty()) {

			switch (lines.poll()) {
			case "Name":
				literatureReference.setName(lines.poll());
				break;

			case "Documentation link":
				literatureReference.setDocumentLink(lines.poll());
				break;

			case "Category":
				literatureReference.setCategory(lines.poll());
				break;

			case "Description":
				literatureReference.setContent(lines.poll());
				break;

			}
		}
		return literatureReference;
	}

}
