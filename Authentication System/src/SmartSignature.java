import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SmartSignature {

	public PublicKey public_key;
	private PrivateKey private_key;
	void getParameters()
	{
	System.out.println("Fingerprint and Aadhaarno taken Successfully");
	}
	public static byte[] getHashValue(String input) throws NoSuchAlgorithmException
    {  
        MessageDigest md = MessageDigest.getInstance("SHA-256"); 
        return md.digest(input.getBytes(StandardCharsets.UTF_8)); 
    }
	void authenticate_DigitalSignature(long aadhaarno,String fingerprint,String cname)throws Exception
	{
		
		FingerPrintTemplate f=new FingerPrintTemplate();
		double uniqueID=f.uniqueCodeForMinutiae(fingerprint);
		System.out.println("UniqueID in double:"+uniqueID);
		byte[] b=f.doubleToByte(uniqueID);
		String uniqueIDGen=f.encodeHexString(b);
		System.out.println("Generated UniqueID :"+uniqueIDGen);
		String aadhaarANDuniqueID= Long.toString(aadhaarno).concat(uniqueIDGen);
		System.out.println("Aadhaar and UniqueID together: "+aadhaarANDuniqueID);
		try
		{
			String hash=f.encodeHexString(getHashValue(aadhaarANDuniqueID));
			System.out.println("  Generated Hash Value:\n  "+hash);
			
			if( authentication(hash,cname)== true ) 
			{
				generateDigitalSignature(aadhaarANDuniqueID);
			}
			else
			{
				System.out.println(" Failed to authenticate the Customer for Document Signing");
				return;
			}
			

		}
		catch(Exception e)
		{}
			
	}
	
	public boolean authentication(String customerhash, String cname) throws Exception
	{ 
		System.out.println("\t Authenticating customer....................................................");
		
		String url="jdbc:mysql://localhost:3306/project";
		String username="root";
		String password="minato@14";
		String query="select hashvalue from customer where cname='"+cname+"'";
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection(url,username,password); 
		Statement st=con.createStatement();
		ResultSet rs=st.executeQuery(query);
		rs.next();
		String hash=rs.getString("hashvalue");
		con.close();
		if(hash.equalsIgnoreCase(customerhash))
		{
			System.out.println("Matched");
			return true;
		}
		
		System.out.println("Not Matched");
		return false;	
	}
	
	void generateDigitalSignature(String aadhaarANDuniqueID) throws Exception
	{
		System.out.println("\n  Customer is approved to sign the document(Authentication successful)");
		KeyPair k=Generate_RSA_KeyPair();
		public_key=k.getPublic();
		private_key=k.getPrivate();
		//System.out.println(private_key.getFormat());
		byte[] signature=create_Digital_Signature(aadhaarANDuniqueID.getBytes(),private_key);
		FingerPrintTemplate f=new FingerPrintTemplate();
		System.out.println("\nSignature Value:\n  "+ f.encodeHexString(signature));
		
		
	}
	
	
	
	public static KeyPair Generate_RSA_KeyPair()
	        throws Exception
	    {
	        SecureRandom secureRandom
	            = new SecureRandom();
	        KeyPairGenerator keyPairGenerator
	            = KeyPairGenerator
	                  .getInstance("RSA");
	        keyPairGenerator.initialize(2048, secureRandom);
	        return keyPairGenerator
	            .generateKeyPair();
	    }
	
	
	public static byte[] create_Digital_Signature(byte[] input, PrivateKey Key) throws Exception
	    {
	        Signature signature
	            = Signature.getInstance("SHA256withRSA");
	        signature.initSign(Key);
	        signature.update(input);
	        return signature.sign();
	    }
	
	
	
}
