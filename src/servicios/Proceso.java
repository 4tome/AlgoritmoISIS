package servicios;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Proceso extends Thread {
	//Semaforos
	private Semaphore semTiempo;
	private Semaphore sem_Mensajes;
	private Semaphore sem_Fichero;
	private Semaphore sem_Propuestas;
	private Semaphore sem_Proceso;
	//Arrays
	private ArrayList<Mensaje> cola = new ArrayList<Mensaje>();
	private ArrayList<Mensaje> listaPropuestas = new ArrayList<Mensaje>();
	//Variables
	private String id;
	private int orden;
	private int numProcesos;
	private int numMensajes;
	private String rutaLog;
	private String[][] arrayProcesos;

	private Mensaje Mensaje;//mensaje que envia el proceso
	private int contador;
	
	
	//Constructor
	public Proceso(String id, int time, int numProcess, String[][] arrayProcesos){
		//Iniciamos los semaforos
		this.semTiempo = new Semaphore(1);
		this.sem_Mensajes = new Semaphore(1);
		this.sem_Fichero = new Semaphore(1);
		this.sem_Propuestas = new Semaphore(1);
		this.sem_Proceso = new Semaphore(1);
		//Arrays de mensajes
		this.arrayProcesos = arrayProcesos;
		//Variables
		this.rutaLog = System.getProperty("user.home");
		this.numMensajes = 0;
		this.id = id;
		this.orden = time;
		this.numProcesos = numProcess;
		
		try {
			sem_Proceso.acquire(1);
			sem_Fichero.acquire(1);
			File fichero = new File(this.rutaLog + "/proceso" + this.id + ".log");
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
		//Guardar mensaje para propuestas
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
	
	//Métodos de recepción de mensajes
	public void recibirMensaje(String msg) {
		LC1();
		String[] parts = msg.split(";");
		try {
			sem_Mensajes.acquire(1);
			Mensaje = new Mensaje(parts[0], Integer.parseInt(parts[1]), "PROVISIONAL", 0);
			cola.add(Mensaje);
			sem_Mensajes.release(1);
			this.unicast(propuesta(parts[0], this.orden), parts[0].substring(0,3));
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
			if(Mensaje == null) {
				System.out.println("ERROR: No se ha encontrado el mensaje de la cola ");
			}else {
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
						f = new FileWriter(this.rutaLog + "/proceso" + this.id + ".log", true);
						sem_Fichero.acquire(1);
						pw = new PrintWriter(f);
						pw.println(Mensaje.getId() + " " + Mensaje.getOrden() + " " + Mensaje.getState());
						//Comprobar si se han escrito todos los mensajes enviados
						this.numMensajes += 1;
						if (this.numMensajes == (10*2)) {
							sem_Proceso.release(1);
						}
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
				System.out.println("ERROR: No se ha encontrado el mensaje de propuesta");
			}else {
				Mensaje.setOrden(Math.max(Mensaje.getOrden(), Integer.parseInt(parts[1])));
				Mensaje.setNumP(Mensaje.getNumP() + 1);
				if(Mensaje.getNumP() == numProcesos) {
					this.multicast(acuerdo(Mensaje.getId(),  Mensaje.getOrden()), 1);
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
	public void multicast(String mensaje, int destino){
		Client proceso = ClientBuilder.newClient();
		for(int i=0; i<arrayProcesos.length; i++) {
			URI uri = UriBuilder.fromUri("http://" + arrayProcesos[i][1] + ":8080/AlgoritmoISIS").build();
			WebTarget target = proceso.target(uri);
			//Llamar al servicio
			//delay
			long time = (long)(Math.random()*(5-2)+2);
			try {
				Thread.sleep((time*100));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (destino == 0) {
				System.out.println(target.path("rest/Servidor/multicast").queryParam("mensaje", mensaje).queryParam("destino", arrayProcesos[i][0]).request(MediaType.TEXT_PLAIN).get(String.class));
			}else {
				System.out.println(target.path("rest/Servidor/multicastDefinitivo").queryParam("mensaje", mensaje).queryParam("destino", arrayProcesos[i][0]).request(MediaType.TEXT_PLAIN).get(String.class));
			}	
		}
	}
	
	public void unicast(String msg, String p){
		//Lanzamos el servicio del dispatcher
		Client proceso = ClientBuilder.newClient();
		for(int i=0; i<arrayProcesos.length; i++) {
			if (arrayProcesos[i][0].equals(p)) {
				URI uri = UriBuilder.fromUri("http://" + arrayProcesos[i][1]  + ":8080/AlgoritmoISIS").build();
				WebTarget target = proceso.target(uri);
				//Llamar al servicio
				System.out.println(target.path("rest/Servidor/enviarMensaje").queryParam("mensaje", msg).queryParam("destino", p).request(MediaType.TEXT_PLAIN).get(String.class));
			}
		}
	}
	
	//Metodo run
	public void run(){
		//bucle de envio de mensajes
		try {
			for(contador = 1 ; contador <= 10; contador++) {
				this.multicast(mensaje(this.orden), 0);
				long time = (long)(Math.random()*(5-0)+0);
				Thread.sleep(1000 + (time*100));
			}
			sem_Proceso.acquire(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sem_Proceso.release(1);
	}
}