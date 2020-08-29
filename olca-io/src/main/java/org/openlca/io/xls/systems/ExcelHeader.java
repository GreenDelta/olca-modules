package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ExcelHeader {

	private final List<String> headers = new ArrayList<>();
	private final List<IExcelHeaderEntry> entries = new ArrayList<>();
	private final Map<Integer, Integer> indexMapping = new HashMap<>();

	int getHeaderSize() {
		return headers.size();
	}

	String getHeader(int count) {
		return headers.size() > count ? headers.get(count) : "";
	}

	int getEntryCount() {
		return entries.size();
	}

	IExcelHeaderEntry getEntry(int count) {
		return entries.size() > count ? entries.get(count)
				: new EmptyHeaderEntry();
	}

	int mapIndex(int from) {
		return indexMapping.getOrDefault(from, from);
	}

	public void setHeaders(String[] headers) {
		if (headers != null) {
			this.headers.addAll(Arrays.asList(headers));
		}
	}

	public void setEntries(IExcelHeaderEntry[] entries) {
		if (entries != null) {
			this.entries.addAll(Arrays.asList(entries));
		}
	}

	public void putIndexMapping(int from, int to) {
		indexMapping.put(from, to);
	}

	private static class EmptyHeaderEntry implements IExcelHeaderEntry {
		@Override
		public String getValue(int count) {
			return "";
		}
	}
}
