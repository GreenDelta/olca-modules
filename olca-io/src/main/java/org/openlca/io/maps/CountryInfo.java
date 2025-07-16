package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// ISO 2- and 3-letter codes of countries and their corresponding openLCA
/// reference ID.
public record CountryInfo(
		String name, String alpha2, String alpha3, String refId
) {

	public static List<CountryInfo> getAll() {

		var stream = CountryInfo.class.getResourceAsStream("country-codes.csv");
		if (stream == null) {
			log().error("could not find resource: country-codes.csv");
			return List.of();
		}

		var infos = new ArrayList<CountryInfo>();
		try (stream;
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var buffer = new BufferedReader(reader)
		) {
			String line;
			boolean first = true;
			while ((line = buffer.readLine()) != null) {
				if (first) {
					first = false;
					continue;
				}
				var cells = line.split(";");
				if (cells.length < 4)
					continue;
				var info = new CountryInfo(
						cells[2].strip(),
						cells[1].strip(),
						cells[0].strip(),
						cells[3].strip()
				);
				infos.add(info);
			}
		} catch (Exception ignored) {
			log().error("could not read location codes");
		}

		return infos;
	}

	private static Logger log() {
		return LoggerFactory.getLogger(CountryInfo.class);
	}

}
