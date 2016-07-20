package org.openlca.io.xls.results;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.openlca.io.xls.Excel;

class CellStyles {

	private static final int BOLD = 1;
	private static final int DATE = 2;
	private final Map<String, CellStyle> styles = new HashMap<>();
	private final Workbook workbook;

	CellStyles(Workbook workbook) {
		this.workbook = workbook;
	}

	CellStyle date() {
		return get(DATE);
	}

	CellStyle bold() {
		return get(BOLD);
	}

	CellStyle bold(Color color) {
		return get(BOLD, color);
	}

	CellStyle normal(Color color) {
		return get(-1, color);
	}

	private CellStyle get(int weight) {
		return get(weight, null);
	}

	private CellStyle get(int type, Color color) {
		String key = toKey(type, color);
		if (styles.containsKey(key))
			return styles.get(key);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		applyType(style, type);
		applyColor(style, color);
		styles.put(key, style);
		return style;
	}

	private void applyType(CellStyle style, int type) {
		if (type == BOLD) {
			Font font = workbook.createFont();
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			style.setFont(font);
		} else if (type == DATE) {
			style.setDataFormat(Excel.dateFormat(workbook));
		}
	}

	private void applyColor(XSSFCellStyle style, Color color) {
		if (color == null)
			return;
		style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(new XSSFColor(color));
	}

	private String toKey(int type, Color color) {
		if (color == null)
			return Integer.toString(type);
		return type + "_" + color.getRGB();
	}

}
