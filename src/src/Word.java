package src;

import java.util.ArrayList;

import org.bouncycastle.util.encoders.Hex;

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
		this.signature = Hex.toHexString(Ed25519Bc.sign(peer.privateKey, tp1.sha256(result)));
	}
	
	public String generateMessage()
	{
		String result = "";
		for(Letter l : chain)
		{
			result = result + "letter " + l.letter + " " + Hex.toHexString(l.head) + " " + Hex.toHexString(l.author) + " " + Hex.toHexString(l.signature) + " ";
		}
		result += politician + " ";
		result += signature;
		return result;
	}
	
	public String toString()
	{
		String result = "";
		for(Letter l : chain)
			result += l.letter;
		return result;
	}
}
