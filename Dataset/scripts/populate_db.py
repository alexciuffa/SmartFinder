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

def random_date(start, end):
    """
    This function will return a random datetime between two datetime 
    objects.
    """
    delta = end - start
    int_delta = (delta.days * 24 * 60 * 60) + delta.seconds
    random_second = random.randrange(int_delta)
    return start + timedelta(seconds=random_second)

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
    log_file = open('log.txt', 'a')
    log_file.write("\n\n#### Iniciando ####")
    database = r"../database/SmartFinder.db"
    data_path = '../data/lfw/'
    model_path = '../../Model/tflite/facenet.tflite'

    conn = create_connection(database)

    tf_lite_model = tf.lite.Interpreter(model_path=model_path)
    tf_lite_model.allocate_tensors()

    start_time = time.time()

    count_person = 4519
    count = 5100
    for person_name in sorted(listdir(data_path))[5100:5800]:
        person_in_db = False
        person = {'name':person_name,
                   'birthday':datetime(1990, 1, 1),
                   'status':'Procurado',
                   'created_at':datetime.now(),
                   'updated_at':datetime.now()
        }

        for image_path in sorted(listdir(data_path+person_name))[:2]:
            embedding = get_image_embedding(data_path+person_name+'/'+image_path, tf_lite_model)
            if embedding.shape == (1, 512):
                if person_in_db == False:
                    person_id = insert_person(conn, person)
                    person_in_db = True
                    count_person += 1
                
                image = {'person_id':person_id,
                        'created_at':datetime.now(),
                        'path':image_path,
                        'embedding': np.array2string(embedding, precision=8, separator=',', suppress_small=True).replace('\n', '').replace(' ', '')
                        }

                insert_image(conn, image)

        
        if count_person % 100 == 0:
            print('Making partial db with {} people'.format(count_person))
            log_file.write('\nMaking partial db with {} people'.format(count_person))
            copyfile(
                src="../database/SmartFinder.db", 
                dst="../database/SmartFinder_{}.db".format(count_person)
                )

        count += 1

        log_file.write("\nPerson: {} --- {:0.2f} minutes ---".format(person_name, (time.time() - start_time)/60))
        print("{} Person: {} --- {:0.2f} minutes ---".format(count, person_name, (time.time() - start_time)/60))
    log_file.write("\nTotal time: {:0.2f} minutes".format((time.time() - start_time)/60))
    print("\nTotal time: {:0.2f} minutes".format((time.time() - start_time)/60))

    log_file.close()

    print("#####\nFinal count people: ", count_person)

if __name__ == '__main__':
    main()
