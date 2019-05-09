Sync Drive to Azure

Data_compression.py: compresses an image file to a csv format
Run as: python data_compression.py filename  > output.csv

ML_Image_Recognition.py: calls the ML image recognition web services and returns the class probabilities and scored label for a given image in csv format
Run as: ML_Image_Recognition.py filename 

ML_Image_Validation.py: calls the ML image recognition web services and returns whether image is valid or not (yes or no)l for a given image in csv format
Run as: ML_Image_Validation.py filename 

Dataset_generation.py: creates the entire dataset for image recognition using BLOB Rest calls and return in a csv format
Run as: python dataset_generation.py > data.csv

Validation_data_generation.py: creates the entire dataset for image validation using BLOB Rest calls and return in a csv format
Run as: python Validation_data_generation.py  > data.csv

