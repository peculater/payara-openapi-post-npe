# Reproduction for a NullPointerException in OpenAPI on Payara for @POST calls that take a parameter of a type that implements an interface

I've verified this in both Payara micro and the standalone server. 

## Requirements

* Java 11
* Maven

## What to do and notice

Run the `payara.sh` script, which compiles and runs the project with Payara Micro. Open a web browser to http://localhost:8080/ .  Click on "Hello JAX-RS endpoint" to see that the endpoint successfully replies with "Hello World".  Click on "Open API Documentation" to first cause a 500 Server Error, then try it again to show an abbreviated document with incorrect data for the POST endpoint.  The stacktrace for the exception is in Payara's log, copied at the end of this document.

This is a pretty simple example application, based on a microprofile starter, and the JAX-RS template from Apache Netbeans.  There is a Hello World style JAX-RS endpoint with two methods.  One is a GET that replies with a string.  The other is a POST that takes a PostablePojo and replies with a string in that POJO.  The JAX-RS parts work fine.

```
$ curl http://localhost:8080/data/hello
Hello World

$ curl -XPOST -d '{ "message": "yo dawg" }' -H 'Content-Type: application/json' http://localhost:8080/data/hello
yo dawg
```

Unfortunately, the OpenAPI implementation runs through visitPOST and then has trouble figuring on the type of the parameter to the method annotated by the POST.  I'm not super sure why, but the line in question seem to point to a TypeProxy that is never instantiated for the reflected Parameter.

The class annotated with @POST for the JAX-RS endpoint extends an abstract class with that parameter as a generic.  That's almost certainly what's confusing the reflection code.

## Expected versus Actual

Fist, I don't expect an exception when calling the /openapi endpoint.  :-)

Second, on subsequent calls to /openapi there's clearly something missing.

Expected (pulled from OpenLiberty, run ./liberty.sh and compare http://localhost:9080/openapi )
```
paths:
    ...
    post:
      operationId: sayMessage
      requestBody:
        content:
          '*/*':
            schema:
              $ref: '#/components/schemas/PostablePojo'
      responses:
        default:
          description: default response
          content:
            '*/*':
              schema:
                type: string
```

Actual, from second-and-subsequent calls to http://localhost:8080/openapi
```
paths:
    ...
    post:
      operationId: sayMessage
      responses: {}
```

This feels like a cached error, I'm sure that fixing the NPE will make this look right.

## The Exception

```
  StandardWrapperValve[fish.payara.microprofile.openapi.impl.rest.app.OpenApiApplication]: Servlet.service() for servlet fish.payara.microprofile.openapi.impl.rest.app.OpenApiApplication threw exception
java.lang.NullPointerException
        at org.glassfish.hk2.classmodel.reflect.impl.ParameterImpl.getType(ParameterImpl.java:63)
        at fish.payara.microprofile.openapi.impl.processor.ApplicationProcessor.createSchema(ApplicationProcessor.java:1049)
        at fish.payara.microprofile.openapi.impl.processor.ApplicationProcessor.createSchema(ApplicationProcessor.java:1020)
        at fish.payara.microprofile.openapi.impl.processor.ApplicationProcessor.insertDefaultRequestBody(ApplicationProcessor.java:967)
        at fish.payara.microprofile.openapi.impl.processor.ApplicationProcessor.visitPOST(ApplicationProcessor.java:195)
        at fish.payara.microprofile.openapi.impl.visitor.OpenApiWalker.lambda$getAnnotationVisitor$1(OpenApiWalker.java:189)
        at fish.payara.microprofile.openapi.impl.visitor.OpenApiWalker.processAnnotation(OpenApiWalker.java:168)
        at fish.payara.microprofile.openapi.impl.visitor.OpenApiWalker.processAnnotation(OpenApiWalker.java:131)
        at fish.payara.microprofile.openapi.impl.visitor.OpenApiWalker.accept(OpenApiWalker.java:120)
        at fish.payara.microprofile.openapi.impl.processor.ApplicationProcessor.process(ApplicationProcessor.java:153)
        at fish.payara.microprofile.openapi.impl.OpenAPISupplier.get(OpenAPISupplier.java:136)
        at fish.payara.microprofile.openapi.impl.OpenApiService.getDocument(OpenApiService.java:149)
        at fish.payara.microprofile.openapi.impl.rest.app.service.OpenApiResource.getResponse(OpenApiResource.java:84)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.glassfish.jersey.server.model.internal.ResourceMethodInvocationHandlerFactory.lambda$static$0(ResourceMethodInvocationHandlerFactory.java:52)
        at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher$1.run(AbstractJavaResourceMethodDispatcher.java:124)
        at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.invoke(AbstractJavaResourceMethodDispatcher.java:167)
        at org.glassfish.jersey.server.model.internal.JavaResourceMethodDispatcherProvider$ResponseOutInvoker.doDispatch(JavaResourceMethodDispatcherProvider.java:176)
        at org.glassfish.jersey.server.model.internal.AbstractJavaResourceMethodDispatcher.dispatch(AbstractJavaResourceMethodDispatcher.java:79)
        at org.glassfish.jersey.server.model.ResourceMethodInvoker.invoke(ResourceMethodInvoker.java:469)
        at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:391)
        at org.glassfish.jersey.server.model.ResourceMethodInvoker.apply(ResourceMethodInvoker.java:80)
        at org.glassfish.jersey.server.ServerRuntime$1.run(ServerRuntime.java:253)
        at org.glassfish.jersey.internal.Errors$1.call(Errors.java:248)
        at org.glassfish.jersey.internal.Errors$1.call(Errors.java:244)
        at org.glassfish.jersey.internal.Errors.process(Errors.java:292)
        at org.glassfish.jersey.internal.Errors.process(Errors.java:274)
        at org.glassfish.jersey.internal.Errors.process(Errors.java:244)
        at org.glassfish.jersey.process.internal.RequestScope.runInScope(RequestScope.java:265)
        at org.glassfish.jersey.server.ServerRuntime.process(ServerRuntime.java:232)
        at org.glassfish.jersey.server.ApplicationHandler.handle(ApplicationHandler.java:680)
        at org.glassfish.jersey.servlet.WebComponent.serviceImpl(WebComponent.java:394)
        at org.glassfish.jersey.servlet.WebComponent.service(WebComponent.java:346)
        at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:366)
        at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:319)
        at org.glassfish.jersey.servlet.ServletContainer.service(ServletContainer.java:205)
        at org.apache.catalina.core.StandardWrapper.service(StandardWrapper.java:1636)
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:259)
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:161)
        at org.apache.catalina.core.StandardPipeline.doInvoke(StandardPipeline.java:757)
        at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:577)
        at com.sun.enterprise.web.WebPipeline.invoke(WebPipeline.java:99)
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:158)
        at org.apache.catalina.connector.CoyoteAdapter.doService(CoyoteAdapter.java:371)
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:238)
        at com.sun.enterprise.v3.services.impl.ContainerMapper$HttpHandlerCallable.call(ContainerMapper.java:520)
        at com.sun.enterprise.v3.services.impl.ContainerMapper.service(ContainerMapper.java:217)
        at org.glassfish.grizzly.http.server.HttpHandler.runService(HttpHandler.java:182)
        at org.glassfish.grizzly.http.server.HttpHandler.doHandle(HttpHandler.java:156)
        at org.glassfish.grizzly.http.server.HttpServerFilter.handleRead(HttpServerFilter.java:218)
        at org.glassfish.grizzly.filterchain.ExecutorResolver$9.execute(ExecutorResolver.java:95)
        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeFilter(DefaultFilterChain.java:260)
        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeChainPart(DefaultFilterChain.java:177)
        at org.glassfish.grizzly.filterchain.DefaultFilterChain.execute(DefaultFilterChain.java:109)
        at org.glassfish.grizzly.filterchain.DefaultFilterChain.process(DefaultFilterChain.java:88)
        at org.glassfish.grizzly.ProcessorExecutor.execute(ProcessorExecutor.java:53)
        at org.glassfish.grizzly.nio.transport.TCPNIOTransport.fireIOEvent(TCPNIOTransport.java:524)
        at org.glassfish.grizzly.strategies.AbstractIOStrategy.fireIOEvent(AbstractIOStrategy.java:89)
        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.run0(WorkerThreadIOStrategy.java:94)
        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.access$100(WorkerThreadIOStrategy.java:33)
        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy$WorkerThreadRunnable.run(WorkerThreadIOStrategy.java:114)
        at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.doWork(AbstractThreadPool.java:569)
        at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.run(AbstractThreadPool.java:549)
        at java.base/java.lang.Thread.run(Thread.java:834)
]]
```
