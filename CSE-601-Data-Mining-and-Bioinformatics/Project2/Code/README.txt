To run a file, please the open the desired file in a jupyter notebook. For example, for K-Means algorithm, open the kmeans.ipynb file in the jupyter notebook after initializing Anaconda Navigator.

Each algorithm is implemented in its own code file. The code files are named as kmeans.ipynb, DBScan.ipynb, GMM.ipynb, Spectral.ipynb and hierarchical.ipynb

Parameter that can be provided according to the algorithm are given below.


For the file kmeans.ipynb:

1. In cell 3, please provide the file name in variable 'file_name'.

2. In cell 4, please provide the list of gene ids that are to be used as initial centers in 'cluster_ids' (starting with 1) and please specify the maximum iterations to run the kmeans algorithm for in the variable labeled 'max_iterations'.

3. Run each cell and sequentially the Rand Index, Jaccard coefficient and the scatterplot will be displayed.


For the file DBScan.ipynb:

1. In cell 3, please provide the file name in variable 'file_name'.

2. In cell 5, please provide the 'epsilon' and 'min_points' in the variables provided.

3. Run each cell and sequentially the Rand Index, Jaccard coefficient and the scatterplot will be displayed.


For the file GMM.ipynb:

1. In cell 2, please provide the file name in variable 'file_name'.

2. In cell 4, please provide 'k' value for number of clusters, convergance threshold  value in 'cov_threshold', smoothing value in 'smoothing_val'.

3. Run each cell and sequentially the Rand Index, Jaccard coefficient and the scatterplot will be displayed.


For the file Spectral.ipynb:

1. In cell 5, please provide the file name in variable 'file_name' and sigma value in 'sigma'.

2. In cell 18, please provide the list of gene ids that are to be used as initial centers in variable 'cluster_ids' (starting with 1).

3. In cell 22, please specify the maximum iterations to run the kmeans algorithm for in the variable labeled 'max_iterations'.

4. Run each cell and sequentially the Rand Index, Jaccard coefficient and the scatterplot will be displayed.


For the file hierarchical.ipynb:

1. In cell 2, please provide the file name in variable 'file_name'.

2. In cell 2, please provide the number of required clusters (N) in variable 'num_clusters'(Integer).

3. Run each cell and sequentially the Rand Index, Jaccard coefficient and the scatterplot will be displayed.


