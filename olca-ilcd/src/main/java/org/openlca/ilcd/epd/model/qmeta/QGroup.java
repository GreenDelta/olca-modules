package org.openlca.ilcd.epd.model.qmeta;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A QGroup bundles a set of Q metadata questions.
 */
public class QGroup {

	public String name;
	public List<QQuestion> questions = new ArrayList<>();

	/**
	 * The questions in a group should have all the same type. Thus, we return
	 * (the first non-null) type of the first question as the type of this
	 * group.
	 */
	public QQuestionType getType() {
		for (QQuestion q : questions) {
			if (q.type != null)
				return q.type;
		}
		return null;
	}

	/**
	 * Reads a list of question groups from the given JSON file. It returns an
	 * empty list when this failed.
	 */
	/*
	static public List<QGroup> fromFile(File file) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedInputStream buf = new BufferedInputStream(fis)) {
			return fromJson(buf);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
	*/


	/**
	 * Read a list of question groups from a JSON configuration file. It assumes
	 * that the questions and groups are sorted in that configuration.
	 */
	/*
	@SuppressWarnings("unchecked")
	static public List<QGroup> fromJson(InputStream is) {
		if (is == null)
			return Collections.emptyList();
		try (InputStreamReader reader = new InputStreamReader(is, "utf-8")) {
			Gson gson = new Gson();
			Map<String, ?>[] data = gson.fromJson(reader, Map[].class);
			if (data == null || data.length == 0)
				return Collections.emptyList();
			List<QGroup> groups = new ArrayList<>();
			for (Map<String, ?> q : data) {
				QQuestion question = readQuestion(q);
				if (question == null)
					continue;
				Object group = q.get("group");
				if (!(group instanceof String))
					continue;
				QGroup g = groups.stream()
						.filter(gi -> Objects.equals(gi.name, group))
						.findFirst().orElse(null);
				if (g == null) {
					g = new QGroup();
					g.name = (String) group;
					groups.add(g);
				}
				g.questions.add(question);
			}
			return groups;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(QGroup.class);
			log.error("failed to read question groups", e);
			return Collections.emptyList();
		}
	}

	 */

	private static QQuestion readQuestion(Map<String, ?> q) {
		if (q == null)
			return null;
		Object id = q.get("id");
		if (!(id instanceof String))
			return null;
		Object answer = q.get("answer");
		if (!(answer instanceof String))
			return null;
		QQuestion question = new QQuestion();
		question.id = (String) id;
		question.text = (String) answer;
		Object type = q.get("type");
		if (type instanceof String) {
			question.type = readType((String) type);
		}
		return question;
	}

	private static QQuestionType readType(String s) {
		if (s == null)
			return null;
		return switch (s) {
			case "OneInList" -> QQuestionType.ONE_IN_LIST;
			case "Boolean" -> QQuestionType.BOOLEAN;
			default -> null;
		};
	}

}
