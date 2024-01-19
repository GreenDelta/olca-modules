package org.openlca.core.database.upgrades;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

public class Upgrade12 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 11 };
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
		u.renameColumn(
			"tbl_process_docs",
			"completeness",
			"data_completeness CLOB(64 K)"
		);
		u.renameColumn(
			"tbl_process_docs",
			"sampling",
			"sampling_procedure CLOB(64 K)"
		);
		u.renameColumn(
			"tbl_process_docs",
			"f_dataset_owner",
			"f_data_owner BIGINT"
		);
		u.renameColumn(
			"tbl_process_docs",
			"restrictions",
			"access_restrictions CLOB(64 K)"
		);

		u.createColumn("tbl_process_docs", "use_advice CLOB(64 K)");

		u.createTable(
			"tbl_compliance_declarations",
			"""
				CREATE TABLE tbl_compliance_declarations (
				 	id        BIGINT NOT NULL,
				  f_owner   BIGINT,
				  f_source  BIGINT,
				  details   CLOB(64 K),
				  PRIMARY KEY (id)
				)
				"""
		);

		u.createTable(
			"tbl_reviews",
			"""
				CREATE TABLE tbl_reviews (

				  id           BIGINT NOT NULL,
				  f_owner      BIGINT,
				  review_type  VARCHAR(255),
				  scopes       CLOB(64 K),
				  details      CLOB(64 K),
				  f_report     BIGINT,

				  PRIMARY KEY (id)
				)
				"""
		);

		u.createTable(
			"tbl_actor_links",
			"""
				CREATE TABLE tbl_actor_links (
				    f_owner  BIGINT,
				    f_actor  BIGINT
				)
				"""
		);

		// Copy review_details, id from tbl_process_docs to tbl_reviews
		var reviewUpdateInfos = new ArrayList<ReviewUpdateInfo>();
		var sql = NativeSql.on(u.db);
		sql.query(
			"SELECT id, review_details, f_reviewer FROM tbl_process_docs",
			r -> {
				var info = new ReviewUpdateInfo(u);
				info.setDocId(r.getLong(1));
				info.setReviewDetail(r.getString(2));
				info.setReviewerId(r.getLong(3));
				reviewUpdateInfos.add(info);
				return true;
			}
		);

		String stmt =
			"insert into tbl_reviews (id, f_owner, details) values (?, ?, ?)";
		NativeSql
			.on(u.db)
			.batchInsert(
				stmt,
				1,
				(int i, PreparedStatement ps) -> {
					var info = reviewUpdateInfos.get(i);
					ps.setLong(1, ReviewUpdateInfo.getNextId());
					ps.setLong(2, info.getDocId());
					ps.setString(3, info.getReviewDetail());
					return true;
				}
			);

		// For each review copy f_reviewer from tbl_process_docs to tbl_actor_links
		// and id from tbl_reviews to tbl_actor_links
		var reviewIds = new ArrayList<Long>();
		sql.query(
			"SELECT id FROM tbl_reviews",
			r -> {
				reviewIds.add(r.getLong(1));
				return true;
			}
		);
		if (reviewIds.isEmpty()) return;
		for (var i = 0; i < reviewUpdateInfos.size(); i++) {
			var info = reviewUpdateInfos.get(i);
			info.setReviewerId(reviewIds.get(i));
		}
		String stmt2 =
			"insert into tbl_actor_links (f_owner, f_actor) values (?, ?)";
		NativeSql
			.on(u.db)
			.batchInsert(
				stmt2,
				1,
				(int i, PreparedStatement ps) -> {
					var info = reviewUpdateInfos.get(i);
					ps.setLong(1, info.getReviewId());
					ps.setLong(2, info.getReviewerId());
					return true;
				}
			);
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
			"tbl_unit_groups"
		);
		for (var table : tables) {
			u.createColumn(table, "other_properties BLOB(5 M)");
		}
	}

	private class ReviewUpdateInfo {

		private Long docId;
		private String reviewDetail;
		private Long reviewId;
		private Long reviewerId;
		private static AtomicLong nextId;

		ReviewUpdateInfo(DbUtil u) {
			nextId = new AtomicLong(u.getLastID());
		}

		public static Long getNextId() {
			return nextId.incrementAndGet();
		}

		public Long getDocId() {
			return docId;
		}

		public void setDocId(Long docId) {
			this.docId = docId;
		}

		public String getReviewDetail() {
			return reviewDetail;
		}

		public void setReviewDetail(String reviewDetail) {
			this.reviewDetail = reviewDetail;
		}

		public Long getReviewId() {
			return reviewId;
		}

		public void setReviewId(Long reviewId) {
			this.reviewId = reviewId;
		}

		public Long getReviewerId() {
			return reviewerId;
		}

		public void setReviewerId(Long reviewerId) {
			this.reviewerId = reviewerId;
		}
	}
}
