package src;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class MerkleTree
{
	public MerkleTree()
	{
		this.element = null;
		this.left = null;
		this.right = null;
	}
	
	public MerkleTree(byte[] element)
	{
		this.element = element;
		this.left = null;
		this.right = null;
	}
	
	public boolean checkMerkleness(MessageDigest digest)
	{
		if(left == null || right == null)
			return true;
		if(Arrays.equals(concat_hash(left.element, right.element), this.element))
			return left.checkMerkleness(digest) && right.checkMerkleness(digest);
		else
			return false;
	}
	
	public static byte[] concat_hash(byte[] h1, byte[] h2)
	{
		byte[] res = new byte[h1.length + h2.length];
		for(int i = 0; i < h1.length; ++i)
			res[i] = h1[i];
		for(int i = 0; i < h2.length; ++i)
			res[h1.length + i] = h2[i];
		
		try {
			return tp1.sha256(res);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] element;
	public MerkleTree left, right;
	
	public static MerkleTree makeComplete(String[] values_i)
	{
		int nb_values = values_i.length;
		int count_square = 2;
		while(count_square < nb_values)
			count_square*=2;
		String[] values = new String[count_square];
		for(int i = 0; i<count_square; i++)
		{
			if(i<nb_values)
				values[i] = values_i[i];
			else
				values[i] = "";
		}
		ArrayList<MerkleTree> leafs = new ArrayList<MerkleTree>();
		ArrayList<MerkleTree> nodes = new ArrayList<MerkleTree>();
		for(String v : values)
		{
			leafs.add(new MerkleTree(tp1.sha256(v)));
		}
		while(!leafs.isEmpty() || (nodes.size() > 1))
		{
			if(leafs.isEmpty()) 
			{
				leafs = nodes;
				nodes = new ArrayList<MerkleTree>();
			}
			MerkleTree tmpLeft = leafs.remove(0);
			MerkleTree tmpRight = leafs.remove(0);
			MerkleTree current = new MerkleTree(concat_hash(tmpLeft.element,tmpRight.element));
			current.left = tmpLeft;
			current.right = tmpRight;
			nodes.add(current);
		}
		return nodes.get(0);
	}
	
	public static MerkleTree makeComplete(ArrayList<String> values_i)
	{
		int nb_values = values_i.size();
		int count_square = 2;
		while(count_square < nb_values)
			count_square*=2;
		String[] values = new String[count_square];
		for(int i = 0; i<count_square; i++)
		{
			if(i<nb_values)
				values[i] = values_i.get(i);
			else
				values[i] = "";
		}
		ArrayList<MerkleTree> leafs = new ArrayList<MerkleTree>();
		ArrayList<MerkleTree> nodes = new ArrayList<MerkleTree>();
		for(String v : values)
		{
			leafs.add(new MerkleTree(tp1.sha256(v)));
		}
		while(!leafs.isEmpty() || (nodes.size() > 1))
		{
			if(leafs.isEmpty()) 
			{
				leafs = nodes;
				nodes = new ArrayList<MerkleTree>();
			}
			MerkleTree tmpLeft = leafs.remove(0);
			MerkleTree tmpRight = leafs.remove(0);
			MerkleTree current = new MerkleTree(concat_hash(tmpLeft.element,tmpRight.element));
			current.left = tmpLeft;
			current.right = tmpRight;
			nodes.add(current);
		}
		return nodes.get(0);
	}
	
	public static MerkleTree makeCompleteRec(MessageDigest digest, ArrayList<MerkleTree> trees, int i, int depth)
	{
		MerkleTree left = trees.get(i);
		MerkleTree right = trees.get(i + 1);
		//MerkleTree res = new MerkleTree(hash_concat(digest, left.element, right.element));
		
		return null;
	}
	
	class Pair{
		public Pair(char c, byte[] b)
		{
			this.c = c;
			this.b = b;
		}
		public char c;
		public byte[] b;
	}
	
	/*public ArrayList<Pair> witness(int index)
	{
		ArrayList<Pair> res = new ArrayList<Pair>();
		
		BigInteger bigInt = BigInteger.valueOf(index);      
		byte[] binaryIndex = bigInt.toByteArray();
		
		int d = depth();
		for(byte b : binaryIndex)
		{
			for(int i = 0; i < 8; ++i)
			{
				byte digit = (byte) ((b >> (7 - i)) & 1);
				if(digit == 0)
					++count;
				else
					return count;
				--d;
				if(d == 0)
				{

					return res;
				}
			}
		}
	}
	*/
	public int depth()
	{
		if(left == null && right == null)
			return 0;
		else
			return 1 + left.depth();
	}
	
	public void fillString(StringBuilder builder, int depth)
	{
		builder.append(bytesToHex(element));
		if(left != null || right != null)
		{
			builder.append("\n");
			for(int i = 0; i < depth; ++i)
				builder.append(" ");
			left.fillString(builder, depth + 1);
			builder.append("\n");
			for(int i = 0; i < depth; ++i)
				builder.append(" ");
			right.fillString(builder, depth + 1);
			builder.append(")");
		}
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		fillString(builder, 0);
		return builder.toString();
	}
	
	public static String bytesToHex(byte[] hash)
	{
	    StringBuffer hexString = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
}
