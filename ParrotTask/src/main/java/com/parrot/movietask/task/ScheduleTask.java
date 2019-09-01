package com.parrot.movietask.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.parrot.movietask.service.DynamoService;
import com.parrot.movietask.service.S3Service;

@Component
public class ScheduleTask {
	private Logger logger = Logger.getLogger(ScheduleTask.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	@Autowired
	AsyncTasks asyncTasks;
	
	@Autowired
	DynamoService dynamoService;
	
	@Autowired
	S3Service s3Service;
	
	//Start at 0 and do it every hour  
	//@Scheduled(cron = "0 0 0/1 * * ? *")
	@Scheduled(cron = "0/20 * * * * ?")
    public void dynamoTask() throws Exception {
		logger.info("Inserting data into DynamoDB start ：" + dateFormat.format(new Date()));
		long start = System.currentTimeMillis();
		Future<String> task1 = asyncTasks.insertDynamoDBTask(0);
		Future<String> task2 = asyncTasks.insertDynamoDBTask(1);
		Future<String> task3 = asyncTasks.insertDynamoDBTask(2);
		Future<String> task4 = asyncTasks.insertDynamoDBTask(3);
		Future<String> task5 = asyncTasks.insertDynamoDBTask(4);
		while(true) {
			if(task1.isDone() && task2.isDone() && task3.isDone() && task4.isDone() && task5.isDone()) {
				break;
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Inserting data into DynamoDB finished,  the total time consuming ：" + (end - start) + " millisecond");
    }
	
	@Scheduled(cron = "0/40 * * * * ?")
	public void s3Task() throws Exception {
		logger.info("Uploading files into S3 start : " + dateFormat.format(new Date()));
		long start = System.currentTimeMillis();
		Future<String> task = asyncTasks.uploadFileToS3Task();
		while(true) {
			if(task.isDone()) {
				break;
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Uploading files into S3 finished,  the total time consuming ：" + (end - start) + " millisecond");
    }
}
