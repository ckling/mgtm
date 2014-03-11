package jgibblda;

public class Option {

	public boolean est = true;
	public boolean estc = false;
	public boolean inf = false;
	public String dir = "";
	public String dfile = "";
	public String modelName = "";
	public double alpha = -1.0;
	public double beta = -1.0;
	public double zeta = 1.0;
	public int K = 25;
	public int R = 10;
	public int L = 500;
	public double delta = 1.0;
	public int niters = 5000;
	public int savestep = 100;
	public int twords = 100;
	public int rtopics = 5;
	public boolean withrawdata = false;
	public String wordMapFileName = "wordmap.txt";
	public int runs = 1;
	
	public Double gammaa = 1.0;
	public Double gammab = 0.1;
	public Double Alphaa = 1.0;
	public Double Alphab = 1.0;
	public Double alpha0a = 1.0;
	public Double alpha0b = 1.0;
	public Double betaa = 0.1;
	public Double betab = 0.1;
	
	public Double Alpha = 1.0;
	public Double gamma = 1.0;
	public Double alpha0 = 1.0;
	
	
	public boolean sampleHyper = false;
	
	public Option(String[] input) {
		
		for (int i=0; i < input.length; i++) {
			
			if (input[i].equals("-est")) {
				 est=true;
				 estc= false;
				 inf = false;
			}
			
			if (input[i].equals("-estc")) {
				 estc= true;
				 est = false;
				 inf=false;
			}
			
			if (input[i].equals("-inf")) {
				 est=false;
				 estc= false;
				 inf = true;
			}
			
			if (input[i].equals("-modelName")) 
				modelName = input[++i];
			
			if (input[i].equals("-dfile")) 
				dfile = input[++i];
			
			if (input[i].equals("-dir")) 
				dir = input[++i];
			
			if (input[i].equals("-K")) 
				K = Integer.valueOf(input[++i]);
			
			if (input[i].equals("-R")) 
				R = Integer.valueOf(input[++i]);		
			
			if (input[i].equals("-L")) 
				L = Integer.valueOf(input[++i]);
			
			if (input[i].equals("-twords")) 
				twords = Integer.valueOf(input[++i]);
					
			if (input[i].equals("-savestep")) 
				savestep = Integer.valueOf(input[++i]);
			
			if (input[i].equals("-delta")) 
				delta = Double.valueOf(input[++i]);
			
			if (input[i].equals("-niters")) 
				niters = Integer.valueOf(input[++i]);
			
			if (input[i].equals("-alpha")) 
				alpha = Double.valueOf(input[++i]);
			
			if (input[i].equals("-beta")) 
				beta = Double.valueOf(input[++i]);
									
			if (input[i].equals("-zeta")) 
				zeta = Double.valueOf(input[++i]);
						
			if (input[i].equals("-gammaa")) 
				gammaa = Double.valueOf(input[++i]);
			
			if (input[i].equals("-gammab")) 
				gammab = Double.valueOf(input[++i]);
			
			if (input[i].equals("-alpha0a")) 
				alpha0a = Double.valueOf(input[++i]);

			if (input[i].equals("-alpha0b")) 
				alpha0b = Double.valueOf(input[++i]);

			if (input[i].equals("-Alphaa")) 
				Alphaa = Double.valueOf(input[++i]);
			
			if (input[i].equals("-Alphab")) 
				Alphab = Double.valueOf(input[++i]);
			
			if (input[i].equals("-betaa")) 
				betaa = Double.valueOf(input[++i]);
			
			if (input[i].equals("-betab")) 
				betab = Double.valueOf(input[++i]);
			
			if (input[i].equals("-sampleHyper")) 
				sampleHyper = Boolean.valueOf(input[++i]);
			
			if (input[i].equals("-runs")) 
				runs = Integer.valueOf(input[++i]);
			
			if (input[i].equals("-alpha0")) 
				alpha0 = Double.valueOf(input[++i]);
			
			if (input[i].equals("-Alpha")) 
				Alpha = Double.valueOf(input[++i]);
			
			if (input[i].equals("-gamma")) 
				gamma = Double.valueOf(input[++i]);
			
		}
		
	}
	
}
