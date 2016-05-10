package me.ccrama.redditslide.Synccit;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

/**
 * https://github.com/drakeapps/synccit#example-json-update-call
 */
public abstract class SynccitUpdateTask extends SynccitTask {

	@SuppressWarnings("unused")
	private static final String TAG = SynccitUpdateTask.class.getSimpleName();
	
	private static final String UPDATE_MODE = "update";
	
	public SynccitUpdateTask(String devName) {
		super(devName);
	}

	@Override
	protected String getMode() {
		return UPDATE_MODE;
	}

	@Override
	protected SynccitResponse onInput(InputStream in) throws Exception {
		// read the entire stream into a String
		Scanner s = new Scanner(in);
		String json = s.useDelimiter("\\A").next();

		JSONObject obj = new JSONObject(json);
		String key = obj.has("success") ? "success" : "error";
		return new SynccitResponse(key, obj.get(key).toString());
	}


}
