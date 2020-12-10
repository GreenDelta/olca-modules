package org.openlca.proto.services;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.2)",
    comments = "Source: services.proto")
public final class FlowMapServiceGrpc {

  private FlowMapServiceGrpc() {}

  public static final String SERVICE_NAME = "protolca.services.FlowMapService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo,
      org.openlca.proto.services.Services.Status> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = org.openlca.proto.services.Services.FlowMapInfo.class,
      responseType = org.openlca.proto.services.Services.Status.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo,
      org.openlca.proto.services.Services.Status> getDeleteMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo, org.openlca.proto.services.Services.Status> getDeleteMethod;
    if ((getDeleteMethod = FlowMapServiceGrpc.getDeleteMethod) == null) {
      synchronized (FlowMapServiceGrpc.class) {
        if ((getDeleteMethod = FlowMapServiceGrpc.getDeleteMethod) == null) {
          FlowMapServiceGrpc.getDeleteMethod = getDeleteMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.FlowMapInfo, org.openlca.proto.services.Services.Status>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowMapInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Status.getDefaultInstance()))
              .setSchemaDescriptor(new FlowMapServiceMethodDescriptorSupplier("Delete"))
              .build();
        }
      }
    }
    return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo,
      org.openlca.proto.services.Services.FlowMapStatus> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = org.openlca.proto.services.Services.FlowMapInfo.class,
      responseType = org.openlca.proto.services.Services.FlowMapStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo,
      org.openlca.proto.services.Services.FlowMapStatus> getGetMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.FlowMapInfo, org.openlca.proto.services.Services.FlowMapStatus> getGetMethod;
    if ((getGetMethod = FlowMapServiceGrpc.getGetMethod) == null) {
      synchronized (FlowMapServiceGrpc.class) {
        if ((getGetMethod = FlowMapServiceGrpc.getGetMethod) == null) {
          FlowMapServiceGrpc.getGetMethod = getGetMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.FlowMapInfo, org.openlca.proto.services.Services.FlowMapStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Get"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowMapInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowMapStatus.getDefaultInstance()))
              .setSchemaDescriptor(new FlowMapServiceMethodDescriptorSupplier("Get"))
              .build();
        }
      }
    }
    return getGetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.services.Services.FlowMapInfo> getGetAllMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAll",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.services.Services.FlowMapInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.services.Services.FlowMapInfo> getGetAllMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.services.Services.FlowMapInfo> getGetAllMethod;
    if ((getGetAllMethod = FlowMapServiceGrpc.getGetAllMethod) == null) {
      synchronized (FlowMapServiceGrpc.class) {
        if ((getGetAllMethod = FlowMapServiceGrpc.getGetAllMethod) == null) {
          FlowMapServiceGrpc.getGetAllMethod = getGetAllMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.services.Services.FlowMapInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAll"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowMapInfo.getDefaultInstance()))
              .setSchemaDescriptor(new FlowMapServiceMethodDescriptorSupplier("GetAll"))
              .build();
        }
      }
    }
    return getGetAllMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowMap,
      org.openlca.proto.services.Services.Status> getPutMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Put",
      requestType = org.openlca.proto.Proto.FlowMap.class,
      responseType = org.openlca.proto.services.Services.Status.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowMap,
      org.openlca.proto.services.Services.Status> getPutMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowMap, org.openlca.proto.services.Services.Status> getPutMethod;
    if ((getPutMethod = FlowMapServiceGrpc.getPutMethod) == null) {
      synchronized (FlowMapServiceGrpc.class) {
        if ((getPutMethod = FlowMapServiceGrpc.getPutMethod) == null) {
          FlowMapServiceGrpc.getPutMethod = getPutMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.FlowMap, org.openlca.proto.services.Services.Status>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Put"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.FlowMap.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Status.getDefaultInstance()))
              .setSchemaDescriptor(new FlowMapServiceMethodDescriptorSupplier("Put"))
              .build();
        }
      }
    }
    return getPutMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static FlowMapServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceStub>() {
        @java.lang.Override
        public FlowMapServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FlowMapServiceStub(channel, callOptions);
        }
      };
    return FlowMapServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static FlowMapServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceBlockingStub>() {
        @java.lang.Override
        public FlowMapServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FlowMapServiceBlockingStub(channel, callOptions);
        }
      };
    return FlowMapServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static FlowMapServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<FlowMapServiceFutureStub>() {
        @java.lang.Override
        public FlowMapServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new FlowMapServiceFutureStub(channel, callOptions);
        }
      };
    return FlowMapServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class FlowMapServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Delete the flow map with the given name.
     * </pre>
     */
    public void delete(org.openlca.proto.services.Services.FlowMapInfo request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the flow map with the given name form the
     * database.
     * </pre>
     */
    public void get(org.openlca.proto.services.Services.FlowMapInfo request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the information (basically just the names) of all
     * flow maps that are available in the database.
     * </pre>
     */
    public void getAll(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAllMethod(), responseObserver);
    }

    /**
     * <pre>
     * Inserts the given flow map into the database. It overwrites
     * an existing flow map if there is a flow map with the same
     * name already available in the database.
     * </pre>
     */
    public void put(org.openlca.proto.Proto.FlowMap request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getDeleteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.FlowMapInfo,
                org.openlca.proto.services.Services.Status>(
                  this, METHODID_DELETE)))
          .addMethod(
            getGetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.FlowMapInfo,
                org.openlca.proto.services.Services.FlowMapStatus>(
                  this, METHODID_GET)))
          .addMethod(
            getGetAllMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.services.Services.FlowMapInfo>(
                  this, METHODID_GET_ALL)))
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.FlowMap,
                org.openlca.proto.services.Services.Status>(
                  this, METHODID_PUT)))
          .build();
    }
  }

  /**
   */
  public static final class FlowMapServiceStub extends io.grpc.stub.AbstractAsyncStub<FlowMapServiceStub> {
    private FlowMapServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FlowMapServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FlowMapServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Delete the flow map with the given name.
     * </pre>
     */
    public void delete(org.openlca.proto.services.Services.FlowMapInfo request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the flow map with the given name form the
     * database.
     * </pre>
     */
    public void get(org.openlca.proto.services.Services.FlowMapInfo request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the information (basically just the names) of all
     * flow maps that are available in the database.
     * </pre>
     */
    public void getAll(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapInfo> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetAllMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Inserts the given flow map into the database. It overwrites
     * an existing flow map if there is a flow map with the same
     * name already available in the database.
     * </pre>
     */
    public void put(org.openlca.proto.Proto.FlowMap request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class FlowMapServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<FlowMapServiceBlockingStub> {
    private FlowMapServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FlowMapServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FlowMapServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Delete the flow map with the given name.
     * </pre>
     */
    public org.openlca.proto.services.Services.Status delete(org.openlca.proto.services.Services.FlowMapInfo request) {
      return blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the flow map with the given name form the
     * database.
     * </pre>
     */
    public org.openlca.proto.services.Services.FlowMapStatus get(org.openlca.proto.services.Services.FlowMapInfo request) {
      return blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the information (basically just the names) of all
     * flow maps that are available in the database.
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.services.Services.FlowMapInfo> getAll(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetAllMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Inserts the given flow map into the database. It overwrites
     * an existing flow map if there is a flow map with the same
     * name already available in the database.
     * </pre>
     */
    public org.openlca.proto.services.Services.Status put(org.openlca.proto.Proto.FlowMap request) {
      return blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class FlowMapServiceFutureStub extends io.grpc.stub.AbstractFutureStub<FlowMapServiceFutureStub> {
    private FlowMapServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected FlowMapServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new FlowMapServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Delete the flow map with the given name.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.Status> delete(
        org.openlca.proto.services.Services.FlowMapInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the flow map with the given name form the
     * database.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.FlowMapStatus> get(
        org.openlca.proto.services.Services.FlowMapInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Inserts the given flow map into the database. It overwrites
     * an existing flow map if there is a flow map with the same
     * name already available in the database.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.Status> put(
        org.openlca.proto.Proto.FlowMap request) {
      return futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DELETE = 0;
  private static final int METHODID_GET = 1;
  private static final int METHODID_GET_ALL = 2;
  private static final int METHODID_PUT = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final FlowMapServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(FlowMapServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DELETE:
          serviceImpl.delete((org.openlca.proto.services.Services.FlowMapInfo) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status>) responseObserver);
          break;
        case METHODID_GET:
          serviceImpl.get((org.openlca.proto.services.Services.FlowMapInfo) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapStatus>) responseObserver);
          break;
        case METHODID_GET_ALL:
          serviceImpl.getAll((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowMapInfo>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((org.openlca.proto.Proto.FlowMap) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class FlowMapServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    FlowMapServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.openlca.proto.services.Services.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("FlowMapService");
    }
  }

  private static final class FlowMapServiceFileDescriptorSupplier
      extends FlowMapServiceBaseDescriptorSupplier {
    FlowMapServiceFileDescriptorSupplier() {}
  }

  private static final class FlowMapServiceMethodDescriptorSupplier
      extends FlowMapServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    FlowMapServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (FlowMapServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new FlowMapServiceFileDescriptorSupplier())
              .addMethod(getDeleteMethod())
              .addMethod(getGetMethod())
              .addMethod(getGetAllMethod())
              .addMethod(getPutMethod())
              .build();
        }
      }
    }
    return result;
  }
}
