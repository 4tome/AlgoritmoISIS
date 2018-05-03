package servicios;

import java.net.URI;
import java.util.ArrayList;
//import java.util.Arrays;
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
	private int orden;
	private Semaphore semTiempo;
	private Mensaje mensaje;//mensaje que envia el proceso
	private int numProcess;
	
	//Constructor
	public Proceso(String id, int time, int numProcess){
		this.id = id;
		this.orden = time;
		this.semTiempo = new Semaphore(1);
		this.numProcess = numProcess;
	}
	
	//Metodos para el incremento del tiempo logico (Lamport)
	public void LC1(){
		try {
		      semTiempo.acquire(1);
		      this.orden += 1;
		      semTiempo.release(1);
		    } catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		    }
	}
	
	public void LC2(int tLamportNew){
	    try {
	      semTiempo.acquire(1);
	      this.orden = Math.max(this.orden, tLamportNew) + 1;
	      semTiempo.release(1);
	    } catch (InterruptedException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
		
	}
	
	//Metodos para enviar los diferentes tipos de mensajes
	public String mensaje(int orden)
	{
		String newId = "P" + this.id + " " + this.contador;
		String Mensaje = newId + ";" + this.orden + ";" + " " + ";";
		mensaje = new Mensaje(newId, this.orden, "", 0);
		return Mensaje;
	}
	
	public String propuesta (String id, int orden) {
		String Propuesta = id + ";" + orden + ";" + "PROVISIONAL" + ";";
		return Propuesta;
	}
	
	public String acuerdo(String id, int orden) {
		String Propuesta = id + ";" + orden + ";" + "DEFINITIVO" + ";";
		return Propuesta;
	}
	
	//Metodos para recibir los diferentes tipos de mensajes
	public void recibirMsg(String msg)
	{
		String[] parts = msg.split(";");
		if(parts[2] != null && parts[2].equals("PROVISIONAL")) {
			//Al recibir un mensaje de propuesta
			mensaje.setOrden(Math.max(mensaje.getOrden(), Integer.parseInt(parts[1])));
			LC2(Integer.parseInt(parts[1]));
			mensaje.setNumP(mensaje.getNumP() + 1);
			if (mensaje.getNumP() == this.numProcess) {
				mensaje.setState("DEFINITIVO");
				this.unicast(acuerdo(mensaje.getId(),  mensaje.getOrden()), 0);
			}
			
		}else if(parts[2] != null && parts[2].equals("DEFINITIVO")){
			//Buscamos el mensaje en nuestra cola
		    int i = busquedaMensaje(cola, parts[0]);
		    if (i == 100) {
		    	System.out.println("ERROR: No se ha encontrado el mensaje solicitado");
		    }else {
		    	Mensaje mensaje = cola.get(i);
			    mensaje.setOrden(Integer.parseInt(parts[1]));
			    LC2(Integer.parseInt(parts[1]));
			    mensaje.setState("DEFINITIVO");
			    
		    }
		    cola.sort(null); 
	    }else{
			LC1();
			cola.add(new Mensaje(parts[0], Integer.parseInt(parts[1]), "PROVISIONAL", 0));
			this.unicast(propuesta(parts[0], this.orden), Integer.parseInt(mensaje.getId().substring(2,3)));
		}
		
	}
	
	public int busquedaMensaje(List<Mensaje> cola, String id) {
		int index = 100;
		for (int i=0; i<cola.size(); i++) {
			Mensaje mensaje = cola.get(i);
			if(mensaje.getId().equals(id)) {
	    		index = i;
	    	}
		}
		return index;
	}
	
	//Metodos para el envio de mensajes (multicast, unicast)
	public void multicast(String mensaje){
		Client proceso = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/AlgoritmoISIS").build();
		WebTarget target = proceso.target(uri);
		//Llamar al servicio
		System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("mensaje", mensaje).queryParam("destino", 0).request(MediaType.TEXT_PLAIN).get(String.class));
		
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
		//bucle de envio de mensajes
		for(contador =0; contador<1; contador++) {
			try {
				this.multicast(mensaje(this.orden));
				long time = (long)(Math.random()*(5-2)+2);
				Thread.sleep(time*100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Métodos de comprobación
	public void imprimirCola(List<Mensaje> cola) {
		for (int index=0; index<cola.size(); index++) {
			Mensaje mensaje = cola.get(index);
			System.out.println(mensaje.imprimir() + " --> " + this.id);
		}
	}
	
	public void servicioImprimirCola() {
		imprimirCola(this.cola);
	}

}