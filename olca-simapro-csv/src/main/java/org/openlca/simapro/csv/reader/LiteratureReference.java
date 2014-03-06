package org.openlca.simapro.csv.reader;

import java.util.Queue;

import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;

final class LiteratureReference {

	static LiteratureReferenceBlock parse(Queue<String> lines) {
		LiteratureReferenceBlock literatureReference = new LiteratureReferenceBlock(
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
