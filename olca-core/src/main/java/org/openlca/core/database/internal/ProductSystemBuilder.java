/*******************************************************************************
 * Copyright (c) 2007 - 2012 GreenDeltaTC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Mozilla Public License v1.1
 * which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 *
 * Contributors:
 *     	GreenDeltaTC - initial API and implementation
 *		www.greendeltatc.com
 *		tel.:  +49 30 4849 6030
 *		mail:  gdtc@greendeltatc.com
 *******************************************************************************/

package org.openlca.core.database.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
import org.openlca.core.jobs.IProgressMonitor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductSystemBuilder implements IProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private IProgressMonitor progressMonitor;

	private IDatabase database;
	private boolean preferSystemProcesses;
	private PreparedStatement insertProcessLinkStatement;
	private PreparedStatement insertProcessStatement;
	private PreparedStatement isSystemProcessStatement;
	private PreparedStatement providerOutputStatement;
	private PreparedStatement recipientInputStatement;

	/**
	 * key = process id, value = true if the process is a system process (LCI
	 * result), false otherwise
	 */
	private Map<String, Boolean> systemProcessMap = new HashMap<>();

	public ProductSystemBuilder(IDatabase database,
			boolean preferSystemProcesses) {
		this.database = database;
		this.preferSystemProcesses = preferSystemProcesses;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public void autoComplete(ProductSystem system) {
		autoComplete(system, system.getReferenceProcess());
	}

	@Override
	public void autoComplete(ProductSystem system, Process process) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			prepareStatments(con, system.getRefId());
			run(system, process.getRefId());
			clearStatements();
			database.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
		}
	}

	private void run(ProductSystem productSystem, String startProcessId)
			throws Exception {

		// initialize
		List<String> productSystemProcesses = new ArrayList<>();
		Queue<String> processes = new LinkedList<>();
		productSystemProcesses.add(startProcessId);
		for (Process process : productSystem.getProcesses()) {
			productSystemProcesses.add(process.getRefId());
		}
		// get existing links
		Map<Long, org.openlca.core.model.ProcessLink> existingLinks = new HashMap<>();
		for (org.openlca.core.model.ProcessLink link : productSystem
				.getProcessLinks()) {
			existingLinks.put(link.getRecipientInput().getId(), link);
		}
		processes.add(startProcessId);

		while (!processes.isEmpty() && !canceled()) {
			String recipientProcessId = processes.poll();
			List<ProcessLink> links = prepareLinks(recipientProcessId);
			for (ProcessLink link : links) {
				org.openlca.core.model.ProcessLink existingLink = existingLinks
						.get(link.recipientInputId);
				if (existingLink == null) {
					completeLink(link);
				} else {
					if (!productSystemProcesses.contains(existingLink
							.getProviderProcess().getRefId())) {
						processes.add(existingLink.getProviderProcess()
								.getRefId());
						productSystemProcesses.add(existingLink
								.getProviderProcess().getRefId());
					}
				}
			}

			for (ProcessLink link : links) {
				if (link.isValid()) {
					boolean addProcess = !productSystemProcesses
							.contains(link.providerProcessId);
					if (addProcess) {
						insertProductSystemProcess(link.providerProcessId);
						productSystemProcesses.add(link.providerProcessId);
						processes.add(link.providerProcessId);
					}
					insertProcessLink(link);
				}
			}
		}

	}

	private boolean canceled() {
		return progressMonitor != null && progressMonitor.isCanceled();
	}

	private void prepareStatments(Connection con, String productSystemId)
			throws SQLException {
		String sql = "SELECT e.id, e.f_flow, e.f_default_provider FROM "
				+ "tbl_exchanges e INNER JOIN "
				+ "tbl_flows f on e.f_flow = f.id WHERE e.f_owner = ? "
				+ "AND e.input = 1 AND f.flowtype <> 0";
		recipientInputStatement = con.prepareStatement(sql);

		sql = "SELECT tbl_exchanges.id, f_owner FROM tbl_exchanges "
				+ "INNER JOIN tbl_processes ON tbl_processes.id = tbl_exchanges.f_owner "
				+ "WHERE f_flow = ? AND input = 0";
		providerOutputStatement = con.prepareStatement(sql);

		sql = "SELECT id FROM tbl_processes WHERE id = ? AND processtype = 0";
		isSystemProcessStatement = con.prepareStatement(sql);

		sql = "INSERT INTO tbl_productsystem_process(f_process,f_productsystem) "
				+ "VALUES (?,'" + productSystemId + "')";
		insertProcessStatement = con.prepareStatement(sql);

		sql = "INSERT INTO tbl_processlinks(id, f_recipientinput, f_recipientprocess, "
				+ "f_provideroutput, f_providerprocess, f_productsystem) "
				+ "VALUES (?,?,?,?,?,'" + productSystemId + "')";
		insertProcessLinkStatement = con.prepareStatement(sql);
	}

	/** Prepare the input links for the given recipient process. */
	private List<ProcessLink> prepareLinks(String recipientProcessId)
			throws SQLException {
		List<ProcessLink> links = new ArrayList<>();
		recipientInputStatement.setString(1, recipientProcessId);
		try (ResultSet rs = recipientInputStatement.executeQuery()) {
			while (rs.next()) {
				ProcessLink link = new ProcessLink();
				link.id = UUID.randomUUID().toString();
				link.recipientProcessId = recipientProcessId;
				link.recipientInputId = rs.getString("id");
				link.flowId = rs.getString("f_flow");
				link.defaultProviderId = rs.getString("f_default_provider");
				links.add(link);
			}
		}
		return links;
	}

	/** Adds the provider to the given link, if possible. */
	private void completeLink(ProcessLink link) throws SQLException {
		providerOutputStatement.setString(1, link.flowId);
		try (ResultSet rs = providerOutputStatement.executeQuery()) {
			while (rs.next()) {
				link.providerOutputId = rs.getString("id");
				link.providerProcessId = rs.getString("f_owner");
				if (link.defaultProviderId != null
						&& link.defaultProviderId
								.equals(link.providerProcessId))
					break; // default provider found
				if (link.defaultProviderId == null
						&& isSystemProcess(link.providerProcessId) == preferSystemProcesses)
					break; // no default provider and correct process type
			}
		}
	}

	private void insertProcessLink(ProcessLink link) throws SQLException {
		insertProcessLinkStatement.setString(1, link.id);
		insertProcessLinkStatement.setString(2, link.recipientInputId);
		insertProcessLinkStatement.setString(3, link.recipientProcessId);
		insertProcessLinkStatement.setString(4, link.providerOutputId);
		insertProcessLinkStatement.setString(5, link.providerProcessId);
		insertProcessLinkStatement.executeUpdate();
	}

	private void insertProductSystemProcess(String processId)
			throws SQLException {
		insertProcessStatement.setString(1, processId);
		insertProcessStatement.executeUpdate();
	}

	private boolean isSystemProcess(String processId) throws SQLException {
		Boolean isSystemProcess = systemProcessMap.get(processId);
		if (isSystemProcess == null) {
			isSystemProcessStatement.setString(1, processId);
			try (ResultSet rs = isSystemProcessStatement.executeQuery()) {
				isSystemProcess = rs.next();
			}
			systemProcessMap.put(processId, isSystemProcess);
		}
		return isSystemProcess;
	}

	private void clearStatements() throws SQLException {
		recipientInputStatement.close();
		providerOutputStatement.close();
		isSystemProcessStatement.close();
		insertProcessStatement.close();
		insertProcessLinkStatement.close();
	}

	private class ProcessLink {
		private String id;
		private String flowId;
		private String defaultProviderId;
		private String providerOutputId;
		private String providerProcessId;
		private String recipientInputId;
		private String recipientProcessId;

		boolean isValid() {
			return recipientInputId != null && recipientProcessId != null
					&& providerOutputId != null && providerProcessId != null;
		}

	}

}
