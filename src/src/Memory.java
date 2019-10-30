package src;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

public class Memory {
	public Peer peer;
	public int size;
	public Ed25519PublicKeyParameters[] public_keys;
	public ArrayList<ArrayList<Character>> letter_bags;
	public ArrayList<Word> word_pool;
	
	public Memory(int size, Peer peer)
	{
		this.size = size;
		this.peer = peer;
		public_keys = new Ed25519PublicKeyParameters[size];
		letter_bags = new ArrayList<ArrayList<Character>>();
		
		
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		
		for(int i = 0 ; i < size; i++)
		{
			ArrayList<Character> tmp_letter_bag = new ArrayList<Character>();
			for (int j = 0; j < alphabet.length(); j++){
				tmp_letter_bag.add(alphabet.charAt(j));
			}
			letter_bags.add(tmp_letter_bag);
		}
	}
	
	public String generateLetterMessage()
	{
		ArrayList<Character> letter_bag = letter_bags.get(peer.id);
		if(!letter_bag.isEmpty())
		{
			Collections.shuffle(letter_bag);
			char letter = letter_bag.remove(0);
			String author = tp1.bytesToHex(peer.publicKey.getEncoded());
			Collections.shuffle(word_pool);
			String head = word_pool.get(0).head;
			String signature = null;
			try {
				signature = tp1.bytesToHex(Ed25519Bc.sign(peer.privateKey, peer.publicKey,tp1.bytesToHex(tp1.sha256(letter + head + author))));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return "inject_letter " + letter + " " + author + " " + head + " " + signature;
		}
		return null;
	}
	
	
}
