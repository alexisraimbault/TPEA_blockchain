package src;

public class Main {
	public static void main(String[] args)
	{
		Dict_utils dict = new Dict_utils();
		Peer peer1 = new Peer(0, dict);
		Peer peer2 = new Peer(0, dict);
		Peer peer3 = new Peer(1, dict);
		Peer peer4 = new Peer(1, dict);
		Peer peer5 = new Peer(0, dict);
		
		peer1.startListeningFirst(1001);
		peer2.startListening(1002, 1);
		peer3.startListening(1003, 1);
		peer4.startListening(1004, 1);
		peer5.startListening(1005, 1);
		
		peer2.connectFirst(1001);
		peer3.connectFirst(1001);
		peer4.connectFirst(1001);
		peer5.connectFirst(1001);
	}
}
