package com.parrot.movietask.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;
import com.parrot.movietask.repositories.IqiyiSearchTermsTVRepository;
import com.parrot.movietask.service.IqiyiSearchTermsTVService;

@Service
public class IqiyiSearchTermsTVServiceImpl implements IqiyiSearchTermsTVService{
	
	@Resource
	IqiyiSearchTermsTVRepository iqiyiSearchTermsTVRepository;
	
	@Override
	public List<IqiyiSearchTermsTV> findAllIqiyiSearchTermsTV() {
		return iqiyiSearchTermsTVRepository.findAll();
	}
	
	@Override
	public Page<IqiyiSearchTermsTV> findIqiyiSearchTermsTVByPage(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "title");
		return iqiyiSearchTermsTVRepository.findAll(pageable);
	}
	
	@Override
	public long getIqiyiSearchTermsTVCount() {
		return iqiyiSearchTermsTVRepository.count();
	}

	@Override
	public Integer getEachTaskSize(int taskCount) {
		Integer taskSize = 0 ;
		if(taskCount > 0) {
			if(getIqiyiSearchTermsTVCount() % taskCount ==0)
				taskSize = (int) (getIqiyiSearchTermsTVCount() / taskCount);
			else
				taskSize = (int) (getIqiyiSearchTermsTVCount() / taskCount) + 1;
		}
		return taskSize;
	}

	
}
