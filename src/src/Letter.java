package src;

import java.security.NoSuchAlgorithmException;

public class Letter
{
	public Letter( char letter, byte[] author)
	{
		this.letter = letter;
		this.author = author;
		this.head = tp1.sha256("");
	}
	
	public Letter(byte[] head, char letter, byte[] author)
	{
		this.letter = letter;
		this.head = head;
		this.author = author;
	}
	
	public Letter(byte[] head, char letter, byte[] author, byte[] signature)
	{
		this.letter = letter;
		this.head = head;
		this.author = author;
		this.signature = signature;
	}
	
	public char letter;
	public byte[] head;
	public byte[] author;
	public byte[] signature;
	
	private void sign(Peer peer)
	{
		byte[] result = new byte[1 + 8 + head.length + author.length];
		// TODO check UTF-8 String conversion
		Binary.copy(Binary.intToBytes((int)letter, 1), result, 0);
		Binary.copy(head, result, 1);
		Binary.copy(author, result, 1 + head.length);
		
		try {
			this.signature = Ed25519Bc.sign(peer.privateKey, tp1.sha256(result));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] toBytes()
	{
		byte[] result = new byte[1 + head.length + author.length + signature.length];
		// TODO check UTF-8 String conversion
		Binary.copy(Binary.intToBytes((int)letter, 1), result, 0);
		//Binary.copy(Binary.intToBytes(period, 8), result, 1);
		Binary.copy(head, result, 1);
		Binary.copy(author, result, 1 + head.length);
		Binary.copy(signature, result, 1 + head.length + author.length);
		return result;
	}
	
	public String toBinaryString()//used for word signature
	{
		String result = "";
		result += Integer.toBinaryString(letter);
		for(byte b : head)
			result += Integer.toBinaryString(b);
		for(byte b : author)
			result += Integer.toBinaryString(b);
		for(byte b : signature)
			result += Integer.toBinaryString(b);
		return result;
	}
	
	@Override
	public String toString()
	{
		return Character.toString(letter);
	}
	
	public String toJson()
	{
		// TODO
		return null;
	}
	
}
