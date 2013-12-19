package org.openlca.core.math;

import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;

/**
 * Helper class for calculating the reference amount for a calculation setup.
 */
public final class ReferenceAmount {

    private ReferenceAmount() {
    }

    public static double get(ProductSystem productSystem) {
        if (productSystem == null)
            return 0;
       return get(productSystem.getTargetAmount(),
               productSystem.getTargetUnit(),
               productSystem.getTargetFlowPropertyFactor());
    }

    public static double get(CalculationSetup calculationSetup) {
        if(calculationSetup == null)
            return 0;
        return get(calculationSetup.getAmount(), calculationSetup.getUnit(),
                calculationSetup.getFlowPropertyFactor());
    }

    private static double get(double amount, Unit unit,
                              FlowPropertyFactor flowPropertyFactor) {
        double refAmount = amount;
        if(unit != null)
            refAmount = refAmount * unit.getConversionFactor();
        if(flowPropertyFactor != null)
            refAmount = refAmount / flowPropertyFactor.getConversionFactor();
        return refAmount;
    }

}
