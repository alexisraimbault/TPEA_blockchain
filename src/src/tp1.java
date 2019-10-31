package src;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.BiConsumer;

public class tp1 {
	
	public static byte[] generateKey()
	{
		try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

            // Initialize KeyPairGenerator.
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);

            // Generate Key Pairs, a private key and a public key.
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            Base64.Encoder encoder = Base64.getEncoder();
            return publicKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
		return null;
	}
	
	//EXO1.1
	public static byte[] sha256 (byte[] s) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(s);
	}
	
	public static byte[] sha256(String s)
	{
		try {
			return sha256(s.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] hash_id(String nom, String prenom) throws NoSuchAlgorithmException 
	{
		String concat = nom + ":" + prenom;
		byte[] res = Arrays.copyOf(sha256(concat), 8);
		return res;
	}
	
	public static String bytesToHex(byte[] hash) {
	    StringBuffer hexString = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
	public static void test_exo1() throws NoSuchAlgorithmException
	{
		System.out.println(bytesToHex(hash_id("koff", "jean-pierre")));
	}
	
	//EXO1.2
	public static byte[] hash_value(byte[] id, int nonce) throws NoSuchAlgorithmException
	{
	    BigInteger bigInt = BigInteger.valueOf(nonce);      
	    byte[] nonce_byte = bigInt.toByteArray();
	    byte[] concat = new byte[id.length + 4];
	    int cpt = 0;
	    for(byte b : id)
	    {
	    	concat[cpt] = b;
	    	cpt++;
	    }
	    for(byte b : nonce_byte)
	    {
	    	concat[cpt] = b;
	    	cpt++;
	    }
	    return sha256(concat);
	}
	
	//EXO2.1
	public static int count_zero_prefix(byte[] hash) 
	{
		int count = 0;
		for(byte b : hash)
		{
			for(int i = 0; i < 8; ++i)
			{
				byte digit = (byte) ((b >> (7 - i)) & 1);
				if(digit == 0)
					++count;
				else
					return count;
			}
		}
		return count;
	}
	
	//EXO2.2
	public static boolean is_valid( String nom, String prenom, int nonce, int n) throws NoSuchAlgorithmException
	{
		return count_zero_prefix(hash_value(hash_id(nom, prenom), nonce)) >= n;
	}
	
	//EXO2.3
	public static int mine(String nom, String prenom, int n) throws NoSuchAlgorithmException
	{
		int nonce=0;
		while(!is_valid(nom, prenom, nonce, n))
		{
			nonce++;
		}
		return nonce;
	}

	public static void consume_graph(BiConsumer<Long, Integer> consumer) throws NoSuchAlgorithmException 
	{
		for(int n = 1; n < 100; ++n)
		{
			long start = System.currentTimeMillis();
			
			mine("koff", "jean-pierre", n);
			
			consumer.accept(System.currentTimeMillis() - start, n);
		}
	}
	
	//EXO2.4
	public static void plot_mining() throws NoSuchAlgorithmException
	{
		consume_graph((time, n) -> System.out.println(n + " " + time));
	}
	
	
	public static void main(String[] args) {
		try {
			//test_exo1();
			
			//System.out.println(mine("koff", "jean-pierre", 15));
			
			//plot_mining();
			String test = "be3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855f41422fc5ed9c97fd7ee9a9c58203dd80bf0101f142d4a0be438a51f5d6639c9";
			System.out.println(bytesToHex(sha256(test)).getBytes(StandardCharsets.UTF_8).equals(bytesToHex(sha256(test)).getBytes(StandardCharsets.UTF_8)));
			System.out.println(test.getBytes(StandardCharsets.UTF_8).equals(test.getBytes(StandardCharsets.UTF_8)));
			System.out.println(bytesToHex(sha256(test)).getBytes());
			System.out.println(bytesToHex(sha256(test)).getBytes());
			//System.out.println(generateKey().length);
			//System.out.println(bytesToHex(generateKey()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
