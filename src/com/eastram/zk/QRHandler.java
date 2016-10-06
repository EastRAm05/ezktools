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
	public List<Object[]> consultaArray(Connection con, String consulta, Object[] parametros) {
		try {
			return getQueryRunner().query(con, consulta, parametros, new ArrayListHandler());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public Object consultaSimple(Connection con, String consulta, Object[] parametros) {
		try {
			return getQueryRunner().query(con, consulta, parametros, new ScalarHandler());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public <T> List<T> consultaListT(Connection con, String consulta, Class<T> clase, Object[] paramentros) {
		try {
			return getQueryRunner().query(con, consulta, paramentros, new BeanListHandler<T>(clase));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int update(Connection con, String consulta, Object[] parametros) {
		try {
			return getQueryRunner().update(con, consulta, parametros);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
