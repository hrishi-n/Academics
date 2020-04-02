input_string = raw_input()

len_input = len(input_string)+1

lps = ""

LDP_Table=[[0] * (len_input) for i in range(len_input)]

for i in range(len_input):
    for j in range(len_input):
        if i == 0:
            LDP_Table[i][j] = 0

        elif j==0:
            LDP_Table[i][j] = 0

        elif input_string[i - 1] == (input_string[::-1])[j - 1]:
            LDP_Table[i][j] = 1+LDP_Table[i-1][j-1]

        else:

            if(LDP_Table[i-1][j]>LDP_Table[i][j-1]):
                LDP_Table[i][j] =LDP_Table[i-1][j]
            else:
                LDP_Table[i][j] = LDP_Table[i][j - 1]


#ndex for input
i = len_input-1

#ndex for reverse input
j = len_input-1

while j > 0 and i > 0:

    if (input_string[i - 1] == (input_string[::-1])[j - 1]):
        i =i- 1
        j = j- 1
        lps += input_string[i]

    elif LDP_Table[i - 1][j] < LDP_Table[i][j - 1]:
        j -= 1

    else:
        i -= 1

#print(LDP_Table[len_input-1][len_input-1])
print(len(lps))
print(lps)
