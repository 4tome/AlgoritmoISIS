package servicios;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Proceso extends Thread {
	private String id;
	private int contador;
	private List<Mensaje> cola = new ArrayList<Mensaje>();
	//private int Ci, Cj;
	//usare orden de momento, porque con Ci y Cj no me aclaro.
	private int orden;
	private Semaphore semTiempo;
	private Mensaje mensaje;//mensaje que envia el proceso
	private int numProcess;
	
	//Constructor
	public Proceso(String id, int time, int numProcess){
		this.id = id;
		this.orden = time;
		this.semTiempo = new Semaphore(1);
		this.numProcess = numProcess - 1;
	}
	
	//Metodos para el incremento del tiempo logico (Lamport)
	public void LC1(int tLamport){
		try {
		      semTiempo.acquire(1);
		      tLamport += 1;
		      semTiempo.release(1);
		    } catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		    }
		
		//Creo que no haria falta pasar la variable reloj al proceso
		//no entiendo lo de los tiempos
	}
	
	public void LC2(int tLamport, int tLamportNew){
	    try {
	      semTiempo.acquire(1);
	      tLamport = Math.max(tLamport, tLamportNew) + 1;
	      semTiempo.release(1);
	    } catch (InterruptedException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
		
	}
	//Metodos para enviar los diferentes tipos de mensajes
	public String mensaje(int orden)
	{
		//Creamos el Mensaje
		String newId = "P" + this.id + " " + this.contador;
		//Creamos el String que se enviará.
		String Mensaje = newId + ";" + this.orden + ";" + " " + ";";
		mensaje = new Mensaje(newId, this.orden, "", 0);
		return Mensaje;
	}
	
	public String propuesta (String id, int orden) {
		//Creacion del String
		String Propuesta = id + ";" + orden + ";" + "PROVISIONAL" + ";";
		return Propuesta;
	}
	
	public String acuerdo(String id, int orden) {
		//Creacion del String
		String Propuesta = id + ";" + orden + ";" + "DEFINITIVO" + ";";
		return Propuesta;
	}
	
	//Metodos para recibir los diferentes tipos de mensajes
	public void recibirMsg(String msg)
	{
		//Descomposición del mensaje
		String[] parts = msg.split(";");
		//Comprobar tipo de mensaje
		if(parts[2] != null && parts[2].equals("PROVISIONAL")) {
			//Al recibir un mensaje de propuesta
			mensaje.setOrden(Math.max(mensaje.getOrden(), Integer.parseInt(parts[1])));
			LC2(this.orden, Integer.parseInt(parts[1]));
			mensaje.setNumP(mensaje.getNumP() + 1);
			if (mensaje.getNumP() == this.numProcess) {
				//Enviar mensaje de acuerdo.
				mensaje.setState("DEFINITIVO");
			}
			
		}else {
			LC1(this.orden);
			cola.add(new Mensaje(parts[0], Integer.parseInt(parts[1]), "PROVISIONAL", 0));
			System.out.println("mensaje recibido");
			//Enviar propuesta
			this.unicast(propuesta(parts[0], this.orden), Integer.parseInt(mensaje.getId().substring(3,4)));
			
		}
		
	}
	
	
	//Metodos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		//Creamos el mensaje
		for(contador =0; contador<1; contador++) {
			this.unicast(mensaje(this.orden), contador);
			//Tiempo de espera
			try {
				long time = (long)(Math.random()*(5-2)+2);
				Thread.sleep(time*100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void unicast(String msg, int destino){
		//Lanzamos el servicio del dispatcher
		Client proceso = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/AlgoritmoISIS").build();
		WebTarget target = proceso.target(uri);
		//Llamar al servicio
		System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("mensaje", msg).queryParam("destino", destino).request(MediaType.TEXT_PLAIN).get(String.class));
	}
	
	//Metodo run
	public void run(){
		//Mandar 1 mensaje normal
		this.multicast();
	}


}