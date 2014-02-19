package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.io.ecospold1.importer.ES1KeyGen;

public class ES1CategoryContent implements IMappingContent {

	private String category;
	private String subCategory;
	private String localCategory;
	private String localSubCategory;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getLocalCategory() {
		return localCategory;
	}

	public void setLocalCategory(String localCategory) {
		this.localCategory = localCategory;
	}

	public String getLocalSubCategory() {
		return localSubCategory;
	}

	public void setLocalSubCategory(String localSubCategory) {
		this.localSubCategory = localSubCategory;
	}

	@Override
	public String getKey() {
		return KeyGen.get(category + localCategory);
	}

}
