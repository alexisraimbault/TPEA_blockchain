package src;

import java.util.ArrayList;

public class Memory {
	public int size;
	public ArrayList<ArrayList<Character>> letter_bags;
	
	public Memory(int size)
	{
		this.size = size;
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
}
