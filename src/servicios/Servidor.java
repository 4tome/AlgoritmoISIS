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
	private Proceso p1;
	private Proceso p2;
	private String[][] arrayProcesos = {{"P01","172.28.171.113"},{"P02","172.28.171.113"},{"P03","172.28.171.113"},{"P04","172.28.171.113"}};
	private String[] arrayDispatcher = {"172.28.171.113", "172.28.236.144"};
	public Servidor(){
		//Creamos los procesos
		p1 = new Proceso("01", 0, 4, arrayProcesos, arrayDispatcher);
		p2 = new Proceso("02", 0, 4, arrayProcesos, arrayDispatcher); 
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
								@QueryParam(value="destino")Integer destino) //el m�todo debe retornar String
	{ 
		if (destino == 1) {
			p1.recibirAcuerdo(msg);
			p2.recibirAcuerdo(msg);
			return "DEFINITIVO " + msg + " ENVIADO";
		}else {
			p1.recibirMensaje(msg);
			p2.recibirMensaje(msg);
			return "MENSAJE " + msg + " ENVIADO";
		}
		
	}
	
	@GET //tipo de petici�n HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("enviarPropuesta") //ruta al m�todo
	public String enviarPropuesta(@QueryParam(value="mensaje")String msg,
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
	
	//Servicios de comprobación

	@Path("compruebaP1")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String compruebap1()
	{
		System.out.println("------------------------------------Cola p1----------------------------");
		p1.servicioImprimirCola();
		//Servicio para arrancar los procesos.
		return "comprobacion de la cola de p1";
	}
	
	@Path("compruebaP2")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String compruebap2()
	{
		System.out.println("------------------------------------Cola p2----------------------------");
		p2.servicioImprimirCola();
		//Servicio para arrancar los procesos.
		return "comprobacion de la cola de p2";
	}
}
