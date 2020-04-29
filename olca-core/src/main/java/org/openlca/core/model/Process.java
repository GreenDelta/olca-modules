package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_processes")
public class Process extends ParameterizedEntity {

	@Column(name = "default_allocation_method")
	@Enumerated(EnumType.STRING)
	public AllocationMethod defaultAllocationMethod;

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_process")
	public final List<AllocationFactor> allocationFactors = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public List<Exchange> exchanges = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_location")
	public Location location;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_process_doc")
	public ProcessDocumentation documentation;

	@Column(name = "process_type")
	@Enumerated(EnumType.STRING)
	public ProcessType processType = ProcessType.UNIT_PROCESS;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_quantitative_reference")
	public Exchange quantitativeReference;

	@Column(name = "infrastructure_process")
	public boolean infrastructureProcess;

	/**
	 * This is used as a sequence for the exchange's internal id
	 */
	@Column(name = "last_internal_id")
	public int lastInternalId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_process")
	public final List<SocialAspect> socialAspects = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_currency")
	public Currency currency;

	@OneToOne
	@JoinColumn(name = "f_dq_system")
	public DQSystem dqSystem;

	@Column(name = "dq_entry")
	public String dqEntry;

	@OneToOne
	@JoinColumn(name = "f_exchange_dq_system")
	public DQSystem exchangeDqSystem;

	@OneToOne
	@JoinColumn(name = "f_social_dq_system")
	public DQSystem socialDqSystem;

	@Override
	public Process clone() {
		return new ProcessCopy().create(this);
	}

	public Exchange exchange(Flow flow) {
		return add(Exchange.from(flow));
	}

	public Exchange exchange(Flow flow, FlowProperty property, Unit unit) {
		return add(Exchange.from(flow, property, unit));
	}

	private Exchange add(Exchange exchange) {
		exchange.internalId = ++lastInternalId;
		exchanges.add(exchange);
		return exchange;
	}

	public Exchange getExchange(int internalId) {
		for (Exchange exchange : exchanges)
			if (exchange.internalId == internalId)
				return exchange;
		return null;
	}

}
