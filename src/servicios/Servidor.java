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

	
	public Servidor(){
		//Creamos los procesos
		Proceso p1 = new Proceso("01", 0);
		Proceso p2 = new Proceso("02", 0); 
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
	public String enviarMensaje(@QueryParam(value="id")String id) //el m�todo debe retornar String
	{ 
		//
		System.out.println("Enviar mensaje:" + " " + id);
		return "MENSAJE ENVIADO";
	}
	

	
	//
	
	
}
