package org.openlca.core.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_epds")
public class Epd extends RootEntity {

	/**
	 * A URN that points to the origin of the EPD.
	 */
	@Column(name = "urn")
	public String urn;

	@Embedded
	public EpdProduct product;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_epd")
	public final List<EpdModule> modules = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_manufacturer")
	public Actor manufacturer;

	@OneToOne
	@JoinColumn(name = "f_verifier")
	public Actor verifier;

	@OneToOne
	@JoinColumn(name = "f_pcr")
	public Source pcr;

	@OneToOne
	@JoinColumn(name = "f_program_operator")
	public Actor programOperator;

	public static Epd of(String name, Flow product) {
		var epd = new Epd();
		Entities.init(epd, name);
		epd.product = EpdProduct.of(product, 1.0);
		return epd;
	}

	@Override
	public Epd copy() {
		var copy = new Epd();
		Entities.copyRefFields(this, copy);
		copy.urn = urn;
		if (product != null) {
			copy.product = product.copy();
		}
		for (var module : modules) {
			copy.modules.add(module.copy());
		}
		copy.manufacturer = manufacturer;
		copy.verifier = verifier;
		copy.pcr = pcr;
		copy.programOperator = programOperator;
		return copy;
	}

}
