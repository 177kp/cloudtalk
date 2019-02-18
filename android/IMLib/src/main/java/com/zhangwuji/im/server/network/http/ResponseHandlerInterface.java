package com.zhangwuji.im.server.network.http;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Interface to standardize implementations
 */
public interface ResponseHandlerInterface {

    /**
     * Returns data whether request completed successfully
     *
     * @param response HttpResponse object with data
     * @throws IOException if retrieving data from response fails
     */
    void sendResponseMessage(HttpResponse response) throws IOException;

    /**
     * Notifies callback, that request started execution
     */
    void sendStartMessage();

    /**
     * Notifies callback, that request was completed and is being removed from thread pool
     */
    void sendFinishMessage();

    /**
     * Notifies callback, that request (mainly uploading) has progressed
     *
     * @param bytesWritten number of written bytes
     * @param bytesTotal   number of total bytes to be written
     */
    void sendProgressMessage(int bytesWritten, int bytesTotal);

    /**
     * Notifies callback, that request was handled successfully
     *
     * @param statusCode   HTTP status code
     * @param headers      returned headers
     * @param responseBody returned data
     */
    void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody);

    /**
     * Returns if request was completed with error code or failure of implementation
     *
     * @param statusCode   returned HTTP status code
     * @param headers      returned headers
     * @param responseBody returned data
     * @param error        cause of request failure
     */
    void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error);

    /**
     * Notifies callback of retrying request
     */
    void sendRetryMessage();

    /**
     * Returns URI which was used to request
     *
     * @return uri of origin request
     */
    URI getRequestURI();

    /**
     * Returns Header[] which were used to request
     *
     * @return headers from origin request
     */
    Header[] getRequestHeaders();

    /**
     * Helper for handlers to receive Request URI info
     *
     * @param requestURI claimed request URI
     */
    void setRequestURI(URI requestURI);

    /**
     * Helper for handlers to receive Request Header[] info
     *
     * @param requestHeaders Headers, claimed to be from original request
     */
    void setRequestHeaders(Header[] requestHeaders);

    /**
     * Can set, whether the handler should be asynchronous or synchronous
     *
     * @param useSynchronousMode whether data should be handled on background Thread on UI Thread
     */
    void setUseSynchronousMode(boolean useSynchronousMode);
}
