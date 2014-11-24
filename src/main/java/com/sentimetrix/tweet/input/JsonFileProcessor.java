package com.sentimetrix.tweet.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonFileProcessor implements Iterator<JSONObject> {

	static private final Logger logger = Logger.getLogger(JsonFileProcessor.class.getName());

	protected BufferedReader fileBuffer;
	protected boolean endOfFile;
	protected String nextLine;
		
	private int lineCount;
	
	public JsonFileProcessor(File f) {
	
		lineCount = 0;
		endOfFile = false;

		InputStreamReader isr;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
			br = new BufferedReader(isr);
			nextLine = br.readLine();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "file {0} unsupported encoding: {1}", new Object[]{f.getAbsoluteFile(), e.getLocalizedMessage()});
			endOfFile = true;
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "file {0} not found: {1}", new Object[]{f.getAbsoluteFile(), e.getLocalizedMessage()});
			endOfFile = true;
		} catch (IOException e) {
			logger.log(Level.WARNING, "file {0} i/o error: {1}", new Object[]{f.getAbsoluteFile(), e.getLocalizedMessage()});
			endOfFile = true;
		} finally {
			fileBuffer = br;
		}
	}

	public boolean hasNext() {
		return !endOfFile;
	}

	public JSONObject next() {

		JSONObject obj = null;

		lineCount++;
		
		try {
			obj = (JSONObject) new JSONObject(nextLine);
		} catch (JSONException ex) {
			Logger.getLogger(JsonFileProcessor.class.getName()).log(
					Level.SEVERE, "unable to parse line("+lineCount+"): " + nextLine, ex);
		}
		
		try {
			nextLine = fileBuffer.readLine();
			if (nextLine == null) {
				endOfFile = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}