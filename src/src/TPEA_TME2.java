package src;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TPEA_TME2
{
	public static void main(String[] args) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] bytes = MerkleTree.concat_hash( digest.digest("abc".getBytes()), digest.digest("def".getBytes()));
		
		
		MerkleTree tree = new MerkleTree(bytes);
		tree.left = new MerkleTree(digest.digest("abc".getBytes()));
		tree.right = new MerkleTree(digest.digest("def".getBytes()));
		
		
		//System.out.println(tree.checkMerkleness(digest));
		String[] values = {"bonjou","je","mappelle","alex","is "};
		MerkleTree built = MerkleTree.makeComplete( values);
		System.out.println(built);
		System.out.println(built.checkMerkleness(digest));
	}
}
