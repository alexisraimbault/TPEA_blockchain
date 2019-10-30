package src;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Word {
	public String head;
	public ArrayList<Letter> chain;
	public String politician;
	public String signature;
	
	public Word(String head, String politician, ArrayList<Letter> chain)
	{
		this.head = head;
		this.politician = politician;
		this.chain = chain;
		this.signature = null;
	}
	
	public void sign(Peer peer)
	{
		String result = "";
		for(Letter l : chain)
			result += l.toBinaryString();
		result += head;
		result += politician;
		try {
			this.signature = tp1.bytesToHex(Ed25519Bc.sign(peer.privateKey, peer.publicKey, tp1.bytesToHex(tp1.sha256(result))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public String toString()
	{
		String result = "";
		for(Letter l : chain)
			result += l.letter;
		return result;
	}
}
