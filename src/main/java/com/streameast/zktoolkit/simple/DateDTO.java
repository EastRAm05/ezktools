package com.streameast.simple;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateDTO {
	
	private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	public SimpleDateFormat formatH = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public String newDate(Timestamp date, SimpleDateFormat formato) {
		String reg = "";
		if(date != null) {
			reg = formato.format(date);
		}
		return reg;
	}
	
	public String newDate(Timestamp d) {
		return newDate(d, format);
	}
}
