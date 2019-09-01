package com.parrot.movietask.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.parrot.movietask.bean.IqiyiSearchTermsTV;
import com.parrot.movietask.service.DynamoService;
@Service
public class DynamoServiceImpl  implements DynamoService{
	private static Logger logger = Logger.getLogger(DynamoServiceImpl.class);
	
	private static final String TABLE_NAME="spe-nicolas";
	private static final String CHARSET_NAME = "UTF-8";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Autowired  
    private Environment env;  
	
	public  void insertDynamoDB(List<IqiyiSearchTermsTV> tvList) throws Exception {
		//Connect AWS DynamoDB
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion(Regions.US_EAST_1).build();
		
	    DynamoDB dynamoDB = new DynamoDB(client);
	    //Get DynamoDB Tableinfo
	    Table table = dynamoDB.getTable(TABLE_NAME);
	    
	    
	    String url = env.getProperty("API_URL");
	    if(tvList != null) {
	    	StringBuffer comments = new StringBuffer();
	    	comments.append("[");
	    	int i = 1;
	    	for(IqiyiSearchTermsTV istTV : tvList) {
	    		String title = istTV.getTitle();
	    		String timestamp = dateFormat.format(new Date());
	    		String mappingValue = istTV.getMappingValue();
	    		//url should be as follow
	    		// url = url + "?file_id=" + istTV.getTitleId();
	    		JsonParser parser = new JsonFactory().createParser(loadGETJson(url));
	    		JsonNode rootNode = new ObjectMapper().readTree(parser);
	    		JsonNode commentsNode = rootNode.path("comments");
	    		Iterator<JsonNode> iter = commentsNode.iterator();
	    		ObjectNode currentNode;
	    		while (iter.hasNext()) {
	                currentNode = (ObjectNode) iter.next(); 
	                comments.append("{\"content\":\"" + URLEncoder.encode(currentNode.path("content").asText(), CHARSET_NAME) + "\","
	                								+"\"author\":\"" + currentNode.path("author").path("name").asText() +   "\","
	                								+"\"rating\":\"" + currentNode.path("rating").path("value").asText() + "\","
	                								+"\"created_at\":\"" + currentNode.path("created_at").asText() + "\"},");
	                i++;
	    		}
	    		comments.replace(comments.length()-1, comments.length(),"]");
	    		try {
                    table.putItem(new Item().withPrimaryKey("title", title, "timestamp", timestamp)
                    		.withString("mappigvalue", mappingValue)
                    		.withJSON("comments", comments.toString()));
                }
                catch (Exception e) {
                	logger.error("Unable to add TV: " + title + "- " + mappingValue + "-" + timestamp, e);
                    break;
                }
	    		
	            parser.close();
	    	}
	    }
		
	}
	
	/**
	 * The request of GET the  API and  return JSON
	 * @param url
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	private static Reader loadGETJson(String url) throws IOException {
		URL oracle = new URL(url);
		URLConnection urlConnection = oracle.openConnection();
		Reader read = new InputStreamReader(urlConnection.getInputStream(), CHARSET_NAME);
		return read;
	}

//	public static void main(String[] args) throws JsonParseException, IOException {
//		List<IqiyiSearchTermsTV> tvListByPage = iqiyiSearchTermsTVService.findAllIqiyiSearchTermsTV();
//		insertDynamoDB(tvListByPage);
//		System.out.println("插入数据成功");
//	}
}
