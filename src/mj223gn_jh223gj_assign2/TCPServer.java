package mj223gn_jh223gj_assign2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

import org.omg.CORBA.Request;

import mj223gn_jh223gj_assign2.exceptions.InvalidRequestFormatException;

public class TCPServer {
	
	public static final int BUFSIZE= 1024;
    public static final int MYPORT= 4950;
	
	public static void main(String[] args) throws IOException{
		TCPServer server = new TCPServer();
		server.run(args);
	}
	
	public void run(String[] args){	
		try {
			/* Create Socket */
			ServerSocket socket = new ServerSocket(MYPORT);
			
			/* Endless loop waiting for client connections */
			while(true){
				/* Open new thread for each new client connection */
				new Thread(new ConnectionHandler(socket.accept())).start();
				
				/* Print out to investigate open threads */
				printThreadsInfo();
			}
		} catch (IOException e) {
		}
	}
	
	/* Prints the status of the currently active threads in the console */
	private void printThreadsInfo(){
		System.out.println("******************************************");
		System.out.println("Active Threads:" + Thread.activeCount());
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for (Thread t : threads)
		System.out.println(t.toString());
		System.out.println("******************************************");
	}
	
	/* Handles client connection */
	class ConnectionHandler implements Runnable{
		private Socket connection;
	    
		public ConnectionHandler(Socket connection){ 
			this.connection = connection;
		}
		@Override
		public void run() {
			try {
				/* while client stays connected */
				// FIX => needs a variable for Header Connection: alive/close
				while (true) {
					try {
					/* Parse HTTP Request from input stream */
					HTTPReader parser = new HTTPReader(connection.getInputStream());
					HTTPRequest request = parser.read();
										
					/* Print status of connection */
					System.out.printf("TCP echo request from %s", connection.getInetAddress().getHostAddress());
				    System.out.printf(" using port %d", connection.getPort());
				    System.out.printf(" handled from thread %d; Method-Type: <%s>,", Thread.currentThread().getId(), request.getType());
				    System.out.printf(" URL: %s\n", request.getUrl());
				    if (request.getRequestBody() !=  null) System.out.println(" Content: " + request.getRequestBody());
				    
				    /* Client wants to close connection */
				    if (request.closeConnection()) break;
					else if (connection.getInputStream().read() == -1) break;
				    
					} catch (InvalidRequestFormatException e) {
						e.printStackTrace();
					}

				}
				/* Tear down connection and print closing-status */
				System.out.printf("TCP Connection from %s using port %d handled by thread %d is closed.\n", connection.getInetAddress().getHostAddress(), connection.getPort(), Thread.currentThread().getId());
				connection.close();
			} catch (IOException e){
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	

}
