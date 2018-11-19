package com.streameast.zktoolkit;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.zkoss.bind.Binder;
import org.zkoss.bind.impl.BinderUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.streameast.zktoolkit.simple.OtherTool;

/**
 * Manages the model, brings the information from the database, manages the components 
 * that suppose filters to the query and divides the large queries by blocks
 * @author streameast
 *
 * @param <T>
 * Object that represents a data record
 */
public class ListManager<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;
	private String page;
	private int size;
	private DataSource source;
	private Component view;
	private Paging paging;
	private boolean warnings;
	private String comboAll;
	private String query;
	private String useQuery;
	private List<String> queryValids;
	private List<String> formatValids;
	private List<String> quitValids;
	private List<Object> paramValids;
	private Binder binder;

	private final String FORMSTR = "!STRING";
	private final String FORMINT = "!INTEGER";
	private final String FORMDTE = "!DATE";
	private final String DATEFORM = "dd/MM/yyyy";
	private final String MATCHVALID = "<#([^#]+)#>";
	private final String MATCHFORMAT = "![\\\\S]+";
	private final Class<T> GENERICCLASS;
	private final String[] PARAMSQUERY = { "SELECT * FROM (SELECT A.*, ROWNUM RNUM FROM (",
			") A WHERE ROWNUM <= #MAX#) WHERE RNUM >= #MIN#", "SELECT MAX(ROWNUM) FROM (", ")" };

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public DataSource getSource() {
		return source;
	}

	public void setSource(DataSource source) {
		this.source = source;
	}

	public Component getView() {
		return view;
	}

	public void setView(Component view) {
		this.view = view;
		this.binder = BinderUtil.getBinder(this.view);
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
		this.paging.addEventListener("onPaging", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				PagingEvent pagingEvent = (PagingEvent) event;
				int index = pagingEvent.getActivePage() * size;
				executeQuery(index);
				binder.loadComponent(view, true);
			}
		});
	}

	public boolean isWarnings() {
		return warnings;
	}

	public void setWarnings(boolean warnings) {
		this.warnings = warnings;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = extractQuery(query);
	}

	public String getComboAll() {
		return comboAll;
	}

	public void setComboAll(String comboAll) {
		this.comboAll = comboAll;
	}

	public ListManager(Class<T> clase) {
		this.page = null;
		this.size = 8;
		this.source = null;
		this.view = null;
		this.paging = null;
		this.warnings = false;
		this.GENERICCLASS = clase;
		this.binder = null;
		this.query = null;
		this.useQuery = null;
		this.comboAll = "TODOS";
		this.queryValids = new ArrayList<String>();
		this.formatValids = new ArrayList<String>();
		this.quitValids = new ArrayList<String>();
		this.paramValids = new ArrayList<Object>();
	}
	
	public void init(DataSource source, String page, String query, Component view) {
		setSource(source);
		setPage(page);
		setQuery(query);
		setView(view);
	}
	
	public void search(Object... components) {
		collectComponents(components);
		executeQuery(0);
		binder.loadComponent(view, true);
	}
	
	public void collectComponents(Object... components) {
		for(Object component : components) {
			collectComponent(component);
		}
		useQuery = query;
		for(String valid : quitValids) {
			useQuery = useQuery.replace(valid, "");
		}
	}
	
	private String extractQuery(String query) {
		Matcher validQuery = Pattern.compile(MATCHVALID).matcher(query);
		queryValids.clear();
		String fooQuery = null;
		while(validQuery.find()) {
			String fooValid = validQuery.group(1);
			String fooFormat = extractFormat(fooValid);
			fooQuery = query.replace(fooValid, fooFormat);
		}
		fooQuery = fooQuery.replaceAll("<#", "");
		fooQuery = fooQuery.replaceAll("#>", "");
		return fooQuery;
	}
	
	private String extractFormat(String valid) {
		Matcher validFormat = Pattern.compile(MATCHFORMAT).matcher(valid);
		String foo = null;
		if(validFormat.find()) {
			foo = validFormat.group();
		}
		formatValids.add(foo);
		if(foo == null) {
			foo = valid;
		} else {
			foo = valid.replace(foo + " ", "");
		}
		return foo;
	}
	
	private void collectComponent(Object component) {
		Object foo = component;
		if(component instanceof Combobox) {
			Combobox combo = (Combobox) component;
			Comboitem item = combo.getSelectedItem();
			foo = item == null? comboAll : item.getId();
			if (foo.equals("")) {
				foo = combo.getValue().toUpperCase();
			} else if(foo.equals(comboAll)) {
				foo = null;
			}
		} else if(component instanceof Component) {
			try {
				foo = (String) OtherTool.getMetodo("getValue", component.getClass()).invoke(component);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		validParameter(foo);
	}
	
	private void validParameter(Object value) {
		if(value == null || (value instanceof String && ((String) value).trim().equals(""))) {
			quitValids.add(queryValids.get(paramValids.size() + quitValids.size()));
		} else {
			paramValids.add(validFormat(value));
		}
	}
	
	private Object validFormat(Object value) {
		String fooFormat = formatValids.get(paramValids.size() + quitValids.size());
		Object foo = null;
		if(fooFormat != null) {
			String[] fooAFormat = fooFormat.replaceAll("~", " ").replace("|", "#").split("#");
			fooFormat = fooAFormat[0];
			String fooDateFormat = DATEFORM;
			if(fooAFormat.length >= 2 && !fooAFormat[1].equals("")) {
				fooDateFormat = fooAFormat[1];
			}
			SimpleDateFormat formManage = new SimpleDateFormat(fooDateFormat);
			if(fooFormat != null && !value.getClass().toString().endsWith(fooFormat)) {
				if(fooFormat.equals(FORMSTR)) {
					foo = value.toString();
					if(value instanceof Date) {
						foo = formManage.format(value);
					}
				} else if(fooFormat.equals(FORMINT)) {
					foo = Integer.parseInt(value.toString());
				} else if(fooFormat.equals(FORMDTE)) {
					try {
						foo = formManage.parse(value.toString());
					} catch (ParseException e) {
						e.printStackTrace();
						foo = value;
					}
				}
			}
		}
		return foo;
	}
	
	private void executeQuery(int index) {
		try {
			Connection conn = source.getConnection();
			try {
				Object[] params = paramValids.toArray(new Object[paramValids.size()]);
				if(index == 0 && paging != null) {
					paging.setTotalSize(querySize(conn, params));
					paging.setPageSize(size);
					paging.setActivePage(index);
				}
				List<T> foo = QRHandler.queryListT(conn, queryPage(index), GENERICCLASS, params);
				addAll(foo);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int querySize(Connection conn, Object[] params) {
		String fooQuery = PARAMSQUERY[0] + useQuery + PARAMSQUERY[1];
		Object foo = QRHandler.queryObject(conn, fooQuery, params);
		if(foo == null || foo.toString().equals("")) {
			foo = 0;
		}
		return Integer.parseInt(foo.toString());
	}
	
	private String queryPage(int index) {
		String fooQuery = useQuery;
		if(index >= 0 && paging != null) {
			fooQuery = PARAMSQUERY[2] + useQuery + PARAMSQUERY[3];
			fooQuery = fooQuery.replace("#MIN#", (index + 1) + "");
			fooQuery = fooQuery.replace("#MAX#", (index + size) + "");
		}
		return fooQuery;
	}
}
