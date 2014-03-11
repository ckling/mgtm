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

import java.io.File;
import ckling.math.BasicMath;

import org.knowceans.util.DirichletEstimation;
import org.knowceans.util.RandomSamplers;

import jgibblda.Model;

public class Estimator {

	// output model
	protected Model trnModel;

	//constant variables
	protected double Kalpha;
	protected double Vbeta;
	protected double Jzeta;
	protected Option option;
	protected int[] qLength;

	protected int T; //number of tables, calculated in updatePrior()
	protected double[] nj;
	protected int[][] mjkl; //cluster customer count
	protected int mj; //doc table count
	protected int[][] mkl; //table count per cluster
	protected int[] nmkNew; //for testing phase, save counts for new topic

	protected int[] countj;
	protected int sampleCount;
	protected boolean initialized;
	protected int trainingLength;

	private RandomSamplers samp;
	private double[] p;

	public double perplexity=0.0;
	public int Ksave;


	public boolean init(Option option){

		this.option = option;
		trnModel = new Model();

		if (option.est){
			if (!trnModel.initNewModel(option))
				return false;
			trnModel.data.localDict.writeWordMap(option.dir + File.separator + trnModel.wordMapFile);
		}

		return true;
	}

	public void estimate(){
		System.out.println("Sampling " + trnModel.niters + " iteration!");

		//load sampler
		samp = new RandomSamplers();

		//setting constants
		Vbeta = trnModel.V * trnModel.beta;

		trnModel.pi_0[trnModel.K] = 1;
		for (int j=0;j<trnModel.L;j++) {
			trnModel.pi_s[j][trnModel.K] = 1;
		}


		double[] nj = new double[trnModel.L];
		for (int m = 0; m < trnModel.M; m++) {
			for (int k=0;k<trnModel.K;k++) {
				nj[trnModel.q[m]]+= trnModel.length[m];
			}
		}


		p = new double[trnModel.K+1];

		//setting temporary variables for sampling the estimate
		sampleCount = 0;

		qLength = new int[trnModel.L];
		for (int m = 0; m < trnModel.M; m++){			
			for (int n = 0; n < trnModel.length[m]; n++){
				qLength[trnModel.q[m]]++;
			}
		}

		//sample training data - might be the whole data
		trainingLength =  (int) (trnModel.trainingSize*trnModel.M);

		int lastIter = trnModel.liter;
		for (trnModel.liter = lastIter + 1; trnModel.liter < trnModel.niters; trnModel.liter++){
			System.out.println("Iteration " + trnModel.liter + " ");

			// for all z_i
			for (int m = 0; m < trainingLength; m++){			
				// sample from p(z_i|z_-i, w)
				sampling(m);

			}// end for each document
			System.out.print(".");

			updatePrior();

			System.out.print(".");

			//if sampleHyper is set and iterations > 10: sample Dirichlet parameters
			if (trnModel.sampleHyper) {
				updateHyper();
			}

			System.out.print(".");

			initialized = true;

			if (trnModel.liter > 50 && (trnModel.K<6 || trnModel.K > 8)) break;
		}// end iterations		

		System.out.println("Gibbs sampling completed!\n");
		System.out.println("Saving the final model!\n");
		sampleCount++;
		computePhi();
		updatePrior();
		trnModel.liter--;
		trnModel.saveModel("model-final");

		Ksave = trnModel.K;
		
		//if training size < 100%, calculate perplexite with rest
		if (trnModel.trainingSize != 1.0) {
			getPerplexity();
		}

	}

	/**
	 * Do sampling
	 * @param m document number
	 * @param n word number
	 * @return topic id
	 */
	public void sampling(int m){

		int topic;
		int cluster = trnModel.q[m];
		int[] parents = new int[trnModel.qqN[cluster].length+1];
		parents[trnModel.qqN[cluster].length]=cluster;
		System.arraycopy(trnModel.qqN[cluster], 0, parents, 0, trnModel.qqN[cluster].length);
		//get weights on beginning or when there is a new topic
		boolean getWeight = true;
		//pi^s_l * eta_cjl
		double[] pisetaSum = null;
		//for every word in document m
		for (int n = 0; n < trnModel.data.docs[m].length; n++){
			if (getWeight) {
				//calculate weights for different clusters
				//pi weights for parent distributions
				pisetaSum = new double[trnModel.K+1];
				for (int l=0;l<parents.length;l++) {
					int neighbour = parents[l];
					for (int k=0;k<trnModel.K+1;k++) {
						pisetaSum[k]+=trnModel.pi_s[neighbour][k]*trnModel.eta[cluster][l];
					}
				}

				getWeight = false;
			}

			// remove z_i from the count variable
			int w = trnModel.data.docs[m].words[n];

			if (initialized) {
				topic = trnModel.z[m][n];
				trnModel.nmk[m][topic]--;
				trnModel.nkt[topic][w]--;
				trnModel.nk[topic]--;
			}
			else {
				topic = -1;
			}
			
			//sampling for document topics
			for (int k = 0; k < trnModel.K; k++){
				int kk = trnModel.kactive.get(k);
				p[k] = (trnModel.Alpha*pisetaSum[k] + trnModel.nmk[m][kk]) * 
						((trnModel.nkt[kk][w] + trnModel.beta)/(trnModel.nk[kk] + Vbeta));
			}
			p[trnModel.K]=(trnModel.Alpha*pisetaSum[trnModel.K]) / trnModel.V;

			//cumulate
			for (int k = 1; k < trnModel.K+1; k++){
				p[k] += p[k-1];
			}



			// scaled sample because of unnormalized p[]
			double u = Math.random() * p[trnModel.K];

			int topicNew = 0;
			while(u > p[topicNew]){
				topicNew++;
			}

			//Debug
			if (topicNew > trnModel.K) {
				System.out.println("error in sampling topic, printing p");
				for (topicNew = 0; topicNew < trnModel.K+1; topicNew++){
					int kk = trnModel.kactive.get(topicNew);
					System.out.println("k " + topicNew + " kk " + kk + " p " + p[topicNew] + " alphaq " + trnModel.Alpha + " eta " + trnModel.eta[cluster][0]+ " sigma " + trnModel.pi_s[cluster][topicNew] +" nmk " + trnModel.nmk[m][kk] + " nkt " +trnModel.nkt[kk][w] + " nk " +trnModel.nk[kk]);
				}
			}

			if (topicNew == trnModel.K) {
				addTopic(m,n,w);
				getWeight=true;
			}
			else {
				int kk = trnModel.kactive.get(topicNew);
				trnModel.nmk[m][kk]++;
				trnModel.nkt[kk][w]++;
				trnModel.nk[kk]++;
				trnModel.z[m][n]=kk;
			}

			if (initialized && trnModel.nk[topic] == 0) {
				rmTopic(topic);
			}

		}
		return;
	}

	/**
	 * Do sampling in test phase
	 * @param m document number
	 * @param n word number
	 * @return topic id
	 */
	public void samplingTest(int m){


		int topic;
		int cluster = trnModel.q[m];
		int[] parents = new int[trnModel.qqN[cluster].length+1];
		parents[trnModel.qqN[cluster].length]=cluster;
		System.arraycopy(trnModel.qqN[cluster], 0, parents, 0, trnModel.qqN[cluster].length);
		//get weights on beginning or when there is a new topic
		//pi^s_l * eta_cjl
		double[] pisetaSum = null;
		//for every word in document m
		for (int n = 0; n < trnModel.data.docs[m].length; n++){
			//calculate weights for different clusters
			//pi weights for parent distributions
			pisetaSum = new double[trnModel.K+1];
			for (int lp=0;lp<parents.length;lp++) {
				int neighbour = parents[lp];
				for (int k=0;k<trnModel.K+1;k++) {
					pisetaSum[k]+=trnModel.pi_s[neighbour][k]*trnModel.eta[cluster][lp];
				}
			}


			// remove z_i from the count variable
			int w = trnModel.data.docs[m].words[n];

			if (initialized) {
				topic = trnModel.z[m][n];
				if(topic == -1) {
					nmkNew[m]--;
				}
				else {
					trnModel.nmk[m][topic]--;
				}
			}
			else {
				topic = -1;
			}




			//sampling for testing document topics
			for (int k = 0; k < trnModel.K; k++){
				int kk = trnModel.kactive.get(k);
				p[k] = (trnModel.Alpha*pisetaSum[k] + trnModel.nmk[m][kk]) * 
						((trnModel.nkt[kk][w] + trnModel.beta)/(trnModel.nk[kk] + Vbeta));
			}
			p[trnModel.K]= (trnModel.Alpha*pisetaSum[trnModel.K] + nmkNew[m]) / trnModel.V;

			//accumulate
			for (int k = 1; k < trnModel.K+1; k++){
				p[k] += p[k-1];
			}



			// scaled sample because of unnormalized p[]
			double u = Math.random() * p[trnModel.K];

			int topicNew = -1;
			for (topicNew = 0; topicNew < trnModel.K+1; topicNew++){
				if (p[topicNew] > u) //sample topic w.r.t distribution p
					break;
			}

			//Debug
			if (topicNew > trnModel.K) {
				System.out.println("error in sampling topic, printing p");
				for (topicNew = 0; topicNew < trnModel.K+1; topicNew++){
					int kk = trnModel.kactive.get(topicNew);
					System.out.println("k " + topicNew + " kk " + kk + " p " + p[topicNew] + " alphaq " + trnModel.Alpha + " sigma " + trnModel.pi_s[cluster][topicNew] +" nmk " + trnModel.nmk[m][kk] + " nkt " +trnModel.nkt[kk][w] + " nk " +trnModel.nk[kk]);
				}
			}

			if (topicNew == trnModel.K) {
				nmkNew[m]++;
				trnModel.z[m][n]=-1;
			}
			else {
				int kk = trnModel.kactive.get(topicNew);
				trnModel.nmk[m][kk]++;
				trnModel.z[m][n]=kk;
			}
		}
		return;
	}

	/*
	 * add topic, increase count for document m and term t
	 */
	public void addTopic(int m, int n, int t) {

		System.out.println("adding topic " + trnModel.K + " for doc " + m);

		if (trnModel.kgaps.isEmpty()) {
			trnModel.K++;
			//create new topic with number K / index K-1
			trnModel.kactive.add(trnModel.K-1);
			trnModel.z[m][n]=trnModel.K-1;

			p=new double[trnModel.K+1];

			//expand phi,pi,sigma,tau,nkt,nk,nmk
			int[][] nmkOld = trnModel.nmk;
			trnModel.nmk=new int[trnModel.M][trnModel.K];
			for (int mm=0;mm<trnModel.M;mm++) {
				System.arraycopy(nmkOld[mm], 0, trnModel.nmk[mm], 0,trnModel.K-1);	
			}
			int[] nkOld = trnModel.nk;
			trnModel.nk=new int[trnModel.K];
			System.arraycopy(nkOld, 0, trnModel.nk, 0,trnModel.K-1);	
			int[][] nktOld = trnModel.nkt;
			trnModel.nkt=new int[trnModel.K][trnModel.V];
			System.arraycopy(nktOld, 0, trnModel.nkt, 0,trnModel.K-1);

			trnModel.nmk[m][trnModel.K-1]=1;
			trnModel.nkt[trnModel.K-1][t]=1;
			trnModel.nk[trnModel.K-1]=1;		

			double[] tauOld = trnModel.pi_0;
			trnModel.pi_0=new double[trnModel.K+1];
			System.arraycopy(tauOld, 0, trnModel.pi_0, 0,trnModel.K);
			double[][] sigmaOld = trnModel.pi_s;
			trnModel.pi_s=new double[trnModel.L][trnModel.K+1];
			for(int j=0;j<trnModel.L;j++) {
				System.arraycopy(sigmaOld[j], 0, trnModel.pi_s[j], 0,trnModel.K);
			}
			double[][] piOld = trnModel.pi_d;
			trnModel.pi_d=new double[trnModel.M][trnModel.K+1];
			for(int mm=0;mm<trnModel.M;mm++) {
				System.arraycopy(piOld[mm], 0, trnModel.pi_d[mm], 0,trnModel.K);
			}

		}
		else {
			int newk =  trnModel.kgaps.first();
			trnModel.kgaps.remove(newk);
			trnModel.kactive.add(newk);
			trnModel.nmk[m][newk]=1;
			trnModel.nkt[newk][t]=1;
			trnModel.nk[newk]=1;
			trnModel.K++;
			trnModel.z[m][n]=newk;
		}

		updatePrior();

	}

	public void rmTopic(int kk) {

		System.out.println("removing topic " + kk + " from " +  trnModel.K + " topics");

		int k;
		for (k = 0; k < trnModel.K; k++){
			if (trnModel.kactive.get(k)==kk) break;
		}
		trnModel.kgaps.add(kk);
		trnModel.kactive.remove(k);
		trnModel.K--;
		updatePrior();
	}

	public void updateHyper() {

		//Sample Dirichlet parameter from table counts

		trnModel.Alpha= samp.randConParam(trnModel.Alpha,trnModel.lengthTrain, mj, trnModel.Alphaa, trnModel.Alphab, 20);

		int[] nk = new int[trnModel.L];
		for (int j = 0; j < trnModel.L; j++) {	
			for (int k = 0; k < trnModel.K; k++){
				nk[j]+= mjkl[j][k];
			}
		}

		trnModel.alpha0=samp.randConParam(trnModel.alpha0, nk, T, trnModel.alpha0a, trnModel.alpha0b, 20);
		trnModel.gamma=samp.randConParam(trnModel.gamma, T, trnModel.K, trnModel.gammaa, trnModel.gammab, 20);

		trnModel.beta = DirichletEstimation
				.estimateAlphaMap(trnModel.nkt, trnModel.nk, trnModel.beta, trnModel.betaa, trnModel.betab);
	
		System.out.println();
		System.out.println("K: " + trnModel.K);
		System.out.println("hyper:");
		System.out.println("Gamma = " + trnModel.gamma);
		System.out.println("alpha_0 = " +trnModel.alpha0);
		System.out.println("Alpha = " +trnModel.Alpha);
		System.out.println("beta = " +trnModel.beta);


	}


	public void updatePrior(){
		updatePi_0();
		updatePi_s();
		//We dont need document specific topic distributions
		//for sampling as they are integrated out!
		//updatePi_d();
	}



	//This function also samples for table counts in order to calculate pi_0
	public void updatePi_0(){

		mjkl = new int[trnModel.L][trnModel.K];
		mj = 0;

		for (int l=0;l<trnModel.L;l++) {
			int[] docs = trnModel.qd[l];
			int[] parents = new int[trnModel.qqN[l].length+1];
			parents[trnModel.qqN[l].length]=l;
			System.arraycopy(trnModel.qqN[l], 0, parents, 0, trnModel.qqN[l].length);

			int[][] mlkl = new int[parents.length][trnModel.K];

			double[][] weights = new double[trnModel.K][parents.length];
			for (int lp=0;lp<parents.length;lp++) {
				int neighbour = parents[lp];
				for (int k=0;k<trnModel.K;k++) {
					weights[k][lp]=trnModel.pi_s[neighbour][k]*trnModel.eta[l][lp]*trnModel.Alpha;
				}
			}

			for (int doc : docs) {
				if (doc < (int) (trnModel.trainingSize*trnModel.M)) {
					//number of customers eating dish - has to be saved because nmk may contain empty topics!
					int[] customers = new int[trnModel.K];
					for (int k = 0; k < trnModel.K; k++){
						int kk = trnModel.kactive.get(k); 
						customers[k]=trnModel.nmk[doc][kk];
					}

					int[][] tables  = samp.randNumTable(weights,customers);
					for (int lp=0;lp<parents.length;lp++) {
						for (int k = 0; k < trnModel.K; k++){
							int tableCount=tables[lp][k];
							mlkl[lp][k]+=tableCount;
							mjkl[parents[lp]][k]+=tableCount;
							mj+=tableCount;
						}
					}
				}
			}

			if (initialized) {
				//sample eta
				for (int lp=0;lp<parents.length;lp++) {
					trnModel.eta[l][lp] = (BasicMath.sum(mlkl[lp]) + trnModel.delta);
				}
				//normalise
				double sum = BasicMath.sum(trnModel.eta[l]);
				for (int lp=0;lp<parents.length;lp++) {
					trnModel.eta[l][lp] /= sum;
				}
			}

		}
		//sample delta: skipped

		//sample mk using method by Teh et al.
		int[] mk = new int[trnModel.K];

		//get tables for cluster j from customers mjkl
		for (int j = 0; j < trnModel.L; j++) {
			for (int k = 0; k < trnModel.K; k++){
				mk[k]+=samp.randNumTable(trnModel.pi_0[k]*trnModel.alpha0,mjkl[j][k]);
			}
		}
		T = BasicMath.sum(mk);

		double[] sampleMkGamma = new double[trnModel.K+1];
		for (int k = 0; k < trnModel.K; k++){
			sampleMkGamma[k]=mk[k];
		}
		sampleMkGamma[trnModel.K] = trnModel.gamma;

		// (36) sample pi_0
		trnModel.pi_0 = samp.randDir(sampleMkGamma);				

	}

	public void updatePi_s(){

		double[][] alpha0TauMjk = new double[trnModel.L][trnModel.K + 1];

		for (int l = 0; l < trnModel.L; l++) {
			for (int k = 0; k < trnModel.K; k++) {
				alpha0TauMjk[l][k]=trnModel.pi_0[k]*trnModel.alpha0 + mjkl[l][k];
				if (!initialized) {
					alpha0TauMjk[l][k]++;
				}
			}
			alpha0TauMjk[l][trnModel.K] = trnModel.pi_0[trnModel.K]*trnModel.alpha0;
		}

		for (int l = 0; l < trnModel.L; l++) {
			//sample pi_s 			
			trnModel.pi_s[l] = samp.randDir(alpha0TauMjk[l]);
			//check if pi_s is 0 for some k
			boolean isZero = false;
			for (int k = 0; k < trnModel.K; k++) {
				if (trnModel.pi_s[l][k]==0) {
					isZero = true;
					break;
				}
			}
			//repair pi_s with 0 values, add a tiny bit and normalise
			if (isZero) {
				double sum = BasicMath.sum(trnModel.pi_s[l]) + trnModel.K * Double.MIN_VALUE;
				for (int k = 0; k < trnModel.K; k++) {
					trnModel.pi_s[l][k]=(trnModel.pi_s[l][k]+Double.MIN_VALUE)/sum;
				}
			}
		}
	}

	public void updatePi_d(){
		//not neccessary for sampling, ~Dir(n_j+alpha_q*sigma_q)
	}

	public void computePhi(){
		trnModel.theta = new double[trnModel.K][trnModel.V];
		for (int k = 0; k < trnModel.K; k++){
			for (int w = 0; w < trnModel.V; w++){
				int kk =  trnModel.kactive.get(k);
				trnModel.theta[k][w] =(trnModel.nkt[kk][w] + trnModel.beta) 
						/ (trnModel.nk[kk] + trnModel.V * trnModel.beta);
			}
		}
	}


	//p(r|l) = 1/(N+1) for base and neighbour clusters
	public void getPerplexity() {

		//sample test data starting after training data
		trainingLength =  (int) (trnModel.trainingSize*trnModel.M);

		//initialise variable for new topic counts
		nmkNew = new int[trnModel.M];
		//reset topic counts for training set to 0 (in case there is another perplexity calculated before)
		for (int m = trainingLength; m < trnModel.M; m++){
			for (int k = 0; k < trnModel.K; k++){
				int kk = trnModel.kactive.get(k);
				trnModel.nmk[m][kk]=0;
			}
		}

		//first, sample topics for the test documents, leaving all other parameters unchanged!
		//sample as often as training data
		initialized = false;
		for (int r = 0; r < trnModel.niters; r++){
			// for all z_i
			for (int m = trainingLength; m < trnModel.M; m++){			
				samplingTest(m);
			}// end for each document
			initialized = true;
		}

		double logLik = 0;
		int wordCount = 0;

		for (int m = trainingLength; m < trnModel.M; m++){

			int cluster = trnModel.q[m];
			int[] parents = new int[trnModel.qqN[cluster].length+1];
			parents[trnModel.qqN[cluster].length]=cluster;
			System.arraycopy(trnModel.qqN[cluster], 0, parents, 0, trnModel.qqN[cluster].length);
			//get weights on beginning or when there is a new topic
			//pi^s_l * eta_cjl
			double[] pisetaSum = null;
			//for every word in document m
			//calculate weights for different clusters
			//pi weights for parent distributions
			pisetaSum = new double[trnModel.K+1];
			for (int l=0;l<parents.length;l++) {
				int neighbour = parents[l];
				for (int k=0;k<trnModel.K+1;k++) {
					pisetaSum[k]+=trnModel.pi_s[neighbour][k]*trnModel.eta[cluster][l];
				}
			}


			wordCount += trnModel.length[m];

			//set no neighbours, cluster is just the base cluster
			//int[] neighbour = new int[0];
			double[] pi_d = new double[trnModel.K+1];

			//calculate pi_d

			//pi_d is the predicted topic distribution for a document given cluster j

			//calculate theta, the topic distribution, for each topic
			for (int k = 0; k < trnModel.K; k++){
				int kk = trnModel.kactive.get(k);
				pi_d[k] = (trnModel.Alpha*pisetaSum[k] + trnModel.nmk[m][kk]);
			}
			pi_d[trnModel.K]=trnModel.Alpha*pisetaSum[trnModel.K] + nmkNew[m];

			//normalise
			double pi_dSum = BasicMath.sum(pi_d);
			for (int k = 0; k < trnModel.K+1; k++){ 
				pi_d[k] /= pi_dSum;
			}

			//for each word
			for (int n = 0; n < trnModel.length[m]; n++){
				//get probability for each word
				int w = trnModel.data.docs[m].words[n];


				double sum = 0;
				for (int k = 0; k < trnModel.K+1; k++){
					if (k < trnModel.K) {
						sum += pi_d[k] * trnModel.theta[k][w];
					}
					else {
						//phi is 1/V for new topics
						sum += pi_d[k] * (1.0 / trnModel.V);
					}
				}

				logLik += Math.log(sum);

			}// end for each word

		}//end for each document

		perplexity = Math.exp(-(logLik / wordCount));
		Ksave = trnModel.K;
	}

}
