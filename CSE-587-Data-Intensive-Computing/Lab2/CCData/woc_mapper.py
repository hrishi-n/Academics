#!/usr/bin/env python
"""mapper.py"""
import sys


topwordlist = ["game","year","one","said","play","season","make","team","first","time"]

for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()
    # split the line into words
    words = line.split()

    for word in topwordlist:
        for i in xrange(0, len(words) - 1):
            if (words[i]==word):
                print "%s|%s\t%s" % (words[i], words[i+1], 1)


