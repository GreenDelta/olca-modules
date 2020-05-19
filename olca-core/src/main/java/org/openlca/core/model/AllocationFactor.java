package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This type describes an allocation factor related to a product or waste flow
 * of a multi-functional process. A multi-functional process has, in contrast to
 * a mono-functional process, multiple product outputs, waste inputs, or both.
 * Allocation factors can be used to transform a multi-functional process into a
 * set of mono-functional processes (or more precisely into a set of
 * mono-functional process vectors). This is done by splitting the amount
 * $m_{i,u}$ of each product input, waste output, or elementary flow $i$ of the
 * multi-functional process $u$ into a value $m_{i,j}$ for each product output
 * or waste input $j$ by applying an allocation factor $\lambda_{j}$
 * respectively:
 *
 * $$m_{i,j} = \lambda_{j} \ m_{i,u}$$
 *
 * In openLCA, allocation factors for different allocation methods (physical,
 * economic, causal allocation) can be stored for a process. While physical and
 * economic allocation factors of a product $j$ are the same for all product
 * inputs, waste outputs, or elementary flows $i$, they can be different for
 * each flow $i$ in case of causal allocation (so $\lambda_{i,j}$ in this case).
 *
 * Furthermore, the value of an allocation factor is between 0 and 1, and the
 * sum off all allocation factors related to each product $j$ (and flow $i$ in
 * case of causal allocation) is 1:
 *
 * $$0 \leq \lambda_{(i,)j} \leq 1 \ \text{and} \ \sum_j{\lambda_{(i,)j}} = 1$$
 *
 */
@Entity
@Table(name = "tbl_allocation_factors")
public class AllocationFactor extends AbstractEntity implements Cloneable {

	/**
	 * The ID of the product flow $j$ to which this allocation factor is
	 * related. In case of waste treatment processes (processes with waste
	 * inputs) this can be also the ID of a waste flow.
	 */
	@Column(name = "f_product")
	public long productId;

	/**
	 * The allocation method to which the factor is related (physical, economic,
	 * or causal).
	 */
	@Column(name = "allocation_type")
	@Enumerated(EnumType.STRING)
	public AllocationMethod method;

	/**
	 * The value of the allocation factor $\lambda_{j}$ (or $\lambda_{i,j}$ in
	 * case of causal allocation).
	 */
	@Column(name = "value")
	public double value;

	/**
	 * The value of an allocation factor can be also calculated via a formula.
	 */
	@Column(name = "formula")
	public String formula;

	/**
	 * If the factor is a causal allocation factor this field contains a
	 * reference to the exchange with flow $i$ to which the allocation factor is
	 * related. Note that there can be multiple exchanges with the same flow $i$
	 * and different allocation factors.
	 */
	@OneToOne
	@JoinColumn(name = "f_exchange")
	public Exchange exchange;

	@Override
	public AllocationFactor clone() {
		var clone = new AllocationFactor();
		clone.productId = productId;
		clone.method = method;
		clone.exchange = exchange;
		clone.value = value;
		clone.formula = formula;
		return clone;
	}

}
