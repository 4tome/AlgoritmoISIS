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
	private int Ci, Cj;
	private Semaphore semTiempo;
	
	//Constructor
	public Proceso(String id, int time){
		this.id = id;
		this.Ci = time;
		this.semTiempo = new Semaphore(1);
	}
	
	//Metodos para el incremento del tiempo logico (Lamport)
	public void LC1(){
		
		try {
		      semTiempo.acquire(1);
		      Ci = Ci + 1;
		      semTiempo.release(1);
		    } catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		    }
		
		//Creo que no haria falta pasar la variable reloj al proceso
		//no entiendo lo de los tiempos
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
	//Metodos para enviar los diferentes tipos de mensajes
	public String newMsg(int type)
	{
		//Creamos el Mensaje
		String newId = "P" + this.id + " " + this.contador;
		//Creamos el String que se enviará.
		String msg = newId + ";" + this.Ci + ";" + type;
		return msg;
	}
	
	//Metodos para recibir los diferentes tipos de mensajes
	public void recibirMsg(String msg)
	{
		//Descomposición del mensaje
		String[] parts = msg.split(";");
		//Comprobar tipo de mensaje
		if(Integer.parseInt(parts[2]) == 0) {
			LC1();
			//Comprobación
			System.out.println("He recibido un mensaje normal de " + parts[0]);
			//Mensaje normal
			Mensaje mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), "PROVISIONAL");
			cola.add(mensaje);
			//enviar propuesta
			String propuesta = newMsg(1);
			//this.unicast(propuesta);
		}else if(Integer.parseInt(parts[2]) == 1) {
			//Comprobación
			System.out.println("He recibido un mensaje de propuesta" );
		}
	}
	
	//Metodos para el envio de mensajes (multicast, unicast)
	public void multicast(){
		//Creamos el mensaje
		for(contador =0; contador<1; contador++) {
			String msg = newMsg(0);
			this.unicast(msg);
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
	
	public void unicast(String msg){
		//Lanzamos el servicio del dispatcher
		Client proceso = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/AlgoritmoISIS").build();
		WebTarget target = proceso.target(uri);
		//Llamar al servicio
		System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("mensaje", msg).request(MediaType.TEXT_PLAIN).get(String.class));
	}
	
	//Metodo run
	public void run(){
		//Mandar 1 mensaje normal
		this.multicast();
	}


}