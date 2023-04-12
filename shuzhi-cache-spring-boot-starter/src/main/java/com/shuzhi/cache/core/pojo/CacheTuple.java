package com.shuzhi.cache.core.pojo;

/**
 * 缓存元组
 * @author zhangyabo
 */
public class CacheTuple implements Comparable<CacheTuple> {
	
	private Double score;
	private String value;
	
	public CacheTuple(){
		
	}
	
	public CacheTuple(String value, Double score){
		this.value = value;
		this.score = score;
	}
	
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(CacheTuple o) {
		return this.score < o.getScore() ? -1 : 1;
	}

}
