package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Peer
{
	public int id;
	public int port;
	public Serveur serveur;
	public Connection connection;
	public Memory memory;
	public Ed25519PrivateKeyParameters privateKey;
	public Ed25519PublicKeyParameters publicKey;
	public String signature;
	public boolean game_started;
	
	public Peer()
	{
		
		this.serveur = null;
		this.connection = null;
		this.game_started = false;
		
		 System.out.println("ED25519 with BC");
        Security.addProvider(new BouncyCastleProvider());
        Provider provider = Security.getProvider("BC");
        System.out.println("Provider          :" + provider.getName() + " Version: " + provider.getVersion());
        // generate ed25519 keys
        SecureRandom RANDOM = new SecureRandom();
        Ed25519KeyPairGenerator keyPairGenerator = new Ed25519KeyPairGenerator();
        keyPairGenerator.init(new Ed25519KeyGenerationParameters(RANDOM));
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = keyPairGenerator.generateKeyPair();
        privateKey = (Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
        publicKey = (Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        
        this.memory = new Memory(3, this);
	}
	
	public void startListeningFirst (int port)
	{
		this.port = port;
		this.id = 0;
		this.signature = tp1.bytesToHex(Ed25519Bc.sign(this.privateKey, this.publicKey, "user "+this.id));
		new ServeurFirst(this);
	}
	
	public void startListening (int port, int nb_accept)
	{
		this.port = port;
		serveur = new Serveur(this, nb_accept);
	}
	
	public void connect(int port)
	{
		connection = new Connection(port, this);
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
			line = entree.readLine();//hello
			sortie.println("register " + this.peer.id + " " + tp1.bytesToHex(this.peer.publicKey.getEncoded()));
			while(true)
			{
				line = entree.readLine();
				System.out.println(peer.port + " : receiving : " + line);
			}
		}
		catch(IOException e) {e.printStackTrace();}
	}
}

class LetterSender extends Thread
{
	PrintStream sortie;
	Peer peer;
	
	public LetterSender(PrintStream sortie, Peer peer)
	{
		this.peer = peer;
		this.sortie = sortie;
		this.start();
	}
	public void run() 
	{
		while(true)
		{
			if(Math.random() < 0.01)
				sortie.println(this.peer.memory.generateLetterMessage());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
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
		try 
		{
			String line;
			String[] line_content;
			do 
			{
				line = entree.readLine();
				System.out.println(this.peer.port + " : receiving : " + line);
				line_content = line.split(" ");
				if(line_content.length == 3 && line_content[0].equals("register"))
				{
					//set public key in mem
					this.peer.memory.public_keys[Integer.parseInt(line_content[1])] = new Ed25519PublicKeyParameters(line_content[2].getBytes(), 0);
					//forward message if necessary
					if(Integer.parseInt(line_content[1]) != (this.peer.id + 1)%this.peer.memory.size)
					{
						while(this.peer.connection == null || this.peer.connection.sortie == null)
						{
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						this.peer.connection.sortie.println(line);
					}else {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//starting game
						System.out.println(this.peer.port + " : starting game...");
						this.peer.game_started = true;
						new LetterSender(this.peer.connection.sortie, this.peer);
					}
				}
			}
			while(!line.equals("goodbye"));
			
			sortie.close();
			entree.close();
			socket.close();
		}
		catch(IOException e) {}
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
			if(line_content.length == 2 && line_content[0].equals("listen"))
				port_to_listen = Integer.parseInt(line_content[1]);
			
			this.peer.id = port_to_listen - 1001;//we get the id from the port because that's how we decided to attribute ports.
			this.peer.signature = tp1.bytesToHex(Ed25519Bc.sign(peer.privateKey, peer.publicKey, "user "+this.peer.id));//set our own signature in mem
			
			line = entree.readLine();
			System.out.println(peer.port + " : receiving : " + line);
			line_content = line.split(" ");
			if(line_content.length == 2 && line_content[0].equals("connect"))
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
			System.out.println("probleme de déconnexion");
		}
	}

	public void run() 
	{
		
	}
}