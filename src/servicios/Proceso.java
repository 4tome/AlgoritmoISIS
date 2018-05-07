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
	private Semaphore sem_Mensajes;
	private Semaphore sem_Cola;
	private Mensaje Mensaje;//mensaje que envia el proceso
	//private int numProcess;
	
	//Constructor
	public Proceso(String id, int time, int numProcess){
		this.id = id;
		this.orden = time;
		this.semTiempo = new Semaphore(1);
		this.sem_Mensajes = new Semaphore(1);
		this.sem_Cola = new Semaphore(1);
		//this.numProcess = numProcess;
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
		String nuevo = newId + ";" + this.orden + ";" + " " + ";";
		return nuevo;
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
		try {
			String[] parts = msg.split(";");
			if(parts[2] != null && parts[2].equals("PROVISIONAL")) {
				//Al recibir un mensaje de propuesta
				LC2(Integer.parseInt(parts[1]));
				int i = busquedaMensaje(cola, parts[0]);
				if (i!=100) {
					//System.out.print("El mensaje original se encuentra en la cola " + parts[0] + " " + this.id);
				}else {
					System.out.print("El mensaje original no se encuentra en la cola " + parts[0] + ", " + this.id + " " + i + "\r");
					this.servicioImprimirCola();
				}
				/*if (i != 100) {
					Mensaje.setOrden(Math.max(Mensaje.getOrden(), Integer.parseInt(parts[1])));
					Mensaje.setNumP(Mensaje.getNumP() + 1);
					//System.out.println("Mensaje: " + mensaje.getNumP());
					if (Mensaje.getNumP() == this.numProcess) {
						System.out.println("Mensajes propuesta recibidos " + this.id + ": " + Mensaje.getNumP());
						Mensaje.setState("DEFINITIVO");
						this.unicast(acuerdo(Mensaje.getId(),  Mensaje.getOrden()), 0);
					}
					
				}else {
				  	System.out.println("ERROR: No se ha encontrado el mensaje solicitado, " + parts[0] + " de P"  + this.id + " " + i);
			    	this.servicioImprimirCola();
				}*/
			}else if(parts[2] != null && parts[2].equals("DEFINITIVO")){
				//Buscamos el mensaje en nuestra cola
			    /*int i = busquedaMensaje(cola, parts[0]);
			    if (i == 100) {
			    	System.out.println("ERROR: No se ha encontrado el mensaje solicitado, " + parts[0] + " de P"  + this.id);
			    	this.servicioImprimirCola();
			    }else {
			    	//sem_Mensajes.acquire(1);
			    	Mensaje mensaje = cola.get(i);
				    mensaje.setOrden(Integer.parseInt(parts[1]));
				    mensaje.setState("DEFINITIVO");
				    //sem_Mensajes.release(1);
				    LC2(Integer.parseInt(parts[1]));
			    }
			    //sem_Mensajes.acquire(1);
			    cola.sort(null);*/
			    //sem_Mensajes.release(1);
			    //Estraer los mensajes que ya estend en definitivo y eliminarlos de la cola, tras escribirlo en un log
			    /*int flag = 0;
			    mensaje = cola.get(0);
			    while (flag == 0 && mensaje.getState().equals("DEFINITIVO")) {
			    	//Escribir mensaje en fichero
			    	cola.remove(0);
			    	if(cola.isEmpty()) {
			    		flag = 1;
			    	}else {
			    		mensaje = cola.get(0);
			    	}
			    }*/
		    }else{
				LC1();
				sem_Cola.acquire(1);
				Mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]), "PROVISIONAL", 0);
				cola.add(Mensaje);
				sem_Cola.release(1);
				sem_Mensajes.acquire(1);
				this.unicast(propuesta(parts[0], this.orden), Integer.parseInt(parts[0].substring(2,3)));
				sem_Mensajes.release(1);
				//System.out.println("Enviar mensaje " + parts[0] + " desde " + this.id + " a " + parts[0].substring(2,3));
				
			}
		}catch (InterruptedException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		}	
	}
	
	public int busquedaMensaje(List<Mensaje> cola, String id) {
		int index = 100;
		//try {
			//sem_Mensajes.acquire(1);
			for (int i=0; i<cola.size(); i++) {
				Mensaje mensaje = cola.get(i);
				if(mensaje.getId().equals(id)) {
					index = i;
				}
			}
			sem_Mensajes.release(1);
		/*} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
				//sem_Mensajes.acquire(1);
				this.multicast(mensaje(this.orden));
				//sem_Mensajes.release(1);
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
		System.out.println("Mostrar cola " + this.id);
		imprimirCola(this.cola);
	}

}