import csv
data = ["NYData","CCData","twitterData"]

for i in data:
    all_lines=[]
    file = open(i+"/output2/part-00000")
    all_lines = file.readlines()
    list_values=[]
    for j in all_lines:
        words, count = j.split("\t")
        count = int(count)
        list_values.append((words,count))
    list_values.sort(key=lambda x: x[1])
    list_values = list_values[::-1]
    with open(i + '/' + i + '_final_word_cooccurence_data.csv', mode='w') as employee_file:
        for k in range(50):
            print(list_values[k])
            employee_writer = csv.writer(employee_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            employee_writer.writerow([list_values[k][0],list_values[k][1]])

