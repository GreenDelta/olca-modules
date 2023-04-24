package org.openlca.ipc;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.openlca.core.services.JsonResultService;
import org.openlca.core.services.ServerConfig;
import org.openlca.ipc.handlers.DataHandler;
import org.openlca.ipc.handlers.ExportHandler;
import org.openlca.ipc.handlers.HandlerContext;
import org.openlca.ipc.handlers.ResultHandler;
import org.openlca.ipc.handlers.RuntimeHandler;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Server {

	private final ServerConfig config;
	private final HttpServer http;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<String, Handler> handlers = new HashMap<>();

	public Server(ServerConfig config) {
		this.config = config;
		try {
			http = HttpServer.create(new InetSocketAddress(config.port()), 0);
			http.createContext("/", this::handle);
			http.setExecutor(Executors.newFixedThreadPool(
					Math.max(config.threadCount(), 1)));
		} catch (Exception e) {
			throw new RuntimeException("failed to create server", e);
		}
	}

	public Server withDefaultHandlers() {
		log.info("Register default handlers");
		var cache = new Cache();
		var results = JsonResultService.of(config);
		var context = new HandlerContext(this, config, results, cache);
		register(new DataHandler(context));
		register(new ResultHandler(context));
		register(new RuntimeHandler(context));
		register(new ExportHandler(context));
		return this;
	}

	/**
	 * Registers the `Rpc` annotated methods of the given handler as request
	 * handlers.
	 */
	public void register(Object handler) {
		if (handler == null)
			return;
		log.info("Register @Rpc methods from instance of {}", handler.getClass());
		try {
			var error = "Cannot register method for {}: it must take an"
					+ "RpcRequest parameter and return an RpcResponse";
			for (var method : handler.getClass().getMethods()) {
				if (!method.isAnnotationPresent(Rpc.class))
					continue;
				var methodId = method.getAnnotation(Rpc.class).value();
				if (handlers.containsKey(methodId)) {
					log.error("A handler for '{}' is already registered", methodId);
					continue;
				}
				var paramTypes = method.getParameterTypes();
				if (paramTypes.length != 1
						|| !Objects.equals(paramTypes[0], RpcRequest.class)) {
					log.error(error, methodId);
					continue;
				}
				if (!Objects.equals(method.getReturnType(), RpcResponse.class)) {
					log.error(error, methodId);
					continue;
				}
				handlers.put(methodId, new Handler(handler, method));
				log.info("Registered method {}", methodId);
			}
		} catch (Exception e) {
			log.error("Failed to register handlers", e);
		}
	}

	public int getListeningPort() {
		return config.port();
	}

	public void start() {
		http.start();
	}

	public void stop() {
		http.stop(1);
	}

	private void handle(HttpExchange t) {
		var method = t.getRequestMethod();
		if (!"POST".equals(method)) {
			serve(t, Responses.requestError("only HTTP POST is allowed"));
			return;
		}
		try (var body = t.getRequestBody();
				 var reader = new InputStreamReader(body, StandardCharsets.UTF_8)) {
			var req = new Gson().fromJson(reader, RpcRequest.class);
			log.trace("handle request {}/{}", req.id, req.method);
			serve(t, getResponse(req));
		} catch (Exception e) {
			serve(t, Responses.requestError(
					"failed to parse request body: " + e.getMessage()));
		}

	}

	private RpcResponse getResponse(RpcRequest req) {
		if (Strings.nullOrEmpty(req.method))
			return Responses.unknownMethod(req);
		Handler handler = handlers.get(req.method);
		if (handler == null)
			return Responses.unknownMethod(req);
		log.trace("Call method {}", req.method);
		return handler.invoke(req);
	}

	private void serve(HttpExchange t, RpcResponse r) {
		try {
			var headers = t.getResponseHeaders();
			headers.put("Content-Type", List.of("application/json"));
			headers.put("Access-Control-Allow-Origin", List.of("*"));
			headers.put("Access-Control-Allow-Methods", List.of("POST"));
			headers.put("Access-Control-Allow-Headers",
					List.of("Content-Type, Allow-Control-Allow-Headers"));
			var json = new Gson().toJson(r).getBytes(StandardCharsets.UTF_8);
			t.sendResponseHeaders(200, json.length);
			try (var body = t.getResponseBody()) {
				body.write(json);
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to serve response", e);
		}
	}

	private record Handler(Object instance, java.lang.reflect.Method method) {

		RpcResponse invoke(RpcRequest req) {
			try {
				Object result = method.invoke(instance, req);
				if (!(result instanceof RpcResponse))
					return Responses.error(500, result
							+ " is not an RpcResponse", req);
				return (RpcResponse) result;
			} catch (Exception e) {
				return Responses.error(500, "Failed to call method "
						+ method + ": " + e.getMessage(), req);
			}
		}
	}

	public static void main(String[] args) {
		var log = LoggerFactory.getLogger(Server.class);
		try {
			log.info("parse server configuration");
			var config = ServerConfig.parse(args);
			var server = new Server(config).withDefaultHandlers();

			// register a shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {

				// shutdown the server
				try {
					log.info("shutdown server");
					server.stop();
				} catch (Exception e) {
					log.error("failed to shutdown server", e);
				}

				// close the database
				try {
					config.db().close();
				} catch (Exception e) {
					log.error("failed to close database");
				}
			}));

			log.info("start the server");
			server.start();

		} catch (Exception e) {
			System.err.println("failed to start server: " + e.getMessage());
			log.error("failed to start server", e);
		}
	}

}
