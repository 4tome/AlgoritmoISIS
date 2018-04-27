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
	
	public Servidor(){
		//Creamos los procesos
		p1 = new Proceso("01", 0);
		p2 = new Proceso("02", 0); 
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
		
		if(destino == 0){			
			p1.recibirMsg(msg);
			p2.recibirMsg(msg);
		}
		if(destino == 1){
			p1.recibirMsg(msg);
		}
		if(destino == 2){
			p2.recibirMsg(msg);
		}
		
		//System.out.print("El valor del destino es: " + destino);
		return "MENSAJE ENVIADO";
	}
	

	
	//
	
	
}
