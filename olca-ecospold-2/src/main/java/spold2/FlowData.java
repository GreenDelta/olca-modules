package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FlowData {

	@XmlElement(name = "intermediateExchange")
	public final List<IntermediateExchange> intermediateExchanges = new ArrayList<>();

	@XmlElement(name = "elementaryExchange")
	public final List<ElementaryExchange> elementaryExchanges = new ArrayList<>();

	@XmlElement(name = "parameter")
	public final List<Parameter> parameters = new ArrayList<>();

}
