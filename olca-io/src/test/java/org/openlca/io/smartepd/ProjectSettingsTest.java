package org.openlca.io.smartepd;

import org.openlca.jsonld.Json;

public class ProjectSettingsTest {

	public static void main(String[] args) {
		var client = SmartEpdClient.of(
				"https://smart-epd.herokuapp.com/",
				"b960428ac1db13f3868e239f4076069b73b418bd01670e880e407aa125d8217f");

		for (var project : client.getProjects().value()) {
			System.out.println(project.name());
			var settings = client.getProjectSettings(project.id()).value();
			var json = Json.toPrettyString(settings.json());
			System.out.println(json);

			for (var method : settings.impactSettings().methods()) {
				System.out.println("method: " + method);
			}

			for (var indicator : settings.impactSettings().indicators()) {
				System.out.println("indicator: " + indicator);
			}

			break;
		}

	}

}
