package com.chainML.pb;

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
    value = "by gRPC proto compiler (version 1.29.0)",
    comments = "Source: chainML_service.proto")
public final class chainMLServiceGrpc {

  private chainMLServiceGrpc() {}

  public static final String SERVICE_NAME = "chainML.chainMLService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.chainML.pb.UploadFileRequest,
      com.chainML.pb.UploadFileResponse> getUploadFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UploadFile",
      requestType = com.chainML.pb.UploadFileRequest.class,
      responseType = com.chainML.pb.UploadFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.chainML.pb.UploadFileRequest,
      com.chainML.pb.UploadFileResponse> getUploadFileMethod() {
    io.grpc.MethodDescriptor<com.chainML.pb.UploadFileRequest, com.chainML.pb.UploadFileResponse> getUploadFileMethod;
    if ((getUploadFileMethod = chainMLServiceGrpc.getUploadFileMethod) == null) {
      synchronized (chainMLServiceGrpc.class) {
        if ((getUploadFileMethod = chainMLServiceGrpc.getUploadFileMethod) == null) {
          chainMLServiceGrpc.getUploadFileMethod = getUploadFileMethod =
              io.grpc.MethodDescriptor.<com.chainML.pb.UploadFileRequest, com.chainML.pb.UploadFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UploadFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chainML.pb.UploadFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chainML.pb.UploadFileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new chainMLServiceMethodDescriptorSupplier("UploadFile"))
              .build();
        }
      }
    }
    return getUploadFileMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static chainMLServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<chainMLServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<chainMLServiceStub>() {
        @java.lang.Override
        public chainMLServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new chainMLServiceStub(channel, callOptions);
        }
      };
    return chainMLServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static chainMLServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<chainMLServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<chainMLServiceBlockingStub>() {
        @java.lang.Override
        public chainMLServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new chainMLServiceBlockingStub(channel, callOptions);
        }
      };
    return chainMLServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static chainMLServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<chainMLServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<chainMLServiceFutureStub>() {
        @java.lang.Override
        public chainMLServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new chainMLServiceFutureStub(channel, callOptions);
        }
      };
    return chainMLServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class chainMLServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<com.chainML.pb.UploadFileRequest> uploadFile(
        io.grpc.stub.StreamObserver<com.chainML.pb.UploadFileResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getUploadFileMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUploadFileMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.chainML.pb.UploadFileRequest,
                com.chainML.pb.UploadFileResponse>(
                  this, METHODID_UPLOAD_FILE)))
          .build();
    }
  }

  /**
   */
  public static final class chainMLServiceStub extends io.grpc.stub.AbstractAsyncStub<chainMLServiceStub> {
    private chainMLServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected chainMLServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new chainMLServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.chainML.pb.UploadFileRequest> uploadFile(
        io.grpc.stub.StreamObserver<com.chainML.pb.UploadFileResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getUploadFileMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class chainMLServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<chainMLServiceBlockingStub> {
    private chainMLServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected chainMLServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new chainMLServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class chainMLServiceFutureStub extends io.grpc.stub.AbstractFutureStub<chainMLServiceFutureStub> {
    private chainMLServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected chainMLServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new chainMLServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_UPLOAD_FILE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final chainMLServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(chainMLServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UPLOAD_FILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.uploadFile(
              (io.grpc.stub.StreamObserver<com.chainML.pb.UploadFileResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class chainMLServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    chainMLServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.chainML.pb.ChainMLService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("chainMLService");
    }
  }

  private static final class chainMLServiceFileDescriptorSupplier
      extends chainMLServiceBaseDescriptorSupplier {
    chainMLServiceFileDescriptorSupplier() {}
  }

  private static final class chainMLServiceMethodDescriptorSupplier
      extends chainMLServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    chainMLServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (chainMLServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new chainMLServiceFileDescriptorSupplier())
              .addMethod(getUploadFileMethod())
              .build();
        }
      }
    }
    return result;
  }
}
