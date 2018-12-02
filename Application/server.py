import socket
import thread
from collections import deque
import json
import sqlite_database as sql_db

PORT_NUM = 3124

MAX_QUEUE_CONNECTIONS = 5
MAX_MESSAGE_LEN = 32768

MESSAGES_QUEUE = deque()
CONNECTED_CLIENTS = {}

# Not Connected Operations
SIGN_UP_CODE = 100
LOG_IN_CODE = 101

# Connected Operations
RECEIVE_MESSAGES_CODE = 200
SEND_MESSAGE_CODE = 201

# Errors
GENERAL_ERROR_CODE = 0
SIGN_UP_DETAILS_MISSING_ERROR_CODE = 1
PHONE_EXISTS_ERROR_CODE = 2

def send_message(db_connection, message_dict):
	pass

def receive_messages(db_connection, message_dict):
	pass

def log_in(db_connection, message_dict):
	pass
	
def sign_up(db_connection, message_dict):
	'''
		Function adds new user to database
		Input: Message (request) dict, contains phone number, password and name
				* assuming valid phone number, password and name (currently, optional future changes)
				* checking: all the necessary information included in the message, does user exist.
		Output: Answer message dict
	'''
	ans_message_dict = {}
	if not ("phone" in message_dict and "password" in message_dict and "name" in message_dict): # Checks if details are missing
		ans_message_dict["code"] = SIGN_UP_DETAILS_MISSING_ERROR_CODE
	elif sql_db.does_user_exist(db_connection, message["phone"]): # Checks if phone number already exists
		ans_message_dict["code"] = PHONE_EXISTS_ERROR_CODE
	else: # Passed all the checks, new user is added
		ans_message_dict["code"] = SIGN_UP_CODE
		sql_db.sign_up(db_connection, message_dict["phone"], message_dict["password"], message_dict["name"])
	return ans_message_dict

def handle_requests(db_connection):
	OPERATIONS_DICT = {SIGN_UP_CODE: sign_up, LOG_IN_CODE: log_in, RECEIVE_MESSAGES_CODE: receive_messages, SEND_MESSAGE_CODE: send_message}
	while True:
		if MESSAGES_QUEUE: # There are messages waiting
			client_socket, message_dict = MESSAGES_QUEUE.popleft().loads() # Receive first message in dict format
			ans_message_dict = OPERATIONS_DICT[message_dict["code"]](db_connection, message_dict)

def client_handler(client_socket):
	'''
		Handle incoming requests from client
		Input: client socket
		Output: None
	'''
	try:
		# Receiving data from the client
		client_message = client_socket.recv(MAX_MESSAGE_LEN)
		MESSAGES_QUEUE.append((client_socket, client_message))
	# Sending data back
	#client_socket.sendall("got it!")
	
	except Exception, e:
		print e
	finally:
		# Closing the conversation socket
		client_socket.close()
	
def listen_and_accept(listening_socket):
	'''
	Function listens to received socket and creates threads for incoming clients
	Input: listening socket
	Output: None
	'''
	# Create a new conversation socket
	while True:
		client_socket, client_address = listening_socket.accept()
		thread.start_new_thread(client_handler, (client_socket, ))

def bind():
	'''
		The function binds to listening socket
		Input: None
		Output: The socket
	'''
	listening_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	# Binding to local port
	server_address = ('localhost', PORT_NUM)
	listening_socket.bind(server_address)
	
	# Listen for incoming connections
	listening_socket.listen(MAX_QUEUE_CONNECTIONS)
	return listening_socket
	
def main():
	sql_database = sql_db.init_and_load('SpeakToMe.db')
	try:
		listening_socket = bind()
	except Exception, e:
		print e
		return 0
		
	try:
		thread.start_new_thread(handle_requests, (sql_database, ))
		listen_and_accept(listening_socket)
	except Exception, e:
		print e	
	finally:
		# Closing the listening socket
		listening_socket.close()
		
		sql_database.close()
		
main()