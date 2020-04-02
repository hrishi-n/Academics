#!/usr/bin/env python

# """reducer.py"""
#
from operator import itemgetter
import sys
import operator


current_word = None
current_count = 0
word = None

wordCount={}

# input comes from STDIN
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()

    # parse the input we got from mapper.py
    word, count = line.split('\t', 1)

    # convert count (currently a string) to int
    try:
        count = int(count)
    except ValueError:
        # count was not a number, so silently
        # ignore/discard this line
        continue
    try:
        wordCount[word]+=count
    except:
        # count was not a number, so silently
        # ignore/discard this line
        wordCount[word] = count


wordCount = sorted(wordCount.items(), key = lambda kv:(kv[1],kv[0]), reverse=True)

i=0

for pair in wordCount:
    if i<10:
        print '%s\t%s' % (pair[0],pair[1])
        i=i+1
#
# # do not forget to output the last word if needed!
# if current_word == word:
#     print '%s\t%s' % (current_word, current_count)
