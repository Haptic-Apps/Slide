package me.ccrama.redditslide.Synccit.http;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class HttpClientFactory {
	
	@SuppressWarnings("unused")
	private static final String TAG = HttpClientFactory.class.getSimpleName();
	
	private static DefaultHttpClient mGzipHttpClient;
	
	private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
	
	/**
	 * http://hc.apache.org/httpcomponents-client/examples.html
	 * @return a Gzip-enabled DefaultHttpClient
	 */
	synchronized public static HttpClient getGzipHttpClient() {
		if (mGzipHttpClient == null) {
			mGzipHttpClient = createGzipHttpClient();
		}
		return mGzipHttpClient;
	}
	
	private static DefaultHttpClient createGzipHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		DefaultHttpClient httpclient = new DefaultHttpClient(params) {
		    @Override
		    protected ClientConnectionManager createClientConnectionManager() {
		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        
		        SSLSocketFactory sslSocketFactory = getHttpsSocketFactory();
		        registry.register(new Scheme("https", sslSocketFactory, 443));
		        
		        HttpParams params = getParams();
				HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
				HttpConnectionParams.setSocketBufferSize(params, 8192);
		        return new ThreadSafeClientConnManager(params, registry);
		    }
		    
		    private SSLSocketFactory getHttpsSocketFactory() {
	        	return SSLSocketFactory.getSocketFactory();
		    }
		};
		
        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
        	public void process(
                    final HttpRequest request,
                    final HttpContext context
            ) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding"))
                    request.addHeader("Accept-Encoding", "gzip");
                
                if (!request.containsHeader("Cache-Control"))
                	request.addHeader("Cache-Control", "no-cache");
            }
        });
        httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
        	@Override
            public void process(
                    final HttpResponse response, 
                    final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(
                                    new GzipDecompressingEntity(response.getEntity())); 
                            return;
                        }
                    }
                }
            }
        });
        return httpclient;
	}
	
    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }
        @Override
        public InputStream getContent()
            throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }
        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }
    
}

