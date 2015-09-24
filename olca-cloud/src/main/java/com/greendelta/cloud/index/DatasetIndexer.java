package com.greendelta.cloud.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greendelta.cloud.model.data.DatasetIdentifier;
import com.greendelta.cloud.util.Search;

public class DatasetIndexer {

	private final static Logger log = LoggerFactory.getLogger(DatasetIndexer.class);
	private final Directory directory;

	public DatasetIndexer(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory.toPath());
		} catch (IOException e) {
			log.error("Error creating dataset indexer", e);
		}
		this.directory = directory;
	}

	public void index(DatasetIdentifier identifier) {
		index(Collections.singletonList(identifier));
	}

	public void index(Collection<DatasetIdentifier> identifiers) {
		delete(getIds(identifiers));
		IndexWriter writer = Search.getWriter(directory, false);
		try {
			for (DatasetIdentifier identifier : identifiers)
				writer.addDocument(convert(identifier));
			writer.close();
		} catch (IOException e) {
			log.error("Error indexing dataset identifiers", e);
		}
	}

	public void delete(String refId) {
		delete(Collections.singletonList(refId));
	}

	public void delete(List<String> refIds) {
		IndexWriter writer = Search.getWriter(directory, false);
		try {
			for (String refId : refIds) {
				Term term = new Term("refId", refId);
				Query query = new TermQuery(term);
				writer.deleteDocuments(query);
			}
			writer.close();
		} catch (IOException e) {
			log.error("Error deleting dataset identifier indices", e);
		}
	}

	public DatasetIdentifier get(ModelType type, String refId) {
		List<DatasetIdentifier> result = get(type, Collections.singletonList(refId));
		if (result.isEmpty())
			return null;
		return result.get(0);
	}

	public List<DatasetIdentifier> get(ModelType type, List<String> refIds) {
		List<DatasetIdentifier> identifiers = new ArrayList<>();
		IndexSearcher searcher = Search.getSearcher(directory);
		if (searcher == null)
			return Collections.emptyList();
		try {
			for (String refId : refIds) {
				Term term = new Term("refId", refId);
				Query query = new TermQuery(term);
				TopDocs topDocs = searcher.search(query, 1);
				if (topDocs.totalHits == 0)
					continue;
				Document document = searcher.doc(topDocs.scoreDocs[0].doc);
				identifiers.add(convert(document));
			}
			return identifiers;
		} catch (IOException e) {
			log.error("Error retrieving dataset identifiers", e);
			return Collections.emptyList();
		}
	}

	public List<DatasetIdentifier> getAll() {
		List<DatasetIdentifier> identifiers = new ArrayList<>();
		IndexReader reader = Search.getReader(directory);
		if (reader == null)
			return Collections.emptyList();
		try {
			for (int i = 0; i < reader.maxDoc(); i++)
				identifiers.add(convert(reader.document(i)));
			reader.close();
			return identifiers;
		} catch (IOException e) {
			log.error("Error retrieving all dataset identifiers", e);
			return Collections.emptyList();
		}
	}

	private List<String> getIds(Collection<DatasetIdentifier> identifiers) {
		List<String> ids = new ArrayList<>();
		for (DatasetIdentifier identifier : identifiers)
			ids.add(identifier.getRefId());
		return ids;
	}

	private DatasetIdentifier convert(Document document) {
		DatasetIdentifier identifier = new DatasetIdentifier();
		identifier.setRefId(document.get("refId"));
		identifier.setType(ModelType.valueOf(document.get("type")));
		identifier.setLastChange(Long.parseLong(document.get("lastChange")));
		identifier.setVersion(document.get("version"));
		return identifier;
	}

	private Document convert(DatasetIdentifier identifier) {
		Document document = new Document();
		document.add(Search.toField("refId", identifier.getRefId()));
		document.add(Search.toField("type", identifier.getType().name()));
		document.add(Search.toField("lastChange", identifier.getLastChange()));
		document.add(Search.toField("version", identifier.getVersion()));
		return document;
	}
}
