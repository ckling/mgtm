package createFiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jgibblda.Pair;

import ckling.geo.Map;
import ckling.math.BasicMath;

public class LGTAMap {

	public static void main(String[] args) throws IOException {
		
		int K = 6;
		int L = 1000;
		//TODO correct after new sample :(
		int ndoc = 121572;
		int V=278;
		
		double[][] loc = new double[ndoc][2];
		double[][] ploc = new double[ndoc][L];
		double[][] pword = new double[K][V];
		double[][] ptop = new double[L][K];
		String[] term = new String[V];
		
		FileReader reader5 = new FileReader("/home/c/work/jgibblda/models/food/food_tag_freq.txt");
		BufferedReader bReader5 = new BufferedReader(reader5);
		int count = 0;
		String line;
		
		while((line = bReader5.readLine()) != null) {		
			
			if (!line.equals("")){
				String[] split = line.split(" ");
				term[count] = split[0];
			}
			count++;
		}
		
		FileReader reader4 = new FileReader("/home/c/work/jgibblda/models/food/1000-6loc.txt");
		BufferedReader bReader4 = new BufferedReader(reader4);
				
		for (int i=0;i<5;i++) bReader4.readLine();
		count = 0;
		while((line = bReader4.readLine()) != null) {		
			
			if (!line.equals("")){
				line=line.trim();
				String[] split = line.split(" ");
				loc[count][0] = Double.valueOf(split[0]);
				loc[count][1] = Double.valueOf(split[1]);
			}
			count++;
			
		}
		
		
		FileReader reader3 = new FileReader("/home/c/work/jgibblda/models/food/1000-6ploc.txt");
		BufferedReader bReader3 = new BufferedReader(reader3);
				
		count = 0;
		for (int i=0;i<5;i++) bReader3.readLine();
		while((line = bReader3.readLine()) != null) {
			
			if (!line.equals("")){
				line=line.trim();
				String[] split = line.split(" ");
				for (int j=0;j<split.length;j++) {
					ploc[j][count] = Double.valueOf(split[j]);
					//if (ploc[j][count]>0) System.out.println(j+ " " + count);
				}
			}
			count++;
			
		}
				
		FileReader reader2 = new FileReader("/home/c/work/jgibblda/models/food/1000-6pword.txt");
		BufferedReader bReader2 = new BufferedReader(reader2);
			
		count = 0;
		for (int i=0;i<5;i++) bReader2.readLine();
		while((line = bReader2.readLine()) != null) {
			
			if (!line.equals("")){
				line=line.trim();
				String[] split = line.split(" ");
				for (int j=0;j<split.length;j++) {
					pword[j][count] = Double.valueOf(split[j]);
				}
			}
			count++;
			
		}
		
		FileReader reader = new FileReader("/home/c/work/jgibblda/models/food/1000-6ptop.txt");
		BufferedReader bReader = new BufferedReader(reader);
				
		count = 0;
		for (int i=0;i<5;i++) bReader.readLine();
		while((line = bReader.readLine()) != null) {
			
			if (!line.equals("")){
				line=line.trim();
				String[] split = line.split(" ");
				for (int j=0;j<split.length;j++) {
					ptop[j][count] = Double.valueOf(split[j]);
				}
			}
			count++;
			
		}
		
		Map map = new Map(51.501904, -0.146301, 10,K);
		map.setFile("/home/c/work/jgibblda/models/food/LGTA.html");

		//point size
		map.size(3);

		//average probability for a topic is 1/K
		double avgP = 1.0 / K;

		//double avgP = 0.05; //experimental - set minimum of p to a small fixed value instead of avg

		String[] rgb = new String[K];

		for (int k=0; k<K; k++) {
			int red = 0,green = 0,blue = 0;

			if (k%3==0) {
				red = 255;
				green =  (int) Math.floor(255.0 * ((double) k / (double)K));
				blue = 0;
			}
			else if (k%3==1) {
				red = 0;
				green =  255;
				blue =  (int) Math.floor(255.0 * ((double) k / (double)K));
			}
			if (k%3==2) {
				red = (int) Math.floor(255.0 * ((double) k / (double)K));
				green =  0;
				blue =  255;
			}

			rgb[k] = ckling.math.BasicMath.convertNumber(red, 16, 2) + 
					ckling.math.BasicMath.convertNumber(green, 16, 2) +
					ckling.math.BasicMath.convertNumber(blue, 16, 2);
			//rgb[k]="ff00ff";
		}
		map.setColours(rgb);


		for (int m=0;m<ndoc;m++) {	

	
				
			double[] p = new double[K];
			for (int k = 0; k < K; k++){
				p[k]=0;
				for (int l = 0; l < L; l++){
					p[k]+=ploc[m][l] * ptop[l][k];
				}
			}
			double sum = BasicMath.sum(p);
			
				
			for (int k = 0; k < K; k++){
				p[k]/=sum;
												
				if (p[k] > avgP) {
					double lat =  loc[m][0];
					double lon =  loc[m][1];
					double opacity = 1.0;

					map.addPoint(lat,lon,opacity, k,2);

				}
			}
			}

				
		String[][] words = new String[K][10];
		String[][] probabilities = new String[K][10];


		for (int k = 0; k < K; k++){
			List<Pair> wordsProbsList = new ArrayList<Pair>(); 
			for (int w = 0; w < V; w++){
				Pair p = new Pair(w, pword[k][w], false);

				wordsProbsList.add(p);
			}//end foreach word

			//print topic				
			Collections.sort(wordsProbsList);

			//reset topic description
			for (int i = 0; i < 10; i++){
					String word = term[(Integer)wordsProbsList.get(i).first];

					words[k][i]=word;
					probabilities[k][i]=String.valueOf(wordsProbsList.get(i).second);
	
			}
		} //end foreach topic	

		map.getMap(words,probabilities);
		
	}
	
}
