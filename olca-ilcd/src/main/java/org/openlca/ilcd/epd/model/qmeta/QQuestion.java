package org.openlca.ilcd.epd.model.qmeta;

import org.openlca.ilcd.epd.conversion.Dom;
import org.openlca.ilcd.epd.conversion.Vocab;
import org.openlca.ilcd.epd.util.Strings;
import org.w3c.dom.Element;

public class QQuestion {

	public String id;
	public String group;
	public QAnswer answer;
	public String comment;
	public QQuestionType type;
	public String text;

	void write(Element parent) {
		if (parent == null)
			return;
		Element elem = Dom.addChild(parent, "norreq:Question", Vocab.SBE_ILCD);
		if (id != null) {
			Dom.addChild(elem,
					"norreq:QuestionID", Vocab.SBE_ILCD)
					.setTextContent(id);
		}
		if (group != null) {
			Dom.addChild(elem,
					"norreq:QuestionGroup", Vocab.SBE_ILCD)
					.setTextContent(group);
		}
		if (answer != null) {
			answer.write(elem);
		}
		if (comment != null) {
			Dom.addChild(elem, "norreq:Comment",
					Vocab.SBE_ILCD)
					.setTextContent(comment);
		}
	}

	static QQuestion read(Element elem) {
		if (elem == null)
			return null;
		if (!"Question".equals(elem.getLocalName()))
			return null;
		if (!Vocab.SBE_ILCD.equals(elem.getNamespaceURI()))
			return null;

		QQuestion q = new QQuestion();
		Element idElem = Dom.getChild(elem, "QuestionID", Vocab.SBE_ILCD);
		if (idElem != null) {
			q.id = idElem.getTextContent();
		}
		Element groupElem = Dom.getChild(elem, "QuestionGroup", Vocab.SBE_ILCD);
		if (groupElem != null) {
			q.group = groupElem.getTextContent();
		}
		q.answer = QAnswer.read(elem);
		Element commentElem = Dom.getChild(elem, "Comment", Vocab.SBE_ILCD);
		if (commentElem != null) {
			q.comment = commentElem.getTextContent();
		}
		return q;
	}

	@Override
	public QQuestion clone() {
		QQuestion clone = new QQuestion();
		clone.id = id;
		clone.group = group;
		clone.comment = comment;
		if (answer != null) {
			clone.answer = answer.clone();
		}
		clone.text = text;
		clone.type = type;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof QQuestion other))
			return false;
		return Strings.nullOrEqual(this.id, other.id);
	}
}
