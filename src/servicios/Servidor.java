package servicios;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton 
@Path("Servidor")
public class Servidor {

	
	public Servidor(){
		Proceso pc = new Proceso( "1");
		pc.start();
	}
	
	
	@Path("start")
	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public String start()
	{
		//Servicio para arrancar los procesos.
		return "Process created sucessfully";
	}
	
	@GET //tipo de petición HTTP
	@Produces(MediaType.TEXT_PLAIN) //tipo de texto devuelto
	@Path("enviarMensaje") //ruta al método
	public String enviarMensaje() //el método debe retornar String
	{ 
		//
		return "MENSAJE ENVIADO";
	}
	

	
	//
	
	
}
