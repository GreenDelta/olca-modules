package org.openlca.core.database.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Version;
import org.openlca.util.Strings;

class Upgrade12 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{11};
	}

	@Override
	public int getEndVersion() {
		return 12;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		addOtherProperties(u);
		updateProcessDocs(u);
	}

	private void updateProcessDocs(DbUtil u) {
		u.renameColumn("tbl_process_docs",
				"completeness", "data_completeness CLOB(64 K)");
		u.renameColumn("tbl_process_docs",
				"sampling", "sampling_procedure CLOB(64 K)");
		u.renameColumn("tbl_process_docs",
				"f_dataset_owner", "f_data_owner BIGINT");
		u.renameColumn("tbl_process_docs",
				"restrictions", "access_restrictions CLOB(64 K)");

		u.createColumn("tbl_process_docs", "use_advice CLOB(64 K)");
		u.createColumn("tbl_process_docs", "flow_completeness CLOB(64 K)");

		u.createTable("tbl_compliance_declarations", """
				CREATE TABLE tbl_compliance_declarations (
				 	id        BIGINT NOT NULL,
				  f_owner   BIGINT,
				  f_system  BIGINT,
				  comment   CLOB(64 K),
				  aspects   CLOB(64 K),
				  PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_reviews", """
				CREATE TABLE tbl_reviews (

				  id           BIGINT NOT NULL,
				  f_owner      BIGINT,
				  review_type  VARCHAR(255),
				  scopes       CLOB(64 K),
				  details      CLOB(64 K),
				  assessment   CLOB(64 K),
				  f_report     BIGINT,

				  PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_actor_links", """
				CREATE TABLE tbl_actor_links (
				    f_owner  BIGINT,
				    f_actor  BIGINT
				)
				""");

		// copy review data from processes
		var nextId = new AtomicLong(u.getLastID() + 1);
		var reviews = ReviewInfo.allOf(nextId, u);
		if (!reviews.isEmpty()) {
			ReviewInfo.createReviews(reviews, u.db);
			ReviewInfo.linkReviewers(reviews, u.db);
			ReviewInfo.updateProcessVersions(reviews, u.db);
			u.setLastID(nextId.get() + 1);
		}
	}

	private void addOtherProperties(DbUtil u) {
		var tables = List.of(
				"tbl_actors",
				"tbl_categories",
				"tbl_currencies",
				"tbl_dq_systems",
				"tbl_epds",
				"tbl_flow_properties",
				"tbl_flows",
				"tbl_impact_categories",
				"tbl_impact_methods",
				"tbl_locations",
				"tbl_parameters",
				"tbl_processes",
				"tbl_product_systems",
				"tbl_projects",
				"tbl_results",
				"tbl_social_indicators",
				"tbl_sources",
				"tbl_unit_groups");
		for (var table : tables) {
			u.createColumn(table, "other_properties BLOB(5 M)");
		}
	}

	private record ReviewInfo(
			long docId, long reviewId, String details, long reviewer) {

		static List<ReviewInfo> allOf(AtomicLong nextId, DbUtil u) {
			if (!u.columnExists("tbl_process_docs", "review_details")
					|| !u.columnExists("tbl_process_docs", "f_reviewer"))
				return Collections.emptyList();
			var q = "SELECT id, review_details, f_reviewer FROM tbl_process_docs";
			var infos = new ArrayList<ReviewInfo>();
			NativeSql.on(u.db).query(q, r -> {
				var details = r.getString(2);
				long reviewer = r.getLong(3);
				if (Strings.isBlank(details) && reviewer == 0)
					return true;
				infos.add(new ReviewInfo(
						r.getLong(1),
						nextId.incrementAndGet(),
						details,
						reviewer));
				return true;
			});
			return infos;
		}

		static void createReviews(List<ReviewInfo> infos, IDatabase db) {
			if (infos.isEmpty())
				return;
			var stmt = "insert into tbl_reviews (id, f_owner, details) values (?, ?, ?)";
			NativeSql.on(db).batchInsert(stmt, infos.size(), (i, s) -> {
				var r = infos.get(i);
				s.setLong(1, r.reviewId);
				s.setLong(2, r.docId);
				s.setString(3, r.details);
				return true;
			});
		}

		static void linkReviewers(List<ReviewInfo> infos, IDatabase db) {
			var revs = infos.stream()
					.filter(i -> i.reviewer != 0)
					.toList();
			if (revs.isEmpty())
				return;
			var stmt = "insert into tbl_actor_links (f_owner, f_actor) values (?, ?)";
			NativeSql.on(db).batchInsert(stmt, revs.size(), (i, s) -> {
				var r = revs.get(i);
				s.setLong(1, r.reviewId);
				s.setLong(2, r.reviewer);
				return true;
			});
		}

		static void updateProcessVersions(List<ReviewInfo> infos, IDatabase db) {
			var docIds = infos.stream()
					.map(ReviewInfo::docId)
					.collect(Collectors.toSet());
			var stmt = "select f_process_doc, version, last_change from tbl_processes";
			NativeSql.on(db).updateRows(stmt, r -> {
				var docId = r.getLong(1);
				if (!docIds.contains(docId))
					return true;
				var version = new Version(r.getLong(2));
				version.incMinor();
				r.updateLong(2, version.getValue());
				r.updateLong(3, System.currentTimeMillis());
				r.updateRow();
				return true;
			});
		}
	}
}
