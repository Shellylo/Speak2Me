import sqlite3
import os

def does_user_exist(db_connection, phone_num):
	'''
		Function checks if user (phone number) exists in database
		Input: database connection, phone number to search
		Output: True if phone number exists in database, False otherwise
	'''
	pass

def sign_up(db_connection, phone_num, password, name):
	'''
		Function adds new user to users table
		Input: database connection, phone number, password and name of the user
		Output: None
	'''
	pass

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
	