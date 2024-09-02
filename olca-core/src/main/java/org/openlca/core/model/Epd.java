package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_epds")
public class Epd extends RootEntity {

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

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_from")
	public Date validFrom;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_until")
	public Date validUntil;

	@OneToOne
	@JoinColumn(name = "f_location")
	public Location location;

	@OneToOne
	@JoinColumn(name="f_original_epd")
	public Source originalEpd;

	@Lob
	@Column(name="manufacturing")
	public String manufacturing;

	@Lob
	@Column(name="product_usage")
	public String productUsage;

	@Lob
	@Column(name="use_advice")
	public String useAdvice;

	@Column(name = "registration_id")
	public String registrationId;

	@OneToOne
	@JoinColumn(name = "f_data_generator")
	public Actor dataGenerator;

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

		if (validFrom != null) {
			copy.validFrom = new Date(validFrom.getTime());
		}
		if (validUntil != null) {
			copy.validUntil = new Date(validUntil.getTime());
		}

		copy.location = location;
		copy.originalEpd = originalEpd;
		copy.manufacturing = manufacturing;
		copy.productUsage = productUsage;
		copy.useAdvice = useAdvice;
		copy.registrationId = registrationId;
		copy.dataGenerator = dataGenerator;
		return copy;
	}

}
