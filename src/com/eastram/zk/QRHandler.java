package com.eastram.zk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

public class QRHandler {
	
	private static QueryRunner queryRuner;
	
	private QueryRunner getQueryRunner() {
		if(queryRuner == null) {
			queryRuner = new QueryRunner();
		}
		return queryRuner;
	}
	
	@SuppressWarnings("deprecation")
	public List<Object[]> queryArray(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new ArrayListHandler());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public Object queryObject(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new ScalarHandler());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public <T> List<T> queryListT(Connection conn, String sqlString, Class<T> classT, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new BeanListHandler<T>(classT));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int update(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().update(conn, sqlString, params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
