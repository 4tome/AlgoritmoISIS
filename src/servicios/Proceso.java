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
	private int reloj;
	private int contador;
	private List<Mensaje> cola = new ArrayList<Mensaje>();
	private int Ci, Cj;
	private Semaphore semTiempo;
	
	//Constructor
	public Proceso(String id, int time){
		this.id = id;
		this.reloj = time;
		this.semTiempo = new Semaphore(1);
	}
	
	//M�todos para el incremento del tiempo l�gico (Lamport)
	public void LC1(){
		
		try {
		      semTiempo.acquire(1);
		      Ci = Ci + 1;
		      semTiempo.release(1);
		    } catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		    }
		
		//Creo que no har�a falta pasar la variable reloj al proceso
		this.reloj = Ci;
	}
	
	public void LC2(){
		
		int t = Ci;
	    
	    try {
	      semTiempo.acquire(1);
	      Cj = Math.max(t, Cj) + 1;
	      semTiempo.release(1);
	    } catch (InterruptedException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
		
	}
	//M�todos para enviar los diferentes tipos de mensajes
	public String newMsg()
	{
		//Creamos el Mensaje
		String newId = "P" + this.id + " " + this.contador;
		//Creamos el String que se enviará.
		String msg = newId + ";" + this.reloj;
		return msg;
	}
	
	//M�todos para recibir los diferentes tipos de mensajes
	public void recibirMsg(String msg)
	{
		//Comprobación
		//System.out.println("He recibido el mensaje: " + msg);
		LC1();
		String[] parts = msg.split(";");
		Mensaje mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]));
		cola.add(mensaje);
		//Comprobación
		System.out.println("Mensajes: " + cola.size());
		System.out.println("Tiempo Lamport proceso" + this.id + ": " + this.reloj);
		
	}
	
	//M�todos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		//Creamos el mensaje
		for(contador =0; contador<10; contador++) {
			String msg = newMsg();
			this.unicast(msg);
		}
		
	}
	
	public void unicast(String msg){
		
		//Lanzamos el servicio del dispatcher
		Client proceso = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/AlgoritmoISIS").build();
		WebTarget target = proceso.target(uri);
		//Llamar al servicio
		System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("mensaje", msg).request(MediaType.TEXT_PLAIN).get(String.class));
	}
	
	//M�todo run
	public void run(){
		//prueba de funcionamiento
		//System.out.println("Hola, soy el proceso" + " " + this.id);
		//Mandar 1 mensaje normal
		this.multicast();
	}


}