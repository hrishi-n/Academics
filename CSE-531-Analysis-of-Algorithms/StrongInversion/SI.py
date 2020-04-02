def mergeSort_Inv(leftSubArray, rightSubArray):
    resutList = []
    numInv = 0
    i = 0
    j = 0

    while(i < len(leftSubArray) and j < len(rightSubArray)):
        if(leftSubArray[i] > 2*rightSubArray[j]):
            numInv += len(leftSubArray)-i
            j += 1
        else:
            i += 1

    i = 0
    j = 0
    while(i < len(leftSubArray) and j < len(rightSubArray)):
        if(leftSubArray[i] < rightSubArray[j]):
            resutList.append(leftSubArray[i])
            i += 1
        else:
            resutList.append(rightSubArray[j])
            j += 1

    resutList+=leftSubArray[i:]

    resutList+=rightSubArray[j:]
    return resutList, numInv


def ehc_mergeSort(numList):
    if len(numList) > 1:

        mid_index = len(numList) // 2

        leftSubArray, m1 = ehc_mergeSort(numList[:mid_index])
        rightSubArray, m2 = ehc_mergeSort(numList[mid_index:])
        resutList, m3 = mergeSort_Inv(leftSubArray, rightSubArray)

        return resutList, (m1+m2+m3)

    else:
        return numList, 0


def strong_inversions(numList):
    finalList, finalCount = ehc_mergeSort(numList)
    return finalCount


num_array=[]
f = input()

for i in range(int(f)):
    f = input()
    num_array.append(int(f))

print(strong_inversions(num_array))