package org.openlca.io.ecospold2.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketProcessCleanUp implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final ProcessDao dao;
	private Set<Long> marketProcessIds = new HashSet<>();

	public MarketProcessCleanUp(IDatabase database) {
		this.dao = new ProcessDao(database);
	}

	@Override
	public void run() {
		log.trace("run market process clean up");
		try {
			List<ProcessDescriptor> descriptors = dao.getDescriptors();
			for (ProcessDescriptor descriptor : descriptors) {
				if (descriptor.getName().toLowerCase()
						.startsWith("market for "))
					marketProcessIds.add(descriptor.getId());
			}
			log.trace("{} market processes in {} processes found",
					marketProcessIds.size(), descriptors.size());
			int i = 0;
			for (ProcessDescriptor descriptor : descriptors) {
				includeMarkets(descriptor);
				i++;
				log.trace("finished {} of {}", i, descriptors.size());
			}
		} catch (Exception e) {
			log.error("failed to remove market processes in supply chain", e);
		}
	}

	private void includeMarkets(ProcessDescriptor descriptor) {
		Process process = dao.getForId(descriptor.getId());
		log.trace("include market processes in {}", process);
		List<Exchange> newExchanges = new ArrayList<>();
		List<Exchange> droppedInputs = new ArrayList<>();
		for (Exchange input : process.getExchanges()) {
			if (!input.isInput() || input.getDefaultProviderId() == 0)
				continue;
			if (!marketProcessIds.contains(input.getDefaultProviderId()))
				continue;
			List<Exchange> marketExchanges = getMarketExchanges(input);
			if (marketExchanges.isEmpty())
				continue;
			newExchanges.addAll(marketExchanges);
			droppedInputs.add(input);
		}
		if (!droppedInputs.isEmpty()) {
			process.getExchanges().removeAll(droppedInputs);
			process.getExchanges().addAll(newExchanges);
			mergeDuplicates(process);
			dao.update(process);
		}
	}

	private List<Exchange> getMarketExchanges(Exchange input) {
		Process market = dao.getForId(input.getDefaultProviderId());
		if (!matches(market, input))
			return Collections.emptyList();
		Exchange marketRef = market.getQuantitativeReference();
		double factor = input.getAmountValue() / marketRef.getAmountValue();
		List<Exchange> exchanges = new ArrayList<>();
		for (Exchange exchange : market.getExchanges()) {
			if (Objects.equals(exchange, marketRef))
				continue;
			Exchange clone = exchange.clone();
			clone.setAmountValue(exchange.getAmountValue() * factor);
			exchanges.add(clone);
		}
		return exchanges;
	}

	private boolean matches(Process market, Exchange input) {
		if (market == null || input == null
				|| market.getQuantitativeReference() == null)
			return false;
		Exchange marketRef = market.getQuantitativeReference();
		return input.isInput() && !marketRef.isInput()
				&& input.getAmountValue() != 0
				&& marketRef.getAmountValue() != 0
				&& Objects.equals(marketRef.getFlow(), input.getFlow())
				&& Objects.equals(marketRef.getUnit(), input.getUnit());
	}

	private void mergeDuplicates(Process process) {
		List<Exchange> exchanges = process.getExchanges();
		List<Exchange> duplicates = new ArrayList<>();
		for (int i = 0; i < exchanges.size(); i++) {
			Exchange first = process.getExchanges().get(i);
			if (Objects.equals(first, process.getQuantitativeReference()))
				continue;
			for (int j = i + 1; j < exchanges.size(); j++) {
				Exchange second = process.getExchanges().get(j);
				if (!isDuplicate(first, second))
					continue;
				second.setAmountValue(first.getAmountValue()
						+ second.getAmountValue());
				second.setUncertainty(null); // TODO: combine values?
				duplicates.add(first);
			}
		}
		process.getExchanges().removeAll(duplicates);
	}

	private boolean isDuplicate(Exchange first, Exchange second) {
		return first.isInput() == second.isInput()
				&& Objects.equals(first.getFlow(), second.getFlow())
				&& Objects.equals(first.getUnit(), second.getUnit())
				&& first.getDefaultProviderId() == second
						.getDefaultProviderId();
	}

}
