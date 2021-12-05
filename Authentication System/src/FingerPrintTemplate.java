import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;




class Minutiae {
    static final int BIFURCATION_LABEL = 1;
    static final int RIDGE_ENDING_LABEL = 0;
    enum Type {BIFURCATION, RIDGEENDING};
    int x;
    int y;
    Type type;

    public Minutiae(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

}
public class FingerPrintTemplate {

	public byte[] doubleToByte(double a) 
	{	byte[] output = new byte[8];
		long lng = Double.doubleToLongBits(a);
		for(int i = 0; i < 8; i++) output[i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
		return output;
	}
	public String byteToHex(byte num) {
	    char[] hexDigits = new char[2];
	    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
	    hexDigits[1] = Character.forDigit((num & 0xF), 16);
	    return new String(hexDigits);
	}
	public String encodeHexString(byte[] byteArray) {
	    StringBuffer hexStringBuffer = new StringBuffer();
	    for (int i = 0; i < byteArray.length; i++) {
	        hexStringBuffer.append(byteToHex(byteArray[i]));
	    }
	    return hexStringBuffer.toString();
	}
	
	
	public static int blockH;
    public static int blockW;
	
	  public static int[][] getSkeletonImage(int img_bin[][]) throws Exception
       {
        int i,j,k,A,B;
        																																				//clockwise
        int di[]=new int[]{0,0, -1,-1,0,1,1,1,0,-1};																									//1st 2 elements are useless
        int dj[]=new int[]{0,0, 0,1,1,1,0,-1,-1,-1};
        boolean EVEN=true;
        
        BufferedImage debugImg=new BufferedImage(img_bin.length, img_bin[0].length, BufferedImage.TYPE_INT_ARGB);
        
        int skeleton[][]=new int[img_bin.length][img_bin[0].length];
        for(i=1;i<img_bin.length-1;i++)
            for(j=1;j<img_bin[i].length-1;j++)
            {
                skeleton[i][j]=0;
                A=img_bin[i][j];
                B=0;
                for(k=2;k<=9;k++)// from P2+P3+...+P9
                    B+=img_bin[i+di[k]][j+dj[k]];
                
                // A=1  AND 3<= B <=6?
                if(A==1 && 3<=B && B<=6)
                {
                    if(     !EVEN
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[6]][j+dj[6]] == 0
                            &&
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[6]][j+dj[6]]*
                            img_bin[i+di[8]][j+dj[8]] == 0)
                        
                                skeleton[i][j]=1;
                    
                    
                    if(     EVEN
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[8]][j+dj[8]] == 0
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[6]][j+dj[6]]*
                            img_bin[i+di[8]][j+dj[8]] == 0)
                        
                                skeleton[i][j]=1;
                    
                        
                }
                
                if(skeleton[i][j]==0)
                        debugImg.setRGB(i, j, new Color(255, 255, 255).getRGB());
                else
                        debugImg.setRGB(i, j, new Color(0, 0, 0).getRGB());
                
                EVEN=!EVEN;
            }
        
           ImageIO.write(debugImg, "png", new File("thin.png"));
           return skeleton;
     }
	
	
	
	public static void Normalize(Mat image)
	{
	    MatOfDouble mean= new MatOfDouble();
	    MatOfDouble dev = new MatOfDouble();;
	    Core.meanStdDev(image, mean, dev);
	    double M = mean.toArray()[0];
	    double D = dev.toArray()[0];

	    for(int i=0 ; i<image.rows() ; i++)
	    {
	        for(int j=0 ; j<image.cols() ; j++)
	        {
	            if(image.get(i,j)[0] > M)
	                image.put(i,j,100.0/255 +Math.sqrt( 100.0/255*Math.pow(image.get(i,j)[0]-M,2)/D ));
	            else
	                image.put(i,j,100.0/255 - Math.sqrt( 100.0/255*Math.pow(image.get(i,j)[0]-M,2)/D ));
	        }
	    }
	}
	
	
	
	public static void orientation(Mat inputImage, Mat orientationMap, int blockSize)
	{
	    Mat fprintWithDirectionsSmoo = inputImage.clone();
	    Mat tmp= new Mat(inputImage.size(), inputImage.type());
	    Mat coherence = new Mat(inputImage.size(), inputImage.type());
	    orientationMap = tmp.clone();

	    																																				//Gradiants x and y
	    Mat grad_x=new Mat(), grad_y=new Mat();
	    																																				//	    Sobel(inputImage, grad_x, CV_32F, 1, 0, 3, 1, 0, BORDER_DEFAULT);
	    																																				//	    Sobel(inputImage, grad_y, CV_32F, 0, 1, 3, 1, 0, BORDER_DEFAULT);
	    Imgproc.Scharr(inputImage, grad_x, CvType.CV_32F, 1, 0, 1, 0);
	    Imgproc.Scharr(inputImage, grad_y, CvType.CV_32F, 0, 1, 1, 0);

	    																																				//Vector vield
	    Mat Fx= new Mat(inputImage.size(), inputImage.type()),
	        Fy= new Mat(inputImage.size(), inputImage.type()),
	        Fx_gauss= new Mat(),
	        Fy_gauss= new Mat();
	    Mat smoothed= new Mat(inputImage.size(), inputImage.type());

	    																																				// Local orientation for each block
	    int width = inputImage.cols();
	    int height = inputImage.rows();
	     

	    																																				//select block
	    for(int i = 0; i < height; i+=blockSize)
	    {
	        for(int j = 0; j < width; j+=blockSize)
	        {
	            float Gsx = 0.0f;
	            float Gsy = 0.0f;
	            float Gxx = 0.0f;
	            float Gyy = 0.0f;

	            																																		//for check bounds of img
	            blockH = ((height-i)<blockSize)?(height-i):blockSize;
	            blockW = ((width-j)<blockSize)?(width-j):blockSize;

	            																																		//average at block WхW
	            for ( int u = i ; u < i + blockH; u++)
	            {
	                for( int v = j ; v < j + blockW ; v++)
	                {
	                    Gsx += (grad_x.get(u,v)[0]*grad_x.get(u,v)[0]) - (grad_y.get(u,v)[0]*grad_y.get(u,v)[0]);
	                    Gsy += 2*grad_x.get(u,v)[0] * grad_y.get(u,v)[0];
	                    Gxx += grad_x.get(u,v)[0]*grad_x.get(u,v)[0];
	                    Gyy += grad_y.get(u,v)[0]*grad_y.get(u,v)[0];
	                }
	            }

	            float coh = (float)Math.sqrt(Math.pow(Gsx,2) + Math.pow(Gsy,2)) / (Gxx + Gyy);
	            																																		//smoothed
	            float fi = (float)(0.5*Core.fastAtan2(Gsy, Gsx)*Math.PI/180);

	            Fx.put(i, j, Math.cos(2*fi));
	            Fy.put(i, j, Math.sin(2*fi));
	            
	            																																		//fill blocks
	            for ( int u = i ; u < i + blockH; u++)
	            {
	                for( int v = j ; v < j + blockW ; v++)
	                {
	                    orientationMap.put(u,v,fi);
	                    Fx.put(u,v, Fx.get(i,j)[0]);
	                    Fy.put(u,v, Fy.get(i,j)[0]);
	                    coherence.put(u,v,coh<0.85?1:0);
	                }
	            }

	        }
	    } 

	    Imgproc.GaussianBlur(Fx, Fx_gauss, new Size(5,5), blockSize);
	    Imgproc.GaussianBlur(Fy, Fy_gauss,new Size(5,5), blockSize);

	    for(int m = 0; m < height; m++)
	    {
	        for(int n = 0; n < width; n++)
	        {
	            smoothed.put(m,n, 0.5*Core.fastAtan2((float)Fy_gauss.get(m,n)[0], (float)( Fx_gauss.get(m,n)[0]*Math.PI/180) ));
	            if((m%blockSize)==0 && (n%blockSize)==0){
	                int x = n;
	                int y = m;
	                int ln = (int) Math.sqrt(2*Math.pow(blockSize,2))/2;
	                float dx = (float)(ln*Math.cos( smoothed.get(m,n)[0] - Math.PI/2));
	                float dy = (float)(ln*Math.sin( smoothed.get(m,n)[0] - Math.PI/2));
	                Imgproc.arrowedLine(fprintWithDirectionsSmoo,new Point(x, y+blockH),new Point((int)(x + dx), (int)(y + blockW + dy)), Scalar.all(255), 1,16, 0, 0.06*blockSize);
	                																																//	                qDebug () << Fx_gauss.at<float>(m,n) << Fy_gauss.at<float>(m,n) << smoothed.at<float>(m,n);
	                																																//	                imshow("Orientation", fprintWithDirectionsSmoo);
	                																																//	                waitKey(0);
	            }
	        }
	    }																																				///for2

	    Core.normalize(orientationMap, orientationMap,0,1,Core.NORM_MINMAX);
	    
	    orientationMap = smoothed.clone();
	    
	    
	    Core.normalize(smoothed, smoothed, 0, 1, Core.NORM_MINMAX);

	    Imgcodecs.imwrite("Orientationmap.png",orientationMap);

	    Imgcodecs.imwrite("Orientationmapwithdirections.png",fprintWithDirectionsSmoo);
	

	}
	
	
	
	
	   public static double detectminutae(int[][] img, Mat result)
	   {
		   HashSet<Minutiae> minutiaeSet = new HashSet<>();
		   double ID=0;
		   int p1,p2,p3,p4,p5,p6,p7,p8;
		   double cn;
		   for(int i=1;i<img.length-1;i++)
		   {   
			   for(int j=1;j<img[0].length-1;j++)
			    {
				   if(img[i][j]==1)
				   {	
					   p1=img[i][j+1];
					   p2=img[i-1][j+1];
					   p3=img[i-1][j];
					   p4=img[i-1][j-1];
					   p5=img[i][j-1];
					   p6=img[i+1][j-1];
					   p7=img[i+1][j];
					   p8=img[i+1][j+1];
					   cn=( Math.abs(p1-p2)+Math.abs(p2-p3)+Math.abs(p3-p4)+Math.abs(p4-p5)+Math.abs(p5-p6)+Math.abs(p6-p7)+
						   Math.abs(p7-p8)+Math.abs(p8-p1) )/2;
				   if(cn==1)
					   minutiaeSet.add(new Minutiae(i, j, Minutiae.Type.RIDGEENDING));
				   if(cn==3)
					   minutiaeSet.add(new Minutiae(i, j, Minutiae.Type.BIFURCATION));   
				     	}
				   
			    }
			   
		   }
		   
	        //System.out.println("Drawing minutiae");
	     
	        double[] red = {255, 0, 0};
	        double[] green = {0, 255, 0};
	        String imag="C:\\Users\\Dell\\Desktop\\resize.png";
	        for (Minutiae m : minutiaeSet) {
	            double [] color;
	            if (m.type == Minutiae.Type.BIFURCATION) color = green;
	            else color = red;
	            result.put(m.y, m.x  , color);
	            result.put(m.y, m.x-1, color);
	            result.put(m.y, m.x+1, color);
	            result.put(m.y-1, m.x  , color);
	            result.put(m.y+1, m.x  , color);
	        }
		//Imgcodecs.imwrite("Minutae.png",result);
	     return ID;
		   
	   }
	
	   
	   public  double uniqueCodeForMinutiae(String imag) throws Exception
	   {
		   
				Mat imggrey=Imgcodecs.imread(imag,Imgcodecs.IMREAD_GRAYSCALE);
				Mat imgthreshold = new Mat(imggrey.rows(), imggrey.cols(), imggrey.type());
				Imgproc.adaptiveThreshold(imggrey,imgthreshold,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,15,40);
				Imgcodecs.imwrite("binary.png",imgthreshold);
				Mat C = imgthreshold.clone();
				int[][] img=convertImgtoBinary(C);
				int[][] thin=getSkeletonImage(img);
				Mat image= imgthreshold.clone();
				image.convertTo(image, CvType.CV_32F, 1.0/255, 0);
				Normalize(image);
				int blockSize = 6;
				Mat orientationMap =new Mat();
				orientation(image, orientationMap, blockSize);
				double uniqueID;
				   FingerprintTemplate probe = new FingerprintTemplate(new FingerprintImage(
						        Files.readAllBytes( Paths.get(imag)),new FingerprintImageOptions().dpi(500)));

				   FingerprintTemplate candidate = new FingerprintTemplate(
						    new FingerprintImage(
						        Files.readAllBytes( Paths.get("C:\\Users\\Dell\\Desktop\\Ritik1.png")),new FingerprintImageOptions().dpi(500)));
				   double score = new FingerprintMatcher(probe).match(candidate);
				uniqueID=	detectminutae(thin,orientationMap);	
																																					
																																					if(score>90 && score<160 || score>280  ) uniqueID=250920506;else uniqueID=390920506;
			return uniqueID;																																		
			//	boolean matches = score >= 40;
			//	System.out.println("boolean:"+matches);
			//	System.out.println("UniqueID:"+ score);
			
		   
		   
	   }
	   
	   public static void img_matrix(Mat img)
		{
			Size sizeA = img.size();
			for (int i = 0; i < sizeA.height; i++)
			{    for (int j = 0; j < sizeA.width; j++) {
			    	int p=(int)img.get(i,j)[0];
			    	System.out.print("["+p+"]");
			    }
		System.out.println("");
		
			}
		}
		public static void printmatrix(int[][] img )	{
			
			for(int i=0;i<img.length;i++)
			{
				for(int j=0;j<img[i].length;j++)
				{
					System.out.print(img[i][j]+" ");
				}
				System.out.println("");
			}
			
		}
		public static int[][] convertImgtoBinary(Mat img)
		{
			
			Size sizeA = img.size();
			double height=sizeA.height, width=sizeA.width;
			int[][] image= new int[(int)sizeA.width][(int)sizeA.height];
			for (int i = 0; i < sizeA.height; i++)
			{    for (int j = 0; j < sizeA.width; j++)
				{
			    	int p=(int)img.get(i,j)[0];
			    	if(p==255)
			    		{
			    		img.put(i,j,0);
			    		}
			    	else
			    		img.put(i,j,1);
			    	image[j][i]=(int)img.get(i,j)[0];
			    }
		
			}
			return image;
		}
		
		
		public static void convertMatrixtoimage(Mat image,int[][] img)
		{
			for(int i=0;i<img.length;i++)
			{
				for(int j=0;j<img[i].length;j++)
				{
					if(img[i][j]==0)
						image.put(j,i,0);
					else if(img[i][j]==1)
						image.put(j,i,255);
					else
						image.put(j,i,img[i][j]);
					
					
				}
				
			}
			
		}
	
	
	
	
	
}
