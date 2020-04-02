README




STEPS OF EXECUTION FOR APRIORI ALGORITHM:
--------------------------------------------------------------------------


Python Version: Python Version 3.6
Libraries used: Numpy, Itertools, Pandas


To run the .py file:
Execute in command line : python3 apriori.py
Enter Minimum Support value = [number]
Enter Minimum Confidence value = [number]
Enter the parameters for template 1(without quotes): 
Par1 = “BODY|HEAD|RULE”
Par2 = “ANY|NONE|1”
Par3 = “Item1,Item2, etc”
Enter the parameters for template 2(without quotes): 
Par1 = “BODY|HEAD|RULE”
Par2 = “Number”
Enter the parameters for template 3(without quotes):
Enter the first parameter as: Any combined templates using AND or OR
Enter rest of the parameters as prompted as it is a combination of Template 1 and Template 2


To run the .ipynb file:
Open Jupyter Notebook.
Import the file
Run every cell until the driver code.
Change the values of Minimum Support, Minimum Confidence.
Enter the parameters for template 1: 
Par1 = “BODY|HEAD|RULE”
Par2 = “ANY|NONE|1”
Par3 = “Item1,Item2, etc”
Enter the parameters for template 2: 
Par1 = “BODY|HEAD|RULE”
Par2 = “Number”
Enter parameter for template 3: Provide the query as a single string separated by “+”
Example: “2and2+head+1+body+2”


Note: Follow the gene value case notation.
