#from flask import Flask
from flask import Flask, render_template, json, request, session, redirect
from werkzeug import secure_filename
import csv
import cv2
import numpy as np
import sys
import random
from azure.storage.blob import BlockBlobService
import os
import sender

blob_account_name = 'cowimagestore'
blob_account_key = 'zgjzOJxE3t0+NeVSz6zuc4ZL4o++VPLeTtYDA4NEvR8Emkjt/YwdNZc+TUzr8eei7+SQnNSsrerDsRRq5ViTIg=='
my_container = 'images'
my_blob = 'CleanedCalvingPredData.csv'
app = Flask(__name__)

@app.route("/")
def main():
    return render_template('index.html')

@app.route("/main")
def return_main():
    return render_template('index.html')

@app.route('/uploadid')
def showupload():
	return render_template('upload.html')

@app.route('/uploader', methods = ['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        data = request.form['imgCSVData']
        t=data.split(",")
        temp = []
        for elem in t:
            temp.append(" "+str(elem))
        

        csv_file = open('image.csv', 'w')
        w = csv.writer(csv_file, delimiter = ',')
        w.writerow(temp)
        csv_file.close()
        result_data=sender.MLCall('image.csv')
        print(result_data)
        temp=result_data.pop().split(":")
        
        result_label=temp[1]
        
        return render_template('results.html', result=result_data,label=result_label)

@app.route('/uploadcow')
def showaddcow():
	return render_template('addcow.html')


@app.route('/addcow', methods = ['GET', 'POST'])
def showadd():
    if request.method == 'POST':
        f = request.form['imgCSVData']
        label=request.form['cow_id']
        print(request.form['cow_id'])
        filename='img_'+label+'_'+str(random.randint(1000000,9999999))+'.csv'
        temp = []
        temp.append(label)
        for elem in f:
            temp.append(" "+str(elem))

        csv_file = open(filename, 'w')
        w = csv.writer(csv_file, delimiter = ',')
        w.writerow(temp)
        csv_file.close()
        blobservice = BlockBlobService(account_name = blob_account_name,account_key = blob_account_key)
        
        local_file_name =filename
        full_path_to_file =os.path.join("", local_file_name)

        blobservice.create_blob_from_path(my_container, filename, full_path_to_file, if_none_match=True)
        return render_template('success.html')
	
