package com.streameast.zk;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.streameast.simple.OtherTool;

/**
 * Gestiona la conexion con la DB para la manipulacion con el Grid.
 * @author Stream East
 */
public class ListHandler<T> implements List<T>{
	
	private List<T> lista = new ArrayList<T>();
	private Class<T> classDto;
	private String page;
	private int pageSize = 8;
	private String QUERY;
	private String PRIN;
	private String COUNT;
	private List<String> vquery;
	private List<String> vform = new ArrayList<String>();
	private List<String> velim;
	private DataSource data;
	private Component view;
	private Paging paging = null;
	private List<Object> params = new ArrayList<Object>();
	private QRHandler consultor = new QRHandler();
	private boolean valWarning = true;
	
	private String[] paramsQuery = 
			{"SELECT * FROM (SELECT A.*, ROWNUM RNUM FROM (",
			 ") A WHERE ROWNUM <= #MAX#) WHERE RNUM >= #MIN#",
			 "SELECT MAX(ROWNUM) FROM (", ")"};
	private String T_WARNING = "Advertencia!";
	private String M_WARNING = "No se encontraron datos para la búsqueda, intente con otros filtros de búsqueda.";
	
	public ListHandler(Class<T> cl) {
		classDto = cl;
	}
	@SuppressWarnings("unchecked")
	public ListHandler(T ob) {
		classDto = (Class<T>) ob.getClass();
	}
	
	/**
	 * Coloca un nombre, para identificar los Logs
	 * @param name
	 */
	public void setPage(String name) {
		page = name;
	}
	/**
	 * Devuelve el nombre de la paguina ingresada prebiamente para logs.
	 * @return
	 */
	public String getPage() {
		return page;
	}
	/**
	 * Cantidad de registros por paguina en el Grid.
	 * 
	 * @param size
	 */
	public void setPageSize(int size) {
		pageSize = size;
	}
	public int getPageSize() {
		return pageSize;
	}
	/**
	 * Agrega el query de consulta para la tabla.
	 * @param query
	 */
	public void setQuery(String query) {
		Matcher valida = Pattern.compile("<#([^#]+)#>").matcher(query);
		vquery = new ArrayList<String>();
		while(valida.find()) {
			String valid = valida.group(1);
			String format = setFormat(valid);
			query = query.replace(valid, format);
			vquery.add(format);
		}
		query = query.replaceAll("<#", "");
		QUERY = query.replaceAll("#>", "");
		setProsQuery(QUERY);
	}
	private String setFormat(String valid) {
		Matcher validForm = Pattern.compile("![\\S]+").matcher(valid);
		String dato = null;
		if(validForm.find()) {
			dato = validForm.group();
		}
		vform.add(dato);
		return dato == null? valid : valid.replace(dato+" ", "");
	}
	private void setProsQuery(String query) {
		PRIN = paramsQuery[0] + query + paramsQuery[1];
		COUNT = paramsQuery[2] + query + paramsQuery[3];
	}
	public String getQuery() {
		return QUERY;
	}
	public void setCount(String query) {
		COUNT = query;
	}
	public String getCount() {
		return COUNT;
	}
	public void setSource(String query, DataSource source) {
		setQuery(query);
		setDataSource(source);
	}
	public void setDataSource(DataSource source) {
		data = source;
	}
	public void setComponentView(Component comp) {
		view = comp;
	}
	public void setComponentView(Component comp, Paging paging) {
		view = comp;
		setPaging(paging);
	}
	public void setPaging(Paging pag) {
		paging = pag;
		paging.addEventListener("onPaging", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				PagingEvent pagEvent = (PagingEvent) event;
				int indice = pagEvent.getActivePage() * pageSize;
				executeQuery(indice);
				view();
			}
		}); 
	}
	public void warnings(boolean val) {
		valWarning = val; 
	}
	public void setWarning(String title, String mensaje) {
		T_WARNING = title;
		M_WARNING = mensaje;
	}
	
	public void simpleSearch(Object... components) throws Exception {
		setArrayComponents(components);
		executeQuery();
		view();
	}
	
	public void view() {
		AnnotateDataBinder binder = new AnnotateDataBinder(view);
		binder.loadAll();
	}
	
	public void setArrayComponents(Object... comps) {
		params = new ArrayList<Object>();
		velim = new ArrayList<String>();
		for(Object comp : comps) {
			setComponent(comp);
		}
		String query = QUERY;
		for(String val : velim) {
			query = query.replace(val, "");
		}
		setProsQuery(query);
	}
	private void setComponent(Object comp) {
		/*if(comp instanceof Checkbox || comp instanceof Radio) {
			Class<? extends Object> clase = comp.getClass();
			String valid = null;
			if((Boolean) Inter.getMetodo("isChecked", clase).invoke(comp)) {
				valid = (String) Inter.getMetodo("getId", clase).invoke(comp);
				if(valid.equals("")) {
					valid = (String) Inter.getMetodo("getLabel", clase).invoke(comp);
				}
			}
			valida(valid);
		} else */if(comp instanceof Combobox) {
			Combobox combo = (Combobox) comp;
			Comboitem item = combo.getSelectedItem();
			String val = item == null? "TODO" : item.getId();
			val = val.equals("")? combo.getValue().toUpperCase() : val;
			if(val.equals("TODO") || val.equals("TODOS")) {
				val = null;
			} 
			valida(val);
		} else if(comp instanceof Component) {
			try {
				valida(OtherTool.getMetodo("getValue", comp.getClass()).invoke(comp));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			valida(comp);
		}
	}
	private void valida(Object valor) {
		if(valor == null || (valor instanceof String && ((String)valor).trim().equals(""))) {
			//velim.add(getValidacion(params.size()+1));
			velim.add(vquery.get(params.size()+velim.size()));
		} else {
			params.add(format(valor));
		}
	}
	private Object format(Object valor) {
		String form = vform.get(params.size()+velim.size());
		Object rest = valor;
		if(form != null) {
			boolean valDate = false;
			String[] sform = form.replaceAll("~", " ").replace("|", "#").split("#");
			form = sform[0];
			String dateForm = sform.length >= 2 && !sform[1].equals("")? sform[1] : "dd/MM/yyyy";
			SimpleDateFormat format = new SimpleDateFormat(dateForm);
			if(valor instanceof Date) {
				valDate = true;
			}
			try {
				if(form != null && !valor.getClass().toString().endsWith(form)) {
					if(form.equals("!String")) {
						rest = valDate? format.format(valor) : valor.toString();
					} else if(form.equals("!Integer")) {
						rest = Integer.parseInt(valor.toString());
					} else if(form.equals("!Date")) {
						rest = format.parse(valor.toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				rest = valor;
			}
		}
		return rest;
	}
	
	public List<T> executeQuery() throws Exception {
		return executeQuery(0);
	}
	public List<T> totalb() throws Exception {
		return executeQuery(-1);
	}
	
	private List<T> executeQuery(int index) throws Exception {
		Connection con = data.getConnection();
		try {
			if(index == 0 && paging != null) {
				paging.setTotalSize(getPagingSize(con));
				paging.setPageSize(pageSize);
				paging.setActivePage(index);
			}
			Object[] param = params.toArray(new Object[params.size()]);
			lista = consultor.queryListT(con, getIndexes(index), classDto, param);
			//params = new ArrayList<Object>();
			if((lista == null || lista.size() == 0) && valWarning) {
				Messagebox.show(M_WARNING, T_WARNING, Messagebox.OK, null);
			}
		} finally {
			con.close();
		}
		return lista;
	}
	private int getPagingSize(Connection con) throws Exception {
		int size = -1;
		Object[] param = params.toArray(new Object[params.size()]);
		Object res = consultor.queryObject(con, COUNT, param);
		res = ""+res;
		res = res == null || ((String)res).equals("") ? "0" : res;
		size = Integer.parseInt(res.toString());
		return size;
	}
	private String getIndexes(int index) {
		String query = PRIN;
		if(index >= 0 && paging != null) {
			query = query.replace("#MIN#", ""+(index+1));
			query = query.replace("#MAX#", ""+(index+pageSize));
		} else {
			query = query.replace(paramsQuery[0], "");
			query = query.replace(paramsQuery[1], "");
		}
		return query;
	}
	public int size() {
		return lista.size();
	}
	public boolean isEmpty() {
		return lista.isEmpty();
	}
	public boolean contains(Object o) {
		return lista.contains(o);
	}
	public Iterator<T> iterator() {
		return lista.iterator();
	}
	public Object[] toArray() {
		return lista.toArray();
	}
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return lista.toArray(a);
	}
	public boolean add(T e) {
		return lista.add(e);
	}
	public boolean remove(Object o) {
		return lista.remove(o);
	}
	public boolean containsAll(Collection<?> c) {
		return lista.containsAll(c);
	}
	public boolean addAll(Collection<? extends T> c) {
		return lista.addAll(c);
	}
	public boolean addAll(int index, Collection<? extends T> c) {
		return lista.addAll(index, c);
	}
	public boolean removeAll(Collection<?> c) {
		return lista.removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return lista.retainAll(c);
	}
	public void clear() {
		lista.clear();
	}
	public T get(int index) {
		return lista.get(index);
	}
	public T set(int index, T element) {
		return lista.set(index, element);
	}
	public void add(int index, T element) {
		lista.add(index, element);
	}
	public T remove(int index) {
		return lista.remove(index);
	}
	public int indexOf(Object o) {
		return lista.indexOf(o);
	}
	public int lastIndexOf(Object o) {
		return lista.lastIndexOf(o);
	}
	public ListIterator<T> listIterator() {
		return lista.listIterator();
	}
	public ListIterator<T> listIterator(int index) {
		return lista.listIterator(index);
	}
	public List<T> subList(int fromIndex, int toIndex) {
		return lista.subList(fromIndex, toIndex);
	}
}
