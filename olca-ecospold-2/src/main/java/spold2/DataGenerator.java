package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataGenerator {

	@XmlAttribute
	public String personId;

	@XmlAttribute
	public String personName;

	@XmlAttribute
	public String personEmail;

	@XmlAttribute
	public String publishedSourceId;

	@XmlAttribute
	public Integer publishedSourceYear;

	@XmlAttribute
	public String publishedSourceFirstAuthor;

	@XmlAttribute
	public boolean isCopyrightProtected;

	@XmlAttribute
	public String pageNumbers;

	@XmlAttribute
	public Integer accessRestrictedTo;

	@XmlAttribute
	public String companyId;

	@XmlAttribute
	public String companyCode;

}
