# RESTed
An annotation based REST client. Supports a custom networking layer and also offers a default implementation.

## Resources and Endpoints
Resources are interfaces that declare REST endpoints through their methods. Use the annotations `Endpoint`, `Header`, `QueryParam` and `Body` to specify the requests properties. The return type of a method declares which data type the response body will be parsed into. `void` will ignore the response body
~~~
@Endpoint(value = RequestMethod.POST, path = "/endpoint", mediaTypes = {Body.JSON}, charsets = {"UTF-8"})
ResponseType myRequest(@Header("HeaderName") String headerValue,
                       @QueryParam("QueryParamName") String queryParamValue,
                       @Body(value = Body.JSON, charset = "UTF-8") Object requestBody) throws Exception;
~~~
Charsets and media types are optional and if omitted they will default to UTF-8 and application/json.

Using `Resource` it is possible to declare a base path that serves as a prefix to all endpoints declared within the interface. Using `PathParam` it is possible to declare variable elements within the endpoints path.
~~~
@Resource("/base/resource/path")
public interface MyResource {

    @Endpoint(value = RequestMethod.GET, path = "/endpoint/{pathParamKey}")
    ResponseType myRequest(@PathParam("pathParamKey") String pathParamValue) throws Exception;

}
~~~
Note that the Resource annotation is optional and doesn't have to be defined for each resource interface.

By default when an error resopnse is received (response status code != 2xx) a `RESTException` will be thrown by the resource method. Using `Error` it is possible to declare custom Exception types for specific error responses. The response body will be parsed into the custom Exception type.
~~~
@Endpoint(value = RequestMethod.GET, path = "/endpoint")
@Error(startCode = 404, endCode = 404, value = UnsupportedOperationException.class)
@Error(startCode = 503, endCode = 507, value = IllegalArgumentException.class)
ResponseType myRequest() throws Exception;
~~~

Using a subtype of `RESTResponse` as a return type or a subtype of `RESTException` for custom errors it is possible to additionally obtain the status code, message and headers of the response.
Using `StreamedRESTResponse` as a return type or a subtype of `StreamedRESTException` for custom errors it is possible to stream the response body through an `InputStream` and skip the response body parsing.

## ResourceFactory
A `ResourceFactory` creates instances of resource interfaces and handles calls to their methods. It is responsible for constructing and execution requests.
~~~
ResourceFactory factory = new ResourceFactory(myClient);
MyResource resource = factory.createResource(MyResource.class);
try {
    ResponseType response = resource.myRequest();
    // handle request success
}catch (Exception ex){
    // handle errors and error responses
}
~~~
`ResourceFactory` uses `RequestInterceptor`s and `ResponseInterceptor`s as callbacks before pending requests or responses are processed. They may be used to alter the request or response, or intercept and abort them. For more on them, see section Middlewares below.

Interceptors are added to a factory and will from there on be notified about all future pending requests and responses.
~~~
factory.addRequestInterceptors(myRequestInterceptor);
factory.addResponseInterceptors(myResponseInterceptor);
~~~

`ResourceFactory` uses `RequestBodyParser`s and `ResponseBodyParser`s to parse the request body into binary data and the response binary body into the desired object type. For more on them, see section Body parsers below.

Parsers are registered at a factory for specific media types. Media types may be explicit, with wildcard or generic.
~~~
factory.registerRequestParser(explicitRequestParser, "application/json");
factory.registerRequestParser(wildcardRequestParser, "application/*");
factory.registerRequestParser(genericRequestParser, "*/*");
        
factory.registerResponseParser(...);
...
~~~
If multiple parsers are applicable for the same media type, the explicit one will be preferred over the wildcard over the generic parser.

## Middlewares
`RequestInterceptor`s are called befor the final construction and execution of a `Request`. They may alter it through its `Builder` or abort it by throwing a `RequestInterceptedException` which will be thrown by the resource method.
~~~
@Override
public void intercept(Request.@NotNull Builder pendingRequest, @Nullable Object bodyObject, @NotNull Method method, @Nullable Object[] params) throws RequestInterceptedException {
    if(bodyObject == null){
        throw new RequestInterceptedException(pendingRequest.build(), "Abort all requests without body");
    }else{
        pendingRequest.addHeader("token-header", "token-value");
    }
}
~~~

`ResponseInterceptor`s are called after a response was received and parsed but before the resource method returns. They may alter the returned value or abort the response by throwing a `ResponseInterceptedException` which will be thrown by the resource method. 
They are also called for error responses that would result in an Exception in which case `parsedResponse` will be the created exception before it is thrown.
~~~
@Override
public void intercept(@NotNull Request request, @NotNull Response response, @Nullable Object parsedResponse, @NotNull Method method, @Nullable Object[] params) throws ResponseInterceptedException {
    if(parsedResponse == null){
        throw new ResponseInterceptedException(request, response, "Abort all responses without body");
    }else if(parsedResponse instanceof MyResponseType){
        ((MyResponseType)parsedResponse).setSomeValue(...)
    }
}
~~~

## Body parsers
`RequestBodyParser`s are called when the `Request` is constructed, before `RequestInterceptor`s are notified. They parse the request body into binary data, structured in a specific media type and encoded with a specific charset.
~~~
@Override
public byte @Nullable [] parse(@NotNull Object body, @NotNull String mediaType, @NotNull String charset) throws Exception {
    byte[] bodyData = ...; // parse body into format of mediaType and encode with charset
    return bodyData;
}
~~~

`ResponseBodyParser`s are called when a `Response` was received, before `ResponseInterceptor`s are notified. They parse the binary response body into the required object type (may be an Exception type for error responses). They aren't 
called if
* The method doesn't define a result type (i.e. void) for a success response
* The response had no body and the result type does not extend from `RESTResponse` or `RESTException`
* The result type is `StreamedRESTResponse` or extends `StreamedRESTException`

Parsers may throw Exceptions which will be thrown by the resource method and abort the request.

## HTTPClient
`HTTPClient` is an interface used to abstract the networking layer. `ResourceFactory` requires a client to execute its `Request`s on it. There are default implementations for http and https, for more on them see the section default client implementations below.
~~~
@Override
public @NotNull Response request(@NotNull Request request, @NotNull Method method, @Nullable Object[] params) throws Exception {
    Response response = ...; // execute the http request here according to its configuration and construct a Response object with the response data
    return response;
}
~~~
Any Exceptions thrown by it will be thrown by the resource method and abort the request.

### Default client implementations
`HttpUrlClient` uses a `java.net.URI` to construct a `java.net.URL` to open a connection. This results in a `java.net.HttpURLConnection` which is used for the request.

`HttpsUrlClient` extends from `HttpUrlClient` and uses a `java.net.HttpsURLConnection` instead. It provides additional options for host verification and alteration of the socket factory to enable usage of things like TrustManagers.

For more on them, refer to their javadoc and the JDK documentation on `java.net.HttpURLConnection` and `java.net.HttpsURLConnection`
