package me.ccrama.redditslide.Synccit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;

/**
 * https://github.com/drakeapps/synccit#example-json-read-call
 */
public abstract class SynccitReadTask extends SynccitTask {
	
	private static final String READ_MODE = "read";
	
	public SynccitReadTask(String devName) {
		super(devName);
	}

	@Override
	protected String getMode() {
		return READ_MODE;
	}

	@Override
	protected SynccitResponse onInput(InputStream in) throws Exception {
		HashSet<String> visitedLinkIds = new HashSet<>();
		
		// read the entire stream into a String
		Scanner s = new Scanner(in);
		String json = s.useDelimiter("\\A").next();
		
		try {
			JSONArray links = new JSONArray(json);
			
			int length = links.length();
			for (int i = 0; i < length; i++) {
				JSONObject linkNode = (JSONObject) links.get(i);
				visitedLinkIds.add(linkNode.get("id").toString());
			}
			
			onVisited(visitedLinkIds);

		} catch (JSONException ex) {
			JSONObject node = new JSONObject(json);
			if (node.has("error")) {
				return new SynccitResponse("error", node.get("error").toString());
			}
		}
		return null;
	}
	
	protected abstract void onVisited(HashSet<String> visitedThreadIds);

}
