package org.openlca.io.maps.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.io.KeyGen;

public class ILCDClassificationContent implements IMappingContent {

	// private List<String> classifications = new ArrayList<>();
	private Map<Integer, String> classifications = new HashMap<Integer, String>();

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		// private Map<Integer, String> classifications = new HashMap<Integer,
		// String>();
		// classifications.add("Level 1");
		// classifications.add("Level 2");
		// classifications.add("Imo");
		//
		// System.out.println(KeyGen.get(classifications
		// .toArray(new String[classifications.size()])));

	}

}
