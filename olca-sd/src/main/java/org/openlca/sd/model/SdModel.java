package org.openlca.sd.model;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.database.IDatabase;
import org.openlca.sd.xmile.Xmile;

public class SdModel {

	private String id;
	private String name;
	private SimSpecs time;
	private final List<Var> vars = new ArrayList<>();
	private final List<Dimension> dimensions = new ArrayList<>();
	private final LcaSetup lca = new LcaSetup();
	private final Map<Id, Rect> positions = new HashMap<>();

	public static Res<SdModel> readFrom(Xmile xmile) {
		return new XmileReader(xmile).read();
	}

	public static Res<SdModel> readFrom(Xmile xmile, IDatabase db) {
		return new XmileReader(xmile, db).read();
	}

	public static Res<SdModel> readFrom(File file) {
		return readFrom(file, null);
	}

	public static Res<SdModel> readFrom(File file, IDatabase db) {
		var res = Xmile.readFrom(file);
		return res.isError()
			? res.castError()
			: readFrom(res.value(), db);
	}

	public static Res<SdModel> readFrom(InputStream stream) {
		return readFrom(stream, null);
	}

	public static Res<SdModel> readFrom(InputStream stream, IDatabase db) {
		var res = Xmile.readFrom(stream);
		return res.isError()
			? res.castError()
			: readFrom(res.value(), db);
	}

	public static Res<Void> writeTo(SdModel model, File file) {
		var xmile = new XmileWriter(model).write();
		return xmile.writeTo(file);
	}

	public static Res<Void> writeTo(SdModel model, OutputStream stream) {
		var xmile = new XmileWriter(model).write();
		return xmile.writeTo(stream);
	}

	public Res<Void> writeTo(File file) {
		return this.toXmile().writeTo(file);
	}

	public Res<Void> writeTo(OutputStream stream) {
		return this.toXmile().writeTo(stream);
	}

	public Xmile toXmile() {
		return new XmileWriter(this).write();
	}
	public String id() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public SimSpecs time() {
		return time;
	}

	public void setTime(SimSpecs time) {
		this.time = time;
	}

	public List<Var> vars() {
		return vars;
	}

	public List<Dimension> dimensions() {
		return dimensions;
	}

	public LcaSetup lca() {
		return lca;
	}

	public Map<Id, Rect> positions() {
		return positions;
	}

}
