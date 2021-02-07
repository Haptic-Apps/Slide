package me.ccrama.redditslide.Synccit;

import org.json.JSONObject;

/**
 * https://github.com/drakeapps/synccit#example-json-update-call
 */
abstract class SynccitUpdateTask extends SynccitTask {

	@SuppressWarnings("unused")
	private static final String TAG = SynccitUpdateTask.class.getSimpleName();
	
	private static final String UPDATE_MODE = "update";

	SynccitUpdateTask(String devName) {
		super(devName);
	}

	@Override
	protected String getMode() {
		return UPDATE_MODE;
	}

	@Override
	protected SynccitResponse onInput(String in) throws Exception {
		JSONObject obj = new JSONObject(in);
		String key = obj.has("success") ? "success" : "error";
		return new SynccitResponse(key, obj.get(key).toString());
	}
}
