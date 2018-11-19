package com.streameast.zktoolkit.simple;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

/**
 * Clase con utilidades correspondientes a validaciones.
 * @author Estuardo Ramos
 */
public class ValidTool {

	/**
	 * valida que los componentes esten en un estado valido.
	 * @param comps lista de componentes a validar
	 * @return true<br/>false
	 * @author Estuardo Ramos
	 */
	public static boolean validaComp(Component... comps) {
		boolean valid = true;
		try {
			for(Component c : comps) {
				Object valor = OtherTool.getMetodo("getValue", c.getClass()).invoke(c);
				valid = valid && valor != null;
				if(valor instanceof String) {
					valid = valid && !((String)valor).equals("");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return valid;
	}
	
	/**
	 * validacion de componentes en forma booleana. Tambien manda 
	 * un mensaje de "Campo no valido." en caso que la validacion sea falsa
	 * @param comps
	 * @return true<br/>false
	 * @throws InterruptedException
	 * @author Estuardo Ramos
	 */
	public static boolean valida(boolean... comps) throws InterruptedException {
		boolean val = true;
		for(boolean v : comps) {
			val = val && v;
		}
		if(!val) {
			Messagebox.show("Campo no valido.", "Advertencia!",Messagebox.OK,null);
		}
		return val;
	}
	
	/**
	 * validacion de componentes. Tambien manda 
	 * un mensaje de "Campo no valido." en caso que la validacion sea falsa
	 * @param comps
	 * @return true<br/>false
	 * @throws InterruptedException
	 * @author Estuardo Ramos
	 */
	public static boolean valida(Component... comps) throws InterruptedException {
		return valida(validaComp(comps));
	}
	
	/**
	 * funcion nvl para java, en caso a == null regresa b.
	 * @param a
	 * @param b
	 * @author Estuardo Ramos
	 */
	public static <T> T nvl(T a, T b) {
		return (a == null)? b : a;
	}
	
	/**
	 * manda una ventana con un mensaje de confirmacion.
	 * @param mensaje mensaje a ser desplegado.
	 * @param onYes instrucciones para la decicion SI.
	 * @throws InterruptedException
	 * @author Estuardo Ramos
	 */
	public static void mensajeComfirmacion(String mensaje, final EventListener onYes) throws InterruptedException {
		Messagebox.show(mensaje, "CONFIRMACION", Messagebox.YES
				+ Messagebox.NO, Messagebox.QUESTION, new EventListener() {
			public void onEvent(Event event) throws Exception {
				if ("onYes".equals(event.getName())) {
					onYes.onEvent(event);
				}
			}
		});
    }
	
	/**
	 * manda una ventana con un mensaje de informacion.
	 * @param mensaje mensaje a ser desplegado.
	 * @throws InterruptedException 
	 * @author Estuardo Ramos
	 */
	public static void mensajeInfo(String mensaje) throws InterruptedException {
    	Messagebox.show(mensaje, "MENSAJE", 
    			Messagebox.OK, Messagebox.QUESTION, null);
    }
}
