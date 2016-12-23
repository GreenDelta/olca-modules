package spold2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class RichText {

	@XmlElement(name = "text")
	public final List<Text> texts = new ArrayList<>();

	@XmlElement(name = "variable")
	public final List<Variable> variables = new ArrayList<>();

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Text {

		@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
		public String lang;

		@XmlAttribute
		public int index;

		@XmlValue
		public String value;

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Variable {

		@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
		public String lang;

		@XmlAttribute
		public String name;

		@XmlValue
		public String value;
	}

	public static RichText of(String val) {
		if (val == null)
			return null;
		Text t = new Text();
		t.index = 1;
		t.lang = "en";
		t.value = val;
		RichText rt = new RichText();
		rt.texts.add(t);
		return rt;
	}

	public static String join(RichText rt) {
		if (rt == null)
			return null;
		Collections.sort(rt.texts, (t1, t2) -> t1.index - t2.index);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rt.texts.size(); i++) {
			builder.append(rt.texts.get(i).value);
			if (i < (rt.texts.size() - 1))
				builder.append(";");
		}
		String text = builder.toString();
		for (Variable v : rt.variables) {
			text = text.replace("{{" + v.name + "}}", v.value);
		}
		return text;
	}
}
