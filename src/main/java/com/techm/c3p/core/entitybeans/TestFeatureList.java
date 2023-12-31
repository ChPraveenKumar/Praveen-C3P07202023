package com.techm.c3p.core.entitybeans;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "T_TSTSTRATEGY_M_TSTFEATURELST")
public class TestFeatureList implements Serializable

{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7545018593601573894L;

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Id
	private int id;

	@Column(name = "testFeature")
	private String testFeature;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinColumn(name = "testName")
	private TestDetail testDetail;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTestFeature() {
		return testFeature;
	}

	public void setTestFeature(String testFeature) {
		this.testFeature = testFeature;
	}

	public TestDetail getTestDetail() {
		return testDetail;
	}

	public void setTestDetail(TestDetail testDetail) {
		this.testDetail = testDetail;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((testDetail == null) ? 0 : testDetail.hashCode());
		result = prime * result + ((testFeature == null) ? 0 : testFeature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestFeatureList other = (TestFeatureList) obj;
		if (id != other.id)
			return false;
		if (testDetail == null) {
			if (other.testDetail != null)
				return false;
		} else if (!testDetail.equals(other.testDetail))
			return false;
		if (testFeature == null) {
			if (other.testFeature != null)
				return false;
		} else if (!testFeature.equals(other.testFeature))
			return false;
		return true;
	}
}
