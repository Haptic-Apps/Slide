package me.ccrama.redditslide.Synccit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * https://github.com/drakeapps/synccit#example-json-read-call
 */
abstract class SynccitReadTask extends SynccitTask {
	
	private static final String READ_MODE = "read";
	
	SynccitReadTask(String devName) {
		super(devName);
	}

	@Override
	protected String getMode() {
		return READ_MODE;
	}

	@Override
	protected SynccitResponse onInput(String in) throws Exception {
		HashSet<String> visitedLinkIds = new HashSet<>();

		try {
			JSONArray links = new JSONArray(in);
			int length = links.length();
			for (int i = 0; i < length; i++) {
				JSONObject linkNode = (JSONObject) links.get(i);
				visitedLinkIds.add(linkNode.get("id").toString());
			}
			onVisited(visitedLinkIds);

		} catch (JSONException ex) {
			JSONObject node = new JSONObject(in);
			if (node.has("error")) {
				return new SynccitResponse("error", node.get("error").toString());
			}
		}
		return null;
	}
	
	protected abstract void onVisited(HashSet<String> visitedThreadIds);

}
