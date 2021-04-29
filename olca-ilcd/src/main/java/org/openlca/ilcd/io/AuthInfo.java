package org.openlca.ilcd.io;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.descriptors.DataStock;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
