package com.parrot.movietask.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.parrot.movietask.bean.IqiyiSearchTermsTV;

public interface IqiyiSearchTermsTVRepository  extends 
JpaRepository<IqiyiSearchTermsTV,String>,JpaSpecificationExecutor<IqiyiSearchTermsTV> {}
