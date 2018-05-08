package servicios;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	private ArrayList<Mensaje> cola = new ArrayList<Mensaje>();
	private ArrayList<Mensaje> listaPropuestas = new ArrayList<Mensaje>();
	//private int Ci, Cj;
	private int orden;
	private Semaphore semTiempo;
	private Semaphore sem_Mensajes;
	private Semaphore sem_Fichero;
	private Semaphore sem_Propuestas;
	private Mensaje Mensaje;//mensaje que envia el proceso
	private int numPropuestas = 2;
	private String rutaLog;
	
	//Constructor
	public Proceso(String id, int time, int numProcess){
		this.id = id;
		this.orden = time;
		this.semTiempo = new Semaphore(1);
		this.sem_Mensajes = new Semaphore(1);
		this.sem_Fichero = new Semaphore(1);
		//this.numProcess = numProcess;
		this.sem_Propuestas = new Semaphore(1);
		this.rutaLog = "/home/amateos/Documentos/logs/";
		
		try {
			sem_Fichero.acquire(1);
			File fichero = new File(this.rutaLog + "proceso" + this.id + ".log");
			if(fichero.exists()) {
				fichero.delete();
			}else {
				try {
					fichero.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sem_Fichero.release(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
		Mensaje = new Mensaje(newId, this.orden, "PROVISIONAL", 0);
		try {
			sem_Propuestas.acquire(1);
			listaPropuestas.add(Mensaje);
			sem_Propuestas.release(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	
	public void recibirMensaje(String msg) {
		LC1();
		String[] parts = msg.split(";");
		try {
			sem_Mensajes.acquire(1);
			Mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]), "PROVISIONAL", 0);
			cola.add(Mensaje);
			sem_Mensajes.release(1);
			this.unicast(propuesta(parts[0], this.orden), Integer.parseInt(parts[0].substring(2,3)));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void recibirAcuerdo(String msg) {
		String[] parts = msg.split(";");
		LC2(Integer.parseInt(parts[1]));
		try {
			sem_Mensajes.acquire(1);
			Mensaje = busquedaMensaje(cola, parts[0]);
			//sem_Mensajes.release(1);
			if(Mensaje == null) {
				System.out.println("ERROR: No se ha encontrado un mensaje ");
			}else {
				//System.out.println("Mensaje definitivo recibido " + this.id);
				//sem_Mensajes.acquire(1);
				Mensaje.setOrden(Integer.parseInt(parts[1]));
			    Mensaje.setState("DEFINITIVO");
			    cola.sort(null);
				Mensaje = cola.get(0);
				int flag = 0;
			    while (flag == 0 && Mensaje.getState().equals("DEFINITIVO")) {
			    	//Escribir mensaje en fichero
			    	FileWriter f = null;
			        PrintWriter pw = null;
			    	try {
						f = new FileWriter(this.rutaLog + "proceso" + this.id + ".log", true);
						sem_Fichero.acquire(1);
						pw = new PrintWriter(f);
						pw.println(Mensaje.getId() + " " + Mensaje.getOrden() + " " + Mensaje.getState());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						try {
							if (null != f) {
								f.close();
								pw.close();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
			    	sem_Fichero.release(1);
			    	cola.remove(0);
			    	if(cola.isEmpty()) {
			    		flag = 1;
			    	}else {
			    		Mensaje = cola.get(0);
			    	}
			    }
			    sem_Mensajes.release(1);
			
			}
			//sem_Mensajes.release(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void recibirPropuesta (String msg) {
		String[] parts = msg.split(";");
		LC2(Integer.parseInt(parts[1]));
		//Busqueda del mensaje enviado
		try {
			sem_Propuestas.acquire(1);
			Mensaje = busquedaMensaje(listaPropuestas, parts[0]);
			if(Mensaje == null) {
				System.out.println("ERROR: No se ha encontrado un mensaje ");
			}else {
				Mensaje.setOrden(Math.max(Mensaje.getOrden(), Integer.parseInt(parts[1])));
				Mensaje.setNumP(Mensaje.getNumP() + 1);
				//System.out.println("Mensaje " + Mensaje.getId() + " ha recibido " + Mensaje.getNumP() + " propuesta");
				if(Mensaje.getNumP() == numPropuestas) {
					//System.out.println("He recibido todas las propuestas del mensaje " + Mensaje.getId());
					//Enviar Definitivo
					this.unicast(acuerdo(Mensaje.getId(),  Mensaje.getOrden()), 0);
				}
				sem_Propuestas.release(1);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Mensaje busquedaMensaje(ArrayList<Mensaje> cola, String id) {
		for(int i = 0; i<cola.size(); i++) {
			Mensaje mensaje = cola.get(i);
			if (mensaje.getId().equals(id)) {
				return mensaje;
			}
		}
		return null;
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
		System.out.println(target.path("rest/Servidor/enviarPropuesta").queryParam("mensaje", msg).queryParam("destino", destino).request(MediaType.TEXT_PLAIN).get(String.class));
	}
	
	//Metodo run
	public void run(){
		//bucle de envio de mensajes
		for(contador = 1 ; contador <= 10; contador++) {
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
		System.out.println("Mostrar cola " + this.id);
		imprimirCola(this.cola);
	}

}