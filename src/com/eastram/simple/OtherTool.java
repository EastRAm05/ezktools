package com.eastram.simple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Timebox;

import com.eastram.zk.QRHandler;

/**
 * Clase con utilidades correspondientes a procesos internos.
 * @author user
 *
 */
public class OtherTool {

	/**
	 * Devuelve el metodo selecionado.
	 * @param name nombre del metodo a devolver.
	 * @param clase clase del objecto que contiene el metodo.
	 * @return Method
	 * @author Estuardo Ramos
	 */
	public static Method getMetodo(String name, Class<? extends Object> clase) {
		Method met = null;
		Method[] metodos = clase.getMethods();
		for(Method m : metodos) {
			if(m.getName().equals(name)) {
				met = m;
				break;
			}
		}
		return met;
	}
	
	/**
	 * Devuelve el componente seleccionado.
	 * @param desktop Objecto desktop.
	 * @param id Id del componente.
	 * @return Component
	 * @author Estuardo Ramos
	 */
	public static Component getComponent(Desktop desktop, String id) {
		@SuppressWarnings("unchecked")
		Iterator<Component> componentIterator = desktop.getComponents().iterator();
		Component comp = null;
		boolean val = true;
		while(componentIterator.hasNext() && val) {
			comp = componentIterator.next();
			if(comp.getId().equals(id)) {
				val = false;
			}
		}
		return val ? null : comp;
	}
	
	/**
	 * Ingresa Items a un Combobox
	 * @param comb : Combo a ser llenado
	 * @param con : Coneccion a la DB
	 * @param query : Query para llenar el Combo
	 * @throws SQLException 
	 */
	public static Combobox setComboChild(Combobox comb, Connection con, String query) throws SQLException {
		List<Object[]> ob = new QRHandler().consultaArray(con, query, null);
		for(int cont = 0; cont<ob.size(); cont++) {
			Object[] array = ob.get(cont);
			Comboitem item = new Comboitem();
			item.setLabel((String) array[array.length-1]);
			if(array.length > 1) {
				item.setId(array[0].toString());
			}
			comb.appendChild(item);
		}
		return comb;
	}
	
	/**
	 * Ejecuta Insert/Update con parametros intaciados de Componet (Textbox, Combobox, etc...)
	 * @param enlace
	 * @param sql
	 * @param comps
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 */
	public static void ejecutaUpdate(Connection enlace, String sql, Object[] comps) {
		Object[] params = new Object[comps.length];
		for(int cont = 0; cont < comps.length; cont++) {
			if(comps[cont] instanceof Component) {
				try {
					params[cont] = getMetodo("getValue", comps[cont].getClass()).invoke(comps[cont]);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} else if(comps[cont] instanceof Component[]) {
				Component[] dates = (Component[]) comps[cont];
				params[cont] = getComboFecha(((Datebox)dates[0]).getValue(), ((Timebox)dates[1]).getValue());
			} else {
				params[cont] = comps[cont];
			}
		}
		new QRHandler().update(enlace, sql, params);
	}
	
	/**
	 * Debuelve la fecha en String de dos componentes fecha y hora 
	 * @param fecha
	 * @param hora
	 * @return
	 */
	public static String getComboFecha(Date fecha, Date hora) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String fch = format.format(fecha)+" ";
		format = new SimpleDateFormat("HH:mm:ss");
		fch += hora == null? "00:00:00" : format.format(hora);
		return fch;
	}
	
	/**
	 * Vacia los componentes ingresados
	 * @param components
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void cleanComponent(Component... components) 
	throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for(Component comp : components) {
			Object ob = null;
			getMetodo("setValue", comp.getClass()).invoke(comp, ob);
		}
	}
}
