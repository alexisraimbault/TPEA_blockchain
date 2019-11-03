package src;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.util.encoders.Hex;


public class Memory {
	public Peer peer;
	public int size;
	public Ed25519PublicKeyParameters[] public_keys;
	public int[] points;
	public ArrayList<ArrayList<Character>> letter_bags;
	public ArrayList<Word> word_pool;
	public ArrayList<Letter> letter_pool;
	public HashMap<Character, Integer> letter_points;
	public ArrayList<String> blocks;//all operations 
	public int total_points;
	public boolean updating_memory;
	
	public Memory(int size, Peer peer) throws NoSuchAlgorithmException
	{
		
		this.size = size;
		this.peer = peer;
		public_keys = new Ed25519PublicKeyParameters[size];
		points = new int[size];
		for(int i = 0; i < size; i++)
			points[i] = 0;
		letter_bags = new ArrayList<ArrayList<Character>>();
		letter_pool = new ArrayList<Letter>();
		blocks = new ArrayList<String>();
		total_points = 0;
		letter_points = new HashMap<Character, Integer>();
		letter_points.put('a', 1);
		letter_points.put('e', 1);
		letter_points.put('i', 1);
		letter_points.put('l', 1);
		letter_points.put('n', 1);
		letter_points.put('o', 1);
		letter_points.put('r', 1);
		letter_points.put('s', 1);
		letter_points.put('t', 1);
		letter_points.put('u', 1);
		letter_points.put('d', 2);
		letter_points.put('g', 2);
		letter_points.put('m', 2);
		letter_points.put('b', 3);
		letter_points.put('c', 3);
		letter_points.put('p', 3);
		letter_points.put('f', 4);
		letter_points.put('h', 4);
		letter_points.put('v', 4);
		letter_points.put('j', 8);
		letter_points.put('q', 8);
		letter_points.put('k', 10);
		letter_points.put('w', 10);
		letter_points.put('x', 10);
		letter_points.put('y', 10);
		letter_points.put('z', 10);
		
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
		w1 = new Word(Hex.toHexString(tp1.sha256("")), "", new ArrayList<Letter>());
		w1.signature = w1.head;//for the 1st empty word
		this.word_pool.add(w1);
		this.updating_memory = false;
	}
	
	
	private void reinitializeMemory() {
		points = new int[size];
		for(int i = 0; i < size; i++)
			points[i] = 0;
		letter_bags = new ArrayList<ArrayList<Character>>();
		letter_pool = new ArrayList<Letter>();
		blocks = new ArrayList<String>();
		total_points = 0;
		
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
		w1 = new Word(Hex.toHexString(tp1.sha256("")), "", new ArrayList<Letter>());
		w1.signature = w1.head;//for the 1st empty word
		this.word_pool.add(w1);
	}
	
	public String generateLetterMessage() throws NoSuchAlgorithmException
	{
		ArrayList<Character> letter_bag = letter_bags.get(peer.id);
		if(!letter_bag.isEmpty())
		{
			Collections.shuffle(letter_bag);
			char letter = letter_bag.get(0);
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
	
	public int checkLetterSignature(byte[] signature)
	{
		int user_idx = 0;
		for (Letter l : this.letter_pool)
		{
			if(Arrays.equals(l.signature, signature))
				return user_idx;
			user_idx++;
		}
		return -1;
	}
	public void undoOperations()
	{
		for(String s : blocks)
			undoOperation(s);
	}
	
	public void undoOperation(String operation) 
	{
		String[] message_content = operation.split(" ");
		//valid keyword
		int parser = 4;
		//reading letters, counting word points, and attributing author points
		int politician_points = 0;
		while(message_content[parser].equals("letter"))
		{
			char letter = message_content[parser + 1].charAt(0);
			byte [] head = Hex.decode(message_content[parser + 2]);
			byte [] author = Hex.decode(message_content[parser + 3]);
			byte [] signature = Hex.decode(message_content[parser + 4]);
			Letter tmp_letter = new Letter(head, letter, author, signature);
			int author_idx = findIdxFromKey(new Ed25519PublicKeyParameters(author, 0));
			int author_points = letter_points.get(letter);
			points[author_idx] -= author_points;
			politician_points -= author_points;
			total_points += author_points;
			
			letter_pool.add(tmp_letter);
			parser += 5;
		}
		
		//reading word head
		String head = message_content[parser];
		
		parser++;
		
		//reading politician public key
		String politician = message_content[parser];
		
		//adding politician points
		points[findIdxFromKey(new Ed25519PublicKeyParameters(Hex.decode(politician), 0))] -= politician_points;
		total_points -= politician_points;
		
		parser++;
		
		//reading signature
		String signature = message_content[parser];
		
		word_pool.remove(findWOrdFromSignature(signature));//TODO
	}
	
	public int findWOrdFromSignature(String signature)
	{
		int cpt = 0;
		for(Word w : word_pool)
		{
			if(w.signature.equals(signature))
			{
				return cpt;
			}
			cpt++;
		}
		return -1;
	}
	
	public void applyLetterMessage(char letter, String author, String head, String signature, String operation)
	{
		Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(Hex.decode(author), 0);
		int user_idx = findIdxFromKey(pk);
		
		//removing letter from user bag
		letter_bags.get(user_idx).remove((Object)letter);
		
		//adding letter to letter_pool
		letter_pool.add(new Letter(Hex.decode(head), letter, Hex.decode(author), Hex.decode(signature)));
		
	}
	
	public boolean verifyWordMessage(String message)
	{
		ArrayList<Letter> red_chain = new ArrayList<Letter>();
		String[] message_content = message.split(" ");
		//valid keyword
		int parser = 0;
		if(!message_content[parser].equals("inject_word"))
			return false;
		parser += 2;
		//verifying Merkle root
		if(!message_content[parser].equals(Hex.toHexString(MerkleTree.makeComplete(blocks).element)))
		{
			parser++;

			System.out.println("merkle root check failed");
			
			if(this.total_points < Integer.parseInt(message_content[parser]))
			{
				System.out.println("better chain received, updating chain...");
				ArrayList<String> blocks_save = (ArrayList<String>) this.blocks.clone();
				
				this.undoOperations();
				
				int tmp_parser = parser + 1;
				
				while(!(message_content[tmp_parser].equals("|")))
					tmp_parser++;
				
				for(int i = tmp_parser + 1; i<message_content.length; i++)
				{
					if(verifyWordMessageNoCheckMerkle(message_content[i]))
						applyWordMessage(message_content[i]);
					else 
					{//undoing changes, the new better chaine was from an unreliable source
						this.reinitializeMemory();
						for(String msg : blocks_save)
							applyWordMessage(msg);
						return false;
					}
				}
				parser++;
			}else {
				return false;
			}
		}
		else
		{
			parser +=2;
		}
		System.out.println("Merkle root ok");
		
		//reading letters
		while(message_content[parser].equals("letter"))
		{
			if(!(message_content.length >= parser + 5))
				return false;
			char letter = message_content[parser + 1].charAt(0);
			byte [] head = Hex.decode(message_content[parser + 2]);
			byte [] author = Hex.decode(message_content[parser + 3]);
			byte [] signature = Hex.decode(message_content[parser + 4]);
			Letter tmp_letter = new Letter(head, letter, author, signature);
			
			if(checkLetterSignature(signature)==-1)
				return false;
			red_chain.add(tmp_letter);
			parser += 5;
		}
		System.out.println("letters ok");
		
		//reading politician public key
		String head = message_content[parser];
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
		
		System.out.println("head ok");
		
		parser++;
		
		//reading politician public key
		String politician = message_content[parser];
		Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(Hex.decode(message_content[parser]), 0);
		int politician_id = findIdxFromKey(pk);
		if(politician_id == -1)
			return false;
		System.out.println("key ok");
		
		parser++;
		//checking signature
		
		String signed_message = "";
		for(Letter l : red_chain)
			signed_message += l.toBinaryString();
		signed_message += head;
		signed_message += politician;
		
		if(!Ed25519Bc.verify(pk,tp1.sha256(signed_message) , Hex.decode(message_content[parser])))
			return false;
		
		System.out.println("signature : ok");
		
		return true;
		
	}


	public boolean verifyWordMessageNoCheckMerkle(String message)
	{
		ArrayList<Letter> red_chain = new ArrayList<Letter>();
		String[] message_content = message.split(" ");
		//valid keyword
		int parser = 0;
		if(!message_content[parser].equals("inject_word"))
			return false;
		parser += 2;
		
		//not verifying Merkle root
		parser += 2;
		
		//reading letters
		while(message_content[parser].equals("letter"))
		{
			if(!(message_content.length >= parser + 5))
				return false;
			char letter = message_content[parser + 1].charAt(0);
			byte [] head = Hex.decode(message_content[parser + 2]);
			byte [] author = Hex.decode(message_content[parser + 3]);
			byte [] signature = Hex.decode(message_content[parser + 4]);
			Letter tmp_letter = new Letter(head, letter, author, signature);
			
			if(checkLetterSignature(signature)==-1)
				return false;
			red_chain.add(tmp_letter);
			parser += 5;
		}
		System.out.println("letters ok");
		
		//reading politician public key
		String head = message_content[parser];
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
		
		System.out.println("head ok");
		
		parser++;
		
		//reading politician public key
		String politician = message_content[parser];
		Ed25519PublicKeyParameters pk = new Ed25519PublicKeyParameters(Hex.decode(message_content[parser]), 0);
		int politician_id = findIdxFromKey(pk);
		if(politician_id == -1)
			return false;
		System.out.println("key ok");
		
		parser++;
		//checking signature
		
		String signed_message = "";
		for(Letter l : red_chain)
			signed_message += l.toBinaryString();
		signed_message += head;
		signed_message += politician;
		
		if(!Ed25519Bc.verify(pk,tp1.sha256(signed_message) , Hex.decode(message_content[parser])))
			return false;
		
		System.out.println("signature : ok");
		
		return true;
		
	}
	
	public void applyWordMessage(String message)
	{
		blocks.add(message);
		ArrayList<Letter> red_chain = new ArrayList<Letter>();
		String[] message_content = message.split(" ");
		//valid keyword
		int parser = 4;
		//reading letters, counting word points, and attributing author points
		int politician_points = 0;
		while(message_content[parser].equals("letter"))
		{
			char letter = message_content[parser + 1].charAt(0);
			byte [] head = Hex.decode(message_content[parser + 2]);
			byte [] author = Hex.decode(message_content[parser + 3]);
			byte [] signature = Hex.decode(message_content[parser + 4]);
			Letter tmp_letter = new Letter(head, letter, author, signature);
			int author_idx = findIdxFromKey(new Ed25519PublicKeyParameters(author, 0));
			int author_points = letter_points.get(letter);
			points[author_idx] += author_points;
			politician_points += author_points;
			total_points += author_points;
			
			int letter_idx = checkLetterSignature(signature);
			red_chain.add(tmp_letter);
			letter_pool.remove(letter_idx);//removing letter
			parser += 5;
		}
		
		//reading word head
		String head = message_content[parser];
		
		parser++;
		
		//reading politician public key
		String politician = message_content[parser];
		
		//adding politician points
		points[findIdxFromKey(new Ed25519PublicKeyParameters(Hex.decode(politician), 0))] += politician_points;
		total_points += politician_points;
		
		parser++;
		
		//reading signature
		String signature = message_content[parser];
		
		word_pool.add(new Word(head, politician, red_chain, signature));
		System.out.println("points : " );
		for(int i = 0; i < points.length; i++)
			System.out.println(i + " -> " + points[i]);
	}
}
