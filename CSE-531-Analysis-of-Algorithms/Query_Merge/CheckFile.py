import filecmp

print(filecmp.cmp("myfile.txt","output1.txt"))

f1=open("myfile.txt","r")
f2=open("output1.txt","r")

i=10000

for j in range(i):
    line1 = f1.readline()
    line2=f2.readline()
    if int(line1.replace(" ", "")) == int(line2.replace(" ", "")):
        print(str(j) + "SAME\n")
    else:
        print(str(j) + "NOT SAME\n")
        break

f1.close()
f2.close()