package com.xaexal.app.Common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.ANY)
public class Result<T> {
	private String message;
	private int code;
	private T value;

	public Result() {
		this(1);
		this.message="";
		this.value=null;
	}
	public Result(int code) {
		this.message="";
		this.value=null;
		this.code=code;
	}
	public Result(int code,String message) {
		this.code=code;
		this.value=null;
		this.message=message;
	}

	public Result(int code, String message,T value) {
		this.code=code;
		this.value=value;
		this.message=message;
	}
}
