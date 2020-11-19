import sqlite3
from sqlite3 import Error

def create_connection(db_file):
    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except Error as e:
        print("Fail to create table: ", e)

    return conn

def insert_person(conn, person):
    sql = ''' INSERT INTO person (created_at, updated_at, name, 
                                     birthday, status)
              VALUES (?,?,?,?,?) '''

    with conn:
        cur = conn.cursor()
        cur.execute(sql, 
                    (person['created_at'], person['updated_at'], person['name'],
                    person['birthday'], person['status']
                    )
        )
    return cur.lastrowid

def insert_image(conn, image):
    sql = ''' INSERT INTO image (person_id, created_at, path, embedding)
              VALUES (?,?,?,?) '''

    with conn:
        cur = conn.cursor()
        cur.execute(sql, 
                    (image['person_id'], image['created_at'],
                     image['path'], image['embedding']
                    )
        )
    return cur.lastrowid

def insert_request(conn, request):
    sql = ''' INSERT INTO request (created_at, lat, long, embedding)
              VALUES (?,?,?,?) '''

    with conn:
        cur = conn.cursor()
        cur.execute(sql, 
                    (request['created_at'], request['lat'],
                    request['long'], request['embedding']
                    )
        )
    return cur.lastrowid

def insert_match(conn, match):
    sql = ''' INSERT INTO match (request_id, person_id, distance)
              VALUES (?,?,?) '''

    with conn:
        cur = conn.cursor()
        cur.execute(sql, 
                    (match['request_id'], match['person_id'], match['distance'])
        )
    return cur.lastrowid

def test_insert():
    from datetime import datetime
    database = r"../database/SmartFinder.db"

    conn = create_connection(database)

    person = {
        'created_at': datetime(2015, 1, 1),
        'updated_at': datetime(2015, 1, 1),
        'name': 'Afonso Oliveira',
        'birthday': datetime(2015, 1, 1),
        'status': 'procurado'
    }

    print("Inserting person")
    person_id = insert_person(conn, person)

    image = {
        'person_id': person_id,
        'created_at': datetime(2015, 1, 1),
        'path': './images/AfonsoOliveira_1.png',
        'embedding': '[1,2,3,4,5,6,7,8,9,10]'
    }

    print("Inserting image")
    image_id = insert_image(conn, image)

    request = {
        'created_at': datetime(2015, 1, 1),
        'lat': 123,
        'long': 123,
        'embedding': ''
    }

    print("Inserting request")
    request_id = insert_request(conn, request)

    match = {
        'request_id': request_id,
        'image_id': image_id,
        'person_id': person_id,
        'distance':0.9
    }

    print("Inserting match")
    match_id = insert_match(conn, match)
    
    return

if __name__ == '__main__':
    test_insert()