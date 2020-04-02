import requests
import json
import gzip
import os
import StringIO
from bs4 import BeautifulSoup
import bs4
import re
import sys
from nltk.stem import PorterStemmer, WordNetLemmatizer
from nltk.corpus import stopwords
from nltk.tag import pos_tag
import time

stop_words = set(stopwords.words('english'))

wnl = WordNetLemmatizer()
ps = PorterStemmer()


topics = ["sports"]


index_list = ["2019-04","2019-09","2019-13"]

record_list = []

def cleanMe(html):
    soup = bs4.BeautifulSoup(html,"html.parser").find_all('p')
    text = ""
    for item in soup:
        text += item.text.strip()+" "
    return text

for topic in topics:
    for index in index_list:

        url = "http://index.commoncrawl.org/CC-MAIN-" + index + "-index?url=espn.com" + "&matchType=domain&output=json"

        response = requests.get(url)

        if response.status_code == 200:

            records = response.content.splitlines()

            for record in records:
                record_list.append(json.loads(record))

    print(len(record_list))

    link_list = []
    count = 0
    for record in record_list:
        if len(link_list) > 120:
            break
        offset, length = int(record['offset']), int(record['length'])
        offset_end = offset + length - 1

        prefix = ' https://commoncrawl.s3.amazonaws.com/'
        resp = requests.get(prefix + record['filename'], headers={'Range': 'bytes={}-{}'.format(offset, offset_end)})

        raw_data = StringIO.StringIO(resp.content)
        f = gzip.GzipFile(fileobj=raw_data)


        data = f.read()
        response = ""

        if len(data):
            try:
                warc, header, response = data.strip().split('\r\n\r\n', 2)
            except:
                pass

        parser = BeautifulSoup(response, "html.parser")
        links = parser.find_all("a")
        if links:
            for link in links:
                href = link.attrs.get("href")
                if href is not None:
                    try:
                        href = str(href.encode('utf-8').decode('unicode-escape'))
                    except:
                        continue
                    if href.startswith("http://www.espn.com/") and "video" not in href:
                        if href not in link_list:
                            link_list.append(href)

                            print(len(link_list))

    file= open("CCData/"+topic+"/"+topic+"_links.txt","w+")
    for link in link_list:
        file.write(link.encode("utf-8"))
        file.write("\n")

topics = ["tennis","soccer","basketball","golf","baseball"]
topics = ["sports"]

for topic in topics:
    j=0
    file = open("CCData/"+topic+"/"+topic+"_links.txt")
    for k in range(100):
        line = file.readline()
        if not line.startswith("http://www.espn.com"):
            line = "http://www.espn.com"+line
        time.sleep(5)
        try:
            url = requests.get(line)
        except:
            continue
        urlData = cleanMe(url.text.lower())
        f_line = re.sub(r"[^a-zA-Z0-9]+", ' ', urlData)
        result = ''.join([i for i in f_line.lower() if not i.isdigit()])
        final1 = ' '.join([i for i in result.split() if i not in stop_words])
        words = ' '.join([w for w in final1.split() if len(w) > 1])
        finalString = ""
        for word in words.split():
            if pos_tag([word])[0][1] == 'NN':
                word1 = word
            elif wnl.lemmatize(word).endswith(('e', 'er', 'al', 'ally', 'ment', 's')):
                word1 = wnl.lemmatize(word)
            else:
                word1 = ps.stem(word)
            finalString += word1 + " "

        if finalString>0:
            CCDataFile = open('CCData/' + topic + '/' + topic + "_article_" + str("{0:0=2d}".format(j)) + ".txt", "w+")
            j = j + 1
            CCDataFile.write(finalString)
            CCDataFile.close()

topics = ["tennis","soccer","basketball","golf","baseball","sports"]

for topic in topics:

    #file = open("CCData/"+topic+"/"+topic+"_links.txt")
    files = os.listdir("CCData/"+topic+"/")
    list_empty_files = []
    for file in files:
        if os.stat("CCData/"+topic+"/"+file).st_size == 0:
            list_empty_files.append(file)

    #list_links = reversed(list())
    list_links = open("CCData/"+topic+"/"+topic+"_links.txt").readlines()
 #   print(type(list_links))
    #list_links = list(list_links)
    list_links1 = list_links[::-1]
#    print(type(list_links1))
    while len(list_empty_files)>0:
        print(list_empty_files)
        line = list_links1[0]
        if not line.startswith("http://www.espn.com"):
            line = "http://www.espn.com"+line
        time.sleep(5)
        try:
            url = requests.get(line)
        except:
            continue
        urlData = cleanMe(url.text.lower())
        f_line = re.sub(r"[^a-zA-Z0-9]+", ' ', urlData)
        result = ''.join([i for i in f_line.lower() if not i.isdigit()])
        final1 = ' '.join([i for i in result.split() if i not in stop_words])
        words = ' '.join([w for w in final1.split() if len(w) > 1])
        finalString = ""
        for word in words.split():
            if pos_tag([word])[0][1] == 'NN':
                word1 = word
            elif wnl.lemmatize(word).endswith(('e', 'er', 'al', 'ally', 'ment', 's')):
                word1 = wnl.lemmatize(word)
            else:
                word1 = ps.stem(word)
            finalString += word1 + " "

        if finalString is not "":
            CCDataFile = open("CCData/"+topic+"/"+list_empty_files[0],"w")
            CCDataFile.write(finalString)
            CCDataFile.close()
            list_empty_files.remove(list_empty_files[0])
            list_links1.remove(list_links1[0])
        else:
            list_links1.remove(list_links1[0])