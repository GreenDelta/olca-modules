package org.openlca.io.xls.results;

import java.awt.Color;

class DQColors {

	static Color get(int index, int total) {
		if (index == 0)
			return new Color(255, 255, 255);
		if (index == 1)
			return new Color(125, 250, 125);
		if (index == total)
			return new Color(250, 125, 125);
		int median = total / 2 + 1;
		if (index == median)
			return new Color(250, 250, 125);
		if (index < median) {
			int divisor = median - 1;
			int factor = index - 1;
			return new Color(125 + (125 * factor / divisor), 250, 125);
		}
		int divisor = median - 1;
		int factor = index - median;
		return new Color(250, 250 - (125 * factor / divisor), 125);
	}

}
