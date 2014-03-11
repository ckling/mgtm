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

import ckling.text.Text;
import java.lang.management.*;

public class LDA3 {

	
	public static void main(String args[]){

		if (args.length==0) {
			String name = "london";

			String dir ="/export/ckling/jgibblda/models/"+name+"/";

			File dirFile = new File(dir);
			if (!dirFile.exists()) {
				dir ="/home/c/work/jgibblda/models/"+name+"/";
				System.out.println(dir);
			}

			String arg = "-dir "+dir+" -dfile "+name+".txt " +
					"-est " +
					"-L 2000 " +
					"-beta 0.5 " +
					"-delta 5.0 " +
					"-savestep 5 " +
					"-twords 20 " +
					"-niters 200 "+
					"-runs 1 "+
					"-sampleHyper true";
			args = arg.split(" ");
		}

		Option option = new Option(args);
		
		int runs = option.runs;
		int[] K=new int[runs];
		double[] ppx = new double[runs];

		double[] timeSpent = new double[runs];
		for (int i=0;i<runs;i++) {

			//time in nanoseconds
			long t = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
			Estimator estimator = new Estimator();
			estimator.init(option);
			estimator.estimate();
			long tspent = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - t;
			System.out.println(tspent);
			timeSpent[i]=tspent;
			
			if (estimator.perplexity!=0.0) {
				K[i]=estimator.Ksave;
				ppx[i]=estimator.perplexity;
			}
			
			//if (estimator.Ksave == 7) break;

		}

		System.out.println("K, Perplexity:");
		if (K[0]!=0) {
			String content = "";
			for (int i=0;i<runs;i++) {
				content += K[i]+","+ppx[i]+"\n";
			}
			System.out.println(content);
			Text text = new Text();
			text.setText(content);
			String fileName = option.dir + "ppx3_"+option.L;
			text.write(fileName);
		}

		System.out.println("time:");
		String timeText ="";

		Text text = new Text(timeText);
		String fileName = option.dir + "time3_"+option.L;
		text.write(fileName);


	}
	 
	public static void main2(String args[]){

		if (args.length==0) {
			String name = "food";

			String dir ="/export/ckling/jgibblda/models/"+name+"/";

			File dirFile = new File(dir);
			if (!dirFile.exists()) {
				dir ="/home/c/work/jgibblda/models/"+name+"/";
				System.out.println(dir);
			}

			String arg = "-dir "+dir+" -dfile "+name+".txt " +
					"-est " +
					"-L 1000 " +
					"-beta 0.5 " +
					"-delta 10.0 " +
					"-savestep 5 " +
					"-twords 20 " +
					"-niters 200 "+
					"-runs 12 "+
					"-sampleHyper true";
			args = arg.split(" ");
		}

		Option option = new Option(args);
		
		int runs = option.runs;
		int[] K=new int[runs];
		double[] ppx = new double[runs];

		int[] regions = {50,250,750};
		double[] timeSpent = new double[runs];
		for (int i=0;i<runs;i++) {
			option.L = regions[i%regions.length];

			//time in nanoseconds
			long t = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
			Estimator estimator = new Estimator();
			estimator.init(option);
			estimator.estimate();
			long tspent = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - t;
			System.out.println(tspent);
			timeSpent[i]=tspent;
			
			estimator.getPerplexity();
			if (estimator.perplexity!=0.0) {
				K[i]=estimator.Ksave;
				ppx[i]=estimator.perplexity;
			}
			
			//if (estimator.Ksave == 7) break;

		}
		
		int rand = (int) Math.floor(Math.random() * 1000);
		System.out.println("K, Perplexity:");
		if (K[0]!=0) {
			String content = "";
			for (int i=0;i<runs;i++) {
				 content += regions[i%regions.length]+","+K[i]+","+ppx[i]+"\n";
				//content += K[i]+","+ppx[i]+"\n";
			}
			System.out.println(content);
			Text text = new Text();
			text.setText(content);
			String fileName = option.dir + "ppx3_"+rand;
			text.write(fileName);
		}

		System.out.println("time:");
		String timeText ="";
		for (int i=0;i<timeSpent.length;i++) {
			timeText += regions[i%regions.length] + " "+ timeSpent[i] + "\n";
			System.out.println(regions[i%regions.length] + " "+ timeSpent[i] + "\n");
		}
		
		Text text = new Text(timeText);
		String fileName = option.dir + "time3_"+rand;
		text.write(fileName);


	}
}
