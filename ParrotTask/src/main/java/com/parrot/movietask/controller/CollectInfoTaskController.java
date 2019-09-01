package com.parrot.movietask.controller;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;
import com.parrot.movietask.service.DynamoService;
import com.parrot.movietask.service.IqiyiSearchTermsTVService;
import com.parrot.movietask.task.AsyncTasks;

@RestController
public class CollectInfoTaskController {
	private Logger logger = Logger.getLogger(CollectInfoTaskController.class);
	
	@Autowired
	IqiyiSearchTermsTVService iqiyiSearchTermsTVService;
	
	@Autowired
	DynamoService dynamoService;
	
	@Autowired
	AsyncTasks asyncTasks;
	
	@GetMapping("/dynamo")
	public String insertDynamo() throws  Exception  {
		List<IqiyiSearchTermsTV> tvList = iqiyiSearchTermsTVService.findIqiyiSearchTermsTVByPage(1, 20).getContent();
		dynamoService.insertDynamoDB(tvList);
	    return tvList.size()+"";
    }
	
	@GetMapping("/hello1")
	public String index1() {
	    return iqiyiSearchTermsTVService.getEachTaskSize(20).toString();
    }
  
	@GetMapping(value = "/movies1/{page}/{size}")
    public List<IqiyiSearchTermsTV> movieList1(@PathVariable("page") Integer page, @PathVariable("size") Integer size){
		List<IqiyiSearchTermsTV> movieList= iqiyiSearchTermsTVService.findIqiyiSearchTermsTVByPage(page, size).getContent();
		for(IqiyiSearchTermsTV stm : movieList) {
			System.out.println("Movie_id : "  + stm.getTitleId() +  "------------Movie_title : " + stm.getTitle()+  "------------Mapping_value : " + stm.getMappingValue());
		}
		return movieList;
    }
	
	@GetMapping(value = "/dynamotask")
	public String dynamotask() throws Exception {
		long start = System.currentTimeMillis();
		Future<String> task1 = asyncTasks.insertDynamoDBTask(0);
		Future<String> task2 = asyncTasks.insertDynamoDBTask(1);
		Future<String> task3 = asyncTasks.insertDynamoDBTask(2);
		Future<String> task4 = asyncTasks.insertDynamoDBTask(3);
		Future<String> task5 = asyncTasks.insertDynamoDBTask(4);
		while(true) {
			if(task1.isDone() && task2.isDone() && task3.isDone() && task4.isDone() && task5.isDone()) {
				// 三个任务都调用完成，退出循环等待
				break;
			}
		}
		long end = System.currentTimeMillis();
		logger.info("任务全部完成，总耗时：" + (end - start) + "毫秒");
		return "任务全部完成，总耗时：" + (end - start) + "毫秒";
	}
	
	@GetMapping(value = "/s3task")
	public String s3task() throws Exception {
		logger.info("Uploading files into S3 start : ");
		long start = System.currentTimeMillis();
		Future<String> task = asyncTasks.uploadFileToS3Task();
		while(true) {
			if(task.isDone()) {
				break;
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Uploading files into S3 finished,  the total time consuming ：" + (end - start) + " millisecond");
		return "任务全部完成，总耗时：" + (end - start) + "毫秒";
	}
}
