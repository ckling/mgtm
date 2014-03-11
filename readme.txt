***************************
Geographical Topic Model Using multi-Dirichlet process mixtures
***************************

(C) Copyright 2012, Christoph Carl Kling

Based on "Knoceans" by Gregor Heinrich Gregor Heinrich (gregor :: arbylon : net)
and JGibbsLDA by Xuan-Hieu Phan and Cam-Tu Nguyen (ncamtu :: gmail : com)
published under GNU GPL.

Tartarus Snowball stemmer by Martin Porter and Richard Boulton published under 
BSD License (see http://www.opensource.org/licenses/bsd-license.html ), with Copyright 
(c) 2001, Dr Martin Porter, and (for the Java developments) Copyright (c) 2002, 
Richard Boulton. 

Java Delaunay Triangulation (JDT) by boaz88 :: gmail : com published under Apache License 2.0 
(http://www.apache.org/licenses/LICENSE-2.0)

MGTM is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by the Free 
Software Foundation; either version 3 of the License, or (at your option) 
any later version.

MGTM is distributed in the hope that it will be useful, but WITHOUT 
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 59 Temple
Place, Suite 330, Boston, MA 02111-1307 USA

***************************
Notes
***************************

This is the implementation of MGTM, a geographical topic model using multi-Dirichlet processes.

The source code of MGTM is found in /sourcecode/nhdp3/ 

The topic sampler is found in Estimator.java
The parameter samplers in RandomSamplers.java
The model options in Model.java
The MisesFisher clustering and the Delaunay triangulation call in MF_Delaunay.java

A list of variables used in the model is given in variables.html

Example call of MGTM for car dataset kindly provided by Zhijun Yin from the paper 
"Geographical Topic DIscovery and Comparison", WWW2011 (the data are encrypted, the password is available on request from MGTM::c-kling:de):

java -Xmx3000M -jar MGTD.jar -dir ./example/ -dfile car.txt -est -J 500 -beta 0.5 -gamma 1.0 -alpha0 1.0 -Alpha 1.0 -sampleHyper true -gammaa 1.0 -gammab 0.1 -alpha0a 1.0 -alpha0b 0.1 -Alphaa 0.1 -Alphab 0.1 -delta 10.0 -savestep 5 -twords 20 -niters 200

Data format:
The first line gives the number of documents in the file.
Every following line corresponds to a document, using the format: 
latitude longitude word1 word2 ... 

Example file format for three documents:

3
56.3 6.4 this is a test
46.2 5.2 words are separated by space
65.3 12.3 thats all you need 
