ve = input()

list_input= ve.split()

num_vertices = int(list_input[0])
num_edges = int(list_input[1])


edges=[]
for i in range(num_edges):
    edges.append(input())


vertices_list = []
vertices_visiting=[]
vertices_visited=[]
graph_dict = {}

for i in range(len(edges)):
    vertex_list = list(edges[i].replace(" ",""))
    v1 = int(vertex_list[0])
    v2 = int(vertex_list[1])
    if v1 not in vertices_list:
        vertices_list.append(v1)
    if v2 not in vertices_list:
        vertices_list.append(v2)
    if v1 in graph_dict:
        graph_dict[v1].append(v2)
    else:
        graph_dict[v1] = []
        graph_dict[v1].append(v2)


def checkforcycle(v1, vertices_list, vertices_visiting, vertices_visited):


    vertices_visiting.append(v1)

    neighbors = graph_dict.get(v1)
    vertices_list.remove(v1)

    if neighbors is not None:

        for neighbor in neighbors:

            if neighbor in vertices_visiting:
                vertices_visiting.append(neighbor)
                return True


            if neighbor in vertices_visited:
                continue

            if checkforcycle(neighbor, vertices_list, vertices_visiting, vertices_visited):
                return True

        vertices_visited.append(v1)

        vertices_visiting.remove(v1)

        return False
    else:

        vertices_visited.append(v1)

        vertices_visiting.remove(v1)



for i in range(len(vertices_list)):
    v1 = vertices_list[0]
    cycleExists = checkforcycle( v1, vertices_list, vertices_visiting, vertices_visited)

    if cycleExists:
        last = vertices_visiting[-1]
        b = vertices_visiting.index(last)
        a = str(len(vertices_visiting[b:len(vertices_visiting) - 1]))
        a += " " + " ".join(str(x) for x in vertices_visiting[b:len(vertices_visiting) - 1])
        print(a)

    else:
        print(0)

    break
