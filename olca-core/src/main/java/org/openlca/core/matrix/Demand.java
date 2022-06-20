package org.openlca.core.matrix;

import org.openlca.core.matrix.index.TechFlow;

/**
 * The demand for which a result is calculated.
 *
 * @param techFlow the demanded product-output or waste-input
 * @param value    the amount of the demand given in the reference unit of the
 *                 product or waste flow
 */
public record Demand(TechFlow techFlow, double value) {


}
