import os
import cv2
import numpy as np
import sys
import cv2
import numpy as np
import sys


print("label, ",end='')
for a in range(9408):
    print('f_'+str(a)+', ',end='')
print('')
for filename in os.listdir(os.getcwd()):
    #print(filename)

    stre = filename
    stre = str(stre)

    img = cv2.imread(stre)
    img = cv2.resize(img,(56,56))

    temp = []
    print('No, ',end='')
    for k in range(0,3):
        for i in range(0,56):
            for j in range(0,56):
                if i==55 and j==55 and k==2:
                   print(str(img[i][j][k]),end='')
                else:
                    print(str(img[i][j][k])+', ',end='')
    print('')
