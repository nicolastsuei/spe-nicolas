package com.parrot.movietask.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/**
 * AWS Dynamo Utilities
 * @author cuilijian
 *
 */
public class DynamoUtil {
	private static Logger logger = Logger.getLogger(DynamoUtil.class);
	private static AmazonDynamoDB dynamoDB = null;
	private static final String TABLE_NAME="test0827";
	 private static final String CHARSET_NAME = "UTF-8";
	 /***
     * Dynamo initial method
     * */
	public static AmazonDynamoDB init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (C:\\Users\\xxx\\.aws\\credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
        	logger.error("Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\xxx\\.aws\\credentials), and is in valid format.", e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion(Regions.US_EAST_1)
            .build();
        
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
        
        return dynamoDB;
    }

	/**
	 * The request of GET the  API and  return JSON
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Reader loadGETJson(String url) throws Exception {
		URL oracle = new URL(url);
		URLConnection urlConnection = oracle.openConnection();
		Reader read = new InputStreamReader(urlConnection.getInputStream(), CHARSET_NAME);
		return read;
	}
	
	 public static Map<String, AttributeValue> newItem(String title, String rating, String... fans) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("title", new AttributeValue(title));
        item.put("rating", new AttributeValue(rating));
        item.put("fans", new AttributeValue().withSS(fans));

        return item;
    }
}
