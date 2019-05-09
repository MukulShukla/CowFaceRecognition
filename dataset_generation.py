# The script MUST contain a function named azureml_main
# which is the entry point for this module.

# imports up here can be used to
import pandas as pd
import requests
import xml.etree.ElementTree as ET

from copy import copy

def dictify(r,root=True):
    if root:
        return {r.tag : dictify(r, False)}
    d=copy(r.attrib)
    if r.text:
        d["_text"]=r.text
    for x in r.findall("./*"):
        if x.tag not in d:
            d[x.tag]=[]
        d[x.tag].append(dictify(x,False))
    return d
# The entry point function can contain up to two input arguments:
#   Param<dataframe1>: a pandas.DataFrame
#   Param<dataframe2>: a pandas.DataFrame

temp = []
#making headers
print("label,",end='')
for a in range(9408):
    print(',f_'+str(a),end='')

temp2 = []
temp2.append(temp)
#completed headers
print('')
#getting a list of csv files
params = (
    ('restype', 'container'),
    ('comp', 'list'),
)
response = requests.get('https://cowimagestore.blob.core.windows.net/images', params=params)
root = ET.fromstring(response.text)
dicty=dictify(root)
for i in range(len(dicty['EnumerationResults']['Blobs'][0]['Blob'])):
    #print(dicty['EnumerationResults']['Blobs'][0]['Blob'][i]['Name'][0]['_text'])
    response = requests.get('https://cowimagestore.blob.core.windows.net/images/'+str(dicty['EnumerationResults']['Blobs'][0]['Blob'][i]['Name'][0]['_text']))
    temp1=[]
    str1 = str(response.text)
    print(str1)

