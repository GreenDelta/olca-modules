package org.openlca.ipc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.ipc.handlers.CacheHandler;
import org.openlca.ipc.handlers.Calculator;
import org.openlca.ipc.handlers.CostHandler;
import org.openlca.ipc.handlers.ExportHandler;
import org.openlca.ipc.handlers.HandlerContext;
import org.openlca.ipc.handlers.ImpactHandler;
import org.openlca.ipc.handlers.InventoryHandler;
import org.openlca.ipc.handlers.ModelHandler;
import org.openlca.ipc.handlers.RuntimeHandler;
import org.openlca.ipc.handlers.UpstreamTreeHandler;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<String, Handler> handlers = new HashMap<>();

	public Server(int port) {
		super(port);
	}

	public Server withDefaultHandlers(IDatabase db, MatrixSolver solver) {
		log.info("Register default handlers");
		var cache = new Cache();
		var context = new HandlerContext(this, db, solver, cache);
		register(new ModelHandler(context));
		register(new Calculator(context));
		register(new InventoryHandler(context));
		register(new ImpactHandler(context));
		register(new CostHandler(context));
		register(new CacheHandler(cache));
		register(new RuntimeHandler(context));
		register(new ExportHandler(context));
		register(new UpstreamTreeHandler(context));
		return this;
	}

	@Override
	public void start() {
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			log.info("Started IPC server @{}", getListeningPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers the `Rpc` annotated methods of the given handler as request
	 * handlers.
	 */
	public void register(Object handler) {
		if (handler == null)
			return;
		log.info("Register @Rpc methods from instance of {}",
				handler.getClass());
		try {
			String errorTemplate = "Cannot register method for {}: it must take an"
					+ "RpcRequest parameter and return an RpcResponse";
			for (java.lang.reflect.Method m : handler.getClass().getMethods()) {
				if (!m.isAnnotationPresent(Rpc.class))
					continue;
				String method = m.getAnnotation(Rpc.class).value();
				if (handlers.containsKey(method)) {
					log.error("A handler for method '{}' is already registered",
							method);
					continue;
				}
				Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length != 1 ||
						!Objects.equals(paramTypes[0], RpcRequest.class)) {
					log.error(errorTemplate, method);
					continue;
				}
				if (!Objects.equals(m.getReturnType(), RpcResponse.class)) {
					log.error(errorTemplate, method);
					continue;
				}
				handlers.put(method, new Handler(handler, m));
				log.info("Registered method {}", method);
			}
		} catch (Exception e) {
			log.error("Failed to register handlers", e);
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		String method = session.getMethod().name();
		if (!"POST".equals(method))
			return serve(Responses.requestError("Only understands http POST"));
		try {
			Map<String, String> content = new HashMap<>();
			session.parseBody(content);
			Gson gson = new Gson();
			RpcRequest req = gson.fromJson(content.get("postData"),
					RpcRequest.class);
			log.trace("handle request {}/{}", req.id, req.method);
			RpcResponse resp = getResponse(req);
			return serve(resp);
		} catch (Exception e) {
			return serve(Responses.requestError(e.getMessage()));
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

	private Response serve(RpcResponse r) {
		String json = new Gson().toJson(r);
		Response resp = newFixedLengthResponse(
				Response.Status.OK, "application/json", json);
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods", "POST");
		resp.addHeader("Access-Control-Allow-Headers",
				"Content-Type, Allow-Control-Allow-Headers");
		return resp;
	}

	private class Handler {

		Object instance;
		java.lang.reflect.Method method;

		Handler(Object instance, java.lang.reflect.Method m) {
			this.instance = instance;
			this.method = m;
		}

		RpcResponse invoke(RpcRequest req) {
			try {
				Object result = method.invoke(instance, req);
				if (!(result instanceof RpcResponse))
					return Responses.error(500, result
							+ " is not an RpcResponse", req);
				return (RpcResponse) result;
			} catch (Exception e) {
				log.error("Failed to call method " + method, e);
				return Responses.error(500, "Failed to call method "
						+ method + ": " + e.getMessage(), req);
			}
		}
	}

}
