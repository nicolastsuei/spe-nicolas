package com.parrot.movietask.service;

import java.util.List;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;

/**
 * AWS S3 operating interfaces
 * @author cuilijian
 *
 */
public interface S3Service {
	/**
	 * Upload JSON files to S3
	 * @throws Exception
	 */
	public void uploadJSONFiles(List<IqiyiSearchTermsTV> tvList) throws Exception;
}
