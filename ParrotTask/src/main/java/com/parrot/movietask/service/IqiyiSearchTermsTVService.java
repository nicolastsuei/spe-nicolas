package com.parrot.movietask.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;

public interface IqiyiSearchTermsTVService {
	/**
	 * Get all the query info in one hour
	 * @return
	 */
	List<IqiyiSearchTermsTV> findAllIqiyiSearchTermsTV();
	/**
	 * Paging to get table data
	 * @param page
	 * @param size
	 * @return
	 */
	Page<IqiyiSearchTermsTV> findIqiyiSearchTermsTVByPage(Integer page,Integer size);
	
	/**
	 * Get the amount of table data
	 * @return
	 */
	long getIqiyiSearchTermsTVCount();
	
	/**
	 * Get the number of search terms for each task according to the number of tasks
	 * @return
	 */
	Integer getEachTaskSize(int taskCount);
	
}
