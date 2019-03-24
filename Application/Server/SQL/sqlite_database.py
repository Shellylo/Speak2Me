import sqlite3
import os
import md5

def hash_str(str):
	hashed_str = md5.new()
	hashed_str.update(str.encode('utf-8'))
	return hashed_str.hexdigest()

def delete_messages(db_connection, phone_num):
	'''
		Function deletes the messages that were sent to phone_num
		Input: database connection and the phone number that the messages of it will be deleted
		Output: None
	'''
	connection_cursor = db_connection.cursor()
	connection_cursor.execute("DELETE FROM MESSAGES WHERE DEST_PHONE = \"" + phone_num + "\"")

def get_new_messages(db_connection, phone_num):
	'''
		Function returns all the messages that are sent to phone_num when he was not connected
		Input: database connection and the phone number that the messages of it should be returned
		Output: the messages of that were sent to the phone number
	'''
	new_messages = []
	connection_cursor = db_connection.cursor()
	messages = connection_cursor.execute("SELECT SRC_PHONE, DEST_PHONE, MESSAGE FROM MESSAGES WHERE DEST_PHONE = \"" + phone_num + "\"" + " ORDER BY MESSAGE_ID ASC").fetchall()
	for message in messages:
		new_messages.append({"src_phone" : message[0], "dst_phone": message[1], "content" : message[2]})
		
	return new_messages

def save_text_message(db_connection, src_phone_num, dst_phone_num, text_message):
	'''
		Function saves text message in database temporarily (until message required by destination)
		Input: database connection, source and destination phone numbers, message in text form
		Output: None
	'''
	connection_cursor = db_connection.cursor()
	connection_cursor.execute("INSERT INTO MESSAGES (SRC_PHONE, DEST_PHONE, MESSAGE) VALUES (\"" + src_phone_num + "\", \"" + dst_phone_num + "\", \"" + text_message + "\")")
	db_connection.commit() # Save changes

def is_login_ok(db_connection, phone_num, password):
	'''
		Function checks if the phone matches the password (and if the phone exists)
		Input: database connection, the phone number of the user that is trying to log in and the password of the user that is trying to log in
		Output: True if the details are correct, False otherwise
	'''
	connection_cursor = db_connection.cursor()
	return len(connection_cursor.execute("SELECT * FROM USERS WHERE PHONE_NUM = \"" + phone_num + "\"" + " AND PASSWORD = \"" + hash_str(password) + "\"").fetchall()) > 0
	
def does_user_exist(db_connection, phone_num):
	'''
		Function checks if user (phone number) exists in database
		Input: database connection, phone number to search
		Output: True if phone number exists in database, False otherwise
	'''
	connection_cursor = db_connection.cursor()
	return len(connection_cursor.execute("SELECT * FROM USERS WHERE PHONE_NUM = \"" + phone_num + "\"").fetchall()) > 0

def sign_up(db_connection, phone_num, password, name):
	'''
		Function adds new user to users table
		Input: database connection, phone number, password and name of the user
		Output: None
	'''
	connection_cursor = db_connection.cursor()
	connection_cursor.execute("INSERT INTO USERS (PHONE_NUM, PASSWORD, NAME) VALUES (\"" + phone_num + "\", \"" + hash_str(password) + "\", \"" + name + "\")")
	db_connection.commit() # Save changes

def init_and_load(database_name):
	'''
		Function loads sql database and intializes it if the database doesn't exist already
		Input: database name (Format: '<database_name>.db')
		Ouptut: database connection
	'''
	database_exists = os.path.isfile(database_name)
	connection = sqlite3.connect(database_name)
	if not database_exists: # Init tables for the first time database created
		connection_cursor = connection.cursor()
		tables_list = ["CREATE TABLE USERS(PHONE_NUM TEXT PRIMARY KEY NOT NULL, PASSWORD TEXT NOT NULL, NAME TEXT NOT NULL)",
						"CREATE TABLE MESSAGES(MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, SRC_PHONE TEXT NOT NULL, DEST_PHONE TEXT NOT NULL, MESSAGE TEXT NOT NULL)"]
		for table in tables_list:
			connection_cursor.execute(table)
	return connection
	