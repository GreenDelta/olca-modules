package org.openlca.sd.xmile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.openlca.commons.Res;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xmile", namespace = Xmile.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Xmile {

	public static final String NS = "http://docs.oasis-open.org/xmile/ns/XMILE/v1.0";

	@XmlElement(name = "header", namespace = NS)
	XmiHeader header;

	@XmlElement(name = "sim_specs", namespace = NS)
	XmiSimSpecs simSpecs;

	@XmlElementWrapper(name = "dimensions", namespace = NS)
	@XmlElement(name = "dim", namespace = Xmile.NS)
	List<XmiDim> dims;

	@XmlElement(name = "model", namespace = NS)
	XmiModel model;

	public static Res<Xmile> readFrom(File file) {
		try (var stream = new FileInputStream(file);
				 var buffer = new BufferedInputStream(stream)) {
			return readFrom(buffer);
		} catch (Exception e) {
			return Res.error("Error reading XMILE file: " + file, e);
		}
	}

	public static Res<Xmile> readFrom(InputStream stream) {
		try {
			var xmile = JAXB.unmarshal(stream, Xmile.class);
			return Res.ok(xmile);
		} catch (Exception e) {
			return Res.error("Error reading XMILE stream", e);
		}
	}

	public Res<Void> writeTo(File file) {
		try (var out = new FileOutputStream(file);
				 var buffer = new BufferedOutputStream(out)) {
			return writeTo(buffer);
		} catch (Exception e) {
			return Res.error("Error writing XMILE file: " + file, e);
		}
	}

	public Res<Void> writeTo(OutputStream stream) {
		try {
			var context = JAXBContext.newInstance(Xmile.class);
			var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(this, stream);
			return Res.ok();
		} catch (Exception e) {
			return Res.error("Error writing XMILE stream", e);
		}
	}

	public XmiHeader header() {
		return header;
	}

	public void setHeader(XmiHeader header) {
		this.header = header;
	}

	public XmiSimSpecs simSpecs() {
		return simSpecs;
	}

	public void setSimSpecs(XmiSimSpecs simSpecs) {
		this.simSpecs = simSpecs;
	}

	public List<XmiDim> dims() {
		return dims != null ? dims : Collections.emptyList();
	}

	public void setDims(List<XmiDim> dims) {
		this.dims = dims;
	}

	public XmiModel model() {
		return model;
	}

	public void setModel(XmiModel model) {
		this.model = model;
	}
}
