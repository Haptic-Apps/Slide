package me.ccrama.redditslide.Synccit;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.ccrama.redditslide.Synccit.http.HttpPostTask;

public abstract class SynccitTask extends HttpPostTask<SynccitResponse> {

	private static final String TAG = SynccitTask.class.getSimpleName();
	
	private static final String API_URL = "https://api.synccit.com/api.php";
	
	private static final String PARAM_TYPE = "type";
	private static final String PARAM_DATA = "data";
	private static final String TYPE_JSON = "json";
	
	private static final String KEY_USERNAME = "username";
	private static final String KEY_AUTH = "auth";
	private static final String KEY_DEV = "dev";
	private static final String KEY_MODE = "mode";
	private static final String KEY_API = "api";
	private static final String KEY_LINKS = "links";
	private static final String API_LEVEL = "1";
	
	/** developer name */
	private String devName;
	
	SynccitTask(String devName) {
		super(API_URL);
		this.devName = devName;
	}

	@Override
	protected SynccitResponse doInBackground(String... linkIds) {
		if (TextUtils.isEmpty(getUsername()) || TextUtils.isEmpty(getAuth())) {
			Log.i(TAG, "synccit username or auth not set. aborting");
			return null;
		}
		
		String data;
		try {
			data = buildJson(linkIds);
		} catch (Exception e) {
			Log.e(TAG, "buildJson", e);
			return null;
		}
			
		return super.doInBackground(
				PARAM_TYPE, TYPE_JSON,
				PARAM_DATA, data
		);
	}
	
	/**
	 * https://github.com/drakeapps/synccit#example-json-update-call
	 * 
	 * {
		    "username"  : "james",
		    "auth"      : "9m89x0",
		    "dev"       : "synccit json",
		    "mode"      : "update",
		    "links"     : [
		        {
		            "id" : "111111"
		        },
		        {
		            "id" : "222222",
		            "comments" : "132"
		        },
		        {
		            "id" : "333333",
		            "comments" : "313",
		            "both" : true
		        },
		        {
		            "id" : "444444"
		        }
		    ]
		}
	 * @throws JSONException 
	 */
	private String buildJson(String... linkIds) throws JSONException {
		JSONObject rootOb = new JSONObject();
		rootOb.put(KEY_USERNAME, getUsername());
		rootOb.put(KEY_AUTH, getAuth());
		rootOb.put(KEY_DEV, devName);
		rootOb.put(KEY_MODE, getMode());
		rootOb.put(KEY_API, API_LEVEL);
		
		JSONArray links = new JSONArray();
		for (String linkId : linkIds) {
			JSONObject linkObject = new JSONObject();
			linkObject.put("id", linkId);
			links.put(linkObject);
		}
		rootOb.put(KEY_LINKS, links);
		
		return rootOb.toString();
	}
	
	protected abstract String getUsername();
	protected abstract String getAuth();
	protected abstract String getMode();

	@Override
	protected void onPostExecute(SynccitResponse result) {
		super.onPostExecute(result);
		if (result != null && result.isError()) {
			Log.w(TAG, "synccit error: " + result.getMessage());
		}
	}

}
