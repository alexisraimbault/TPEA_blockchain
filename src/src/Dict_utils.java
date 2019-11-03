package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class Dict_utils 
{
	public HashSet<String> dict;
	
	public Dict_utils()
	{
		dict = new HashSet<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("dict/dict_100000_1_10.txt"));
			String line = reader.readLine();
			while (line != null) {
				dict.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean contains(String s)
	{
		return dict.contains(s);
	}
}
