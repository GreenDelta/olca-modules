package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
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
				if (!marketProcessIds.contains(descriptor.getId())) {
					includeMarkets(descriptor);
					i++;
					log.trace("finished {} of {}", i, descriptors.size()
							- marketProcessIds.size());
				}
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
		process.getExchanges().removeAll(droppedInputs);
		process.getExchanges().addAll(newExchanges);
		mergeDuplicates(process);
		dao.update(process);
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
		Pair<Exchange, Exchange> pair = null;
		while ((pair = findDuplicate(process)) != null) {
			Exchange first = pair.getLeft();
			Exchange second = pair.getRight();
			second.setAmountValue(first.getAmountValue()
					+ second.getAmountValue());
			process.getExchanges().remove(first);
			second.setUncertainty(null); // TODO: combine distribution values?
		}
	}

	private Pair<Exchange, Exchange> findDuplicate(Process process) {
		for (Exchange first : process.getExchanges()) {
			if (Objects.equals(first, process.getQuantitativeReference()))
				continue;
			for (Exchange second : process.getExchanges()) {
				if (Objects.equals(first, second))
					continue;
				if (isDuplicate(first, second))
					return Pair.of(first, second);
			}
		}
		return null;
	}

	private boolean isDuplicate(Exchange first, Exchange second) {
		return first.isInput() == second.isInput()
				&& Objects.equals(first.getFlow(), second.getFlow())
				&& Objects.equals(first.getUnit(), second.getUnit())
				&& first.getDefaultProviderId() == second
						.getDefaultProviderId();
	}

}
