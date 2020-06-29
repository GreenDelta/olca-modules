package org.openlca.io.ecospold2.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketProcessCleanUp implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ProcessDao dao;
	private final IDatabase database;

	public MarketProcessCleanUp(IDatabase database) {
		this.database = database;
		this.dao = new ProcessDao(database);
	}

	@Override
	public void run() {
		log.trace("run market process clean up");
		try {
			List<ProcessDescriptor> marketProcesses = getMarketProcesses();
			int i = 0;
			for (ProcessDescriptor descriptor : marketProcesses) {
				Process marketProcess = dao.getForId(descriptor.id);
				log.trace("replace market process {}", descriptor);
				marketProcess = fixSelfLoop(marketProcess);
				replaceMarket(marketProcess);
				i++;
				log.trace("finished {} of {}", i, marketProcesses.size());
			}
		} catch (Exception e) {
			log.error("failed to remove market processes in supply chain", e);
		}
	}

	private List<ProcessDescriptor> getMarketProcesses() {
		List<ProcessDescriptor> descriptors = dao.getDescriptors();
		List<ProcessDescriptor> marketProcesses = new ArrayList<>();
		for (ProcessDescriptor descriptor : descriptors) {
			if (descriptor.name.toLowerCase().startsWith("market for "))
				marketProcesses.add(descriptor);
		}
		log.trace("{} market processes in {} processes found",
				marketProcesses.size(), descriptors.size());
		return marketProcesses;
	}

	private Process fixSelfLoop(Process marketProcess) {
		Exchange qRef = marketProcess.quantitativeReference;
		Exchange loopInput = null;
		for (Exchange input : marketProcess.exchanges) {
			if (!input.isInput)
				continue;
			if (input.defaultProviderId == marketProcess.id) {
				loopInput = input;
				break;
			}
		}
		if (loopInput == null)
			return marketProcess;
		qRef.amount = qRef.amount - loopInput.amount;
		marketProcess.exchanges.remove(loopInput);
		log.trace("fixed self loop in {}", marketProcess);
		return dao.update(marketProcess);
	}

	private void replaceMarket(Process marketProcess) {
		List<Long> usedIds = getWhereUsed(marketProcess);
		log.trace("replace {} in {} processes", marketProcess, usedIds.size());
		for (long id : usedIds) {
			Process process = dao.getForId(id);
			log.trace("include market processes in {}", process);
			Exchange input = null;
			for (Exchange exchange : process.exchanges) {
				if (exchange.defaultProviderId == marketProcess.id) {
					input = exchange;
					break;
				}
			}
			if (input == null)
				continue;
			List<Exchange> marketExchanges = getMarketExchanges(input,
					marketProcess);
			if (marketExchanges.isEmpty())
				continue;
			process.exchanges.addAll(marketExchanges);
			process.exchanges.remove(input);
			mergeDuplicates(process);
			dao.update(process);
		}
	}

	private List<Long> getWhereUsed(Process marketProcess) {
		String query = "select distinct f_owner from tbl_exchanges where "
				+ "f_default_provider = " + marketProcess.id;
		var list = new ArrayList<Long>();
		NativeSql.on(database).query(query, result -> {
			list.add(result.getLong(1));
			return true;
		});
		return list;
	}

	private List<Exchange> getMarketExchanges(Exchange input, Process market) {
		if (!matches(market, input))
			return Collections.emptyList();
		Exchange marketRef = market.quantitativeReference;
		double factor = input.amount / marketRef.amount;
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : market.exchanges) {
			if (Objects.equals(exchange, marketRef))
				continue;
			Exchange clone = exchange.clone();
			clone.amount = exchange.amount * factor;
			exchanges.add(clone);
		}
		return exchanges;
	}

	private boolean matches(Process market, Exchange input) {
		if (market == null || input == null
				|| market.quantitativeReference == null)
			return false;
		Exchange marketRef = market.quantitativeReference;
		return input.isInput && !marketRef.isInput
				&& input.amount != 0
				&& marketRef.amount != 0
				&& Objects.equals(marketRef.flow, input.flow)
				&& Objects.equals(marketRef.unit, input.unit);
	}

	private void mergeDuplicates(Process process) {
		List<Exchange> exchanges = process.exchanges;
		List<Exchange> duplicates = new ArrayList<>();
		for (int i = 0; i < exchanges.size(); i++) {
			Exchange first = process.exchanges.get(i);
			if (Objects.equals(first, process.quantitativeReference))
				continue;
			for (int j = i + 1; j < exchanges.size(); j++) {
				Exchange second = process.exchanges.get(j);
				if (!isDuplicate(first, second))
					continue;
				second.amount = first.amount
				+ second.amount;
				second.uncertainty = null; // TODO: combine values?
				duplicates.add(first);
			}
		}
		process.exchanges.removeAll(duplicates);
	}

	private boolean isDuplicate(Exchange first, Exchange second) {
		return first.isInput == second.isInput
				&& Objects.equals(first.flow, second.flow)
				&& Objects.equals(first.unit, second.unit)
				&& first.defaultProviderId == second.defaultProviderId;
	}

}
