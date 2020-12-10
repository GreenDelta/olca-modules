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
public final class DataServiceGrpc {

  private DataServiceGrpc() {}

  public static final String SERVICE_NAME = "protolca.services.DataService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.Status> getDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Delete",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.Status.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.Status> getDeleteMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.Status> getDeleteMethod;
    if ((getDeleteMethod = DataServiceGrpc.getDeleteMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getDeleteMethod = DataServiceGrpc.getDeleteMethod) == null) {
          DataServiceGrpc.getDeleteMethod = getDeleteMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.Status>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Delete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Status.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("Delete"))
              .build();
        }
      }
    }
    return getDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Actor> getGetActorsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetActors",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Actor.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Actor> getGetActorsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Actor> getGetActorsMethod;
    if ((getGetActorsMethod = DataServiceGrpc.getGetActorsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetActorsMethod = DataServiceGrpc.getGetActorsMethod) == null) {
          DataServiceGrpc.getGetActorsMethod = getGetActorsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Actor>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetActors"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Actor.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetActors"))
              .build();
        }
      }
    }
    return getGetActorsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ActorStatus> getGetActorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetActor",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ActorStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ActorStatus> getGetActorMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ActorStatus> getGetActorMethod;
    if ((getGetActorMethod = DataServiceGrpc.getGetActorMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetActorMethod = DataServiceGrpc.getGetActorMethod) == null) {
          DataServiceGrpc.getGetActorMethod = getGetActorMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ActorStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetActor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ActorStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetActor"))
              .build();
        }
      }
    }
    return getGetActorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Actor,
      org.openlca.proto.services.Services.RefStatus> getPutActorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutActor",
      requestType = org.openlca.proto.Proto.Actor.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Actor,
      org.openlca.proto.services.Services.RefStatus> getPutActorMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Actor, org.openlca.proto.services.Services.RefStatus> getPutActorMethod;
    if ((getPutActorMethod = DataServiceGrpc.getPutActorMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutActorMethod = DataServiceGrpc.getPutActorMethod) == null) {
          DataServiceGrpc.getPutActorMethod = getPutActorMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Actor, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutActor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Actor.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutActor"))
              .build();
        }
      }
    }
    return getPutActorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Category> getGetCategoriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCategories",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Category.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Category> getGetCategoriesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Category> getGetCategoriesMethod;
    if ((getGetCategoriesMethod = DataServiceGrpc.getGetCategoriesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetCategoriesMethod = DataServiceGrpc.getGetCategoriesMethod) == null) {
          DataServiceGrpc.getGetCategoriesMethod = getGetCategoriesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Category>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCategories"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Category.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetCategories"))
              .build();
        }
      }
    }
    return getGetCategoriesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.CategoryStatus> getGetCategoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCategory",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.CategoryStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.CategoryStatus> getGetCategoryMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.CategoryStatus> getGetCategoryMethod;
    if ((getGetCategoryMethod = DataServiceGrpc.getGetCategoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetCategoryMethod = DataServiceGrpc.getGetCategoryMethod) == null) {
          DataServiceGrpc.getGetCategoryMethod = getGetCategoryMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.CategoryStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCategory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.CategoryStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetCategory"))
              .build();
        }
      }
    }
    return getGetCategoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Category,
      org.openlca.proto.services.Services.RefStatus> getPutCategoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutCategory",
      requestType = org.openlca.proto.Proto.Category.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Category,
      org.openlca.proto.services.Services.RefStatus> getPutCategoryMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Category, org.openlca.proto.services.Services.RefStatus> getPutCategoryMethod;
    if ((getPutCategoryMethod = DataServiceGrpc.getPutCategoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutCategoryMethod = DataServiceGrpc.getPutCategoryMethod) == null) {
          DataServiceGrpc.getPutCategoryMethod = getPutCategoryMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Category, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutCategory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Category.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutCategory"))
              .build();
        }
      }
    }
    return getPutCategoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Currency> getGetCurrenciesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCurrencies",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Currency.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Currency> getGetCurrenciesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Currency> getGetCurrenciesMethod;
    if ((getGetCurrenciesMethod = DataServiceGrpc.getGetCurrenciesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetCurrenciesMethod = DataServiceGrpc.getGetCurrenciesMethod) == null) {
          DataServiceGrpc.getGetCurrenciesMethod = getGetCurrenciesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Currency>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCurrencies"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Currency.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetCurrencies"))
              .build();
        }
      }
    }
    return getGetCurrenciesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.CurrencyStatus> getGetCurrencyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCurrency",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.CurrencyStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.CurrencyStatus> getGetCurrencyMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.CurrencyStatus> getGetCurrencyMethod;
    if ((getGetCurrencyMethod = DataServiceGrpc.getGetCurrencyMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetCurrencyMethod = DataServiceGrpc.getGetCurrencyMethod) == null) {
          DataServiceGrpc.getGetCurrencyMethod = getGetCurrencyMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.CurrencyStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCurrency"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.CurrencyStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetCurrency"))
              .build();
        }
      }
    }
    return getGetCurrencyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Currency,
      org.openlca.proto.services.Services.RefStatus> getPutCurrencyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutCurrency",
      requestType = org.openlca.proto.Proto.Currency.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Currency,
      org.openlca.proto.services.Services.RefStatus> getPutCurrencyMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Currency, org.openlca.proto.services.Services.RefStatus> getPutCurrencyMethod;
    if ((getPutCurrencyMethod = DataServiceGrpc.getPutCurrencyMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutCurrencyMethod = DataServiceGrpc.getPutCurrencyMethod) == null) {
          DataServiceGrpc.getPutCurrencyMethod = getPutCurrencyMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Currency, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutCurrency"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Currency.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutCurrency"))
              .build();
        }
      }
    }
    return getPutCurrencyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.DQSystem> getGetDQSystemsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDQSystems",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.DQSystem.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.DQSystem> getGetDQSystemsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.DQSystem> getGetDQSystemsMethod;
    if ((getGetDQSystemsMethod = DataServiceGrpc.getGetDQSystemsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetDQSystemsMethod = DataServiceGrpc.getGetDQSystemsMethod) == null) {
          DataServiceGrpc.getGetDQSystemsMethod = getGetDQSystemsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.DQSystem>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDQSystems"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.DQSystem.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetDQSystems"))
              .build();
        }
      }
    }
    return getGetDQSystemsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.DQSystemStatus> getGetDQSystemMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDQSystem",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.DQSystemStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.DQSystemStatus> getGetDQSystemMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.DQSystemStatus> getGetDQSystemMethod;
    if ((getGetDQSystemMethod = DataServiceGrpc.getGetDQSystemMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetDQSystemMethod = DataServiceGrpc.getGetDQSystemMethod) == null) {
          DataServiceGrpc.getGetDQSystemMethod = getGetDQSystemMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.DQSystemStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDQSystem"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.DQSystemStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetDQSystem"))
              .build();
        }
      }
    }
    return getGetDQSystemMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.DQSystem,
      org.openlca.proto.services.Services.RefStatus> getPutDQSystemMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutDQSystem",
      requestType = org.openlca.proto.Proto.DQSystem.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.DQSystem,
      org.openlca.proto.services.Services.RefStatus> getPutDQSystemMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.DQSystem, org.openlca.proto.services.Services.RefStatus> getPutDQSystemMethod;
    if ((getPutDQSystemMethod = DataServiceGrpc.getPutDQSystemMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutDQSystemMethod = DataServiceGrpc.getPutDQSystemMethod) == null) {
          DataServiceGrpc.getPutDQSystemMethod = getPutDQSystemMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.DQSystem, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutDQSystem"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.DQSystem.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutDQSystem"))
              .build();
        }
      }
    }
    return getPutDQSystemMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Flow> getGetFlowsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFlows",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Flow.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Flow> getGetFlowsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Flow> getGetFlowsMethod;
    if ((getGetFlowsMethod = DataServiceGrpc.getGetFlowsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetFlowsMethod = DataServiceGrpc.getGetFlowsMethod) == null) {
          DataServiceGrpc.getGetFlowsMethod = getGetFlowsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Flow>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFlows"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Flow.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetFlows"))
              .build();
        }
      }
    }
    return getGetFlowsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.FlowStatus> getGetFlowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFlow",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.FlowStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.FlowStatus> getGetFlowMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.FlowStatus> getGetFlowMethod;
    if ((getGetFlowMethod = DataServiceGrpc.getGetFlowMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetFlowMethod = DataServiceGrpc.getGetFlowMethod) == null) {
          DataServiceGrpc.getGetFlowMethod = getGetFlowMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.FlowStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFlow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetFlow"))
              .build();
        }
      }
    }
    return getGetFlowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Flow,
      org.openlca.proto.services.Services.RefStatus> getPutFlowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutFlow",
      requestType = org.openlca.proto.Proto.Flow.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Flow,
      org.openlca.proto.services.Services.RefStatus> getPutFlowMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Flow, org.openlca.proto.services.Services.RefStatus> getPutFlowMethod;
    if ((getPutFlowMethod = DataServiceGrpc.getPutFlowMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutFlowMethod = DataServiceGrpc.getPutFlowMethod) == null) {
          DataServiceGrpc.getPutFlowMethod = getPutFlowMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Flow, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutFlow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Flow.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutFlow"))
              .build();
        }
      }
    }
    return getPutFlowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.FlowProperty> getGetFlowPropertiesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFlowProperties",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.FlowProperty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.FlowProperty> getGetFlowPropertiesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.FlowProperty> getGetFlowPropertiesMethod;
    if ((getGetFlowPropertiesMethod = DataServiceGrpc.getGetFlowPropertiesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetFlowPropertiesMethod = DataServiceGrpc.getGetFlowPropertiesMethod) == null) {
          DataServiceGrpc.getGetFlowPropertiesMethod = getGetFlowPropertiesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.FlowProperty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFlowProperties"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.FlowProperty.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetFlowProperties"))
              .build();
        }
      }
    }
    return getGetFlowPropertiesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.FlowPropertyStatus> getGetFlowPropertyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFlowProperty",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.FlowPropertyStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.FlowPropertyStatus> getGetFlowPropertyMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.FlowPropertyStatus> getGetFlowPropertyMethod;
    if ((getGetFlowPropertyMethod = DataServiceGrpc.getGetFlowPropertyMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetFlowPropertyMethod = DataServiceGrpc.getGetFlowPropertyMethod) == null) {
          DataServiceGrpc.getGetFlowPropertyMethod = getGetFlowPropertyMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.FlowPropertyStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFlowProperty"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.FlowPropertyStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetFlowProperty"))
              .build();
        }
      }
    }
    return getGetFlowPropertyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowProperty,
      org.openlca.proto.services.Services.RefStatus> getPutFlowPropertyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutFlowProperty",
      requestType = org.openlca.proto.Proto.FlowProperty.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowProperty,
      org.openlca.proto.services.Services.RefStatus> getPutFlowPropertyMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowProperty, org.openlca.proto.services.Services.RefStatus> getPutFlowPropertyMethod;
    if ((getPutFlowPropertyMethod = DataServiceGrpc.getPutFlowPropertyMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutFlowPropertyMethod = DataServiceGrpc.getPutFlowPropertyMethod) == null) {
          DataServiceGrpc.getPutFlowPropertyMethod = getPutFlowPropertyMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.FlowProperty, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutFlowProperty"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.FlowProperty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutFlowProperty"))
              .build();
        }
      }
    }
    return getPutFlowPropertyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ImpactCategory> getGetImpactCategoriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetImpactCategories",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.ImpactCategory.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ImpactCategory> getGetImpactCategoriesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ImpactCategory> getGetImpactCategoriesMethod;
    if ((getGetImpactCategoriesMethod = DataServiceGrpc.getGetImpactCategoriesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetImpactCategoriesMethod = DataServiceGrpc.getGetImpactCategoriesMethod) == null) {
          DataServiceGrpc.getGetImpactCategoriesMethod = getGetImpactCategoriesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ImpactCategory>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetImpactCategories"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ImpactCategory.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetImpactCategories"))
              .build();
        }
      }
    }
    return getGetImpactCategoriesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ImpactCategoryStatus> getGetImpactCategoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetImpactCategory",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ImpactCategoryStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ImpactCategoryStatus> getGetImpactCategoryMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ImpactCategoryStatus> getGetImpactCategoryMethod;
    if ((getGetImpactCategoryMethod = DataServiceGrpc.getGetImpactCategoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetImpactCategoryMethod = DataServiceGrpc.getGetImpactCategoryMethod) == null) {
          DataServiceGrpc.getGetImpactCategoryMethod = getGetImpactCategoryMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ImpactCategoryStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetImpactCategory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ImpactCategoryStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetImpactCategory"))
              .build();
        }
      }
    }
    return getGetImpactCategoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactCategory,
      org.openlca.proto.services.Services.RefStatus> getPutImpactCategoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutImpactCategory",
      requestType = org.openlca.proto.Proto.ImpactCategory.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactCategory,
      org.openlca.proto.services.Services.RefStatus> getPutImpactCategoryMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactCategory, org.openlca.proto.services.Services.RefStatus> getPutImpactCategoryMethod;
    if ((getPutImpactCategoryMethod = DataServiceGrpc.getPutImpactCategoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutImpactCategoryMethod = DataServiceGrpc.getPutImpactCategoryMethod) == null) {
          DataServiceGrpc.getPutImpactCategoryMethod = getPutImpactCategoryMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.ImpactCategory, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutImpactCategory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ImpactCategory.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutImpactCategory"))
              .build();
        }
      }
    }
    return getPutImpactCategoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ImpactMethod> getGetImpactMethodsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetImpactMethods",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.ImpactMethod.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ImpactMethod> getGetImpactMethodsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ImpactMethod> getGetImpactMethodsMethod;
    if ((getGetImpactMethodsMethod = DataServiceGrpc.getGetImpactMethodsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetImpactMethodsMethod = DataServiceGrpc.getGetImpactMethodsMethod) == null) {
          DataServiceGrpc.getGetImpactMethodsMethod = getGetImpactMethodsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ImpactMethod>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetImpactMethods"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ImpactMethod.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetImpactMethods"))
              .build();
        }
      }
    }
    return getGetImpactMethodsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ImpactMethodStatus> getGetImpactMethodMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetImpactMethod",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ImpactMethodStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ImpactMethodStatus> getGetImpactMethodMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ImpactMethodStatus> getGetImpactMethodMethod;
    if ((getGetImpactMethodMethod = DataServiceGrpc.getGetImpactMethodMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetImpactMethodMethod = DataServiceGrpc.getGetImpactMethodMethod) == null) {
          DataServiceGrpc.getGetImpactMethodMethod = getGetImpactMethodMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ImpactMethodStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetImpactMethod"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ImpactMethodStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetImpactMethod"))
              .build();
        }
      }
    }
    return getGetImpactMethodMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactMethod,
      org.openlca.proto.services.Services.RefStatus> getPutImpactMethodMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutImpactMethod",
      requestType = org.openlca.proto.Proto.ImpactMethod.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactMethod,
      org.openlca.proto.services.Services.RefStatus> getPutImpactMethodMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.ImpactMethod, org.openlca.proto.services.Services.RefStatus> getPutImpactMethodMethod;
    if ((getPutImpactMethodMethod = DataServiceGrpc.getPutImpactMethodMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutImpactMethodMethod = DataServiceGrpc.getPutImpactMethodMethod) == null) {
          DataServiceGrpc.getPutImpactMethodMethod = getPutImpactMethodMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.ImpactMethod, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutImpactMethod"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ImpactMethod.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutImpactMethod"))
              .build();
        }
      }
    }
    return getPutImpactMethodMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Location> getGetLocationsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLocations",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Location.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Location> getGetLocationsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Location> getGetLocationsMethod;
    if ((getGetLocationsMethod = DataServiceGrpc.getGetLocationsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetLocationsMethod = DataServiceGrpc.getGetLocationsMethod) == null) {
          DataServiceGrpc.getGetLocationsMethod = getGetLocationsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Location>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLocations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Location.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetLocations"))
              .build();
        }
      }
    }
    return getGetLocationsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.LocationStatus> getGetLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLocation",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.LocationStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.LocationStatus> getGetLocationMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.LocationStatus> getGetLocationMethod;
    if ((getGetLocationMethod = DataServiceGrpc.getGetLocationMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetLocationMethod = DataServiceGrpc.getGetLocationMethod) == null) {
          DataServiceGrpc.getGetLocationMethod = getGetLocationMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.LocationStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.LocationStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetLocation"))
              .build();
        }
      }
    }
    return getGetLocationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Location,
      org.openlca.proto.services.Services.RefStatus> getPutLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutLocation",
      requestType = org.openlca.proto.Proto.Location.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Location,
      org.openlca.proto.services.Services.RefStatus> getPutLocationMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Location, org.openlca.proto.services.Services.RefStatus> getPutLocationMethod;
    if ((getPutLocationMethod = DataServiceGrpc.getPutLocationMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutLocationMethod = DataServiceGrpc.getPutLocationMethod) == null) {
          DataServiceGrpc.getPutLocationMethod = getPutLocationMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Location, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Location.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutLocation"))
              .build();
        }
      }
    }
    return getPutLocationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Parameter> getGetParametersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetParameters",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Parameter.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Parameter> getGetParametersMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Parameter> getGetParametersMethod;
    if ((getGetParametersMethod = DataServiceGrpc.getGetParametersMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetParametersMethod = DataServiceGrpc.getGetParametersMethod) == null) {
          DataServiceGrpc.getGetParametersMethod = getGetParametersMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Parameter>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetParameters"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Parameter.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetParameters"))
              .build();
        }
      }
    }
    return getGetParametersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ParameterStatus> getGetParameterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetParameter",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ParameterStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ParameterStatus> getGetParameterMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ParameterStatus> getGetParameterMethod;
    if ((getGetParameterMethod = DataServiceGrpc.getGetParameterMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetParameterMethod = DataServiceGrpc.getGetParameterMethod) == null) {
          DataServiceGrpc.getGetParameterMethod = getGetParameterMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ParameterStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetParameter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ParameterStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetParameter"))
              .build();
        }
      }
    }
    return getGetParameterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Parameter,
      org.openlca.proto.services.Services.RefStatus> getPutParameterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutParameter",
      requestType = org.openlca.proto.Proto.Parameter.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Parameter,
      org.openlca.proto.services.Services.RefStatus> getPutParameterMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Parameter, org.openlca.proto.services.Services.RefStatus> getPutParameterMethod;
    if ((getPutParameterMethod = DataServiceGrpc.getPutParameterMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutParameterMethod = DataServiceGrpc.getPutParameterMethod) == null) {
          DataServiceGrpc.getPutParameterMethod = getPutParameterMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Parameter, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutParameter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Parameter.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutParameter"))
              .build();
        }
      }
    }
    return getPutParameterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Process> getGetProcessesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProcesses",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Process.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Process> getGetProcessesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Process> getGetProcessesMethod;
    if ((getGetProcessesMethod = DataServiceGrpc.getGetProcessesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProcessesMethod = DataServiceGrpc.getGetProcessesMethod) == null) {
          DataServiceGrpc.getGetProcessesMethod = getGetProcessesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Process>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProcesses"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Process.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProcesses"))
              .build();
        }
      }
    }
    return getGetProcessesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProcessStatus> getGetProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProcess",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ProcessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProcessStatus> getGetProcessMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProcessStatus> getGetProcessMethod;
    if ((getGetProcessMethod = DataServiceGrpc.getGetProcessMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProcessMethod = DataServiceGrpc.getGetProcessMethod) == null) {
          DataServiceGrpc.getGetProcessMethod = getGetProcessMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProcessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProcess"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ProcessStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProcess"))
              .build();
        }
      }
    }
    return getGetProcessMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Process,
      org.openlca.proto.services.Services.RefStatus> getPutProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutProcess",
      requestType = org.openlca.proto.Proto.Process.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Process,
      org.openlca.proto.services.Services.RefStatus> getPutProcessMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Process, org.openlca.proto.services.Services.RefStatus> getPutProcessMethod;
    if ((getPutProcessMethod = DataServiceGrpc.getPutProcessMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutProcessMethod = DataServiceGrpc.getPutProcessMethod) == null) {
          DataServiceGrpc.getPutProcessMethod = getPutProcessMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Process, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutProcess"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Process.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutProcess"))
              .build();
        }
      }
    }
    return getPutProcessMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ProductSystem> getGetProductSystemsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProductSystems",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.ProductSystem.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.ProductSystem> getGetProductSystemsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ProductSystem> getGetProductSystemsMethod;
    if ((getGetProductSystemsMethod = DataServiceGrpc.getGetProductSystemsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProductSystemsMethod = DataServiceGrpc.getGetProductSystemsMethod) == null) {
          DataServiceGrpc.getGetProductSystemsMethod = getGetProductSystemsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.ProductSystem>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProductSystems"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ProductSystem.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProductSystems"))
              .build();
        }
      }
    }
    return getGetProductSystemsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProductSystemStatus> getGetProductSystemMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProductSystem",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ProductSystemStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProductSystemStatus> getGetProductSystemMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProductSystemStatus> getGetProductSystemMethod;
    if ((getGetProductSystemMethod = DataServiceGrpc.getGetProductSystemMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProductSystemMethod = DataServiceGrpc.getGetProductSystemMethod) == null) {
          DataServiceGrpc.getGetProductSystemMethod = getGetProductSystemMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProductSystemStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProductSystem"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ProductSystemStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProductSystem"))
              .build();
        }
      }
    }
    return getGetProductSystemMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.ProductSystem,
      org.openlca.proto.services.Services.RefStatus> getPutProductSystemMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutProductSystem",
      requestType = org.openlca.proto.Proto.ProductSystem.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.ProductSystem,
      org.openlca.proto.services.Services.RefStatus> getPutProductSystemMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.ProductSystem, org.openlca.proto.services.Services.RefStatus> getPutProductSystemMethod;
    if ((getPutProductSystemMethod = DataServiceGrpc.getPutProductSystemMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutProductSystemMethod = DataServiceGrpc.getPutProductSystemMethod) == null) {
          DataServiceGrpc.getPutProductSystemMethod = getPutProductSystemMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.ProductSystem, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutProductSystem"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ProductSystem.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutProductSystem"))
              .build();
        }
      }
    }
    return getPutProductSystemMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Project> getGetProjectsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProjects",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Project.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Project> getGetProjectsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Project> getGetProjectsMethod;
    if ((getGetProjectsMethod = DataServiceGrpc.getGetProjectsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProjectsMethod = DataServiceGrpc.getGetProjectsMethod) == null) {
          DataServiceGrpc.getGetProjectsMethod = getGetProjectsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Project>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProjects"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Project.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProjects"))
              .build();
        }
      }
    }
    return getGetProjectsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProjectStatus> getGetProjectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProject",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.ProjectStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.ProjectStatus> getGetProjectMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProjectStatus> getGetProjectMethod;
    if ((getGetProjectMethod = DataServiceGrpc.getGetProjectMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProjectMethod = DataServiceGrpc.getGetProjectMethod) == null) {
          DataServiceGrpc.getGetProjectMethod = getGetProjectMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.ProjectStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProject"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.ProjectStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProject"))
              .build();
        }
      }
    }
    return getGetProjectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Project,
      org.openlca.proto.services.Services.RefStatus> getPutProjectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutProject",
      requestType = org.openlca.proto.Proto.Project.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Project,
      org.openlca.proto.services.Services.RefStatus> getPutProjectMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Project, org.openlca.proto.services.Services.RefStatus> getPutProjectMethod;
    if ((getPutProjectMethod = DataServiceGrpc.getPutProjectMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutProjectMethod = DataServiceGrpc.getPutProjectMethod) == null) {
          DataServiceGrpc.getPutProjectMethod = getPutProjectMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Project, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutProject"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Project.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutProject"))
              .build();
        }
      }
    }
    return getPutProjectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.SocialIndicator> getGetSocialIndicatorsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSocialIndicators",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.SocialIndicator.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.SocialIndicator> getGetSocialIndicatorsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.SocialIndicator> getGetSocialIndicatorsMethod;
    if ((getGetSocialIndicatorsMethod = DataServiceGrpc.getGetSocialIndicatorsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetSocialIndicatorsMethod = DataServiceGrpc.getGetSocialIndicatorsMethod) == null) {
          DataServiceGrpc.getGetSocialIndicatorsMethod = getGetSocialIndicatorsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.SocialIndicator>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSocialIndicators"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.SocialIndicator.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetSocialIndicators"))
              .build();
        }
      }
    }
    return getGetSocialIndicatorsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.SocialIndicatorStatus> getGetSocialIndicatorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSocialIndicator",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.SocialIndicatorStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.SocialIndicatorStatus> getGetSocialIndicatorMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.SocialIndicatorStatus> getGetSocialIndicatorMethod;
    if ((getGetSocialIndicatorMethod = DataServiceGrpc.getGetSocialIndicatorMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetSocialIndicatorMethod = DataServiceGrpc.getGetSocialIndicatorMethod) == null) {
          DataServiceGrpc.getGetSocialIndicatorMethod = getGetSocialIndicatorMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.SocialIndicatorStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSocialIndicator"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.SocialIndicatorStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetSocialIndicator"))
              .build();
        }
      }
    }
    return getGetSocialIndicatorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.SocialIndicator,
      org.openlca.proto.services.Services.RefStatus> getPutSocialIndicatorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutSocialIndicator",
      requestType = org.openlca.proto.Proto.SocialIndicator.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.SocialIndicator,
      org.openlca.proto.services.Services.RefStatus> getPutSocialIndicatorMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.SocialIndicator, org.openlca.proto.services.Services.RefStatus> getPutSocialIndicatorMethod;
    if ((getPutSocialIndicatorMethod = DataServiceGrpc.getPutSocialIndicatorMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutSocialIndicatorMethod = DataServiceGrpc.getPutSocialIndicatorMethod) == null) {
          DataServiceGrpc.getPutSocialIndicatorMethod = getPutSocialIndicatorMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.SocialIndicator, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutSocialIndicator"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.SocialIndicator.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutSocialIndicator"))
              .build();
        }
      }
    }
    return getPutSocialIndicatorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Source> getGetSourcesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSources",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.Source.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.Source> getGetSourcesMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Source> getGetSourcesMethod;
    if ((getGetSourcesMethod = DataServiceGrpc.getGetSourcesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetSourcesMethod = DataServiceGrpc.getGetSourcesMethod) == null) {
          DataServiceGrpc.getGetSourcesMethod = getGetSourcesMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.Source>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSources"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Source.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetSources"))
              .build();
        }
      }
    }
    return getGetSourcesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.SourceStatus> getGetSourceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSource",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.SourceStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.SourceStatus> getGetSourceMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.SourceStatus> getGetSourceMethod;
    if ((getGetSourceMethod = DataServiceGrpc.getGetSourceMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetSourceMethod = DataServiceGrpc.getGetSourceMethod) == null) {
          DataServiceGrpc.getGetSourceMethod = getGetSourceMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.SourceStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSource"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.SourceStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetSource"))
              .build();
        }
      }
    }
    return getGetSourceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Source,
      org.openlca.proto.services.Services.RefStatus> getPutSourceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutSource",
      requestType = org.openlca.proto.Proto.Source.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Source,
      org.openlca.proto.services.Services.RefStatus> getPutSourceMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Source, org.openlca.proto.services.Services.RefStatus> getPutSourceMethod;
    if ((getPutSourceMethod = DataServiceGrpc.getPutSourceMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutSourceMethod = DataServiceGrpc.getPutSourceMethod) == null) {
          DataServiceGrpc.getPutSourceMethod = getPutSourceMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Source, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutSource"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Source.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutSource"))
              .build();
        }
      }
    }
    return getPutSourceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.UnitGroup> getGetUnitGroupsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUnitGroups",
      requestType = org.openlca.proto.services.Services.Empty.class,
      responseType = org.openlca.proto.Proto.UnitGroup.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty,
      org.openlca.proto.Proto.UnitGroup> getGetUnitGroupsMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.UnitGroup> getGetUnitGroupsMethod;
    if ((getGetUnitGroupsMethod = DataServiceGrpc.getGetUnitGroupsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetUnitGroupsMethod = DataServiceGrpc.getGetUnitGroupsMethod) == null) {
          DataServiceGrpc.getGetUnitGroupsMethod = getGetUnitGroupsMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.services.Services.Empty, org.openlca.proto.Proto.UnitGroup>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUnitGroups"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.UnitGroup.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetUnitGroups"))
              .build();
        }
      }
    }
    return getGetUnitGroupsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.UnitGroupStatus> getGetUnitGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUnitGroup",
      requestType = org.openlca.proto.Proto.Ref.class,
      responseType = org.openlca.proto.services.Services.UnitGroupStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref,
      org.openlca.proto.services.Services.UnitGroupStatus> getGetUnitGroupMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.UnitGroupStatus> getGetUnitGroupMethod;
    if ((getGetUnitGroupMethod = DataServiceGrpc.getGetUnitGroupMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetUnitGroupMethod = DataServiceGrpc.getGetUnitGroupMethod) == null) {
          DataServiceGrpc.getGetUnitGroupMethod = getGetUnitGroupMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.Ref, org.openlca.proto.services.Services.UnitGroupStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUnitGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.Ref.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.UnitGroupStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetUnitGroup"))
              .build();
        }
      }
    }
    return getGetUnitGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.UnitGroup,
      org.openlca.proto.services.Services.RefStatus> getPutUnitGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutUnitGroup",
      requestType = org.openlca.proto.Proto.UnitGroup.class,
      responseType = org.openlca.proto.services.Services.RefStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.UnitGroup,
      org.openlca.proto.services.Services.RefStatus> getPutUnitGroupMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.UnitGroup, org.openlca.proto.services.Services.RefStatus> getPutUnitGroupMethod;
    if ((getPutUnitGroupMethod = DataServiceGrpc.getPutUnitGroupMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getPutUnitGroupMethod = DataServiceGrpc.getPutUnitGroupMethod) == null) {
          DataServiceGrpc.getPutUnitGroupMethod = getPutUnitGroupMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.UnitGroup, org.openlca.proto.services.Services.RefStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutUnitGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.UnitGroup.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.services.Services.RefStatus.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("PutUnitGroup"))
              .build();
        }
      }
    }
    return getPutUnitGroupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowRef,
      org.openlca.proto.Proto.ProcessRef> getGetProvidersForMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetProvidersFor",
      requestType = org.openlca.proto.Proto.FlowRef.class,
      responseType = org.openlca.proto.Proto.ProcessRef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowRef,
      org.openlca.proto.Proto.ProcessRef> getGetProvidersForMethod() {
    io.grpc.MethodDescriptor<org.openlca.proto.Proto.FlowRef, org.openlca.proto.Proto.ProcessRef> getGetProvidersForMethod;
    if ((getGetProvidersForMethod = DataServiceGrpc.getGetProvidersForMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetProvidersForMethod = DataServiceGrpc.getGetProvidersForMethod) == null) {
          DataServiceGrpc.getGetProvidersForMethod = getGetProvidersForMethod =
              io.grpc.MethodDescriptor.<org.openlca.proto.Proto.FlowRef, org.openlca.proto.Proto.ProcessRef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetProvidersFor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.FlowRef.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.openlca.proto.Proto.ProcessRef.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetProvidersFor"))
              .build();
        }
      }
    }
    return getGetProvidersForMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceStub>() {
        @java.lang.Override
        public DataServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceStub(channel, callOptions);
        }
      };
    return DataServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceBlockingStub>() {
        @java.lang.Override
        public DataServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceBlockingStub(channel, callOptions);
        }
      };
    return DataServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceFutureStub>() {
        @java.lang.Override
        public DataServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceFutureStub(channel, callOptions);
        }
      };
    return DataServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class DataServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Deletes the object with the `id` and `type` of the given descriptor from
     * the database. Note that the type is a string with the name of the
     * corresponding model class, e.g. `Process` or `Flow`.
     * </pre>
     */
    public void delete(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Actor
     * </pre>
     */
    public void getActors(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Actor> responseObserver) {
      asyncUnimplementedUnaryCall(getGetActorsMethod(), responseObserver);
    }

    /**
     */
    public void getActor(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ActorStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetActorMethod(), responseObserver);
    }

    /**
     */
    public void putActor(org.openlca.proto.Proto.Actor request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutActorMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Category
     * </pre>
     */
    public void getCategories(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Category> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCategoriesMethod(), responseObserver);
    }

    /**
     */
    public void getCategory(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CategoryStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCategoryMethod(), responseObserver);
    }

    /**
     */
    public void putCategory(org.openlca.proto.Proto.Category request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutCategoryMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Currency
     * </pre>
     */
    public void getCurrencies(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Currency> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCurrenciesMethod(), responseObserver);
    }

    /**
     */
    public void getCurrency(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CurrencyStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCurrencyMethod(), responseObserver);
    }

    /**
     */
    public void putCurrency(org.openlca.proto.Proto.Currency request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutCurrencyMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for DQSystem
     * </pre>
     */
    public void getDQSystems(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.DQSystem> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDQSystemsMethod(), responseObserver);
    }

    /**
     */
    public void getDQSystem(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.DQSystemStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDQSystemMethod(), responseObserver);
    }

    /**
     */
    public void putDQSystem(org.openlca.proto.Proto.DQSystem request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutDQSystemMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Flow
     * </pre>
     */
    public void getFlows(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Flow> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFlowsMethod(), responseObserver);
    }

    /**
     */
    public void getFlow(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFlowMethod(), responseObserver);
    }

    /**
     */
    public void putFlow(org.openlca.proto.Proto.Flow request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutFlowMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for FlowProperty
     * </pre>
     */
    public void getFlowProperties(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.FlowProperty> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFlowPropertiesMethod(), responseObserver);
    }

    /**
     */
    public void getFlowProperty(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowPropertyStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFlowPropertyMethod(), responseObserver);
    }

    /**
     */
    public void putFlowProperty(org.openlca.proto.Proto.FlowProperty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutFlowPropertyMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for ImpactCategory
     * </pre>
     */
    public void getImpactCategories(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactCategory> responseObserver) {
      asyncUnimplementedUnaryCall(getGetImpactCategoriesMethod(), responseObserver);
    }

    /**
     */
    public void getImpactCategory(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactCategoryStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetImpactCategoryMethod(), responseObserver);
    }

    /**
     */
    public void putImpactCategory(org.openlca.proto.Proto.ImpactCategory request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutImpactCategoryMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for ImpactMethod
     * </pre>
     */
    public void getImpactMethods(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactMethod> responseObserver) {
      asyncUnimplementedUnaryCall(getGetImpactMethodsMethod(), responseObserver);
    }

    /**
     */
    public void getImpactMethod(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactMethodStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetImpactMethodMethod(), responseObserver);
    }

    /**
     */
    public void putImpactMethod(org.openlca.proto.Proto.ImpactMethod request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutImpactMethodMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Location
     * </pre>
     */
    public void getLocations(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Location> responseObserver) {
      asyncUnimplementedUnaryCall(getGetLocationsMethod(), responseObserver);
    }

    /**
     */
    public void getLocation(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.LocationStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetLocationMethod(), responseObserver);
    }

    /**
     */
    public void putLocation(org.openlca.proto.Proto.Location request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutLocationMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Parameter
     * </pre>
     */
    public void getParameters(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Parameter> responseObserver) {
      asyncUnimplementedUnaryCall(getGetParametersMethod(), responseObserver);
    }

    /**
     */
    public void getParameter(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ParameterStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetParameterMethod(), responseObserver);
    }

    /**
     */
    public void putParameter(org.openlca.proto.Proto.Parameter request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutParameterMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Process
     * </pre>
     */
    public void getProcesses(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Process> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProcessesMethod(), responseObserver);
    }

    /**
     */
    public void getProcess(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProcessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProcessMethod(), responseObserver);
    }

    /**
     */
    public void putProcess(org.openlca.proto.Proto.Process request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutProcessMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for ProductSystem
     * </pre>
     */
    public void getProductSystems(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProductSystem> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProductSystemsMethod(), responseObserver);
    }

    /**
     */
    public void getProductSystem(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProductSystemStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProductSystemMethod(), responseObserver);
    }

    /**
     */
    public void putProductSystem(org.openlca.proto.Proto.ProductSystem request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutProductSystemMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Project
     * </pre>
     */
    public void getProjects(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Project> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProjectsMethod(), responseObserver);
    }

    /**
     */
    public void getProject(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProjectStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProjectMethod(), responseObserver);
    }

    /**
     */
    public void putProject(org.openlca.proto.Proto.Project request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutProjectMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for SocialIndicator
     * </pre>
     */
    public void getSocialIndicators(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.SocialIndicator> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSocialIndicatorsMethod(), responseObserver);
    }

    /**
     */
    public void getSocialIndicator(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SocialIndicatorStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSocialIndicatorMethod(), responseObserver);
    }

    /**
     */
    public void putSocialIndicator(org.openlca.proto.Proto.SocialIndicator request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutSocialIndicatorMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for Source
     * </pre>
     */
    public void getSources(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Source> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSourcesMethod(), responseObserver);
    }

    /**
     */
    public void getSource(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SourceStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSourceMethod(), responseObserver);
    }

    /**
     */
    public void putSource(org.openlca.proto.Proto.Source request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutSourceMethod(), responseObserver);
    }

    /**
     * <pre>
     * methods for UnitGroup
     * </pre>
     */
    public void getUnitGroups(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.UnitGroup> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUnitGroupsMethod(), responseObserver);
    }

    /**
     */
    public void getUnitGroup(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.UnitGroupStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUnitGroupMethod(), responseObserver);
    }

    /**
     */
    public void putUnitGroup(org.openlca.proto.Proto.UnitGroup request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPutUnitGroupMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get possible providers for the given flow. For products
     * these are processes with that product on the output side
     * and for waste flows processes with that waste flow on the
     * input side. For elementary flows, an empty stream is
     * returned.
     * </pre>
     */
    public void getProvidersFor(org.openlca.proto.Proto.FlowRef request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProcessRef> responseObserver) {
      asyncUnimplementedUnaryCall(getGetProvidersForMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getDeleteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.Status>(
                  this, METHODID_DELETE)))
          .addMethod(
            getGetActorsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Actor>(
                  this, METHODID_GET_ACTORS)))
          .addMethod(
            getGetActorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ActorStatus>(
                  this, METHODID_GET_ACTOR)))
          .addMethod(
            getPutActorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Actor,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_ACTOR)))
          .addMethod(
            getGetCategoriesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Category>(
                  this, METHODID_GET_CATEGORIES)))
          .addMethod(
            getGetCategoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.CategoryStatus>(
                  this, METHODID_GET_CATEGORY)))
          .addMethod(
            getPutCategoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Category,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_CATEGORY)))
          .addMethod(
            getGetCurrenciesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Currency>(
                  this, METHODID_GET_CURRENCIES)))
          .addMethod(
            getGetCurrencyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.CurrencyStatus>(
                  this, METHODID_GET_CURRENCY)))
          .addMethod(
            getPutCurrencyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Currency,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_CURRENCY)))
          .addMethod(
            getGetDQSystemsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.DQSystem>(
                  this, METHODID_GET_DQSYSTEMS)))
          .addMethod(
            getGetDQSystemMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.DQSystemStatus>(
                  this, METHODID_GET_DQSYSTEM)))
          .addMethod(
            getPutDQSystemMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.DQSystem,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_DQSYSTEM)))
          .addMethod(
            getGetFlowsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Flow>(
                  this, METHODID_GET_FLOWS)))
          .addMethod(
            getGetFlowMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.FlowStatus>(
                  this, METHODID_GET_FLOW)))
          .addMethod(
            getPutFlowMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Flow,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_FLOW)))
          .addMethod(
            getGetFlowPropertiesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.FlowProperty>(
                  this, METHODID_GET_FLOW_PROPERTIES)))
          .addMethod(
            getGetFlowPropertyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.FlowPropertyStatus>(
                  this, METHODID_GET_FLOW_PROPERTY)))
          .addMethod(
            getPutFlowPropertyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.FlowProperty,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_FLOW_PROPERTY)))
          .addMethod(
            getGetImpactCategoriesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.ImpactCategory>(
                  this, METHODID_GET_IMPACT_CATEGORIES)))
          .addMethod(
            getGetImpactCategoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ImpactCategoryStatus>(
                  this, METHODID_GET_IMPACT_CATEGORY)))
          .addMethod(
            getPutImpactCategoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.ImpactCategory,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_IMPACT_CATEGORY)))
          .addMethod(
            getGetImpactMethodsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.ImpactMethod>(
                  this, METHODID_GET_IMPACT_METHODS)))
          .addMethod(
            getGetImpactMethodMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ImpactMethodStatus>(
                  this, METHODID_GET_IMPACT_METHOD)))
          .addMethod(
            getPutImpactMethodMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.ImpactMethod,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_IMPACT_METHOD)))
          .addMethod(
            getGetLocationsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Location>(
                  this, METHODID_GET_LOCATIONS)))
          .addMethod(
            getGetLocationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.LocationStatus>(
                  this, METHODID_GET_LOCATION)))
          .addMethod(
            getPutLocationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Location,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_LOCATION)))
          .addMethod(
            getGetParametersMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Parameter>(
                  this, METHODID_GET_PARAMETERS)))
          .addMethod(
            getGetParameterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ParameterStatus>(
                  this, METHODID_GET_PARAMETER)))
          .addMethod(
            getPutParameterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Parameter,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_PARAMETER)))
          .addMethod(
            getGetProcessesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Process>(
                  this, METHODID_GET_PROCESSES)))
          .addMethod(
            getGetProcessMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ProcessStatus>(
                  this, METHODID_GET_PROCESS)))
          .addMethod(
            getPutProcessMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Process,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_PROCESS)))
          .addMethod(
            getGetProductSystemsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.ProductSystem>(
                  this, METHODID_GET_PRODUCT_SYSTEMS)))
          .addMethod(
            getGetProductSystemMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ProductSystemStatus>(
                  this, METHODID_GET_PRODUCT_SYSTEM)))
          .addMethod(
            getPutProductSystemMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.ProductSystem,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_PRODUCT_SYSTEM)))
          .addMethod(
            getGetProjectsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Project>(
                  this, METHODID_GET_PROJECTS)))
          .addMethod(
            getGetProjectMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.ProjectStatus>(
                  this, METHODID_GET_PROJECT)))
          .addMethod(
            getPutProjectMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Project,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_PROJECT)))
          .addMethod(
            getGetSocialIndicatorsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.SocialIndicator>(
                  this, METHODID_GET_SOCIAL_INDICATORS)))
          .addMethod(
            getGetSocialIndicatorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.SocialIndicatorStatus>(
                  this, METHODID_GET_SOCIAL_INDICATOR)))
          .addMethod(
            getPutSocialIndicatorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.SocialIndicator,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_SOCIAL_INDICATOR)))
          .addMethod(
            getGetSourcesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.Source>(
                  this, METHODID_GET_SOURCES)))
          .addMethod(
            getGetSourceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.SourceStatus>(
                  this, METHODID_GET_SOURCE)))
          .addMethod(
            getPutSourceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Source,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_SOURCE)))
          .addMethod(
            getGetUnitGroupsMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.services.Services.Empty,
                org.openlca.proto.Proto.UnitGroup>(
                  this, METHODID_GET_UNIT_GROUPS)))
          .addMethod(
            getGetUnitGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.Ref,
                org.openlca.proto.services.Services.UnitGroupStatus>(
                  this, METHODID_GET_UNIT_GROUP)))
          .addMethod(
            getPutUnitGroupMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.openlca.proto.Proto.UnitGroup,
                org.openlca.proto.services.Services.RefStatus>(
                  this, METHODID_PUT_UNIT_GROUP)))
          .addMethod(
            getGetProvidersForMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.openlca.proto.Proto.FlowRef,
                org.openlca.proto.Proto.ProcessRef>(
                  this, METHODID_GET_PROVIDERS_FOR)))
          .build();
    }
  }

  /**
   */
  public static final class DataServiceStub extends io.grpc.stub.AbstractAsyncStub<DataServiceStub> {
    private DataServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Deletes the object with the `id` and `type` of the given descriptor from
     * the database. Note that the type is a string with the name of the
     * corresponding model class, e.g. `Process` or `Flow`.
     * </pre>
     */
    public void delete(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Actor
     * </pre>
     */
    public void getActors(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Actor> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetActorsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getActor(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ActorStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetActorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putActor(org.openlca.proto.Proto.Actor request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutActorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Category
     * </pre>
     */
    public void getCategories(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Category> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetCategoriesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getCategory(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CategoryStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCategoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putCategory(org.openlca.proto.Proto.Category request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutCategoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Currency
     * </pre>
     */
    public void getCurrencies(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Currency> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetCurrenciesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getCurrency(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CurrencyStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCurrencyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putCurrency(org.openlca.proto.Proto.Currency request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutCurrencyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for DQSystem
     * </pre>
     */
    public void getDQSystems(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.DQSystem> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetDQSystemsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDQSystem(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.DQSystemStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDQSystemMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putDQSystem(org.openlca.proto.Proto.DQSystem request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutDQSystemMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Flow
     * </pre>
     */
    public void getFlows(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Flow> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetFlowsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getFlow(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetFlowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putFlow(org.openlca.proto.Proto.Flow request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutFlowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for FlowProperty
     * </pre>
     */
    public void getFlowProperties(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.FlowProperty> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetFlowPropertiesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getFlowProperty(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowPropertyStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetFlowPropertyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putFlowProperty(org.openlca.proto.Proto.FlowProperty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutFlowPropertyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for ImpactCategory
     * </pre>
     */
    public void getImpactCategories(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactCategory> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetImpactCategoriesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getImpactCategory(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactCategoryStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetImpactCategoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putImpactCategory(org.openlca.proto.Proto.ImpactCategory request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutImpactCategoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for ImpactMethod
     * </pre>
     */
    public void getImpactMethods(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactMethod> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetImpactMethodsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getImpactMethod(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactMethodStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetImpactMethodMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putImpactMethod(org.openlca.proto.Proto.ImpactMethod request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutImpactMethodMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Location
     * </pre>
     */
    public void getLocations(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Location> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetLocationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLocation(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.LocationStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetLocationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putLocation(org.openlca.proto.Proto.Location request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutLocationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Parameter
     * </pre>
     */
    public void getParameters(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Parameter> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetParametersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getParameter(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ParameterStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetParameterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putParameter(org.openlca.proto.Proto.Parameter request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutParameterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Process
     * </pre>
     */
    public void getProcesses(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Process> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetProcessesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getProcess(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProcessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetProcessMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putProcess(org.openlca.proto.Proto.Process request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutProcessMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for ProductSystem
     * </pre>
     */
    public void getProductSystems(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProductSystem> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetProductSystemsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getProductSystem(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProductSystemStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetProductSystemMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putProductSystem(org.openlca.proto.Proto.ProductSystem request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutProductSystemMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Project
     * </pre>
     */
    public void getProjects(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Project> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetProjectsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getProject(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProjectStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetProjectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putProject(org.openlca.proto.Proto.Project request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutProjectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for SocialIndicator
     * </pre>
     */
    public void getSocialIndicators(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.SocialIndicator> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetSocialIndicatorsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSocialIndicator(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SocialIndicatorStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSocialIndicatorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putSocialIndicator(org.openlca.proto.Proto.SocialIndicator request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutSocialIndicatorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for Source
     * </pre>
     */
    public void getSources(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Source> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetSourcesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSource(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SourceStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSourceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putSource(org.openlca.proto.Proto.Source request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutSourceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * methods for UnitGroup
     * </pre>
     */
    public void getUnitGroups(org.openlca.proto.services.Services.Empty request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.UnitGroup> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetUnitGroupsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUnitGroup(org.openlca.proto.Proto.Ref request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.UnitGroupStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetUnitGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putUnitGroup(org.openlca.proto.Proto.UnitGroup request,
        io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutUnitGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get possible providers for the given flow. For products
     * these are processes with that product on the output side
     * and for waste flows processes with that waste flow on the
     * input side. For elementary flows, an empty stream is
     * returned.
     * </pre>
     */
    public void getProvidersFor(org.openlca.proto.Proto.FlowRef request,
        io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProcessRef> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetProvidersForMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DataServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<DataServiceBlockingStub> {
    private DataServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Deletes the object with the `id` and `type` of the given descriptor from
     * the database. Note that the type is a string with the name of the
     * corresponding model class, e.g. `Process` or `Flow`.
     * </pre>
     */
    public org.openlca.proto.services.Services.Status delete(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getDeleteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Actor
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Actor> getActors(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetActorsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ActorStatus getActor(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetActorMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putActor(org.openlca.proto.Proto.Actor request) {
      return blockingUnaryCall(
          getChannel(), getPutActorMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Category
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Category> getCategories(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetCategoriesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.CategoryStatus getCategory(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetCategoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putCategory(org.openlca.proto.Proto.Category request) {
      return blockingUnaryCall(
          getChannel(), getPutCategoryMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Currency
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Currency> getCurrencies(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetCurrenciesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.CurrencyStatus getCurrency(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetCurrencyMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putCurrency(org.openlca.proto.Proto.Currency request) {
      return blockingUnaryCall(
          getChannel(), getPutCurrencyMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for DQSystem
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.DQSystem> getDQSystems(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetDQSystemsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.DQSystemStatus getDQSystem(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetDQSystemMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putDQSystem(org.openlca.proto.Proto.DQSystem request) {
      return blockingUnaryCall(
          getChannel(), getPutDQSystemMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Flow
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Flow> getFlows(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetFlowsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.FlowStatus getFlow(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetFlowMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putFlow(org.openlca.proto.Proto.Flow request) {
      return blockingUnaryCall(
          getChannel(), getPutFlowMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for FlowProperty
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.FlowProperty> getFlowProperties(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetFlowPropertiesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.FlowPropertyStatus getFlowProperty(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetFlowPropertyMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putFlowProperty(org.openlca.proto.Proto.FlowProperty request) {
      return blockingUnaryCall(
          getChannel(), getPutFlowPropertyMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for ImpactCategory
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.ImpactCategory> getImpactCategories(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetImpactCategoriesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ImpactCategoryStatus getImpactCategory(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetImpactCategoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putImpactCategory(org.openlca.proto.Proto.ImpactCategory request) {
      return blockingUnaryCall(
          getChannel(), getPutImpactCategoryMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for ImpactMethod
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.ImpactMethod> getImpactMethods(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetImpactMethodsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ImpactMethodStatus getImpactMethod(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetImpactMethodMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putImpactMethod(org.openlca.proto.Proto.ImpactMethod request) {
      return blockingUnaryCall(
          getChannel(), getPutImpactMethodMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Location
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Location> getLocations(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetLocationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.LocationStatus getLocation(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetLocationMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putLocation(org.openlca.proto.Proto.Location request) {
      return blockingUnaryCall(
          getChannel(), getPutLocationMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Parameter
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Parameter> getParameters(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetParametersMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ParameterStatus getParameter(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetParameterMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putParameter(org.openlca.proto.Proto.Parameter request) {
      return blockingUnaryCall(
          getChannel(), getPutParameterMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Process
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Process> getProcesses(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetProcessesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ProcessStatus getProcess(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetProcessMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putProcess(org.openlca.proto.Proto.Process request) {
      return blockingUnaryCall(
          getChannel(), getPutProcessMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for ProductSystem
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.ProductSystem> getProductSystems(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetProductSystemsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ProductSystemStatus getProductSystem(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetProductSystemMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putProductSystem(org.openlca.proto.Proto.ProductSystem request) {
      return blockingUnaryCall(
          getChannel(), getPutProductSystemMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Project
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Project> getProjects(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetProjectsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.ProjectStatus getProject(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetProjectMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putProject(org.openlca.proto.Proto.Project request) {
      return blockingUnaryCall(
          getChannel(), getPutProjectMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for SocialIndicator
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.SocialIndicator> getSocialIndicators(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetSocialIndicatorsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.SocialIndicatorStatus getSocialIndicator(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetSocialIndicatorMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putSocialIndicator(org.openlca.proto.Proto.SocialIndicator request) {
      return blockingUnaryCall(
          getChannel(), getPutSocialIndicatorMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for Source
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.Source> getSources(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetSourcesMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.SourceStatus getSource(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetSourceMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putSource(org.openlca.proto.Proto.Source request) {
      return blockingUnaryCall(
          getChannel(), getPutSourceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * methods for UnitGroup
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.UnitGroup> getUnitGroups(
        org.openlca.proto.services.Services.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetUnitGroupsMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.UnitGroupStatus getUnitGroup(org.openlca.proto.Proto.Ref request) {
      return blockingUnaryCall(
          getChannel(), getGetUnitGroupMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.openlca.proto.services.Services.RefStatus putUnitGroup(org.openlca.proto.Proto.UnitGroup request) {
      return blockingUnaryCall(
          getChannel(), getPutUnitGroupMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get possible providers for the given flow. For products
     * these are processes with that product on the output side
     * and for waste flows processes with that waste flow on the
     * input side. For elementary flows, an empty stream is
     * returned.
     * </pre>
     */
    public java.util.Iterator<org.openlca.proto.Proto.ProcessRef> getProvidersFor(
        org.openlca.proto.Proto.FlowRef request) {
      return blockingServerStreamingCall(
          getChannel(), getGetProvidersForMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DataServiceFutureStub extends io.grpc.stub.AbstractFutureStub<DataServiceFutureStub> {
    private DataServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Deletes the object with the `id` and `type` of the given descriptor from
     * the database. Note that the type is a string with the name of the
     * corresponding model class, e.g. `Process` or `Flow`.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.Status> delete(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ActorStatus> getActor(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetActorMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putActor(
        org.openlca.proto.Proto.Actor request) {
      return futureUnaryCall(
          getChannel().newCall(getPutActorMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.CategoryStatus> getCategory(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCategoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putCategory(
        org.openlca.proto.Proto.Category request) {
      return futureUnaryCall(
          getChannel().newCall(getPutCategoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.CurrencyStatus> getCurrency(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCurrencyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putCurrency(
        org.openlca.proto.Proto.Currency request) {
      return futureUnaryCall(
          getChannel().newCall(getPutCurrencyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.DQSystemStatus> getDQSystem(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDQSystemMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putDQSystem(
        org.openlca.proto.Proto.DQSystem request) {
      return futureUnaryCall(
          getChannel().newCall(getPutDQSystemMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.FlowStatus> getFlow(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetFlowMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putFlow(
        org.openlca.proto.Proto.Flow request) {
      return futureUnaryCall(
          getChannel().newCall(getPutFlowMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.FlowPropertyStatus> getFlowProperty(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetFlowPropertyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putFlowProperty(
        org.openlca.proto.Proto.FlowProperty request) {
      return futureUnaryCall(
          getChannel().newCall(getPutFlowPropertyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ImpactCategoryStatus> getImpactCategory(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetImpactCategoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putImpactCategory(
        org.openlca.proto.Proto.ImpactCategory request) {
      return futureUnaryCall(
          getChannel().newCall(getPutImpactCategoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ImpactMethodStatus> getImpactMethod(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetImpactMethodMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putImpactMethod(
        org.openlca.proto.Proto.ImpactMethod request) {
      return futureUnaryCall(
          getChannel().newCall(getPutImpactMethodMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.LocationStatus> getLocation(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetLocationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putLocation(
        org.openlca.proto.Proto.Location request) {
      return futureUnaryCall(
          getChannel().newCall(getPutLocationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ParameterStatus> getParameter(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetParameterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putParameter(
        org.openlca.proto.Proto.Parameter request) {
      return futureUnaryCall(
          getChannel().newCall(getPutParameterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ProcessStatus> getProcess(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetProcessMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putProcess(
        org.openlca.proto.Proto.Process request) {
      return futureUnaryCall(
          getChannel().newCall(getPutProcessMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ProductSystemStatus> getProductSystem(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetProductSystemMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putProductSystem(
        org.openlca.proto.Proto.ProductSystem request) {
      return futureUnaryCall(
          getChannel().newCall(getPutProductSystemMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.ProjectStatus> getProject(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetProjectMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putProject(
        org.openlca.proto.Proto.Project request) {
      return futureUnaryCall(
          getChannel().newCall(getPutProjectMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.SocialIndicatorStatus> getSocialIndicator(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSocialIndicatorMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putSocialIndicator(
        org.openlca.proto.Proto.SocialIndicator request) {
      return futureUnaryCall(
          getChannel().newCall(getPutSocialIndicatorMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.SourceStatus> getSource(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSourceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putSource(
        org.openlca.proto.Proto.Source request) {
      return futureUnaryCall(
          getChannel().newCall(getPutSourceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.UnitGroupStatus> getUnitGroup(
        org.openlca.proto.Proto.Ref request) {
      return futureUnaryCall(
          getChannel().newCall(getGetUnitGroupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.openlca.proto.services.Services.RefStatus> putUnitGroup(
        org.openlca.proto.Proto.UnitGroup request) {
      return futureUnaryCall(
          getChannel().newCall(getPutUnitGroupMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DELETE = 0;
  private static final int METHODID_GET_ACTORS = 1;
  private static final int METHODID_GET_ACTOR = 2;
  private static final int METHODID_PUT_ACTOR = 3;
  private static final int METHODID_GET_CATEGORIES = 4;
  private static final int METHODID_GET_CATEGORY = 5;
  private static final int METHODID_PUT_CATEGORY = 6;
  private static final int METHODID_GET_CURRENCIES = 7;
  private static final int METHODID_GET_CURRENCY = 8;
  private static final int METHODID_PUT_CURRENCY = 9;
  private static final int METHODID_GET_DQSYSTEMS = 10;
  private static final int METHODID_GET_DQSYSTEM = 11;
  private static final int METHODID_PUT_DQSYSTEM = 12;
  private static final int METHODID_GET_FLOWS = 13;
  private static final int METHODID_GET_FLOW = 14;
  private static final int METHODID_PUT_FLOW = 15;
  private static final int METHODID_GET_FLOW_PROPERTIES = 16;
  private static final int METHODID_GET_FLOW_PROPERTY = 17;
  private static final int METHODID_PUT_FLOW_PROPERTY = 18;
  private static final int METHODID_GET_IMPACT_CATEGORIES = 19;
  private static final int METHODID_GET_IMPACT_CATEGORY = 20;
  private static final int METHODID_PUT_IMPACT_CATEGORY = 21;
  private static final int METHODID_GET_IMPACT_METHODS = 22;
  private static final int METHODID_GET_IMPACT_METHOD = 23;
  private static final int METHODID_PUT_IMPACT_METHOD = 24;
  private static final int METHODID_GET_LOCATIONS = 25;
  private static final int METHODID_GET_LOCATION = 26;
  private static final int METHODID_PUT_LOCATION = 27;
  private static final int METHODID_GET_PARAMETERS = 28;
  private static final int METHODID_GET_PARAMETER = 29;
  private static final int METHODID_PUT_PARAMETER = 30;
  private static final int METHODID_GET_PROCESSES = 31;
  private static final int METHODID_GET_PROCESS = 32;
  private static final int METHODID_PUT_PROCESS = 33;
  private static final int METHODID_GET_PRODUCT_SYSTEMS = 34;
  private static final int METHODID_GET_PRODUCT_SYSTEM = 35;
  private static final int METHODID_PUT_PRODUCT_SYSTEM = 36;
  private static final int METHODID_GET_PROJECTS = 37;
  private static final int METHODID_GET_PROJECT = 38;
  private static final int METHODID_PUT_PROJECT = 39;
  private static final int METHODID_GET_SOCIAL_INDICATORS = 40;
  private static final int METHODID_GET_SOCIAL_INDICATOR = 41;
  private static final int METHODID_PUT_SOCIAL_INDICATOR = 42;
  private static final int METHODID_GET_SOURCES = 43;
  private static final int METHODID_GET_SOURCE = 44;
  private static final int METHODID_PUT_SOURCE = 45;
  private static final int METHODID_GET_UNIT_GROUPS = 46;
  private static final int METHODID_GET_UNIT_GROUP = 47;
  private static final int METHODID_PUT_UNIT_GROUP = 48;
  private static final int METHODID_GET_PROVIDERS_FOR = 49;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DataServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DataServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DELETE:
          serviceImpl.delete((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.Status>) responseObserver);
          break;
        case METHODID_GET_ACTORS:
          serviceImpl.getActors((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Actor>) responseObserver);
          break;
        case METHODID_GET_ACTOR:
          serviceImpl.getActor((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ActorStatus>) responseObserver);
          break;
        case METHODID_PUT_ACTOR:
          serviceImpl.putActor((org.openlca.proto.Proto.Actor) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_CATEGORIES:
          serviceImpl.getCategories((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Category>) responseObserver);
          break;
        case METHODID_GET_CATEGORY:
          serviceImpl.getCategory((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CategoryStatus>) responseObserver);
          break;
        case METHODID_PUT_CATEGORY:
          serviceImpl.putCategory((org.openlca.proto.Proto.Category) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_CURRENCIES:
          serviceImpl.getCurrencies((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Currency>) responseObserver);
          break;
        case METHODID_GET_CURRENCY:
          serviceImpl.getCurrency((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.CurrencyStatus>) responseObserver);
          break;
        case METHODID_PUT_CURRENCY:
          serviceImpl.putCurrency((org.openlca.proto.Proto.Currency) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_DQSYSTEMS:
          serviceImpl.getDQSystems((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.DQSystem>) responseObserver);
          break;
        case METHODID_GET_DQSYSTEM:
          serviceImpl.getDQSystem((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.DQSystemStatus>) responseObserver);
          break;
        case METHODID_PUT_DQSYSTEM:
          serviceImpl.putDQSystem((org.openlca.proto.Proto.DQSystem) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_FLOWS:
          serviceImpl.getFlows((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Flow>) responseObserver);
          break;
        case METHODID_GET_FLOW:
          serviceImpl.getFlow((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowStatus>) responseObserver);
          break;
        case METHODID_PUT_FLOW:
          serviceImpl.putFlow((org.openlca.proto.Proto.Flow) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_FLOW_PROPERTIES:
          serviceImpl.getFlowProperties((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.FlowProperty>) responseObserver);
          break;
        case METHODID_GET_FLOW_PROPERTY:
          serviceImpl.getFlowProperty((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.FlowPropertyStatus>) responseObserver);
          break;
        case METHODID_PUT_FLOW_PROPERTY:
          serviceImpl.putFlowProperty((org.openlca.proto.Proto.FlowProperty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_IMPACT_CATEGORIES:
          serviceImpl.getImpactCategories((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactCategory>) responseObserver);
          break;
        case METHODID_GET_IMPACT_CATEGORY:
          serviceImpl.getImpactCategory((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactCategoryStatus>) responseObserver);
          break;
        case METHODID_PUT_IMPACT_CATEGORY:
          serviceImpl.putImpactCategory((org.openlca.proto.Proto.ImpactCategory) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_IMPACT_METHODS:
          serviceImpl.getImpactMethods((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ImpactMethod>) responseObserver);
          break;
        case METHODID_GET_IMPACT_METHOD:
          serviceImpl.getImpactMethod((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ImpactMethodStatus>) responseObserver);
          break;
        case METHODID_PUT_IMPACT_METHOD:
          serviceImpl.putImpactMethod((org.openlca.proto.Proto.ImpactMethod) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_LOCATIONS:
          serviceImpl.getLocations((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Location>) responseObserver);
          break;
        case METHODID_GET_LOCATION:
          serviceImpl.getLocation((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.LocationStatus>) responseObserver);
          break;
        case METHODID_PUT_LOCATION:
          serviceImpl.putLocation((org.openlca.proto.Proto.Location) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_PARAMETERS:
          serviceImpl.getParameters((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Parameter>) responseObserver);
          break;
        case METHODID_GET_PARAMETER:
          serviceImpl.getParameter((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ParameterStatus>) responseObserver);
          break;
        case METHODID_PUT_PARAMETER:
          serviceImpl.putParameter((org.openlca.proto.Proto.Parameter) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_PROCESSES:
          serviceImpl.getProcesses((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Process>) responseObserver);
          break;
        case METHODID_GET_PROCESS:
          serviceImpl.getProcess((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProcessStatus>) responseObserver);
          break;
        case METHODID_PUT_PROCESS:
          serviceImpl.putProcess((org.openlca.proto.Proto.Process) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_PRODUCT_SYSTEMS:
          serviceImpl.getProductSystems((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProductSystem>) responseObserver);
          break;
        case METHODID_GET_PRODUCT_SYSTEM:
          serviceImpl.getProductSystem((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProductSystemStatus>) responseObserver);
          break;
        case METHODID_PUT_PRODUCT_SYSTEM:
          serviceImpl.putProductSystem((org.openlca.proto.Proto.ProductSystem) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_PROJECTS:
          serviceImpl.getProjects((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Project>) responseObserver);
          break;
        case METHODID_GET_PROJECT:
          serviceImpl.getProject((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.ProjectStatus>) responseObserver);
          break;
        case METHODID_PUT_PROJECT:
          serviceImpl.putProject((org.openlca.proto.Proto.Project) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_SOCIAL_INDICATORS:
          serviceImpl.getSocialIndicators((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.SocialIndicator>) responseObserver);
          break;
        case METHODID_GET_SOCIAL_INDICATOR:
          serviceImpl.getSocialIndicator((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SocialIndicatorStatus>) responseObserver);
          break;
        case METHODID_PUT_SOCIAL_INDICATOR:
          serviceImpl.putSocialIndicator((org.openlca.proto.Proto.SocialIndicator) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_SOURCES:
          serviceImpl.getSources((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.Source>) responseObserver);
          break;
        case METHODID_GET_SOURCE:
          serviceImpl.getSource((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.SourceStatus>) responseObserver);
          break;
        case METHODID_PUT_SOURCE:
          serviceImpl.putSource((org.openlca.proto.Proto.Source) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_UNIT_GROUPS:
          serviceImpl.getUnitGroups((org.openlca.proto.services.Services.Empty) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.UnitGroup>) responseObserver);
          break;
        case METHODID_GET_UNIT_GROUP:
          serviceImpl.getUnitGroup((org.openlca.proto.Proto.Ref) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.UnitGroupStatus>) responseObserver);
          break;
        case METHODID_PUT_UNIT_GROUP:
          serviceImpl.putUnitGroup((org.openlca.proto.Proto.UnitGroup) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.services.Services.RefStatus>) responseObserver);
          break;
        case METHODID_GET_PROVIDERS_FOR:
          serviceImpl.getProvidersFor((org.openlca.proto.Proto.FlowRef) request,
              (io.grpc.stub.StreamObserver<org.openlca.proto.Proto.ProcessRef>) responseObserver);
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

  private static abstract class DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.openlca.proto.services.Services.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataService");
    }
  }

  private static final class DataServiceFileDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier {
    DataServiceFileDescriptorSupplier() {}
  }

  private static final class DataServiceMethodDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DataServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (DataServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataServiceFileDescriptorSupplier())
              .addMethod(getDeleteMethod())
              .addMethod(getGetActorsMethod())
              .addMethod(getGetActorMethod())
              .addMethod(getPutActorMethod())
              .addMethod(getGetCategoriesMethod())
              .addMethod(getGetCategoryMethod())
              .addMethod(getPutCategoryMethod())
              .addMethod(getGetCurrenciesMethod())
              .addMethod(getGetCurrencyMethod())
              .addMethod(getPutCurrencyMethod())
              .addMethod(getGetDQSystemsMethod())
              .addMethod(getGetDQSystemMethod())
              .addMethod(getPutDQSystemMethod())
              .addMethod(getGetFlowsMethod())
              .addMethod(getGetFlowMethod())
              .addMethod(getPutFlowMethod())
              .addMethod(getGetFlowPropertiesMethod())
              .addMethod(getGetFlowPropertyMethod())
              .addMethod(getPutFlowPropertyMethod())
              .addMethod(getGetImpactCategoriesMethod())
              .addMethod(getGetImpactCategoryMethod())
              .addMethod(getPutImpactCategoryMethod())
              .addMethod(getGetImpactMethodsMethod())
              .addMethod(getGetImpactMethodMethod())
              .addMethod(getPutImpactMethodMethod())
              .addMethod(getGetLocationsMethod())
              .addMethod(getGetLocationMethod())
              .addMethod(getPutLocationMethod())
              .addMethod(getGetParametersMethod())
              .addMethod(getGetParameterMethod())
              .addMethod(getPutParameterMethod())
              .addMethod(getGetProcessesMethod())
              .addMethod(getGetProcessMethod())
              .addMethod(getPutProcessMethod())
              .addMethod(getGetProductSystemsMethod())
              .addMethod(getGetProductSystemMethod())
              .addMethod(getPutProductSystemMethod())
              .addMethod(getGetProjectsMethod())
              .addMethod(getGetProjectMethod())
              .addMethod(getPutProjectMethod())
              .addMethod(getGetSocialIndicatorsMethod())
              .addMethod(getGetSocialIndicatorMethod())
              .addMethod(getPutSocialIndicatorMethod())
              .addMethod(getGetSourcesMethod())
              .addMethod(getGetSourceMethod())
              .addMethod(getPutSourceMethod())
              .addMethod(getGetUnitGroupsMethod())
              .addMethod(getGetUnitGroupMethod())
              .addMethod(getPutUnitGroupMethod())
              .addMethod(getGetProvidersForMethod())
              .build();
        }
      }
    }
    return result;
  }
}
