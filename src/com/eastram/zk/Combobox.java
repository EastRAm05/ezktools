package com.eastram.zk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.zkoss.zul.Comboitem;

public class Combobox extends org.zkoss.zul.Combobox {

	private static final long serialVersionUID = 2434833817650744043L;
	private QRHandler consultor = new QRHandler();
	
	/**
	 * Ingresa Items a un Combobox
	 * @param con : Coneccion a la DB
	 * @param query : Query para llenar el Combo
	 * @param params : Parametros de busqueda
	 * @throws SQLException 
	 */
	public void setLista(Connection con, String query, Object[] params) throws SQLException {
		List<Object[]> ob = consultor.consultaArray(con, query, params);
		for(int cont = 0; cont<ob.size(); cont++) {
			Object[] array = ob.get(cont);
			Comboitem item = new Comboitem();
			item.setLabel((String) array[array.length-1]);
			if(array.length > 1) {
				item.setId(getId() + array[0].toString());
			}
			this.appendChild(item);
		}
	}
	
	/**
	 * Ingresa Items a un Combobox
	 * @param con : Coneccion a la DB
	 * @param query : Query para llenar el Combo
	 * @throws SQLException 
	 */
	public void setLista(Connection con, String query) throws SQLException {
		setLista(con, query, null);
	}
	
	public String getSelectedItemId() {
		return getSelectedItem() == null? null : getSelectedItem().getId().replace(getId(), "");
	}
}
