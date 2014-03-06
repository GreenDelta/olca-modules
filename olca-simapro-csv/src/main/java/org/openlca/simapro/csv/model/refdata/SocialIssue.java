package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRows("Social issues")
public class SocialIssue extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.SOCIAL_ISSUES;
	}

}