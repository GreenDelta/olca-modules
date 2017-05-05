package org.openlca.ilcd.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.descriptors.DataStock;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"isAuthenticated",
		"user",
		"roles",
		"dataStocks" })
@XmlRootElement(name = "authInfo", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class AuthInfo {

	@XmlElement(name = "authenticated", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public boolean isAuthenticated;

	@XmlElement(name = "userName", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String user;

	@XmlElement(name = "role", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<String> roles = new ArrayList<>();

	@XmlElement(name = "dataStock", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<DataStock> dataStocks = new ArrayList<>();

}
