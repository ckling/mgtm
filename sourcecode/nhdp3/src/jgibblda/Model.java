/*
 * Copyright (C) 2007 by
 * 
 * 	Xuan-Hieu Phan
 *	hieuxuan@ecei.tohoku.ac.jp or pxhieu@gmail.com
 * 	Graduate School of Information Sciences
 * 	Tohoku University
 * 
 *  Cam-Tu Nguyen
 *  ncamtu@gmail.com
 *  College of Technology
 *  Vietnam National University, Hanoi
 *
 * JGibbsLDA is a free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * JGibbsLDA is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGibbsLDA; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package jgibblda;

import ckling.geo.Map;
import ckling.math.BasicMath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;


public class Model {	

	//---------------------------------------------------------------
	//	Class Variables
	//---------------------------------------------------------------

	public static String tassignSuffix;	//suffix for topic assignment file
	public static String pi_sSuffix;		//suffix for \pi^s file (cluster - topic distribution) file
	public static String pi_dSuffix;		//suffix for document specific theta (document - topic distribution) file
	public static String thetaSuffix;		//suffix for phi file (topic - word distribution) file
	public static String othersSuffix; 	//suffix for containing other parameters
	public static String twordsSuffix;		//suffix for file containing words-per-topics
	public static String mapSuffix;		//suffix for file containing words-per-topics
	public static String etaSuffix;		//suffix for file containing words-per-topics

	//---------------------------------------------------------------
	//	Model Parameters and Variables
	//---------------------------------------------------------------

	public String wordMapFile; 		//file that contain word to id map
	public String trainlogFile; 	//training log file	

	public String dir;
	public String dfile;
	public String modelName;
	public int modelStatus; 		//see Constants class for status of model
	public LDADataset data;			// link to a dataset

	public int M; //dataset size (i.e., number of docs)
	public int V; //vocabulary size
	public int K; //number of topics
	public int L; //number of clusters q


	//use distances between neighbours in order to decide where to go
	public boolean CONSIDER_DIST=false;
	public int NR_THREADS =1;

	//set size of the training data, if 1.0 then no perplexity is calculated
	double trainingSize = 0.8;

	public int[][] nmk; // M x K

	public int[][] nkt; //K x V
	public int[] nk;
	public int[][] qd; //documents in cluster q
	public double[][] eta; //L x Neighbours
	public double[] pi_0;  // K+1
	public double[][] pi_s; //(für Gq) // J x K+1
	public double[][] pi_d; //(für Gj) // M x K+1
	public int[][] z; // M x N
	public double gamma;
	public double Alpha;
	public double delta;
	public double alpha0;
	public double beta;
	public double[][] theta; //K x V

	//Hyperparameters for Dirichlet processes / distributions
	public double gammaa; 
	public double gammab;
	public double alpha0a; 
	public double alpha0b; 	  	  
	public double Alphaa; 
	public double Alphab;
	public double deltaa;
	public double deltab;
	public double betaa; 
	public double betab;

	public SortedSet<Integer> kgaps;
	public List<Integer> kactive;

	public int niters; //number of Gibbs sampling iteration
	public int liter; //the iteration at which the model was saved	
	public int savestep; //saving period
	public int twords; //print out top words per each topic
	public int rtopics; //print out top topics per each region
	public int withrawdata;

	// variables for spatial clusters
	public int[][] qqN;
	public double[][] pQ; //probability for a point to belong to its neighbours 

	// Temp variables while sampling
	public int [] length; //length[i] of document i
	public int[] lengthTrain; //document length in training set

	//Topic description for understanding results
	protected String[] topicWords;

	//additional temp variables for spatial clusters
	protected int[] q; //spatial cluster index
	protected int[][] triangles; // array containing the cluster delaunay triangles
	protected double[][] qm; //cluster centroids in spherical coordinates, size 2 x Q

	//words and probabilities for map
	protected String[][] words;
	protected String[][] probabilities;

	//Should the parameters for Dirichlet processes / distributions be sampled? 
	public boolean sampleHyper;

	//---------------------------------------------------------------
	//	Constructors
	//---------------------------------------------------------------	

	public Model(){
		setDefaultValues();	
	}

	/**
	 * Set default values for variables
	 */
	public void setDefaultValues(){
		wordMapFile = "wordmap.txt";
		trainlogFile = "trainlog.txt";
		tassignSuffix = ".tassign";
		pi_dSuffix = ".pi_d";
		pi_sSuffix = ".pi_s";
		thetaSuffix = ".theta";
		othersSuffix = ".others";
		twordsSuffix = ".twords";
		mapSuffix = ".map3.html";
		etaSuffix = ".eta";

		dir = "./";
		dfile = "trndocs.dat";
		modelName = "model-final";
		modelStatus = Constants.MODEL_STATUS_UNKNOWN;		

		M = 0;
		V = 0;
		liter = 0;
		kactive = new ArrayList<Integer>();
		kgaps = new TreeSet<Integer>();

		z = null;
		theta = null;

		//new variables;
		q = null;
		triangles = null;
		qm = null;

		topicWords = null;


		//Set hyperparameters similar to Teh et al., 2006: Hierarchical Dirichlet Processes
		gammaa = 1.0;
		gammab = 0.1;
		alpha0a = 1.0;
		alpha0b = 1.0;
		Alphaa = 1.0;
		Alphab = 1.0;
		betaa = 0.1;
		betab = 0.1;

		sampleHyper = false;

	}



	/**
	 * Save pi^d (document specific topic distribution) for this model
	 */
	public boolean saveModelPi_d(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < M; i++){

				for (int j = 0; j < K; j++){

					writer.write(pi_d[i][j] + " ");

				}
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving topic distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Save word-topic distribution
	 */

	public boolean saveModelTheta(String filename){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			for (int i = 0; i < K; i++){
				for (int j = 0; j < V; j++){
					writer.write(theta[i][j] + " ");
				}
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving word-topic distribution:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}



	/**
	 * Save other information of this model
	 */
	public boolean saveModelOthers(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			writer.write("gamma=" + gamma + "\n");
			writer.write("alpha0=" + alpha0 + "\n");
			writer.write("Alpha=" + Alpha + "\n");
			writer.write("beta=" + beta + "\n");
			writer.write("ntopics=" + K + "\n");
			writer.write("nclusters=" + L + "\n");
			writer.write("ndocs=" + M + "\n");
			writer.write("nwords=" + V + "\n");
			writer.write("liters=" + liter + "\n");

			writer.close();
		}
		catch(Exception e){
			System.out.println("Error while saving model others:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveModelEta(String filename){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			for (int i = 0; i < L; i++){
				for (int j = 0; j < eta[i].length; j++){
					writer.write(eta[i][j] + " ");
				}
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving word-topic distribution:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean saveModelMapTopic2(String filename) {

		Map map = new Map(51.501904, -0.146301, 10,K);
		map.setFile(filename+"3.html");

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

		double[][] pisetaSum = new double[L][K];
		for (int l=0;l<L;l++) {
			int cluster = l;
			int[] parents = new int[qqN[cluster].length+1];
			parents[qqN[cluster].length]=cluster;
			System.arraycopy(qqN[cluster], 0, parents, 0, qqN[cluster].length);

			//calculate weights for different clusters
			//pi weights for parent distributions
			for (int lp=0;lp<parents.length;lp++) {
				int neighbour = parents[lp];
				for (int k=0;k<K;k++) {
					pisetaSum[l][k]+=pi_s[neighbour][k]*eta[cluster][lp];
				}
			}
		}

		for (int m=0;m<trainingSize*M;m++) {	

			if (m%1 == 0) {
				
			double[] p = new double[K];
			for (int k = 0; k < K; k++){
				int kk = kactive.get(k);
				p[k] = (Alpha*pisetaSum[q[m]][k] + nmk[m][kk]);
			}

			//normalise
			double sum = BasicMath.sum(p);
			for (int k = 0; k < K; k++){
				p[k]/=sum;
				
				if (p[k] > avgP) {
					double lat =  data.docs[m].lat;
					double lon =  data.docs[m].lon;
					double opacity = 1.0;

					map.addPoint(lat,lon,opacity, k,2);

				}
			}
			}

		}

		map.getMap(words,probabilities);

		return true;		
	}

	public boolean saveModelMapTopic(String filename) {
		//TODO
		saveModelMapTopic2(filename);

		Map map = new Map(51.501904, -0.146301, 10,K);
		map.setFile(filename);

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
			rgb[k]="ed2c54";
		}
		map.setColours(rgb);

		double[][] pisetaSum = new double[L][K];
		for (int l=0;l<L;l++) {
			int cluster = l;
			int[] parents = new int[qqN[cluster].length+1];
			parents[qqN[cluster].length]=cluster;
			System.arraycopy(qqN[cluster], 0, parents, 0, qqN[cluster].length);

			//calculate weights for different clusters
			//pi weights for parent distributions
			for (int lp=0;lp<parents.length;lp++) {
				int neighbour = parents[lp];
				for (int k=0;k<K;k++) {
					pisetaSum[l][k]+=pi_s[neighbour][k]*eta[cluster][lp];
				}
			}
		}

		for (int k=0; k<K; k++) {


			for (int i=0;i<qm[0].length;i++) {	
				if (pisetaSum[i][k]> avgP) {
					double lat =  qm[0][i];
					double lon =  qm[1][i];
					double opacity = 1.0;

					//Alternatively draw points with changing opacity 
					//opacity = pisetaSum[i][k];

					map.addPoint(lat,lon,opacity, k,1);
				}
			}

			double opacity = 0.3;
			
			for (int l=0;l<L;l++) {
				int cluster = l;
				int[] parents = new int[qqN[cluster].length+1];
				parents[qqN[cluster].length]=cluster;
				System.arraycopy(qqN[cluster], 0, parents, 0, qqN[cluster].length);

				//calculate weights for different clusters
				//pi weights for parent distributions
				for (int lp=0;lp<parents.length;lp++) {
					int neighbour = parents[lp];

//						if (neighbour != cluster && eta[cluster][lp] > 1.0/parents.length) {
							double[] lat = new double[2];
							double[] lon = new double[2];
							lat[0] =  qm[0][cluster];
							lon[0] =  qm[1][cluster];
							lat[1] =  qm[0][neighbour];
							lon[1] =  qm[1][neighbour];

							map.addLine(lat,lon,eta[cluster][lp]*eta[cluster][lp], 1, 5);
//						}					
				}
			}
			
			
			for (int i=0;i<triangles.length;i++) {		

//				if (pisetaSum[triangles[i][0]][k] > avgP && pisetaSum[triangles[i][1]][k] > avgP && pisetaSum[triangles[i][2]][k] > avgP ) {
//
//					double[] lat = new double[3];
//					double[] lon = new double[3];
//					lat[0] =  qm[0][triangles[i][0]];
//					lon[0] =  qm[1][triangles[i][0]];
//					lat[1] =  qm[0][triangles[i][1]];
//					lon[1] =  qm[1][triangles[i][1]];
//					lat[2] =    qm[0][triangles[i][2]];
//					lon[2] =    qm[1][triangles[i][2]];
//
//					map.addPolygon(lat,lon,opacity, k, 1);
//
//				}
//				if (pisetaSum[triangles[i][0]][k] > avgP && pisetaSum[triangles[i][1]][k] > avgP) {
//
//					double[] lat = new double[2];
//					double[] lon = new double[2];
//					lat[0] =  qm[0][triangles[i][0]];
//					lon[0] =  qm[1][triangles[i][0]];
//					lat[1] =  qm[0][triangles[i][1]];
//					lon[1] =  qm[1][triangles[i][1]];
//
//					map.addLine(lat,lon,opacity, k, 1);
//
//				}
//				if (pisetaSum[triangles[i][1]][k] > avgP && pisetaSum[triangles[i][2]][k] > avgP) {
//
//					double[] lat = new double[2];
//					double[] lon = new double[2];
//					lat[0] =  qm[0][triangles[i][1]];
//					lon[0] =  qm[1][triangles[i][1]];
//					lat[1] =  qm[0][triangles[i][2]];
//					lon[1] =  qm[1][triangles[i][2]];
//
//					map.addLine(lat,lon,opacity, k, 1);
//
//				}
//				if (pisetaSum[triangles[i][2]][k] > avgP && pisetaSum[triangles[i][0]][k] > avgP) {
//
//					double[] lat = new double[2];
//					double[] lon = new double[2];
//					lat[0] =  qm[0][triangles[i][2]];
//					lon[0] =  qm[1][triangles[i][2]];
//					lat[1] =  qm[0][triangles[i][0]];
//					lon[1] =  qm[1][triangles[i][0]];
//
//					map.addLine(lat,lon,opacity, k, 1);
//
//				}
											
			}



		}

		map.getMap(words,probabilities);

		return true;		
	}


	/**
	 * Save model the most likely words for each topic
	 */
	public boolean saveModelTwords(String filename){
		try{

			words = new String[K][twords];
			probabilities = new String[K][twords];
			topicWords = new String[K];

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));

			if (twords > V){
				twords = V;
			}

			for (int k = 0; k < K; k++){
				List<Pair> wordsProbsList = new ArrayList<Pair>(); 
				for (int w = 0; w < V; w++){
					Pair p = new Pair(w, theta[k][w], false);

					wordsProbsList.add(p);
				}//end foreach word

				//print topic				
				writer.write("Topic " + k + "th:\n");
				Collections.sort(wordsProbsList);

				//reset topic description
				topicWords[k] = "";
				for (int i = 0; i < twords; i++){
					if (data.localDict.contains((Integer)wordsProbsList.get(i).first)){
						String word = data.localDict.getWord((Integer)wordsProbsList.get(i).first);

						topicWords[k] = topicWords[k] + ", " + word;
						writer.write("\t" + word + " " + wordsProbsList.get(i).second + "\n");
						words[k][i]=word;
						probabilities[k][i]=String.valueOf(wordsProbsList.get(i).second);
					}
				}
			} //end foreach topic			

			writer.close();
		}
		catch(Exception e){
			System.out.println("Error while saving model twords: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Save cluster-region distribution
	 */

	public boolean saveModelPi_s(String filename){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

			for (int j = 0; j < L; j++){
				for (int k = 0; k < K; k++){
					writer.write(pi_s[j][k] + " ");
				}
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving cluster-topic distribution:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Save model
	 */
	public boolean saveModel(String modelName){

		if (!saveModelOthers(dir + File.separator + modelName + othersSuffix)){			
			return false;
		}

		if (twords > 0){
			if (!saveModelTwords(dir + File.separator + modelName + twordsSuffix))
				return false;

		}				

		if (modelName.equals("model-final")) {
			if (!saveModelPi_d(dir + File.separator + modelName + pi_dSuffix)){
				return false;
			}

			if (!saveModelTheta(dir + File.separator + modelName + thetaSuffix)){
				return false;
			}

		}

		if (!saveModelPi_s(dir + File.separator + modelName + pi_sSuffix)){
			return false;
		}

		if (!saveModelMapTopic(dir + File.separator + modelName + mapSuffix)){
			return false;
		}

		if (!saveModelEta(dir + File.separator + modelName + etaSuffix)){
			return false;
		}

		//if (!saveModelMapRegion(dir + File.separator + modelName + mapRegionSuffix)){
		//		return false;
		//}


		return true;
	}

	//---------------------------------------------------------------
	//	Init Methods
	//---------------------------------------------------------------
	/**
	 * initialize the model
	 */
	protected boolean init(Option option){		
		if (option == null)
			return false;

		modelName = "model";
		K = 0;
		L =option.L;

		kactive = new ArrayList<Integer>();
		kgaps = new TreeSet<Integer>();

		if (option.beta >= 0)
			beta = option.beta;

		delta = option.delta;

		niters = option.niters;

		dir = option.dir;
		if (dir.endsWith(File.separator))
			dir = dir.substring(0, dir.length() - 1);

		dfile = option.dfile;
		twords = option.twords;
		rtopics = option.rtopics;
		wordMapFile = option.wordMapFileName;

		gammaa = option.gammaa;
		gammab = option.gammab;
		alpha0a = option.alpha0a;
		alpha0b = option.alpha0b;
		Alphaa = option.Alphaa;
		Alphab = option.Alphab;
		betaa = option.betaa;
		betab = option.betab;

		gamma = option.gamma;
		alpha0 = option.alpha0;
		Alpha = option.Alpha;

		sampleHyper = option.sampleHyper;


		return true;
	}

	/**
	 * Init parameters for estimation
	 */
	public boolean initNewModel(Option option){
		if (!init(option))
			return false;


		data = LDADataset.readDataSet(dir + File.separator + dfile);
		if (data == null){
			System.out.println("Fail to read training data!\n");
			return false;
		}

		MF_Delaunay mf_delaunay = new MF_Delaunay(data.docs, L, dir,trainingSize);
		//		Cluster1 kmeans = new Cluster1(data.docs, J, dir);

		ArrayList<CopyOnWriteArraySet<Integer>> qd2 = mf_delaunay.qd;		
		qd = new int[qd2.size()][];
		for (int q = 0; q < qd2.size();q++) {

			CopyOnWriteArraySet<Integer> docs = (CopyOnWriteArraySet<Integer>) qd2.get(q);
			qd[q] = new int[docs.size()];
			Iterator<Integer> iterator2 = docs.iterator();
			int docCount = 0;
			while (iterator2.hasNext()) {
				int doc = iterator2.next();
				qd[q][docCount++]=doc;
			}
		}

		eta = new double[L][];
		ArrayList<CopyOnWriteArraySet<Integer>> alqqN = mf_delaunay.qqN;
		qqN = new int[alqqN.size()][];		
		for (int j = 0;j<L;j++) {
			qqN[j]=new int[alqqN.get(j).size()];
			eta[j]=new double[alqqN.get(j).size()+1];
			double etaStart = 1.0/(alqqN.get(j).size()+1.0);
			eta[j][alqqN.get(j).size()]=etaStart;
			int i = 0;
			for (Iterator<Integer> iterator=alqqN.get(j).iterator();iterator.hasNext();) {
				int neighbour = iterator.next();
				eta[j][i] = etaStart;
				qqN[j][i]=neighbour;
				i++;
			}			
		}

		q = mf_delaunay.getq();
		triangles = mf_delaunay.triangles;
		qm = mf_delaunay.getqm();

		//clear temporary variables
		mf_delaunay.clearPq();

		//allocate memory and assign values for variables		

		M = data.M;
		V = data.V;
		dir = option.dir;
		savestep = option.savestep;

		length = new int[M];
		lengthTrain = new int[(int) (trainingSize*M)];
		for (int m = 0; m < M; m++){
			if (m<(int) (trainingSize*M)) {
				lengthTrain[m]+=data.docs[m].length;
			}
			length[m]=data.docs[m].length;
		}
		// K: from command line or default value
		// alpha, beta: from command line or default values
		// niters, savestep: from command line or default values

		nmk = new int[M][K];
		nkt = new int[K][V];
		nk = new int[K];

		kactive = new ArrayList<Integer>();
		kgaps = new TreeSet<Integer>();
		//topic assignments for words
		z = new int[M][];
		for (int m = 0; m < data.M; m++){
			z[m] = new int[length[m]];				
		}


		pi_s = new double[L][K+1];

		//initialize delta



		pi_0 = new double[K+1];		
		pi_d = new double[M][K+1];		
		theta = new double[K][V+1];

		return true;
	}

	/**
	 * Init parameters for inference
	 * @param newData DataSet for which we do inference
	 */
	public boolean initNewModel(Option option, LDADataset newData, Model trnModel){
		if (!init(option))
			return false;

		int m;

		K = trnModel.K;
		L = trnModel.L;
		alpha0 = trnModel.alpha0;
		//		for (int i = 0; i < J;i++) {
		//			alphaq[i] = 1.;
		//		}
		Alpha = 1;
		beta = trnModel.beta;

		kactive = new ArrayList<Integer>();
		kgaps = new TreeSet<Integer>();

		System.out.println("K:" + K);

		data = newData;

		//+ allocate memory and assign values for variables		
		M = data.M;
		V = data.V;
		dir = option.dir;
		savestep = option.savestep;
		System.out.println("M:" + M);
		System.out.println("V:" + V);

		// K: from command line or default value
		// alpha, beta: from command line or default values
		// niters, savestep: from command line or default values

		nkt = new int[V][K];


		nk = new int[K];

		z = new int[M][];
		for (m = 0; m < data.M; m++){
			int N = data.docs[m].length;
			z[m] = new int[N];

			// total number of words in document i
			length[m] = N;
		}


		return true;
	}

	/**
	 * Init parameters for inference
	 * reading new dataset from file
	 */
	public boolean initNewModel(Option option, Model trnModel){
		if (!init(option))
			return false;

		LDADataset dataset = LDADataset.readDataSet(dir + File.separator + dfile, trnModel.data.localDict);
		if (dataset == null){
			System.out.println("Fail to read dataset!\n");
			return false;
		}

		return initNewModel(option, dataset , trnModel);
	}

}
