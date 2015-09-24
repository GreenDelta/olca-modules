package com.greendelta.cloud.index;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greendelta.cloud.model.data.FileReference;
import com.greendelta.cloud.util.Search;

public class CommitIndexer {

	private final static Logger log = LoggerFactory.getLogger(CommitIndexer.class);
	private final Directory directory;

	public CommitIndexer(File indexDirectory) {
		Directory directory = null;
		try {
			directory = FSDirectory.open(indexDirectory.toPath());
		} catch (IOException e) {
			log.error("Error creating commit indexer", e);
		}
		this.directory = directory;
	}

	public void index(String commitId, List<FileReference> fileReferences) {
		IndexWriter writer = Search.getWriter(directory, false);
		try {
			writer.addDocument(convert(commitId, fileReferences));
			writer.close();
		} catch (IOException e) {
			log.error("Error indexing file references", e);
		}
	}

	public List<FileReference> get(String commitId) {
		IndexSearcher searcher = Search.getSearcher(directory);
		Term term = new Term("commitId", commitId);
		Query query = new TermQuery(term);
		try {
			TopDocs topDocs = searcher.search(query, 1);
			if (topDocs.totalHits == 0)
				return Collections.emptyList();
			Document document = searcher.doc(topDocs.scoreDocs[0].doc);
			return convert(document);
		} catch (IOException e) {
			log.error("Error retrieving file references", e);
			return Collections.emptyList();
		}
	}

	private List<FileReference> convert(Document document) {
		try {
			String json = document.get("fileReferences");
			List<FileReference> references = Search.parseFileReferences(json);
			return references;
		} catch (IOException e) {
			log.error("Error converting commit index document", e);
			return Collections.emptyList();
		}
	}

	private Document convert(String commitId, List<FileReference> fileReferences) {
		Document document = new Document();
		document.add(Search.toField("commitId", commitId));
		document.add(Search.toField("fileReferences", fileReferences));
		return document;
	}

}
