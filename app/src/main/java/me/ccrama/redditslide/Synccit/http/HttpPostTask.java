package me.ccrama.redditslide.Synccit.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.util.ArrayList;

public abstract class HttpPostTask<Result> extends AsyncTask<String, Long, Result> {
	
	private static final String TAG = HttpPostTask.class.getSimpleName();
	
	protected static final int CONNECTION_TIMEOUT_MILLIS = 5000;
	protected static final int SOCKET_TIMEOUT_MILLIS = 20000;
	
	protected final HttpClient mClient = HttpClientFactory.getGzipHttpClient();
	
	private String mUrl;
	
	/**
	 * @param url Required.
	 * @param activity Optional. May be null.
	 */
	public HttpPostTask(String url) {
		mUrl = url;
	}

	/**
	 * params come in pairs: key/value
	 */
	@Override
	protected Result doInBackground(String... params) {
		
		ArrayList<NameValuePair> nvps = getPostArgs(params);
		
		HttpEntity entity = null;
		InputStream in = null;
		
		try {
			HttpPost httppost = new HttpPost(mUrl);
			httppost.setHeader("User-Agent", getUserAgent());
			httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
            // Set timeout to 45 seconds for login
            HttpParams httpparams = httppost.getParams();
	        HttpConnectionParams.setConnectionTimeout(httpparams, CONNECTION_TIMEOUT_MILLIS);
	        HttpConnectionParams.setSoTimeout(httpparams, SOCKET_TIMEOUT_MILLIS);
	        
            // Perform the HTTP POST request
        	HttpResponse response = mClient.execute(httppost);
        	String status = response.getStatusLine().toString();
        	if (!status.contains("OK")) {
        		throw new HttpException(status);
        	}
        	
        	entity = response.getEntity();
        	in = entity.getContent();
        	
			return onInput(in);
			
		} catch (Exception ex) {
			Log.e(TAG, "Error during POST", ex);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ignore) {}
			try {
				entity.consumeContent();
			} catch (Exception ignore) {}
		}
			
		return null;
	}
	
	private ArrayList<NameValuePair> getPostArgs(String... params) {
		ArrayList<NameValuePair> nvps = new ArrayList<>();
		for (int i = 0; i < params.length; i += 2) {
			try {
				nvps.add(new BasicNameValuePair(params[i], params[i+1]));
			} catch (ArrayIndexOutOfBoundsException ex) {
				// it'll exit out of the for-loop, and just leave off the last param
				Log.e(TAG, "Params didn't come in name/value pairs", ex);
			}
		}
		return nvps;
	}
	
	protected Result onInput(InputStream in) throws Exception {
		return null;
	}
	
	protected abstract String getUserAgent();

}
