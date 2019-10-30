package src;
import java.security.MessageDigest;

public class Block
{
	
	public static byte[] genese(MessageDigest digest)
	{
		return digest.digest("".getBytes());
	}
	
	public Block(char letter, byte[] author, MessageDigest digest)
	{
		this(genese(digest), letter, author, digest);
	}
	
	public Block(byte[] head, char letter, byte[] author, MessageDigest digest)
	{
		this.letter = letter;
		this.head = head;
		this.author = author;
		sign(digest);
	}
	
	public char letter;
	public byte[] head;
	public byte[] author;
	public byte[] signature;
	
	private void sign(MessageDigest digest)
	{
		byte[] result = new byte[1 + 8 + head.length + author.length];
		// TODO check UTF-8 String conversion
		Binary.copy(Binary.intToBytes((int)letter, 1), result, 0);
		//Binary.copy(Binary.intToBytes(period, 8), result, 1);
		Binary.copy(head, result, 8);
		Binary.copy(author, result, 8 + head.length);
		
		this.signature = digest.digest(result);
	}
	
	public byte[] toBytes()
	{
		byte[] result = new byte[1 + 8 + head.length + author.length + signature.length];
		// TODO check UTF-8 String conversion
		Binary.copy(Binary.intToBytes((int)letter, 1), result, 0);
		//Binary.copy(Binary.intToBytes(period, 8), result, 1);
		Binary.copy(head, result, 8);
		Binary.copy(author, result, 8 + head.length);
		Binary.copy(signature, result, 8 + head.length + author.length);
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
