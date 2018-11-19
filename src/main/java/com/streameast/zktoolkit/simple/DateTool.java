package com.streameast.zktoolkit.simple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Clase con utilidades correspondientes a fechas.
 * @author user
 *
 */
public class DateTool {

	/**
	 * Agrega dias a una fecha.
	 * @param fch fecha a sumar los dias.
	 * @param dias a sumar.
	 * @return nueva fecha.
	 * @author Estuardo Ramos
	 */
	public static Date agregaDias(Date fch, int dias) {
    	Calendar cal = new GregorianCalendar();
    	cal.setLenient(false);
    	cal.setTime(fch);
    	cal.add(Calendar.DAY_OF_MONTH, dias);
    	return cal.getTime();
    }
	
	public static Date agregaMes(Date fch, int meses) throws ParseException {
		SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy");
		String[] fecha = form.format(fch).split("/");
		fecha[1] = (Integer.parseInt(fecha[1]) + meses) + "";
		return form.parse(fecha[0]+"/"+fecha[1]+"/"+fecha[2]);
	}
	
	public static Date resetDay(Date fch) throws ParseException {
		SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy");
		String[] fecha = form.format(fch).split("/");
		return form.parse("01/"+fecha[1]+"/"+fecha[2]);
	}
	
	/**
	 * Devuelve el ultimo minuto de la fecha seleccionada.
	 * <p>dd/MM/yyyy 23:
	 * @param fch fecha seleccionada
	 * @return fecha
	 * @throws Exception
	 */
    public static Date finDia(Date fch) throws Exception {
    	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    	String fecha = format.format(fch);
    	format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	fch = format.parse(fecha+" 23:59:59");
    	return fch;
    }
    
    /**
     * valida si la fecha esta entre ini y fin.
     * @param fecha a validar.
     * @param ini fecha de inicio.
     * @param fin fecha fin.
     * @return true<br/>false
     * @throws Exception
     * @author Estuardo Ramos
     */
	public static boolean betweenDates(Date fecha, Date ini, Date fin) throws Exception {
    	fin = finDia(fin);
    	return (fecha.after(ini) || fecha.equals(ini)) && (fecha.before(fin));
    }
    
	/**
	 * Devuelve cantidad de dias entre fechas.
	 * @param ini fecha de inicio.
	 * @param fin fecha fin.
	 * @return numero de dias.
	 * @author Estuardo Ramos
	 */
    public static int difereciaDias(Date ini, Date fin) {
    	final float MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
    	return (int) Math.round((fin.getTime() - ini.getTime()) / MILLSECS_PER_DAY);
    }
}
