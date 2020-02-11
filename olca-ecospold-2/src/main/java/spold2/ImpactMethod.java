package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ImpactMethod {

	@XmlAttribute
	public String id;

	@XmlElement(name = "name")
	public String name;

	@XmlElement(name = "category")
	public final List<ImpactCategory> categories = new ArrayList<>();
}
