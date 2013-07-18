package org.openlca.core.indices;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechnosphereLinkTable implements Closeable {

	private IDatabase database;
	private PreparedStatement insertStatement;
	private Connection con;
	private Logger log = LoggerFactory.getLogger(getClass());

	public TechnosphereLinkTable(IDatabase database) {
		this.database = database;
	}

	/** Deletes all technosphere links for the process with the given ID. */
	public void delete(long processId) {
		log.trace("delete technosphere links for process {}", processId);
		try (Connection con = database.createConnection()) {
			String stmt = "delete from tbl_technosphere_links where f_process = "
					+ processId;
			int rows = con.createStatement().executeUpdate(stmt);
			log.trace("{} rows deleted", rows);
			con.commit();
		} catch (Exception e) {
			log.error("failed to delete technosphere links for " + processId, e);
		}
	}

	public void store(Process process) {
		if (process == null)
			return;
		log.trace("index technosphere links for process {}", process.getId());
		List<TechnosphereLink> links = new ArrayList<>();
		for (Exchange e : process.getExchanges()) {
			if (e.getFlow() == null || e.getFlow().getFlowType() == null)
				continue;
			FlowType type = e.getFlow().getFlowType();
			if (type == FlowType.ELEMENTARY_FLOW)
				continue;
			TechnosphereLink link = new TechnosphereLink();
			link.setAmount(e.getConvertedResult());
			link.setFlowId(e.getFlow().getId());
			link.setInput(e.isInput());
			link.setProcessId(process.getId());
			link.setWaste(type == FlowType.WASTE_FLOW);
			links.add(link);
		}
		try {
			delete(process.getId());
			insert(links);
		} catch (Exception e) {
			log.error("failed to insert technosphere links", e);
		}
	}

	private void insert(List<TechnosphereLink> links) throws Exception {
		if (links.isEmpty())
			return;
		log.trace("index {} links", links.size());
		if (insertStatement == null)
			createInsertStatement();
		for (TechnosphereLink link : links) {
			insertStatement.setLong(1, link.getProcessId());
			insertStatement.setLong(2, link.getFlowId());
			insertStatement.setDouble(3, link.getAmount());
			insertStatement.setBoolean(4, link.isInput());
			insertStatement.setBoolean(5, link.isWaste());
			insertStatement.addBatch();
		}
		insertStatement.executeBatch();
		con.commit();
	}

	private void createInsertStatement() throws Exception {
		if (con == null)
			con = database.createConnection();
		String sql = "insert into tbl_technosphere_links(f_process, f_flow, "
				+ "amount, is_input, is_waste) values (?, ?, ?, ?, ?)";
		insertStatement = con.prepareStatement(sql);
	}

	public List<TechnosphereLink> getAll() {
		log.trace("get all technosphere links");
		String query = "select * from tbl_technosphere_links";
		return fetchLinks(query);
	}

	private List<TechnosphereLink> fetchLinks(String query) {
		try (Connection con = database.createConnection()) {
			ResultSet set = con.createStatement().executeQuery(query);
			List<TechnosphereLink> links = new ArrayList<>();
			while (set.next()) {
				TechnosphereLink link = new TechnosphereLink();
				link.setAmount(set.getDouble("amount"));
				link.setFlowId(set.getLong("f_flow"));
				link.setInput(set.getBoolean("is_input"));
				link.setProcessId(set.getLong("f_process"));
				link.setWaste(set.getBoolean("is_waste"));
				links.add(link);
			}
			log.trace("fetched {} technosphere links", links.size());
			set.close();
			return links;
		} catch (Exception e) {
			log.error("failed to get all technosphere links", e);
			return Collections.emptyList();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (insertStatement != null)
				insertStatement.close();
			if (con != null) {
				con.commit();
				con.close();
			}
		} catch (Exception e) {
			log.error("failed to close tech-index", e);
			throw new IOException(e);
		} finally {
			insertStatement = null;
			con = null;
		}
	}

}
