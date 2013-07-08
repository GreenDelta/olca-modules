package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the functionality for loading of data for a given set of IDs with a
 * minimal amount of queries but with block sizes of ID sets that are not
 * greater than the allowed maximum list size for queries (see
 * {@link BaseDao#MAX_LIST_SIZE}).
 */
public class BlockFetch<T> {

	private QueryFunction<T> func;

	public BlockFetch(QueryFunction<T> func) {
		this.func = func;
	}

	/** Creates a new BlockFetch and runs the given query function. */
	public static <T> List<T> doFetch(List<Long> ids, QueryFunction<T> function) {
		BlockFetch<T> fetch = new BlockFetch<>(function);
		return fetch.doFetch(ids);
	}

	/**
	 * Split the given IDs into chunks with a size not greater than
	 * {@link Dao#MAX_LIST_SIZE}, run the queries and return the results.
	 */
	public List<T> doFetch(List<Long> ids) {
		if (ids == null || ids.isEmpty())
			return Collections.emptyList();
		List<Long> restToLoad = new ArrayList<>(ids);
		List<T> results = new ArrayList<>();
		while (!restToLoad.isEmpty()) {
			int toPos = restToLoad.size() > BaseDao.MAX_LIST_SIZE ? BaseDao.MAX_LIST_SIZE
					: restToLoad.size();
			List<Long> nextChunk = restToLoad.subList(0, toPos);
			List<T> chunkResults = func.fetchChunk(nextChunk);
			results.addAll(chunkResults);
			nextChunk.clear(); // clears also the elements in rest
		}
		return results;
	}

	/**
	 * A function that does the query for a chunk of IDs.
	 */
	public interface QueryFunction<T> {
		List<T> fetchChunk(List<Long> ids);
	}

}
