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
import model_validation

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
        f = request.files['image_file']
        f.save('image.jpg')
        img = cv2.imread('image.jpg')
        img = cv2.resize(img,(56,56))
        temp = []
        for k in range(0,3):
            for i in range(0,56):
                for j in range(0,56):
                    temp.append(" "+str(img[i][j][k]))

        csv_file = open('image.csv', 'w')
        w = csv.writer(csv_file, delimiter = ',')
        w.writerow(temp)
        csv_file.close()
        check=model_validation.model_call('image.csv')
        check=str(check)
        #print(str(check))
        try: 
            check.index('Yes')


            result_data=sender.MLCall('image.csv')
            print(type(result_data))
            temp=result_data.pop().split(":")
            
            result_label=temp[1]
            print(result_label)
            #print(result_data)
            
            return render_template('results.html', result=result_data,label=result_label)
        
        except:
            return render_template('error.html')

@app.route('/uploadcow')
def showaddcow():
	return render_template('addcow.html')


@app.route('/addcow', methods = ['GET', 'POST'])
def showadd():
    if request.method == 'POST':
        f = request.files['cow_image']
        label=request.form['cow_id']
        print(request.form['cow_id'])
        filename='img_'+label+'_'+str(random.randint(1000000,9999999))+'.csv'
        f.save(secure_filename(f.filename))
        img = cv2.imread(secure_filename(f.filename))
        img = cv2.resize(img,(56,56))
        temp = []
        temp2=[]
        temp.append(label)
        for k in range(0,3):
            for i in range(0,56):
                for j in range(0,56):
                    temp.append(img[i][j][k])
                    temp2.append(img[i][j][k])
        csv_file = open(filename, 'w')
        w = csv.writer(csv_file, delimiter = ',')
        w.writerow(temp)
        csv_file.close()
        csv_file = open('check.csv', 'w')
        w = csv.writer(csv_file, delimiter = ',')
        w.writerow(temp2)
        csv_file.close()
        check=model_validation.model_call('check.csv')
        check=str(check)
        try:
            check.index('Yes')

            blobservice = BlockBlobService(account_name = blob_account_name,account_key = blob_account_key)
            
            local_file_name =filename
            full_path_to_file =os.path.join("", local_file_name)

            blobservice.create_blob_from_path(my_container, filename, full_path_to_file, if_none_match=True)
            return render_template('success.html')
        except:
            return render_template('error.html')

	