
public class Document_Authentication_System {

	String fingerprint;
	long aadhaarno;
	String cname;
	void requestAadhaarnoandCname(long aadhaarno,String cname)
	{
		
		System.out.println("Enter you Aadhaarno:"+aadhaarno);
		this.aadhaarno=aadhaarno;
		this.cname=cname;
		

	}
	void requestFingerprint()
	{
		System.out.println("Please Place Your Index Finger on the Fingerprint Scanner ");
		fingerprint="C:\\Users\\Dell\\Desktop\\Other1.png";
	}
	
	void authenticateUser() throws Exception
	{
		SmartSignature s1=new SmartSignature();
		s1.authenticate_DigitalSignature(aadhaarno,fingerprint,cname);
		
	}
	
}
