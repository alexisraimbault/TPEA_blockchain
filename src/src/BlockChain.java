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
			Block back = blocks.get(blocks.size() - 1);
			byte[] head = digest.digest(back.toBytes());
			blocks.add(new Block(head, letter, author, digest));
		}
		else
			blocks.add(new Block(letter, author, digest));
	}
	
	public void pop(Block b)
	{
		blocks.remove(blocks.size() - 1);
	}
	
	public void pushAll(List<Block> blocks)
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
	
	public List<Block> blocks;
	public MessageDigest digest;
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for(Block b : blocks)
			builder.append(b.letter);
		return builder.toString();
	}
}
