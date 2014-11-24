package com.sentimetrix.tweet.twitter;


import com.sentimetrix.tweet.openauthentication.OAuthExample;
import com.sentimetrix.tweet.openauthentication.OAuthTokenSecret;
import com.sentimetrix.tweet.util.OAuthUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RESTSearch
{
	private static String DEFAULT_QUERY = "lang:en :) OR :(";
    private static String DEFAULT_FILENAME = "searchresults";

    BufferedWriter outFileWriter;
    OAuthTokenSecret oAuthTokens;
    OAuthConsumer consumer;
    /**
     * Creates a OAuthConsumer with the current consumer & user access tokens and secrets
     * @return consumer
     */
    public OAuthConsumer getConsumer()
    {
        OAuthConsumer consumer = new DefaultOAuthConsumer(OAuthUtils.CONSUMER_KEY, OAuthUtils.CONSUMER_SECRET);
        consumer.setTokenWithSecret(oAuthTokens.getAccessToken(), oAuthTokens.getAccessSecret());
        return consumer;
    }
    
    /**
     * Load the User Access Token, and the User Access Secret
     */
    public void loadTwitterToken()
    {
        oAuthTokens = OAuthExample.DEBUGUserAccessSecret();
    }

    /**
     * Fetches tweets matching a DEFAULT_QUERY
     * @param DEFAULT_QUERY for which tweets need to be fetched
     * @return an array of status objects
     */
    public JSONArray getSearchResults(Long lastId, String query)
    {
        try{
            //construct the request url
            String URL_PARAM_SEPERATOR = "&";
            StringBuilder url = new StringBuilder();
            url.append("https://api.twitter.com/1.1/search/tweets.json?q=");
            //DEFAULT_QUERY needs to be encoded
            url.append(URLEncoder.encode(query, "UTF-8"));
            url.append(URL_PARAM_SEPERATOR);
            url.append("count=100"); // twitter's max is 100
            if(!lastId.equals(0)) {
	            url.append(URL_PARAM_SEPERATOR);
	            url.append("max_id=" + lastId);
            }
            URL navurl = new URL(url.toString());
            HttpURLConnection huc = (HttpURLConnection) navurl.openConnection();
            huc.setReadTimeout(5000);
            consumer.sign(huc);
            huc.connect();
            if(huc.getResponseCode()==400||huc.getResponseCode()==404||huc.getResponseCode()==429)
            {
                System.out.println(huc.getResponseMessage());
                try {
                    huc.disconnect();
                    Thread.sleep(this.getWaitTime("/search/tweets"));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } 
            else if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
            {
                System.out.println(huc.getResponseMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            else 
            {
            	BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
            
	            String temp;
	            StringBuilder page = new StringBuilder();
	            while( (temp = bRead.readLine())!=null)
	            {
	                page.append(temp);
	            }
	            JSONTokener jsonTokener = new JSONTokener(page.toString());
	            try {
	                JSONObject json = new JSONObject(jsonTokener);
	                JSONArray results = json.getJSONArray("statuses");
	                return results;
	            } catch (JSONException ex) {
	                Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
	            }   
            }
        } catch (OAuthCommunicationException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthMessageSignerException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthExpectationFailedException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

     /**
     * Retrieves the rate limit status of the application
     * @return
     */
   public JSONObject getRateLimitStatus()
   {
     try{
            URL url = new URL("https://api.twitter.com/1.1/application/rate_limit_status.json");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setReadTimeout(5000);
            consumer.sign(huc);
            huc.connect();
            
            int responseCode = huc.getResponseCode();
            
            if(responseCode == 200) {
	            BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
	            StringBuffer page = new StringBuffer();
	            String temp= "";
	            while((temp = bRead.readLine())!=null)
	            {
	                page.append(temp);
	            }
	            bRead.close();
	            return (new JSONObject(page.toString()));
            }
        } catch (JSONException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthCommunicationException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (OAuthMessageSignerException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthExpectationFailedException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
     return null;
   }

   /**
    * Initialize the file writer
    * @param path of the file
    * @param outFilename name of the file
    */
   public void initializeWriters(String outFilename) {
        try {
            File fl = new File(outFilename);
            if(!fl.exists())
            {
                fl.createNewFile();
            }
            /**
             * Use UTF-8 encoding when saving files to avoid
             * losing Unicode characters in the data
             */
            outFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename,true),"UTF-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

   /**
    * Close the opened filewriter to save the data
    */
   public void cleanup()
   {
        try {
            outFileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
   }

   /**
    * Writes the retrieved data to the output file
    * @param data containing the retrived information in JSON
    * @param user name of the user currently being written
    */
    public void write(JSONArray searchResults)
    {
        try
        {
            for(int i=0;i<searchResults.length();i++)
            {
                try {
                    outFileWriter.write(searchResults.getJSONObject(i).toString());
                    outFileWriter.newLine();
                } catch (JSONException ex) {
                    Logger.getLogger(RESTSearch.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Retrieves the wait time if the API Rate Limit has been hit
     * @param api the name of the API currently being used
     * @return the number of milliseconds to wait before initiating a new request
     */
    public long getWaitTime(String api)
    {
        JSONObject jobj = this.getRateLimitStatus();
        if(jobj!=null)
        {
            try {
                if(!jobj.isNull("resources"))
                {
                    JSONObject resourcesobj = jobj.getJSONObject("resources");
                    JSONObject statusobj = resourcesobj.getJSONObject("statuses");
                    JSONObject apilimit = statusobj.getJSONObject(api);
                    int numremhits = apilimit.getInt("remaining");
                    if(numremhits<=1)
                    {
                        long resettime = apilimit.getInt("reset");
                        resettime = resettime*1000; //convert to milliseconds
                        return resettime;
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return 15 * 60 * 1000; //15 mins
    }

    /**
     * Creates an OR search DEFAULT_QUERY from the supplied terms
     * @param queryTerms
     * @return a String formatted as term1 OR term2
     */
    public String createORQuery(ArrayList<String> queryTerms)
    {
        String OR_Operator = " OR ";
        StringBuffer querystr = new StringBuffer();
        int count = 1;
        for(String term:queryTerms)
        {
            if(count==1)
            {
                querystr.append(term);
            }
            else
            {
                querystr.append(OR_Operator).append(term);
            }
        }
        return querystr.toString();
    }

    public static void main(String[] args)
    {
        RESTSearch rse = new RESTSearch();
        ArrayList<String> queryterms = new ArrayList<String>();        
        String outfilename = rse.DEFAULT_FILENAME;
        if(args!=null)
        {
            if(args.length>0)
            {
                for(int i=0;i<args.length;i++)
                {
                    queryterms.add(args[i]);
                    outfilename += "_" + args[i];
                }
            }
            else
            {
                queryterms.add(rse.DEFAULT_QUERY);
                outfilename += "_" + rse.DEFAULT_QUERY;
            }
        }

    	rse.loadTwitterToken();
        rse.consumer = rse.getConsumer();
        
		rse.initializeWriters(outfilename + ".json");

    	Long lastId = 0l;

    	while(true){
            System.out.println(rse.getRateLimitStatus());
            JSONArray results = rse.getSearchResults(lastId, rse.createORQuery(queryterms));
            
            if(results!=null && results.length() > 0)
            {

	            System.out.println("retrieved " + results.length() + " tweets");
	            if(!lastId.equals((Long) results.getJSONObject(results.length()-1).get("id"))) {
	                lastId = (Long) results.getJSONObject(results.length()-1).get("id");
	                rse.write(results);	            	
	            } else {
	            	System.out.println("done retrieving at max_id: " + lastId);
	            	break;
	            }
            } 
            else if(results ==null ) 
            {
            	System.out.println("trying again");            
    		} 
            else 
            {
            	System.out.println("done. no tweets retreived");
            	break;
            }
            
        }

        rse.cleanup();
    }
}

