package src;
import java.security.MessageDigest;
import java.util.List;

public class BlockChain
{
	public String head;
	public BlockChain(MessageDigest digest)
	{
		this.digest = digest;
	}
	
	public void push(char letter, byte[] author)
	{
		if(blocks.size() > 0)
		{
			Letter back = blocks.get(blocks.size() - 1);
			byte[] head = digest.digest(back.toBytes());
			blocks.add(new Letter(head, letter, author));
		}
		else
			blocks.add(new Letter(letter, author));
	}
	
	public void pop(Letter b)
	{
		blocks.remove(blocks.size() - 1);
	}
	
	public void pushAll(List<Letter> blocks)
	{
		blocks.addAll(blocks);
	}
	
	public int size()
	{
		return blocks.size();
	}
	
	public char get(int i)
	{
		return blocks.get(i).letter;
	}
	
	public List<Letter> blocks;
	public MessageDigest digest;
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(Letter b : blocks)
			builder.append(b.letter);
		return builder.toString();
	}
}
