package org.openlca.ilcd.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "authenticated", "userName", "roles" })
@XmlRootElement(name = "authInfo", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class Authentication {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected boolean authenticated;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected String userName;

	@XmlElement(name = "role", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	protected List<String> roles;

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean value) {
		this.authenticated = value;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String value) {
		this.userName = value;
	}

	public List<String> getRoles() {
		if (roles == null) {
			roles = new ArrayList<>();
		}
		return this.roles;
	}

	public boolean isReadAllowed() {
		return getRoles().contains("READ");
	}

	public boolean isExportAllowed() {
		return getRoles().contains("EXPORT");
	}

}
