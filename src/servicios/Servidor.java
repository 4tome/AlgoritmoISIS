package servicios;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Singleton 
@Path("Servidor")
public class Servidor {
	//Procesos que controla el Dispatcher
	private Proceso p1;
	private Proceso p2;
	private int numProcesos = 2;
	//Array de los procesos del sistema
	private String[][] arrayProcesos = {{"P01","localhost"},{"P02","localhost"}};
	
	public Servidor(){
		//Creamos los procesos
		p1 = new Proceso("01", 0, numProcesos, arrayProcesos);
		p2 = new Proceso("02", 0, numProcesos, arrayProcesos); 
		//Arrancamos los procesos
		p1.start();
		p2.start();
	}
	
	
	@Path("start")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String start()
	{
		//Servicio para arrancar los procesos.
		return "Procesos creados satisfactoriamente";
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("enviarMensaje") //ruta al m�todo
	public String enviarMensaje(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirPropuesta(msg);
			return "PROPUESTA " + msg + " ENVIADO";
		}else {
			p2.recibirPropuesta(msg);
			return "PROPUESTA " + msg + " ENVIADO";
		}
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("multicast") //ruta al m�todo
	public String multicast(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirMensaje(msg);
			return "MENSAJE " + msg + " ENVIADO A " + destino;
		}else {
			p2.recibirMensaje(msg);
			return "MENSAJE " + msg + " ENVIADO A " + destino;
		}
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("multicastDefinitivo") //ruta al m�todo
	public String multicastDefinitivo(@QueryParam(value="mensaje")String msg,
								@QueryParam(value="destino")String destino) //el m�todo debe retornar String
	{ 
		if (destino.equals("P01")) {
			p1.recibirAcuerdo(msg);
			return "DEFINITIVO " + msg + " ENVIADO A " + destino;
		}else {
			p2.recibirAcuerdo(msg);
			return "DEFINITIVO " + msg + " ENVIADO A " + destino;
		}
	}
	
}
