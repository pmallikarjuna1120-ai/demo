package com.example.batch.entity;

public class Customer {
	private Long id;
	private String name;
	private String status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String s) {
		this.status = s;
	}
}