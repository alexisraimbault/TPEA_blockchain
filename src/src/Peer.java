package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

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
	public int role;//0 -> author and 1 -> politician
	
	public Peer(int role)
	{
		this.role = role;
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
        
        try {
			this.memory = new Memory(3, this);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void startListeningFirst (int port)
	{
		this.port = port;
		this.id = 0;
		this.signature = Hex.toHexString(Ed25519Bc.sign(this.privateKey, this.publicKey.getEncoded()));
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
			//trgistring to our own memory
			this.peer.memory.public_keys[peer.id] =  peer.publicKey;
			//registring to the other peers's memory 
			sortie.println("register " + this.peer.id + " " + Hex.toHexString(this.peer.publicKey.getEncoded()));
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
			if(Math.random() < 0.04)
			{
				try {
					String injection = this.peer.memory.generateLetterMessage();
					String[] line_content = injection.split(" ");
					if(line_content.length == 6 && line_content[0].equals("inject_letter"))
					{
						System.out.println(this.peer.port + " : generating injection, checking...");
						int id = Integer.parseInt(line_content[1]);
						char letter = line_content[2].charAt(0);
						String author = line_content[3];
						String head = line_content[4];
						String signature = line_content[5];
						if(peer.memory.verifyLetterMessage(tp1.sha256(letter+head+author), letter, author, head, signature))
						{
							System.out.println(this.peer.port + " : applying and sending to the network...");
							peer.memory.applyLetterMessage(letter, author, head, signature);
							sortie.println(injection);
						}else {
							System.out.println(this.peer.port + " : injection wasn't verified locally...");
						}
					}
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
	}
}

class WordFinder extends Thread
{
	PrintStream sortie;
	Peer peer;
	
	public WordFinder(PrintStream sortie, Peer peer)
	{
		this.peer = peer;
		this.sortie = sortie;
		this.start();
	}
	public void run() 
	{
		ArrayList<String> dict = new ArrayList<String>();
		dict.add("le");
		dict.add("la");
		dict.add("re");
		dict.add("je");
		dict.add("tu");
		dict.add("vu");
		dict.add("va");
		dict.add("nu");
		dict.add("na");
		dict.add("ra");
		dict.add("ma");
		dict.add("si");
		dict.add("fa");
		dict.add("mi");
		dict.add("sa");
		dict.add("do");
		dict.add("de");
		dict.add("or");
		dict.add("ou");
		dict.add("da");
		//TODO real dict
		while(true)
		{
			ArrayList<Word> word_pool = (ArrayList<Word>) this.peer.memory.word_pool.clone();
			ArrayList<Letter> letter_pool = (ArrayList<Letter>) this.peer.memory.letter_pool.clone();
			
			if(letter_pool.size() > 0 && word_pool.size() > 0)
			{
				Collections.shuffle(letter_pool);
				ArrayList<String> used_authors = new ArrayList<String>();
				ArrayList<Letter> chain = new ArrayList<Letter>();
				String word_in_construction = "";
				String word_chosen = Hex.toHexString(letter_pool.get(0).head);
				Word w_chosen = null;
				for(Word w : word_pool)
					if(w.signature.equals(word_chosen))
					{
						word_in_construction = w.toString();
						w_chosen = w;
					}
						
				for(Letter l : letter_pool)
				{
					if(!used_authors.contains(Hex.toHexString(l.author)) && !word_chosen.equals(Hex.toHexString(l.author)))
					{
						used_authors.add(Hex.toHexString(l.author));
						word_in_construction += l.letter;
						chain.add(l);
						System.out.println(this.peer.port + " : checking word : " + word_in_construction);
						if(dict.contains(word_in_construction))
						{
							System.out.println(this.peer.port + " : FOUND WORD !!!!!!!!!!! : " + word_in_construction);
							System.out.println(this.peer.port + " : generating word injection message : " + word_in_construction + "...");
							Word w = new Word(w_chosen.signature, Hex.toHexString(this.peer.publicKey.getEncoded()), chain);
							w.sign(this.peer);
							String injection_message = w.generateMessage(this.peer);
							System.out.println(this.peer.port + " : checking validity for word injection message : " + injection_message + "...");
							if(peer.memory.verifyWordMessage(injection_message))
							{
								System.out.println(this.peer.port + " : word injection message valid. Applying locally and sending to the network ...");
								peer.memory.applyWordMessage(injection_message);
								sortie.println(injection_message);
							}else {
								System.out.println(this.peer.port + " : check failed ...");
							}
						}
					}
				}
				
			}
			try {
				Thread.sleep(200);
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
					this.peer.memory.public_keys[Integer.parseInt(line_content[1])] = new Ed25519PublicKeyParameters(Hex.decode(line_content[2]), 0);
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
						if(this.peer.role == 0)
							new LetterSender(this.peer.connection.sortie, this.peer);
						if(this.peer.role == 1)
							new WordFinder(this.peer.connection.sortie, this.peer);
					}
				}
				if(line_content.length == 6 && line_content[0].equals("inject_letter"))
				{
					System.out.println(this.peer.port + " : checking letter injection...");
					int id = Integer.parseInt(line_content[1]);
					char letter = line_content[2].charAt(0);
					String author = line_content[3];
					String head = line_content[4];
					String signature = line_content[5];
					try {
						if(peer.memory.verifyLetterMessage(tp1.sha256(letter+head+author), letter, author, head, signature))
						{
							System.out.println(this.peer.port + " : injection valid, applying...");
							peer.memory.applyLetterMessage(letter, author, head, signature);
							
							//forward message if necessary
							if(Integer.parseInt(line_content[1]) != (this.peer.id + 1)%this.peer.memory.size)
							{
								System.out.println(this.peer.port + " : forwarding injection...");
								this.peer.connection.sortie.println(line);
							}
							
						}else {
							System.out.println(this.peer.port + " : injection not valid...");
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				if(line_content[0].equals("inject_word"))
				{
					System.out.println(this.peer.port + " : checking word injection...");
					if(this.peer.memory.verifyWordMessage(line))
					{
						System.out.println(this.peer.port + " : injection valid, applying...");
						this.peer.memory.applyWordMessage(line);
						//forward message if necessary
						if(Integer.parseInt(line_content[1]) != (this.peer.id + 1)%this.peer.memory.size)
						{
							System.out.println(this.peer.port + " : forwarding injection...");
							this.peer.connection.sortie.println(line);
						}
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
			this.peer.signature = Hex.toHexString(Ed25519Bc.sign(peer.privateKey, peer.publicKey.getEncoded()));//set our own signature in mem
			
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