package org.openlca.io.openepd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class Api {

	private Api() {
	}

	public static Optional<JsonObject> getRawEpd(Ec3Client client, String id) {
		try {
			var r = client.getEpd(id);
			if (!r.hasJson())
				return Optional.empty();
			var json = r.json();
			return json.isJsonObject()
				? Optional.of(json.getAsJsonObject())
				: Optional.empty();
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Api.class);
			log.error("Failed to download EPD " + id, e);
			return Optional.empty();
		}
	}

	public static Ec3CategoryTree getCategoryTree(Ec3Client client) {
		try {
			var r = client.get("categories/root");
			if (!r.hasJson())
				return Ec3CategoryTree.empty();
			var root = Ec3Category.fromJson(r.json());
			return root.isEmpty()
				? Ec3CategoryTree.empty()
				: Ec3CategoryTree.of(root.get());
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Api.class);
			log.error("Failed to load category index", e);
			return Ec3CategoryTree.empty();
		}
	}

	public static DescriptorRequest descriptors(Ec3Client client) {
		return new DescriptorRequest(client);
	}

	public static class DescriptorRequest {

		private final Ec3Client client;
		private int page = 1;
		private int pageSize = 50;
		private String query = null;

		private DescriptorRequest(Ec3Client client) {
			this.client = client;
		}

		public DescriptorRequest page(int page) {
			this.page = page;
			return this;
		}

		public DescriptorRequest pageSize(int pageSize) {
			this.pageSize = pageSize;
			return this;
		}

		public DescriptorRequest query(String query) {
			this.query = query;
			return this;
		}

		private String path() {
			var path = "/epds" +
				"?page_number=" + page +
				"&page_size=" + pageSize;
			// we remove the field filters for now as this does not always work; e.g.
			// manufacturer is missing  when we add a filter
			// "&fields=id,category,name,description," +
			// "manufacturer,declared_unit,openepd";
			if (query != null) {
				var q = query.trim();
				if (Strings.notEmpty(q)) {
					path += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
				}
			}
			return path;
		}

		public DescriptorResponse get() {
			try {
				return DescriptorResponse.get(this);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(Api.class);
				log.error("Failed to get EPDs for " + path(), e);
				return new DescriptorResponse(page, 0, 0, Collections.emptyList());
			}
		}
	}

	public record DescriptorResponse(
		int page,
		int totalCount,
		int totalPages,
		List<Ec3EpdInfo> descriptors) {

		private static DescriptorResponse get(DescriptorRequest req) {
			var r = req.client.get(req.path());
			List<Ec3EpdInfo> descriptors = r.hasJson()
				? parse(r.json())
				: Collections.emptyList();
			return new DescriptorResponse(
				req.page, r.totalCount(), r.pageCount(), descriptors);
		}

		private static List<Ec3EpdInfo> parse(JsonElement json) {
			if (json == null || !json.isJsonArray())
				return Collections.emptyList();
			return Json.stream(json.getAsJsonArray())
				.map(Ec3EpdInfo::fromJson)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		}
	}

}
