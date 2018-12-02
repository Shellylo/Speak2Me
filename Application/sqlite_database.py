import sqlite3
import os

def init_and_load(database_name):
	'''
		Function loads sql database and intializes it if the database doesn't exist already
		Input: database name (Format: '<database_name>.db')
		Ouptut: database connection
	'''
	database_exists = os.path.isfile(database_name)
	connection = sqlite3.connect(database_name)
	if not database_exists: # Init tables for the first time database created
		connection_cursor = conn.cursor()
		tables_list = ["CREATE TABLE USERS(PHONE_NUM TEXT PRIMARY KEY NOT NULL, PASSWORD TEXT NOT NULL, NAME TEXT NOT NULL)",
						"CREATE TABLE MESSAGES(MESSAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, SRC_PHONE TEXT NOT NULL, DEST_PHONE TEXT NOT NULL, MESSAGE TEXT NOT NULL)"]
		for table in tables_list:
			c.execute(table)
	return connection
	