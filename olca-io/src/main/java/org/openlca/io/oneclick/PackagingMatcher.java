package org.openlca.io.oneclick;

import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

public class PackagingMatcher {

	private final Pattern pattern;

	private PackagingMatcher(String p) {
		pattern = Pattern.compile(p);
	}

	public static PackagingMatcher createDefault() {
		var pattern = ".*\\b(" +
			"packaging" +
			"|packing" +
			"|core board production" +
			"|kraft paper production" +
			"|wood wool production" +
			")\\b.*";
		return new PackagingMatcher(pattern);
	}

	public static PackagingMatcher create(String pattern) {
		return new PackagingMatcher(pattern);
	}

	public String pattern() {
		return pattern.pattern();
	}

	public boolean matches(String label) {
		if (Strings.nullOrEmpty(label))
			return false;
		var s = label.strip().toLowerCase(Locale.US);
		return pattern.matcher(s).matches();
	}

	public boolean matches(Descriptor d) {
		return d != null && matches(d.name);
	}
}
