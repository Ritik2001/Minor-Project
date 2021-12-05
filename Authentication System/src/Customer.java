import java.util.*; // for input and output from console
import java.security.KeyPair;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Scanner;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import com.machinezoo.sourceafis.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.opencv.core.*;

public class Customer {
	
			String cname;
			String pin;
			long aadhaarno;
			Scanner sc=new Scanner(System.in);
			Customer()
			{
				System.out.println("Enter your name:");
				Scanner sc=new Scanner(System.in);
				cname=sc.nextLine();
				login();
			}
			
			void login()
			{ System.out.println("\n............Login Screen............\n");
				System.out.println("Enter your 12 digit aadhaar no.:");
				aadhaarno=sc.nextLong();
				System.out.println("Enter you pin:");
				sc.nextLine();
				pin=sc.nextLine();
			}
			
			void displaycustomerinfo()
			{ System.out.println("\n..............Displaying Customer Info...........\n");
				System.out.println("Customer Name:"+cname);
				System.out.println("Aadhaar no:"+aadhaarno);
				System.out.println("Pin :"+pin);
			}
			void tosigndocs()
			{
				
				
			}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//my aadhaarno: 436236004980
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			System.out.println("..............Welcome to Our System..................\n");
			Customer c1=new Customer();
			c1.displaycustomerinfo();
			char choice;
			System.out.println("\n  1. : To Sign a Document");
			System.out.println("  Any Other key: Exit System");
			
			choice=c1.sc.next().charAt(0);
	if(choice=='1')
		{ 
		try
		
			{
				Document_Authentication_System d=new Document_Authentication_System();
				d.requestAadhaarnoandCname(c1.aadhaarno, c1.cname);
				d.requestFingerprint();
				d.authenticateUser();
				
			}
		catch(Exception e)
			{
				System.out.println("Error in autherticateUser()");
			}
		}
	
	else
	{
		System.out.println("Thank you for Being a Customer");
	}
	}

}
