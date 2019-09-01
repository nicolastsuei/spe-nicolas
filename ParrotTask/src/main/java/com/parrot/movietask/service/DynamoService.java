package com.parrot.movietask.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.parrot.movietask.bean.IqiyiSearchTermsTV;

/**
 * AWS DynamoDB operating interfaces
 * @author cuilijian
 *
 */
public interface DynamoService {

	/**
	 * Paging table data query to get interface data and add it to Dynamo database.
	 * @param tvListByPage
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public void insertDynamoDB(List<IqiyiSearchTermsTV> tvList) throws Exception ;
}
