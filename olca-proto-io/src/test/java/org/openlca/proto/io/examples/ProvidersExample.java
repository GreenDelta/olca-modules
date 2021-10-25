package org.openlca.proto.io.examples;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannelBuilder;
import org.openlca.proto.grpc.DataFetchServiceGrpc;

public class ProvidersExample {

	public static void main(String[] args) {
		var channel = ManagedChannelBuilder.forAddress("localhost", 8080)
			.usePlaintext()
			.build();
		var fetch = DataFetchServiceGrpc.newBlockingStub(channel);

		var techFlows = fetch.getTechFlows(Empty.newBuilder().build());
		var productCount = 0;
		var wasteCount = 0;
		while (techFlows.hasNext()) {
			var next = techFlows.next();
			if (next.hasProduct()) {
				productCount++;
			} else if (next.hasWaste()) {
				wasteCount++;
			}
		}

		System.out.printf("found %d provider flows with %d " +
			"product flows and %d waste flows%n",
			productCount + wasteCount, productCount, wasteCount);
		channel.shutdownNow();
	}
}
