package com.parrot.movietask.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;
import com.parrot.movietask.service.DynamoService;
import com.parrot.movietask.service.IqiyiSearchTermsTVService;
import com.parrot.movietask.service.S3Service;

@Component
public class AsyncTasks {
	@Autowired
	private IqiyiSearchTermsTVService iqiyiSearchTermsTVService;
	@Autowired
	DynamoService dynamoService;
	@Autowired
	S3Service s3Service;
	
	private Logger logger = Logger.getLogger(AsyncTasks.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * Inserting to DynamoDB operating
	 * @param taskNumber
	 * @return
	 * @throws Exception
	 */
	//@Async
    public Future<String> insertDynamoDBTask(int taskNumber) throws Exception {
		logger.info("Start the insertDynamo task of " + taskNumber + " : " + dateFormat.format(new Date()));
		int taskSize = iqiyiSearchTermsTVService.getEachTaskSize(5);
		long start = System.currentTimeMillis();
		List<IqiyiSearchTermsTV> tvList= iqiyiSearchTermsTVService.findIqiyiSearchTermsTVByPage(taskNumber, taskSize).getContent();
		//Paging table data to insert dynamoDB.
		dynamoService.insertDynamoDB(tvList);
		
        long end = System.currentTimeMillis();
        logger.info("Finish the task"+taskNumber+", return "+tvList.size()+ " elapsed time : " + (end - start) + "millisecond");
        return new AsyncResult<>("insertDynamoDBTask"+taskNumber + " finished");
    }
	
	/**
	 * Uploading files to S3 operation
	 * @return
	 * @throws Exception
	 */
	@Async
    public Future<String> uploadFileToS3Task() throws Exception {
		logger.info("Uploading files into S3 start ：" + dateFormat.format(new Date()));
		long start = System.currentTimeMillis();
		List<IqiyiSearchTermsTV> tvList= iqiyiSearchTermsTVService.findAllIqiyiSearchTermsTV();
		s3Service.uploadJSONFiles(tvList);
		long end = System.currentTimeMillis();
		logger.info("Uploading files into S3 finished,  the total time consuming ：" + (end - start) + " millisecond");
		 return new AsyncResult<>("uploadFileToS3Task finished");
	}
}
