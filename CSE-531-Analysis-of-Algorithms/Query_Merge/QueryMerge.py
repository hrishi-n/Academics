f = open("input1.txt", "r")
#print(f.readline())
num_array=[]
operations=[]

f1 = open("myfile.txt", "x")

f_input = f.readline()
f_input_int = f_input.split()
print(f_input_int)
for j in range(int(f_input_int[0])):
    num_array.append([j+1])

print(num_array)
# for j in range(int(f_input_int[1])):
#     f_input = f.readline()
#     f_input_int = f_input.split()
#     if(f_input_int[0]=='M'):
#         f_input_int[1] = int(f_input_int[1])
#         f_input_int[2] = int(f_input_int[2])
#     else:
#         f_input_int[1] = int(f_input_int[1])
#     operations.append(f_input_int)

for j in range(int(f_input_int[1])):
    s_input = f.readline()
    s_input_int = s_input.split()
    if(s_input_int[0]=='M'):
        s_input_int[1] = int(s_input_int[1])
        s_input_int[2] = int(s_input_int[2])
    else:
        s_input_int[1] = int(s_input_int[1])
    operations.append(s_input_int)

print(operations)
def query(i):
    for el in num_array:
        for _el in el:
            if _el == i[1]:
                print(len(el))
                f1.write(str(len(el)))
                f1.write("\n")

def merge(i, arr):
    a=i[1]
    b=i[2]
    arr1=[]
    f_el=0
    s_el=0

    for el in arr:
        for _el in el:
            if _el == a:
                f_el = el
    for el in arr:
        for _el in el:
            if _el == b:
                s_el = el

    if(f_el!=s_el):
        arr.remove(f_el)
        arr.remove(s_el)
        arr1.append(f_el + s_el)
        for i in arr:
            arr1.append(i)
        arr = arr1


    return arr


for i in operations:
    if i[0]=='M':
        num_array=merge(i, num_array);
    elif i[0]=='Q':
        query(i)