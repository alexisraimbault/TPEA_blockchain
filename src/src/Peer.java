package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer
{
	public int id;
	public int port;
	public Serveur serveur;
	public Memory memory;
	
	public Peer()
	{
		this.memory = new Memory(3);//TODO
		this.serveur = null;
	}
	
	public void startListeningFirst (int port)
	{
		this.port = port;
		new ServeurFirst(this);
	}
	
	public void startListening (int port, int nb_accept)
	{
		this.port = port;
		serveur = new Serveur(this, nb_accept);
	}
	
	public void connect(int port)
	{
		new Connection(port, this);
	}
	
	public void connectFirst(int port)
	{ 
		new ConnectionFirst(port, this);
	}
	
}

class Connection extends Thread 
{
	Socket socket;
	BufferedReader entree;
	PrintStream sortie;
	Peer peer;
	int port;

	public Connection(int port, Peer peer) 
	{
		this.port = port;
		this.peer = peer;
		this.start();
	}

	public void run() 
	{
		try {
			System.out.println(peer.port + " : connecting on port : " + port);
			socket = new Socket("localhost", port);
			entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sortie = new PrintStream(socket.getOutputStream());
			
			String line;
			while(true)
			{
				line = entree.readLine();
				System.out.println(peer.port + " : receiving : " + line);
				if(line.contains("Alex"))
					sortie.println("hello Alex");
			}
		}
		catch(IOException e) {e.printStackTrace();}
	}
}

class ConnectionFirst extends Thread 
{
	Socket socket;
	BufferedReader entree;
	PrintStream sortie;
	Peer peer;
	int port;

	public ConnectionFirst(int port, Peer peer) 
	{
		this.port = port;
		this.peer = peer;
		this.start();
	}

	public void run() 
	{
		try {
			System.out.println(peer.port + " : connecting on port : " + port);
			socket = new Socket("localhost", port);
			entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sortie = new PrintStream(socket.getOutputStream());
			
			int port_to_listen = 0;
			int port_to_connect = 0;
			String[] line_content;
			String line;
			line = entree.readLine();
			System.out.println(peer.port + " : receiving : " + line);
			line_content = line.split(" ");
			if(line_content.length == 2 && line_content[0].contains("listen"))
				port_to_listen = Integer.parseInt(line_content[1]);
			
			line = entree.readLine();
			System.out.println(peer.port + " : receiving : " + line);
			line_content = line.split(" ");
			if(line_content.length == 2 && line_content[0].contains("connect"))
				port_to_connect = Integer.parseInt(line_content[1]);
			
			peer.serveur.disconnect();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			peer.startListening(port_to_listen, -1);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			peer.connect(port_to_connect);
		}
		catch(IOException e) {e.printStackTrace();}
	}
}

class Link extends Thread 
{
	Socket socket;
	BufferedReader entree;
	PrintStream sortie;
	Peer peer;

	public Link(Socket socket, Peer peer) 
	{
		this.peer = peer;
		this.socket = socket;
		try 
		{
			entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sortie = new PrintStream(socket.getOutputStream());
			this.start();
		}
		catch(IOException exc) 
		{
			try
			{
				socket.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void run() 
	{
		sortie.println("hello");
		sortie.println("my name");
		sortie.println("is Alex");
		try 
		{
			String line;
			do 
			{
				line = entree.readLine();
				System.out.println(this.peer.port + " : receiving : " + line);
			}
			while(!line.equals("goodbye"));
			
			sortie.close();
			entree.close();
			socket.close();
		}
		catch(IOException e) {}
	}
}

class Serveur extends Thread 
{
	public Peer peer;
	ServerSocket standardiste;
	int nb_accept;
	
	public Serveur(Peer peer, int nb_accept)
	{
		this.peer = peer;
		this.nb_accept = nb_accept;
		this.start();
	}
	
	public void run() 
	{
		
		Socket socket;

		try 
		{
			standardiste = new ServerSocket(peer.port);
			if(nb_accept == -1) {
				while(true) 
				{
					System.out.println("listening new comers on port : " + peer.port);
					socket = standardiste.accept();
					new Link(socket, peer);
		 			
				}
			}else {
				int cpt = 0;
				while(cpt < nb_accept) 
				{
					System.out.println("listening new comers on port : " + peer.port + ", " + cpt + " accepts done, total : " + nb_accept);
					socket = standardiste.accept();
					new Link(socket, peer);
					cpt++;
		 			
				}
			}
				
		}
		catch(IOException exc) 
		{
		}
	}
	
	public void disconnect()
	{
		try {
			standardiste.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class ServeurFirst extends Thread 
{
	public int nb_peers;
	public Peer peer;
	public final int nb_max_peers = 3;
	public WelcomeLink[] peer_sockets;
	
	public ServeurFirst(Peer peer)
	{
		peer_sockets = new WelcomeLink[nb_max_peers];
		peer_sockets[0] = null;
		nb_peers = 1; 
		this.peer = peer;
		this.start();
	}
	
	public void run() 
	{
		ServerSocket standardiste;
		Socket socket;

		try 
		{
			standardiste = new ServerSocket(peer.port);
			while(nb_peers < nb_max_peers) 
			{
				socket = standardiste.accept();
	 			WelcomeLink tmp = new WelcomeLink(socket);
	 			peer_sockets[nb_peers] = tmp;
	 			nb_peers++;
	 			
			}
			
			standardiste.close();
			peer.startListening(1001, -1);
			
			
			for(int i = 1; i < nb_max_peers ; i++)
			{
				peer_sockets[i].sendMessage("listen " + (1001 + i));
			}
			
			for(int i = 1; i < nb_max_peers - 1; i++)
			{
				peer_sockets[i].sendMessage("connect " + (1001 + i + 1));
				peer_sockets[i].disconnect();
			}
			peer_sockets[nb_max_peers - 1].sendMessage("connect " + 1001);

			Thread.sleep(1000);
			
			peer.connect(1001 + 1);
			
			
			
			while(nb_peers < nb_max_peers) 
			{
				socket = standardiste.accept();
				System.out.println("test");
	 			new Link(socket, this.peer);
	 			
			}
			
			
		}
		catch(Exception exc) 
		{
			exc.printStackTrace();
		}
	}
}

class WelcomeLink extends Thread 
{
	Socket socket;
	BufferedReader entree;
	PrintStream sortie;
	
	int port_to_connect;
	int port_to_listen;

	public WelcomeLink(Socket socket) 
	{
		this.socket = socket;
		try 
		{
			entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sortie = new PrintStream(socket.getOutputStream());
			this.start();
		}
		catch(IOException exc) 
		{
			try
			{
				socket.close();
			}
			catch(IOException e){}
		}
	}

	public void sendMessage(String message) {
		sortie.println(message);
	}
	
	public void disconnect()
	{
		try 
		{
			sortie.close();
	        entree.close();
			socket.close();
		}
		catch(Exception e) 
		{
			System.out.println("probleme de d�connexion");
		}
	}

	public void run() 
	{
		
	}
}