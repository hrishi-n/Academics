import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.manifold import TSNE

features = []
diseases = []

filename = input("Enter file name: ")

with open(filename,'r') as f:
    for line in f:
        disease = line.split('\t')[-1].rstrip()
        values = line.split('\t')[:-1]
        values = [float(i) for i in values]
        diseases.append(disease)
        features.append(values)
features = pd.DataFrame(features)
diseases = pd.DataFrame(diseases)

mean = np.mean(features, axis=0)
X = features-mean

def plotData(data, algo, filename):
    data.columns = ['PC1','PC2','Legend']
    plt.figure(figsize=(12,9))
    sns.scatterplot(data.PC1, y=data.PC2, hue=data.Legend, data=data, s=50, alpha=1.0)
    plt.legend(loc='best', handletextpad = 0.1, labelspacing = 0.5, fontsize = 'large', title = None)
    plt.title(algo.upper()+" for "+filename+" file")
    file = filename.split(".")[0]
    plt.savefig(algo.lower()+"_"+file+".png")
    

#TSNE Algorithm
tsne_data = TSNE(n_components=2).fit_transform(features)
tsne_data = pd.DataFrame(np.append(tsne_data,diseases,1))   
plotData(tsne_data, "tSNE",filename)
    
#SVD Algorithm
U, Sigma, Vh = np.linalg.svd(X)
svd_data = pd.DataFrame(np.append(U[:,0:2],diseases,1))
plotData(svd_data, "SVD",filename)

#PCA Algorithm
C =  (1 / (X.shape[0]-1))*X.T.dot(X)
eigen_vecs = np.linalg.eig(C)[1]
fevecs = features.dot(eigen_vecs[:,0:2])
dfObj = pd.DataFrame(fevecs.astype(np.float64))
di = pd.DataFrame(diseases)
PCA_data = pd.DataFrame(np.append(dfObj,di,1))
plotData(PCA_data, "PCA",filename)
