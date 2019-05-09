import cv2
import numpy as np
import sys


stre = sys.argv[1]
stre = str(stre)

img = cv2.imread(stre)
#print(img)
#print[img[0][0][0]]
img = cv2.resize(img,(56,56))
#print(img)
temp = []
for k in range(0,3):
    for i in range(0,56):
        for j in range(0,56):
            print(', '+str(img[i][j][k]),end='')


