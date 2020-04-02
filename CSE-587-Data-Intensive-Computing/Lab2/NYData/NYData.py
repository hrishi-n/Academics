import requests
import json
import time
import bs4
import datetime
import nltk
import re
import os
# nltk.download('stopwords')
# nltk.download('wordnet')
# nltk.download('averaged_perceptron_tagger')

from nltk.stem import PorterStemmer, WordNetLemmatizer
from nltk.corpus import stopwords
from nltk.tag import pos_tag

stop_words = set(stopwords.words('english'))

wnl = WordNetLemmatizer()
ps = PorterStemmer()

topics = ["sports","tennis","soccer","basketball","golf","baseball"]

key = "vmnd1Qqow98QAN3udjVu1PrinJA0AoNl"
secret = "P7SoSECyXoFRDQV6"


def cleanMe(html):
    soup = bs4.BeautifulSoup(html,'lxml').find_all('p')
    text = ""
    for item in soup:
        text += item.text.strip()+" "
    return text


for topic in topics:
    j = 0
    for i in range(0,10):
        time.sleep(7)
        url = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=" + topic + "&page="+str(i) + "&facet=true&begin_date=20190101&api-key=" + key

        response = requests.get(url)
        data = response.text
        parsed_data = json.loads(data)
        date_start = datetime.datetime.strptime('2019-01-01', '%Y-%m-%d')

        for item in parsed_data['response']['docs']:
            if(datetime.datetime.strptime(item['pub_date'][0:10],"%Y-%m-%d")>date_start):
                url = requests.get(item['web_url'])
                urlData=cleanMe(url.text.lower())
                f_line=re.sub(r"[^a-zA-Z0-9]+", ' ', urlData)
                result = ''.join([i for i in f_line.lower() if not i.isdigit()])
                final1 = ' '.join([i for i in result.split() if i not in stop_words])
                words = ' '.join([w for w in final1.split() if len(w) > 1])
                finalString = ""
                for word in words.split():
                    if pos_tag([word])[0][1] == 'NN':
                        word1 = word
                    elif wnl.lemmatize(word).endswith(('e','er','al','ally','ment','s')):
                        word1 = wnl.lemmatize(word)
                    else:
                        word1=ps.stem(word)
                    finalString+=word1+" "
                if not os.path.exists(topic):
                    os.makedirs(topic)
                NYDataFile = open(topic + '/' + topic + "_article_" + str("{0:0=2d}".format(j)) + ".txt", "w+")
                j=j+1
                NYDataFile.write(finalString)
                NYDataFile.close()
