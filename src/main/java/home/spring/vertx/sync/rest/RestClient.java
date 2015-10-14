package home.spring.vertx.sync.rest;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for issuing rest requests parametrized.
 *
 * Uses {@link AllowAllHostnameVerifier} and {@link TrustAllTrustManager} => no SSL validation.
 *
 * @author aardelean
 *
 */
@Component
public class RestClient {

	private Client client;

	/**
	 * Setting up the proper configuration for serializing the requests sent.
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	@PostConstruct
	public void setUp() throws KeyManagementException, NoSuchAlgorithmException {
		SSLContext ctx = SSLContext.getInstance("SSL");
		X509TrustManager trustManager = new TrustAllTrustManager();
		TrustManager[] tm = { trustManager };
		ctx.init(null, tm, null);

		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.READ_TIMEOUT, 300000);

		client = ClientBuilder.newBuilder().sslContext(ctx).hostnameVerifier(new AllowAllHostnameVerifier()).build();
	}

	/**
	 * Issue a get request via rest, parametrized.
	 * @param url - of the endpoint where the request is called against
	 * @param responseClass - for deserialization when the response arrives.
	 * @param params - array of parameters which might be needed to be put in a query string.
	 * @return - the response deserialized received.
	 * @throws UnsupportedEncodingException
	 */
	public <T> T get(String url, Class<T> responseClass, Parameter... params) throws UnsupportedEncodingException {
		WebTarget target = targetWithParams(url, params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).get(responseClass);
	}

	/**
	 * Issue a get request via rest, parametrized.
	 * @param url - of the endpoint where the request is called against
	 * @param  responseType representation of a generic Java type the response
	 *                     entity will be converted to.
	 * @param params - array of parameters which might be needed to be put in a query string.
	 * @return - the response deserialized received.
	 * @throws UnsupportedEncodingException
	 */
	public <T> T get(String url, GenericType<T> responseType, Parameter... params) throws UnsupportedEncodingException {
		WebTarget target = targetWithParams(url, params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).get(responseType);
	}

	/**
	 * Makes a post request to a server, serializing an object to be sent, and returns the response deserialized.
	 * @param urlStr - of the endpoint where the request is done.
	 * @param request - object to be serialized and sent via post.
	 * @param responseClass - class of the response to be expected, so the deserialization will automatically cast to it.
	 * @return the deserialized object expected as a response from the rest service.
	 * @throws Exception
	 */
	public <T> T postObject(String urlStr, Object request, Class<T> responseClass) throws Exception {
		WebTarget target = client.target(urlStr);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), responseClass);
	}

	public <T> T postObject(String urlStr, Object request, GenericType<T> responseType) throws Exception {
		WebTarget target = client.target(urlStr);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), responseType);
	}

	/**
	 * Makes a post request to a server, serializing an object to be sent, and returns the response deserialized.
	 * @param urlStr - of the endpoint where the request is done.
	 * @param request - object to be serialized and sent via post.
	 * @param responseClass - class of the response to be expected, so the deserialization will automatically cast to it.
	 * @param params - form parameters to be send as post within the request.
	 * @return the deserialized object expected as a response from the rest service.
	 * @throws Exception
	 */
	public <T> T postObject(String urlStr, Object request, Class<T> responseClass, Parameter... params) throws Exception {
		WebTarget target = targetWithParams(urlStr, params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), responseClass);
	}

	/**
	 * Makes a post rest request to a parametrized server url, with configurable form parameters, and returns a deserialized response received back. 
	 * @param urlStr - of the endpoint where the request will be issued.
	 * @param responseClass - to be expected as response from the endpoint, needed in the deserialization process.
	 * @param params - form parameters to be send as post within the request.
	 * @return the response deserialized received from the endpoint.
	 * @throws Exception
	 */
	public <T> T post(String urlStr, Class<T> responseClass, Parameter... params) throws Exception {
		Form form = new Form();
		WebTarget target = targetWithParams(urlStr, params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), responseClass);
	}

	/**
	 * Makes a post rest request to a parametrized server url, with configurable form parameters, and returns a deserialized response received back.
	 * @param urlStr - of the endpoint where the request will be issued.
	 * @param responseClass - to be expected as response from the endpoint, needed in the deserialization process.
	 * @return the response deserialized received from the endpoint.
	 * @throws Exception
	 */
	public <T> T postObject(String urlStr, Class<T> responseClass, Object param) throws Exception {
		WebTarget target = client.target(urlStr);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(param, MediaType.APPLICATION_JSON_TYPE), responseClass);
	}

	/**
	 * Makes a post rest request to a parametrized server url, with configurable form parameters, and returns a deserialized response received back.
	 * @param urlStr - of the endpoint where the request will be issued.
	 * @param responseClass - to be expected as response from the endpoint, needed in the deserialization process.
	 * @param params - form parameters to be send as post within the request.
	 * @return the response deserialized received from the endpoint.
	 * @throws Exception
	 */
	public <T> T postUrlParameters(String urlStr, Class<T> responseClass, Parameter... params) throws Exception {
		WebTarget target = targetWithParams(urlStr, params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity("", MediaType.APPLICATION_FORM_URLENCODED_TYPE), responseClass);
	}

	/**
	 * Issues a delete rest request to an endpoint.
	 * @param urlStr - of the endpoint where the request is going.
	 * @throws Exception
	 */
	public void delete(String urlStr) throws Exception {
		WebTarget target = client.target(urlStr);
		target.request().delete();
	}

	/**
	 * Makes a put rest call to a parametrized endpoint with form parameters and returns a deserialized response.
	 * @param urlStr - the endpoint where the request is issued against.
	 * @param responseClass - the type of the response expected needed in the deserialization process.
	 * @param params - the form parameters to be submitted with the request.
	 * @return the deserialized response received from the rest service endpoint.
	 * @throws Exception
	 */
	public <T> T put(String urlStr, Class<T> responseClass, Parameter... params) throws Exception {
		WebTarget target = client.target(urlStr);
		Form form = formWithParams(params);
		return target.request(MediaType.APPLICATION_JSON_TYPE).put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), responseClass);
	}

	/**
	 * Makes a put rest call to a parametrized endpoint with form parameters and returns nothing.
	 * @param urlStr - the endpoint where the request is issued against.
	 * @param params - the form parameters to be submitted with the request.
	 * @throws Exception
	 */
	public void putAsync(String urlStr, Parameter... params) throws Exception {
		WebTarget target = client.target(urlStr);
		Form form = formWithParams(params);
		target.request(MediaType.APPLICATION_JSON_TYPE).async().put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), Void.class);
	}

	/**
	 * Makes a put rest call to a parametrized endpoint with json body and url parameters.
	 * @param urlStr - the endpoint where the request is issued against.
	 * @param payload - the body request.
	 * @param params - the form parameters to be submitted with the request.
	 * @throws Exception
	 */
	public void putJsonAsync(String urlStr, Serializable payload, Parameter... params) throws Exception {
		WebTarget target = targetWithParams(urlStr, params);
		target.request(MediaType.APPLICATION_JSON_TYPE).async().put(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE), Void.class);
	}

	/**
	 * Forwards a request to a different rest service and returns the stream resulted from that endpoint, unmarshalled.
	 */
	public InputStream forwardGet(String urlStr, Parameter... params) throws UnsupportedEncodingException {
		WebTarget target = targetWithParams(urlStr, params);
		return target.request(new MediaType[] { MediaType.APPLICATION_JSON_TYPE }).get(InputStream.class);
	}

	public WebTarget targetWithParams(String url, Parameter... params) {
		WebTarget target = client.target(url);
		if (params != null && params.length > 0) {
			for (Parameter parameter : params) {
				target = target.queryParam(parameter.getKey(), parameter.getValue());
			}
		}
		return target;
	}

	protected Form formWithParams(Parameter... params) {
		Form form = new Form();
		if (params != null && params.length > 0) {
			for (Parameter parameter : params) {
				form.param(parameter.getKey(), parameter.getValue());
			}
		}
		return form;
	}
}
