package com.streameast.zk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

public class QRHandler {
	
	private static QueryRunner queryRuner;
	
	private static QueryRunner getQueryRunner() {
		if(queryRuner == null) {
			queryRuner = new QueryRunner();
		}
		return queryRuner;
	}
	
	@SuppressWarnings("deprecation")
	public static List<Object[]> queryArray(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new ArrayListHandler());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Object queryObject(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new ScalarHandler());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static <T> List<T> queryListT(Connection conn, String sqlString, Class<T> classT, Object[] params) {
		try {
			return getQueryRunner().query(conn, sqlString, params, new BeanListHandler<T>(classT));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static int update(Connection conn, String sqlString, Object[] params) {
		try {
			return getQueryRunner().update(conn, sqlString, params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
