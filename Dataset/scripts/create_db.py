'''
As funções foram inspiradas no tutorial de sqlite3 do site:
https://www.sqlitetutorial.net/sqlite-python/
'''

import sqlite3
from sqlite3 import Error

def create_connection(db_file):
    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except Error as e:
        print("Fail to create table: ", e)

    return conn

def create_table(conn, create_table_sql):
    try:
        c = conn.cursor()
        c.execute(create_table_sql)
    except Error as e:
        print(e)

def create_all_tables():
    database = r"../database/SmartFinder.db"

    sql_create_person_table = """ 
        CREATE TABLE IF NOT EXISTS person (
            id integer PRIMARY KEY,
            created_at datetime NOT NULL,
            updated_at datetime NOT NULL,
            name text,
            birthday  datetime,
            status text
        ); """

    sql_create_image_table = """
        CREATE TABLE IF NOT EXISTS image (
            id integer PRIMARY KEY,
            person_id integer NOT NULL,
            created_at datetime,
            path text,
            embedding text,
            FOREIGN KEY (person_id) REFERENCES person (id)
        );"""

    sql_create_request_table = """
        CREATE TABLE IF NOT EXISTS request (
            id integer PRIMARY KEY,
            created_at datetime,
            lat text,
            long text,
            embedding text
        );"""

    sql_create_match_table = """
        CREATE TABLE IF NOT EXISTS match (
            id integer PRIMARY KEY,
            request_id integer NOT NULL,
            image_id integer NOT NULL,
            person_id integer NOT NULL,
            distance float NOT NULL,
            FOREIGN KEY (request_id) REFERENCES request (id),
            FOREIGN KEY (image_id) REFERENCES image (id),
            FOREIGN KEY (person_id) REFERENCES person (id)
        );"""

    conn = create_connection(database)
    

    if conn is not None:
        print("Creating person table")
        create_table(conn, sql_create_person_table)
        print("Creating image table")
        create_table(conn, sql_create_image_table)
        print("Creating request table")
        create_table(conn, sql_create_request_table)
        print("Creating match table")
        create_table(conn, sql_create_match_table)

        conn.close()
    else:
        print("Error! cannot create the database connection.")

if __name__ == '__main__':
    create_all_tables()