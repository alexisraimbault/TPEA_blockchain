package src;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.Hex;


public class Memory {
	public Peer peer;
	public int size;
	public Ed25519PublicKeyParameters[] public_keys;
	public ArrayList<ArrayList<Character>> letter_bags;
	public ArrayList<Word> word_pool;
	public ArrayList<Letter> letter_pool;
	
	public Memory(int size, Peer peer) throws NoSuchAlgorithmException
	{
		this.size = size;
		this.peer = peer;
		public_keys = new Ed25519PublicKeyParameters[size];
		letter_bags = new ArrayList<ArrayList<Character>>();
		letter_pool = new ArrayList<Letter>();
		
		
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		
		for(int i = 0 ; i < size; i++)
		{
			ArrayList<Character> tmp_letter_bag = new ArrayList<Character>();
			for (int j = 0; j < alphabet.length(); j++){
				tmp_letter_bag.add(alphabet.charAt(j));
			}
			letter_bags.add(tmp_letter_bag);
		}
		
		Word w1 = null;
		this.word_pool = new ArrayList<Word>();
		w1 = new Word(tp1.bytesToHex(tp1.sha256("")), "", new ArrayList<Letter>());
		w1.signature = w1.head;//for the 1st empty word
		this.word_pool.add(w1);
	}
	
	public String generateLetterMessage() throws NoSuchAlgorithmException
	{
		ArrayList<Character> letter_bag = letter_bags.get(peer.id);
		if(!letter_bag.isEmpty())
		{
			Collections.shuffle(letter_bag);
			char letter = letter_bag.remove(0);
			String author = Hex.toHexString(peer.publicKey.getEncoded());
			Collections.shuffle(word_pool);
			String head = word_pool.get(0).signature;
			byte[] signature = null;
			signature = Ed25519Bc.sign(peer.privateKey, tp1.sha256(letter + head + author));
			String decoded = null;
			decoded = Hex.toHexString(signature);
			byte[] encoded = Hex.decode(decoded);
			//System.out.println("TEST : " + Ed25519Bc.verify(new Ed25519PublicKeyParameters( Hex.decode(Hex.toHexString(peer.publicKey.getEncoded())),0), tp1.sha256(letter + head + author) , Hex.decode(decoded)));
			return"inject_letter " + peer.id + " " + letter + " " + author + " " + head + " " + decoded;
		}
		return null;
	}
	
	public boolean verifyLetterMessage(byte[] message, char letter, String author, String head, String signature)
	{
		Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(Hex.decode(author), 0);
		
		int user_idx = 0;
		//check if key is regstered
		boolean validKey = false;
		for (Ed25519PublicKeyParameters k : this.public_keys)
		{
			if(Arrays.equals(k.getEncoded(),pk.getEncoded()))
			{
				validKey = true;
				//System.out.println("TEST : " + Ed25519Bc.verify(k, tp1.sha256(letter + head + author) , Hex.decode(signature)));
			}
			if(!validKey)
				user_idx++;
		}
				
		if(!validKey)
			return false;
		System.out.println("key : ok");
		
		//check if user has that letter in bag
		if(!letter_bags.get(user_idx).contains(letter))
			return false;
		
		System.out.println("letter : ok");
		//check if wordPool contains head
		
		boolean wordPool_check = false;
		for(Word w : word_pool)
		{
			if(w.signature.equals(head))
			{
				wordPool_check = true;
			}
		}
		
		if(!wordPool_check)
			return false;
		
		System.out.println("word_pool : ok");
		
		 byte[]encoded = Hex.decode(signature); 
		 
		if(!Ed25519Bc.verify(pk, message, encoded))
			return false;
		
		System.out.println("signature : ok");
		
		return true;
		
	}
	
	public int findIdxFromKey(Ed25519PublicKeyParameters pk)
	{
		int user_idx = 0;
		for (Ed25519PublicKeyParameters k : this.public_keys)
		{
			if(Arrays.equals(k.getEncoded(),pk.getEncoded()))
				return user_idx;
			user_idx++;
		}
		return -1;
	}
	
	public void applyLetterMessage(char letter, String author, String head, String signature)
	{
		Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(Hex.decode(author), 0);
		int user_idx = findIdxFromKey(pk);
		
		//removing letter from user bag
		letter_bags.get(user_idx).remove((Object)letter);
		
		//adding letter to letter_pool
		letter_pool.add(new Letter(head.getBytes(), letter, Hex.decode(author)));
		
	}
}
