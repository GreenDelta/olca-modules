package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A scaling factor is a factor for scaling a linked process in a product
 * system.
 */
@Entity
@Table(name = "tbl_scalingfactors")
public class ScalingFactor extends AbstractEntity implements Cloneable {

	/**
	 * <p style="margin-top: 0">
	 * The factor
	 * </p>
	 */
	@Column(name = "factor")
	private double factor;

	/**
	 * <p style="margin-top: 0">
	 * The id of process the scaling factor belongs to
	 * </p>
	 */
	@Column(length = 36, name = "processid")
	private String processId;

	/**
	 * <p style="margin-top: 0">
	 * The id of product exchange the scaling factor belongs to
	 * </p>
	 */
	@Column(length = 36, name = "productid")
	private String productId;

	/**
	 * <p style="margin-top: 0">
	 * Uncertainty of the product
	 * </p>
	 */
	@Column(name = "uncertainty")
	private double uncertainty;

	/**
	 * <p style="margin-top: 0">
	 * Creates a new scaling factor
	 * </p>
	 */
	public ScalingFactor() {
	}

	@Override
	public ScalingFactor clone() {
		final ScalingFactor scalingFactor = new ScalingFactor();
		scalingFactor.setFactor(getFactor());
		scalingFactor.setUncertainty(getUncertainty());
		scalingFactor.setProductId(getProductId());
		scalingFactor.setProcessId(getProcessId());
		return scalingFactor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the factor-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The factor
	 *         </p>
	 */
	public double getFactor() {
		return factor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the processId-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The id of process the scaling factor belongs to
	 *         </p>
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the productId-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The id of product exchange the scaling factor belongs to
	 *         </p>
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the uncertainty-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Uncertainty of the product
	 *         </p>
	 */
	public double getUncertainty() {
		return uncertainty;
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the factor-field
	 * </p>
	 * 
	 * @param factor
	 *            <p style="margin-top: 0">
	 *            The factor
	 *            </p>
	 */
	public void setFactor(final double factor) {
		this.factor = factor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the processId-field
	 * </p>
	 * 
	 * @param processId
	 *            <p style="margin-top: 0">
	 *            The id of process the scaling factor belongs to
	 *            </p>
	 */
	public void setProcessId(final String processId) {
		this.processId = processId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the productId-field
	 * </p>
	 * 
	 * @param productId
	 *            <p style="margin-top: 0">
	 *            The id of product exchange the scaling factor belongs to
	 *            </p>
	 */
	public void setProductId(final String productId) {
		this.productId = productId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the uncertainty-field
	 * </p>
	 * 
	 * @param uncertainty
	 *            <p style="margin-top: 0">
	 *            Uncertainty of the product
	 *            </p>
	 */
	public void setUncertainty(final double uncertainty) {
		this.uncertainty = uncertainty;
	}

}
