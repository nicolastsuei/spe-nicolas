package com.parrot.movietask.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Bean of Iqiyi_search_terms_TV table
 * @author cuilijian
 *
 */
@Entity
@Table(name = "iqiyi_search_terms_tv")
public class IqiyiSearchTermsTV {
	@Id
	@Column(name = "title_id")
	private String titleId;
	@Column(name = "title")
	private String title;
	@Column(name = "mapping_value")
	private String mappingValue;
	public String getTitleId() {
		return titleId;
	}
	public void setTitleId(String titleId) {
		this.titleId = titleId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMappingValue() {
		return mappingValue;
	}
	public void setMappingValue(String mappingValue) {
		this.mappingValue = mappingValue;
	}
}
