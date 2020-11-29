import time
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime, timedelta
#from flask_cors import CORS

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///../../Dataset/database/SmartFinder.db' #'sqlite:///test.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

#cors = CORS(app, resources={r"/*": {"origins": "*"}})

      #############
########  Utils  ########
      #############

import numpy as np
def get_distance(embedding_1, embedding_2):
    return np.linalg.norm(np.array(eval(embedding_1)) - np.array(eval(embedding_2)))

def is_new_match(json_match, new_name):
    for match in json_match:
        if match['person_name'] == new_name:
            return False
    return True

      #############
########  Models ########
      #############

class Person(db.Model):
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(200), nullable=False)
    birthday = db.Column(db.DateTime, nullable=False)
    status = db.Column(db.String(200), nullable=False)
    created_at = db.Column(db.DateTime, nullable=True)
    updated_at = db.Column(db.DateTime, nullable=True)
    images = db.relationship('Image', uselist=True, backref='person')
    matches = db.relationship('Match', uselist=True, backref='person')

    def __repr__(self):
        return '<Person {}: {} - status: {}>'.format(self.id, self.name, self.status)
# Person(name='Alexandre Ciuffatelli', birthday=datetime(1996, 7, 8), status='Procurado', created_at=datetime(2020, 10, 28), updated_at=datetime(2020, 10, 28))

class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    person_id = db.Column(db.Integer, db.ForeignKey('person.id'), nullable=True)
    path = db.Column(db.String(200), nullable=False)
    embedding = db.Column(db.String(500), nullable=False)
    created_at = db.Column(db.DateTime, nullable=True)
    matches = db.relationship('Match', uselist=True, backref='image')

    def __repr__(self):
        return '<Image {}: {}>'.format(self.id, self.path)

class Request(db.Model):
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    lat = db.Column(db.Integer, nullable=False)
    long = db.Column(db.Integer, nullable=False)
    embedding = db.Column(db.String(500), nullable=False)
    created_at = db.Column(db.DateTime, nullable=True)
    matches = db.relationship('Match', uselist=True, backref='request')

    def __repr__(self):
        return '<Request {}: {}>'.format(self.id, self.embedding)

class Match(db.Model):
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    request_id = db.Column(db.Integer, db.ForeignKey('request.id'), nullable=True)
    image_id = db.Column(db.Integer, db.ForeignKey('image.id'), nullable=True)
    person_id = db.Column(db.Integer, db.ForeignKey('person.id'), nullable=True)
    distance = db.Column(db.Float, nullable=True)

    def __repr__(self):
        return '<Match {}: request {} with person {}>'.format(self.id,
                                                          self.request_id, 
                                                          self.person_id)

      ########################
########  Global variables  ########
      ########################
print("Iniciando o servidor...")
db_embeddings = np.array([eval(image.embedding) for image in Image.query.all()])
match_distance = 1

      #############
########  Views  ########
      #############

@app.route('/get_wanted_people', methods=['POST'])
def get_wanted_people():
    start_time = time.time()
    if request.headers.get('x-api-key') != 'mySuperSecretKey':
        print("##########\nPermissão negada!!!\n##########")
        return jsonify({'error': 'Permission denied'}), 403
    
    request_lat = request.args.get('lat')
    request_lon = request.args.get('lon')
    request_embedding = request.args.get('embedding')

    #print("\n#####")
    #print(request.args)
    #print(request_lat)
    #print(request_lon)
    #print(request_embedding)
    #print("#####\n")

    new_request = Request(lat=request_lat,
                          long=request_lon,
                          embedding=str(request_embedding),
                          created_at=datetime.now()
                        )

    db.session.add(new_request)
    try:
        db.session.flush()
        print('New_request successfull')
    except:
        print('Error in adding new_request')
        return jsonify({'error': 'Error in adding new_request'}), 500 # Internal Server Error

    try:
        request_embedding = np.array(eval(request_embedding))
    except:
        return jsonify({'error': 'Error in reading embedding'}), 500 # Internal Server Error

    image_list = Image.query.all()
    distances = np.linalg.norm(request_embedding - db_embeddings[None, :, :], axis=-1)[0]
    matchs_info = [(image_list[index], distances[index]) for index in (np.where(distances<match_distance)[0])]
    
    matches = []
    for image, distance in matchs_info:
        person_match = Person.query.filter(Person.id == image.person_id).first()
        if is_new_match(matches, person_match.name):
            matches.append({
                'person_name':person_match.name,
                'distance':distance[0]
            })
        print("image id {}: distance {}".format(image.id, distance))
        db.session.add(Match(request_id=new_request.id, image_id=image.id, person_id=image.person_id, distance=distance))

    if len(matches) == 0:
        matches.append({
                'person_name':'Sem matchs',
                'distance':0
            })

    end_time = time.time()
    print("Total request time: {:d}m {:d}s".format(int((end_time - start_time)//60), int((end_time - start_time)%60)))
    try:
        db.session.commit()    
        return jsonify({'matches': matches}), 200 # OK
    except:
        print('Error in adding matches')
        return jsonify({'error': 'Error in adding matches'}), 500 # Internal Server Error

@app.route('/get_people', methods=['GET'])
def get_people():
    people_query = Person.query.all()
    people = []

    for person in people_query:
        people.append({
                'id':person.id,
                'name':person.name,
                'birthday':person.birthday,
                'status':person.status,
                'created_at':person.created_at,
                'updated_at':person.updated_at
        })

    return jsonify({'data':people}), 200

@app.route('/get_count_people', methods=['GET'])
def get_count_people():
    people_query = Person.query.all()
    #count = Person.query.count() # Testar isso

    count = 0
    for person in people_query:
        count += 1

    return jsonify({'data':count}), 200

@app.route('/get_images', methods=['GET'])
def get_images():
    image_query = Image.query.all()
    images = []

    for image in image_query:
        images.append({
            'id':image.id,
            'person_id':image.person_id,
            'path':image.path,
            'embedding':image.embedding,
            'created_at':image.created_at
        })
    return jsonify({'data': images}), 200

@app.route('/get_count_images', methods=['GET'])
def get_count_images():
    image_query = Image.query.all()
    #count = Person.query.count() # Testar isso

    count = 0
    for image in image_query:
        count += 1

    return jsonify({'data':count}), 200

@app.route('/get_requests', methods=['GET'])
def get_requests():
    request_query = Request.query.all()
    requests = []

    for request in request_query:
        requests.append({
            'id':request.id,
            'created_at':request.created_at,
            'lat':request.lat,
            'long':request.long,
            'embedding':request.embedding
        })
    return jsonify({'data': requests}), 200

@app.route('/get_matches', methods=['GET'])
def get_matches():
    match_query = Match.query.all()
    matches = []

    for match in match_query:
        matches.append({
            'id':match.id,
            'request_id':match.request_id,
            'image_id':match.image_id,
            'person_id':match.person_id,
            'distance':match.distance
        })
    return jsonify({'data': matches}), 200

@app.route('/time', methods=['GET'])
def get_current_time():
    return jsonify({'time': time.time()}), 200

      ##############
########  Run App ########
      ##############
      
if __name__ == "__main__":
    #app.run()
    app.run(host="0.0.0.0", port=5000)#, ssl_context='adhoc')