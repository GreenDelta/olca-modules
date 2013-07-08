package org.openlca.core.database.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductSystemTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public ProductSystemTable(IDatabase database) {
		this.database = database;
	}

	public void merge(ProductSystem system, Collection<ProductLink> newLinks) {
		Set<ProductLink> oldLinks = fetchLinks(system);
		Set<Long> oldProcesses = fetchProcesses(system);
		Set<Long> newProcesses = fetchProcesses(newLinks);
		insertProcesses(system.getId(), filter(newProcesses, oldProcesses));
		insertLinks(system.getRefId(), filter(newLinks, oldLinks));
		database.getEntityFactory().getCache().evictAll();
	}

	private void insertLinks(String systemId, Set<ProductLink> links) {
		if (links.isEmpty())
			return;
		String sql = "INSERT INTO tbl_processlinks(id, f_recipientinput, f_recipientprocess, "
				+ "f_provideroutput, f_providerprocess, f_productsystem) "
				+ "VALUES (?, ?, ?, ?, ?, '" + systemId + "')";
		log.trace("insert {} process links", links.size());
		try (Connection con = database.createConnection()) {
			PreparedStatement stmt = con.prepareStatement(sql);
			for (ProductLink link : links) {
				stmt.setString(1, UUID.randomUUID().toString()); // TODO: will
																	// fail
				stmt.setLong(2, link.getInput().getId());
				stmt.setLong(3, link.getInput().getProcessId());
				stmt.setLong(4, link.getOutput().getId());
				stmt.setLong(5, link.getOutput().getProcessId());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (Exception e) {
			log.error("Failed to insert process links", e);
		}
	}

	private void insertProcesses(long systemId, Set<Long> processes) {
		if (processes.isEmpty())
			return;
		String sql = "INSERT INTO tbl_productsystem_process(f_process,f_productsystem) "
				+ "VALUES (?,'" + systemId + "')";
		log.trace("insert {} system processes", processes.size());
		try (Connection con = database.createConnection()) {
			PreparedStatement stmt = con.prepareStatement(sql);
			for (Long processId : processes) {
				stmt.setLong(1, processId);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (Exception e) {
			log.error("Failed to insert product system processes", e);
		}
	}

	private Set<Long> fetchProcesses(Collection<ProductLink> newLinks) {
		Set<Long> newProcesses = new HashSet<>();
		for (ProductLink newLink : newLinks) {
			newProcesses.add(newLink.getInput().getProcessId());
			newProcesses.add(newLink.getOutput().getProcessId());
		}
		return newProcesses;
	}

	private <T> Set<T> filter(Collection<T> collection, Set<T> exclude) {
		Set<T> filtered = new HashSet<>();
		for (T val : collection) {
			if (!exclude.contains(val))
				filtered.add(val);
		}
		return filtered;
	}

	private Set<Long> fetchProcesses(ProductSystem system) {
		Set<Long> oldProcesses = new HashSet<>();
		for (Process process : system.getProcesses())
			oldProcesses.add(process.getId());
		return oldProcesses;
	}

	private Set<ProductLink> fetchLinks(ProductSystem system) {
		Set<ProductLink> oldLinks = new HashSet<>();
		for (ProcessLink realLink : system.getProcessLinks()) {
			ProductExchange input = makeInput(realLink);
			ProductExchange output = makeOutput(realLink);
			oldLinks.add(new ProductLink(input, output));
		}
		return oldLinks;
	}

	private ProductExchange makeOutput(ProcessLink realLink) {
		ProductExchange output = new ProductExchange();
		Exchange exchange = realLink.getProviderOutput();
		output.setAmount(exchange.getResultingAmount().getValue());
		output.setFlowId(exchange.getFlow().getId());
		output.setId(exchange.getId());
		output.setProcessId(realLink.getProviderProcess().getId());
		return output;
	}

	private ProductExchange makeInput(ProcessLink realLink) {
		ProductExchange input = new ProductExchange();
		Exchange exchange = realLink.getRecipientInput();
		input.setAmount(exchange.getResultingAmount().getValue());
		input.setDefaultProviderId(exchange.getDefaultProviderId());
		input.setFlowId(exchange.getFlow().getId());
		input.setId(exchange.getId());
		input.setProcessId(realLink.getRecipientProcess().getId());
		return input;
	}

}
