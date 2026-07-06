package com.sportbuddy.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: gigachat.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ModelsServiceGrpc {

  private ModelsServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "gigachat.v1.ModelsService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.sportbuddy.grpc.ListModelsRequest,
      com.sportbuddy.grpc.ListModelsResponse> getListModelsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListModels",
      requestType = com.sportbuddy.grpc.ListModelsRequest.class,
      responseType = com.sportbuddy.grpc.ListModelsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.sportbuddy.grpc.ListModelsRequest,
      com.sportbuddy.grpc.ListModelsResponse> getListModelsMethod() {
    io.grpc.MethodDescriptor<com.sportbuddy.grpc.ListModelsRequest, com.sportbuddy.grpc.ListModelsResponse> getListModelsMethod;
    if ((getListModelsMethod = ModelsServiceGrpc.getListModelsMethod) == null) {
      synchronized (ModelsServiceGrpc.class) {
        if ((getListModelsMethod = ModelsServiceGrpc.getListModelsMethod) == null) {
          ModelsServiceGrpc.getListModelsMethod = getListModelsMethod =
              io.grpc.MethodDescriptor.<com.sportbuddy.grpc.ListModelsRequest, com.sportbuddy.grpc.ListModelsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListModels"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sportbuddy.grpc.ListModelsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sportbuddy.grpc.ListModelsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ModelsServiceMethodDescriptorSupplier("ListModels"))
              .build();
        }
      }
    }
    return getListModelsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.sportbuddy.grpc.RetrieveModelRequest,
      com.sportbuddy.grpc.RetrieveModelResponse> getRetrieveModelMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RetrieveModel",
      requestType = com.sportbuddy.grpc.RetrieveModelRequest.class,
      responseType = com.sportbuddy.grpc.RetrieveModelResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.sportbuddy.grpc.RetrieveModelRequest,
      com.sportbuddy.grpc.RetrieveModelResponse> getRetrieveModelMethod() {
    io.grpc.MethodDescriptor<com.sportbuddy.grpc.RetrieveModelRequest, com.sportbuddy.grpc.RetrieveModelResponse> getRetrieveModelMethod;
    if ((getRetrieveModelMethod = ModelsServiceGrpc.getRetrieveModelMethod) == null) {
      synchronized (ModelsServiceGrpc.class) {
        if ((getRetrieveModelMethod = ModelsServiceGrpc.getRetrieveModelMethod) == null) {
          ModelsServiceGrpc.getRetrieveModelMethod = getRetrieveModelMethod =
              io.grpc.MethodDescriptor.<com.sportbuddy.grpc.RetrieveModelRequest, com.sportbuddy.grpc.RetrieveModelResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RetrieveModel"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sportbuddy.grpc.RetrieveModelRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sportbuddy.grpc.RetrieveModelResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ModelsServiceMethodDescriptorSupplier("RetrieveModel"))
              .build();
        }
      }
    }
    return getRetrieveModelMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ModelsServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ModelsServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ModelsServiceStub>() {
        @java.lang.Override
        public ModelsServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ModelsServiceStub(channel, callOptions);
        }
      };
    return ModelsServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ModelsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ModelsServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ModelsServiceBlockingStub>() {
        @java.lang.Override
        public ModelsServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ModelsServiceBlockingStub(channel, callOptions);
        }
      };
    return ModelsServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ModelsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ModelsServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ModelsServiceFutureStub>() {
        @java.lang.Override
        public ModelsServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ModelsServiceFutureStub(channel, callOptions);
        }
      };
    return ModelsServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void listModels(com.sportbuddy.grpc.ListModelsRequest request,
        io.grpc.stub.StreamObserver<com.sportbuddy.grpc.ListModelsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListModelsMethod(), responseObserver);
    }

    /**
     */
    default void retrieveModel(com.sportbuddy.grpc.RetrieveModelRequest request,
        io.grpc.stub.StreamObserver<com.sportbuddy.grpc.RetrieveModelResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRetrieveModelMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ModelsService.
   */
  public static abstract class ModelsServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ModelsServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ModelsService.
   */
  public static final class ModelsServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ModelsServiceStub> {
    private ModelsServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ModelsServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ModelsServiceStub(channel, callOptions);
    }

    /**
     */
    public void listModels(com.sportbuddy.grpc.ListModelsRequest request,
        io.grpc.stub.StreamObserver<com.sportbuddy.grpc.ListModelsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListModelsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void retrieveModel(com.sportbuddy.grpc.RetrieveModelRequest request,
        io.grpc.stub.StreamObserver<com.sportbuddy.grpc.RetrieveModelResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRetrieveModelMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ModelsService.
   */
  public static final class ModelsServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ModelsServiceBlockingStub> {
    private ModelsServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ModelsServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ModelsServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.sportbuddy.grpc.ListModelsResponse listModels(com.sportbuddy.grpc.ListModelsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListModelsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.sportbuddy.grpc.RetrieveModelResponse retrieveModel(com.sportbuddy.grpc.RetrieveModelRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRetrieveModelMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ModelsService.
   */
  public static final class ModelsServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ModelsServiceFutureStub> {
    private ModelsServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ModelsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ModelsServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.sportbuddy.grpc.ListModelsResponse> listModels(
        com.sportbuddy.grpc.ListModelsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListModelsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.sportbuddy.grpc.RetrieveModelResponse> retrieveModel(
        com.sportbuddy.grpc.RetrieveModelRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRetrieveModelMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_MODELS = 0;
  private static final int METHODID_RETRIEVE_MODEL = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_MODELS:
          serviceImpl.listModels((com.sportbuddy.grpc.ListModelsRequest) request,
              (io.grpc.stub.StreamObserver<com.sportbuddy.grpc.ListModelsResponse>) responseObserver);
          break;
        case METHODID_RETRIEVE_MODEL:
          serviceImpl.retrieveModel((com.sportbuddy.grpc.RetrieveModelRequest) request,
              (io.grpc.stub.StreamObserver<com.sportbuddy.grpc.RetrieveModelResponse>) responseObserver);
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

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListModelsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.sportbuddy.grpc.ListModelsRequest,
              com.sportbuddy.grpc.ListModelsResponse>(
                service, METHODID_LIST_MODELS)))
        .addMethod(
          getRetrieveModelMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.sportbuddy.grpc.RetrieveModelRequest,
              com.sportbuddy.grpc.RetrieveModelResponse>(
                service, METHODID_RETRIEVE_MODEL)))
        .build();
  }

  private static abstract class ModelsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ModelsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.sportbuddy.grpc.GigaChatProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ModelsService");
    }
  }

  private static final class ModelsServiceFileDescriptorSupplier
      extends ModelsServiceBaseDescriptorSupplier {
    ModelsServiceFileDescriptorSupplier() {}
  }

  private static final class ModelsServiceMethodDescriptorSupplier
      extends ModelsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ModelsServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (ModelsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ModelsServiceFileDescriptorSupplier())
              .addMethod(getListModelsMethod())
              .addMethod(getRetrieveModelMethod())
              .build();
        }
      }
    }
    return result;
  }
}
