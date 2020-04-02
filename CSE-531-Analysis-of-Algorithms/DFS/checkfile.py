edges=[]
vertices_list=[]
graph_dict={}
with open("output3.txt") as f:
    for line in f:
        edges=line.split()
print(len(edges))

print(edges.index("4633"))

for i in range(len(edges)):
    vertices_list.append(edges[i])

print(len(vertices_list))