package src;

public class Main {
	public static void main(String[] args)
	{
		Peer peer1 = new Peer(0);
		Peer peer2 = new Peer(0);
		Peer peer3 = new Peer(1);
		
		peer1.startListeningFirst(1001);
		peer2.startListening(1002, 1);
		peer3.startListening(1003, 1);
		
		peer2.connectFirst(1001);
		peer3.connectFirst(1001);
	}
}
