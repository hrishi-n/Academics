import re
import nltk
#nltk.download('stopwords')
from nltk.stem import PorterStemmer, WordNetLemmatizer
from nltk.corpus import stopwords
import os

nltk.download('stopwords')
nltk.download('wordnet')
nltk.download('averaged_perceptron_tagger')

stop_words = set(stopwords.words('english'))

wnl = WordNetLemmatizer()
ps = PorterStemmer()
from nltk.tag import pos_tag

stop_words = set(stopwords.words('english'))
topics = ["sports","tennis","soccer","basketball","golf","baseball"]

import csv

print(os.getcwd())

for topic in topics:
    dataWriter = open("twitterData/"+topic+"/"+topic+"_tweets.txt", "w+")
    dataWriter1 = open("twitterData/alldata/" + topic + "_tweets.txt", "w+")
    with open("twitterData/"+topic+'/'+topic+'_tweets_text.csv') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')

        for row in csv_reader:
            finalString = ""
            row = " ".join(row)
            text = re.sub(r'(https|http)?:\/\/(\w|\.|\/|\?|\=|\&|\%)*\b', '', row, flags=re.MULTILINE)
            f_line = re.sub(r"[^a-zA-Z0-9]+", ' ', text)
            result = ''.join([i for i in f_line if not i.isdigit()])
            final1 = ' '.join([i for i in result.lower().split() if i not in stop_words])
            words = ' '.join([w for w in final1.lower().split() if len(w) > 1])

            for word in words.split():
                if pos_tag([word])[0][1] == 'NN':
                    word1 = word
                elif wnl.lemmatize(word).endswith(('e', 'er', 'al', 'ally', 'ment', 's')):
                    word1 = wnl.lemmatize(word)
                else:
                    word1 = ps.stem(word)
                finalString += word1 + " "
            print(finalString)
            dataWriter.write(finalString)
            dataWriter.write("\n")
            dataWriter1.write(finalString)
            dataWriter1.write("\n")
        dataWriter.close()
        dataWriter1.close()