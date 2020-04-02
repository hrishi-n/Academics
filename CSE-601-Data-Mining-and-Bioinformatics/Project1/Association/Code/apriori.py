#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Sep 24 17:51:52 2019

@author: hrishi
"""

import numpy as np
from itertools import combinations
import pandas as pd

file_name = "associationruletestdata"

#Code for getting data from the file and converting it into a matrix for processing
def getData(file_name):
    file_handle = open(file_name + ".txt", "r")
    lines = file_handle.readlines()
    input_matrix = []
    disease_matrix = []
    for line in lines:
        all_list = line.strip().split("\t")
        updown_list = []
        disease=[]
        j = 1
        for expression in all_list:
            if j == len(all_list):
                disease.append(expression)
                updown_list.append(expression)
            else:
                updown_list.append("G" + str(j) + "_" + expression)
            j += 1
        input_matrix.append(updown_list)
        disease_matrix.append(disease)
    return input_matrix, disease_matrix


#This function returns a dictionary which has individual itemsets as keys and their count as follows
def getInitialData(input_matrix,support):
    count_dict = {}
    accepted_list = []
    for row in range(0, len(input_matrix)):
        for col in range(0, len(input_matrix[0])):
            if not input_matrix[row][col] in count_dict:
                count_dict[input_matrix[row][col]] = 1
            else:
                count_dict[input_matrix[row][col]] += 1
    for key in count_dict.keys():
        accepted_list.append(key)
    return count_dict, accepted_list

#This function returns the frequent itemsets that have the support value greater than or equal to minimum support value
def findFreqItemSets(globalSupDict,input_matrix, accepted_list, support):
    countAllItemsets = 0
    globalSupDict={}
    final_accepted_list = []
    for i in range(1, len(input_matrix[0])-1):
        count_dict = {}
        combinations_list = list(combinations(set(accepted_list), i))
        for row in range(len(input_matrix)):
            for tuples in combinations_list:
                if(all(elm in input_matrix[row] for elm in list(tuples))):
                    if not tuples in count_dict:
                        count_dict[tuples] = 1
                    else:
                        count_dict[tuples] += 1
        count=0;
        accepted_list=[]
        for key in count_dict:
            if (count_dict.get(key)/(len(input_matrix))) >= (support/100.0):
                count+=1
                globalSupDict[tuple(sorted(key))] = count_dict[key]#/(len(input_matrix))
                if(key not in final_accepted_list):
                    final_accepted_list.append(list(key))
                for j in key:
                    accepted_list.append(j)
        countAllItemsets+=count
        if(len(accepted_list)<1):
            print("Number of all lengths frequent itemsets: " + str(countAllItemsets))
            return final_accepted_list, globalSupDict
        print("Number of length-"+str(i)+" frequent itemsets: " + str(count))
        

#This function generates association rules based on the frequent itemsets dictionary and their support and confidence values
def generateRules(globalSupDict, min_conf):
    
    headlst = []
    bodylst = []
    rules = []
    keys = globalSupDict.keys()
    count = 0
   
    for key in keys:
        for s1 in range(1, len(key)):
            comb = set(combinations(key,s1))
            for item in comb:
                confidence = globalSupDict.get(key)/globalSupDict.get(item)
                if confidence >= min_conf/100.0:
                    head = list(key)
                    head1 = []
                    for x in head:
                        if(x not in item):
                            head1.append(x)
                    headlst.append(list(head1))
                    bodylst.append(list(item))
                    count = count+1      
                    rules.append(str(list(head1))+"-->"+str(list(item)))
    
    return headlst, bodylst, rules, count


#This function returns the result for the query in Template1 
def template1(p1,p2,p3): 
    count = 0
    outputList = []
    allfreqs1 = []
    if(p1.upper() == "BODY"):
        if(p2.upper()=="ANY"):
            for i in range(0,len(bodies)):
                if(set(p3).issubset(set(bodies[i]))):
                    outputList.append(i)
                    allfreqs1.append(bodies[i])
                    count = count+1
        elif(p2.upper()=="NONE"):
            for i in range(0,len(bodies)):
                if(len(set(p3).intersection(set(bodies[i])))==0):
                    outputList.append(i)
                    allfreqs1.append(bodies[i])
                    count = count+1
        elif(p2=="1"):
            for i in range(0,len(bodies)):
                if(len(set(p3).intersection(set(bodies[i])))==1):
                    outputList.append(i)
                    allfreqs1.append(bodies[i])
                    count = count+1
            
    elif(p1.upper() == "HEAD"):
        if(p2.upper()=="ANY"):
            for i in range(0,len(heads)):
                if(set(p3).issubset(set(heads[i]))):
                    outputList.append(i)
                    allfreqs1.append(heads[i])
                    count = count+1
        elif(p2.upper()=="NONE"):
            for i in range(0,len(heads)):
                if(len(set(p3).intersection(set(heads[i])))==0):
                    outputList.append(i)
                    allfreqs1.append(heads[i])
                    count = count+1
        elif(p2=="1"):
            for i in range(0,len(heads)):
                if(len(set(p3).intersection(set(heads[i])))==1):
                    allfreqs1.append(heads[i])
                    outputList.append(i)
                    count = count+1
            
    elif(p1.upper() == "RULE"):
        if(p2.upper()=="ANY"):
            for i in range(len(bodies)):
                if(len(set(p3).intersection(set(bodies[i]))) == 1 or len(set(p3).intersection(set(heads[i])))) == 1:
                    #outputList.append(i)
                    allfreqs1.append(str(heads[i])+"-->"+str(bodies[i]))
                    count = count+1

        elif(p2.upper()=="NONE"):
            for i in range(len(bodies)):
                if(len(set(p3).intersection(set(bodies[i])))) == 0:
                    if(len(set(p3).intersection(set(heads[i])))) == 0:
                        #allfreqs.append(heads[i])
                        allfreqs1.append(str(heads[i])+"-->"+str(bodies[i]))
                        count = count+1
        elif(p2=="1"):
            for i in range(len(bodies)):
                if len(set(p3).intersection(set(bodies[i]))) == 1 or len(set(p3).intersection(set(heads[i]))) == 1:
                    if not(len(set(p3).intersection(set(bodies[i]))) == 1 and len(set(p3).intersection(set(heads[i])))) == 1:
                        allfreqs1.append(str(heads[i])+"-->"+str(bodies[i]))
                        count = count +1
    return allfreqs1, outputList, count

#This function returns the result for the query in Template2
def template2(par1,par2):
    count=0
    par2 = int(par2)
    outputList = []
    allfreqs2 = []
    if(par1.upper()=="HEAD"):
        for i in range(0, len(heads)):
            if(len(heads[i])>=par2):
                outputList.append(i)
                allfreqs2.append(heads[i])
                count = count+1
    elif(par1.upper()=="BODY"):
        for i in range(0,len(bodies)):
            if(len(bodies[i])>=par2):
                outputList.append(i)
                allfreqs2.append(bodies[i])
                count = count+1
    elif(par1.upper() == "RULE"):
        for i in range(len(heads)):
            if(len(heads[i])+len(bodies[i])>=par2):
                allfreqs2.append(str(heads[i])+"-->"+str(bodies[i]))
                count = count+1
        
    return allfreqs2, outputList, count

#This function returns the result for the query in Template3
def template3(par1):
    parameters = par1.split("+")
    overallList = []
    op = parameters[0]
    final_list=[]
    
    if(op[1:-1].lower()=="or"):
        
        if(op[0]=="1"):
            allfre1, output1, count=template1(parameters[1],parameters[2],parameters[3].split(","))

        elif(op[0]=="2"):
            allfre1, output1, count=template2(parameters[1],parameters[2])
            
        overallList.append(output1)
        
        if(op[-1]=="1"):
            allfre1, output1, count=template1(parameters[-3:][0],parameters[-3:][1],parameters[-3:][2].split(","))
        elif(op[-1]=="2"):
            allfre1, output1, count=template2(parameters[-2:][0],parameters[-2:][1])
            
        overallList.append(output1)
        
        final_list = list(set(overallList[0]).union(set(overallList[1])))
    
    elif(op[1:-1].lower()=="and"):
        
        if(op[0]=="1"):
            allfre1, output1, count=template1(parameters[1],parameters[2],parameters[3].split(","))
        elif(op[0]=="2"):
            allfre1, output1, count=template2(parameters[1],parameters[2])
        
        overallList.append(output1)
        
        if(op[-1]=="1"):
            allfre1, output1, count=template1(parameters[-3:][0],parameters[-3:][1],parameters[-3:][2].split(","))
        elif(op[-1]=="2"):
            allfre1, output1, count=template2(parameters[-2:][0],parameters[-2:][1])
        
        overallList.append(output1)
        
        final_list = [value for value in overallList[0] if value in overallList[1]] 
    count = len(final_list)
    return final_list, count

#Driver Code - Execution starts here
#support = 30
try:
    support = float(input("Enter minimum support value: "))
    #min_conf =70
    min_conf = float(input("Enter minimum confidence value: "))


    #Find all the frequent itemsets in the data
    input_matrix, disease_matrix = getData(file_name)
    count_Dict, accepted_list = getInitialData(input_matrix, support)
    accepted_list, globalSupDict=findFreqItemSets(count_Dict,input_matrix, accepted_list, support)
    
    #Generate rules for the frequent itemsets
    heads, bodies, rules, numRules = generateRules(globalSupDict, min_conf)
    print("Number of rules generated: "+str(numRules))

# print("Rules:")
# for i in rules:
#     print(i)

#Template1 query code
#par1 = "BODY"
#par2 = "NONE"
#par3 = "G59_Up"
#allfreqitems1, outputList1, count1=template1(par1,par2,par3.split(","))
#print((str(count1))+" rules found!")
# print("Itemsets:")
# for i in allfreqitems:
#     print(i)

#Template2 query code
#par1 = "HEAD"
#par2 = "2"
#allfreqitems2, outputList2, count2=template2(par1,par2)
#print((str(count2))+" rules found!")

#Template3 query code
#par1 = "1or1+body+any+G10_Down+head+1+G59_Up"
#outputList3, count3=template3(par1)
#print((str(count3))+" rules found!")




    #Takes input from user:
    i=0
    while(i==0):
        choice = input("Enter template choice 1, 2, 3: ")
        final_count = 0
        try:
            if(choice == "1"):
                par1 = input("Enter first parameter: ")
                par2 = input("Enter second parameter: ")
                par3 = input("Enter third paramter: ")
                allfreq1, outputList2, count=template1(par1,par2,par3.split(","))
                final_count = count
                print((str(final_count))+" rules found!")
            
            elif(choice == "2"):
                par1 = input("Enter first parameter: ")
                par2 = input("Enter second parameter: ")
                allfreq2, outputList2, count=template2(par1,par2)
                final_count = count
                print((str(final_count))+" rules found!")
                
            elif(choice == "3"):
                count = 0
                query = ""
                par1 = input("Enter first parameter: ")
                if(par1[0]=="1"):
                    print("Enter parameters for 1st template: ")
                    par2 = input("Enter first parameter: ")
                    par3 = input("Enter second parameter: ")
                    par4 = input("Enter third paramter: ")
                    query += par1+"+"+par2+"+"+par3+"+"+par4
                elif(par1[0]=="2"):
                    print("Enter parameters for 2nd template: ")
                    par2 = input("Enter first parameter: ")
                    par3 = input("Enter second parameter: ")
                    query += par1+"+"+par2+"+"+par3
                if(par1[-1] == "1"):
                    print("Enter parameters for 1st template: ")
                    par1 = input("Enter first parameter: ")
                    par2 = input("Enter second parameter: ")
                    par3 = input("Enter third paramter: ")
                    query += "+"+par1+"+"+par2+"+"+par3
                elif(par1[-1]=="2"):
                    print("Enter parameters for 2nd template: ")
                    par1 = input("Enter first parameter: ")
                    par2 = input("Enter second parameter: ")
                    query += "+"+par1+"+"+par2
                outputList2, count=template3(query)
                final_count = count
                print((str(final_count))+" rules found!")
            else:
                print("Program Exiting!")  
                break
        except Exception as e:
            print(e)
            print("Invalid template choice. Please run the program again.")
        
except:
  print("Invalid values. Program exiting. Please run the program again")      
        
        
