import numpy as np
import tensorflow as tf
from PIL import Image
import mtcnn
from datetime import datetime, timedelta

from os import listdir, environ
from os.path import isfile, join

import time
import random

from shutil import copyfile

from insert_db import create_connection, insert_person, insert_image, insert_request, insert_match
tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)

def get_image_embedding(image_path, tf_lite_model):
    # Open image
    image = Image.open(image_path).convert('RGB')
    pixels = np.asarray(image)
    # detect faces in the image
    detector = mtcnn.MTCNN()
    results = detector.detect_faces(pixels)
    # extract the bounding box from the first face
    if len(results) != 1:
        #print("+ de 1 pessoa: ",image_path)
        return np.array([])
    x1, y1, width, height = results[0]['box']
    x1, y1 = abs(x1), abs(y1)
    x2, y2 = x1 + width, y1 + height
    # extract the face
    face = pixels[y1:y2, x1:x2]
    # resize pixels to the model size
    image = Image.fromarray(face)
    image = image.resize((160, 160))
    face_array = np.asarray(image)
    face_array_norm = face_array /255
    
    input_details = tf_lite_model.get_input_details()
    output_details = tf_lite_model.get_output_details()

    tf_lite_model.set_tensor(input_details[0]['index'], face_array_norm.reshape(1,160,160,3).astype(np.float32))
    tf_lite_model.invoke()

    return tf_lite_model.get_tensor(output_details[0]['index'])

def main():
    database = r"../database/SmartFinder.db"
    data_path = '../data/lfw/'
    model_path = '../../Model/tflite/facenet.tflite'

    conn = create_connection(database)

    tf_lite_model = tf.lite.Interpreter(model_path=model_path)
    tf_lite_model.allocate_tensors()

    person_name = 'Alexandre_Ciuffatelli'
    person_id = 149
    image_name = person_name + '_0002.jpeg'
    
    embedding = get_image_embedding(data_path+person_name+'/'+image_name, tf_lite_model)
    if embedding.shape == (1, 512):
        print("Inserindo...")
        
        image = {'person_id':person_id,
                'created_at':datetime.now(),
                'path':image_name,
                'embedding': np.array2string(embedding, precision=8, separator=',', suppress_small=True).replace('\n', '').replace(' ', '')
                }

        insert_image(conn, image)
    else:
        print("Imagem com problema")

    print("Pronto")

if __name__ == '__main__':
    main()
