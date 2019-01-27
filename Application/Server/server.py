import socket
import thread
from collections import deque
import json
import sqlite_database as sql_db
import base64
import re

HOST = "192.168.42.207" #10.0.0.7, localhost
PORT_NUM = 3124

MAX_QUEUE_CONNECTIONS = 5
MAX_SIZE_LEN = 10

MESSAGES_QUEUE = deque()
CONNECTED_CLIENTS = {}

# Not Connected Operations
SIGN_UP_CODE = 100
LOG_IN_CODE = 101

# Connected Operations
RECEIVE_MESSAGES_CODE = 200
SEND_TEXT_MESSAGE_CODE = 201
PUSH_MESSAGE_CODE = 202
SPEECH_TO_TEXT_CODE = 203

# Errors
GENERAL_ERROR_CODE = 0
DETAILS_MISSING_ERROR_CODE = 1
PHONE_EXISTS_ERROR_CODE = 2
ALREADY_CONNECTED_ERROR_CODE = 3
INCORRECT_LOGIN_ERROR_CODE = 4
SOURCE_INVALID_ERROR_CODE = 5
DESTINATION_UNREACHABLE_ERROR_CODE = 6
INCORRECT_SIGNUP_DETAILS_ERROR_CODE = 7

def isPhoneCorrect(phone):
	pattern = re.compile("^05[0-9]{8}$") #starts with 05 and contains 10 characters total
	return bool(pattern.match(phone))
	
def isPasswordCorrect(password):
	pattern = re.compile("(?=^[A-Za-z0-9]{4,}$)(?=^.*[A-Za-z].*$)") #contains only letters and numbers, contains at least 4 characters, contains at least 1 letter
	return bool(pattern.match(password))
	
def isNameCorrect(name):
	pattern = re.compile("^[A-Za-z][A-Za-z0-9]+$") #starts with a letter, can contain only letters and numbers, contains at least 2 characters
	return bool(pattern.match(name))

def message_details_exist(message):
	''' Function checks if message has the necessary details: source phone, destination phone, content
		Input: The message (dictionary)
		Output: True if message contains all the details, false otherwise
	'''
	return "src_phone" in message and "dst_phone" in message and "content" in message

def is_user_connected(client_socket, phone):
	'''
		Function checks if client is a logged user
		Input: The socket of the client, phone number
		Output: True if user connected, false otherwise
	'''
	return phone in CONNECTED_CLIENTS and CONNECTED_CLIENTS[phone] == client_socket

def is_socket_phone_connected(client_socket, phone):
	'''
		Function checks if client is already connected (already went through log in successfully)
		Input: The socket of the client, input phone number
		Output: True if already connected, false otherwise
	'''
	return client_socket in CONNECTED_CLIENTS.values() or phone in CONNECTED_CLIENTS
	
def speech_to_text(db_connection, client_socket, message_dict):
	'''
		Function receives audio and transfers it into text.
		The text message is returned to sender (client)
		Input: Sqlite database connection
			   client socket
			   message (request) dict, contains: source and audio file
		Output: Answer message dict, contains text message		
	'''
	ans_messages_dict = {}
	if not message_details_exist(message_dict): # Details missing
		ans_messages_dict[client_socket] = { "code": DETAILS_MISSING_ERROR_CODE }
		
	elif not is_user_connected(client_socket, message_dict["src_phone"]): # Socket / phone isn't connected
		ans_messages_dict[client_socket] = { "code": SOURCE_INVALID_ERROR_CODE }
		
	else:
		#audio_file_data = base64.b64decode(message_dict["content"]) # decode file data
		# Speech to text - to do
		ans_messages_dict[client_socket] = { "code": SPEECH_TO_TEXT_CODE, "messages": [{"src_phone": message_dict["src_phone"], "dst_phone": message_dict["dst_phone"], "content": message_dict["content"]}] }
		
	return ans_messages_dict

def send_text_message(db_connection, client_socket, message_dict):
	'''
		Function receives text message to send, sends it to destination / saves it to database if destination isn't connected
		Text message is returned to client as well.
		Input: Sqlite database connection, message (request) dict, contains source, destination and the message
				* assuming phones and text message are valid (optional future changes)
				* checking: all the necessary information included in the message, if both src and dst phones exist in db
		Output: Answer message dict
	'''
	ans_messages_dict = {}
	if not message_details_exist(message_dict): #Details missing
		ans_messages_dict[client_socket] = { "code": DETAILS_MISSING_ERROR_CODE }
		
	elif not is_user_connected(client_socket, message_dict["src_phone"]): # Socket / phone isn't connected
		ans_messages_dict[client_socket] = { "code": SOURCE_INVALID_ERROR_CODE }
		
	elif not sql_db.does_user_exist(db_connection, message_dict["dst_phone"]): # Checks if destination phone exists in database
		ans_messages_dict[client_socket] = { "code": DESTINATION_UNREACHABLE_ERROR_CODE }
		
	else:
		text_message = message_dict["content"]
		# Set message that will be returned to sender and receiver (including code, message source and the text message)
		ans_messages_dict[client_socket] = { "code": SEND_TEXT_MESSAGE_CODE, "messages": [{"src_phone": message_dict["src_phone"], "dst_phone": message_dict["dst_phone"], "content": text_message }] }
		
		# Send message to destination client if connected
		if message_dict["dst_phone"] in CONNECTED_CLIENTS: # Destination connected
			ans_messages_dict[CONNECTED_CLIENTS[message_dict["dst_phone"]]] = { "code": PUSH_MESSAGE_CODE, "messages": [{ "src_phone": message_dict["src_phone"], "dst_phone": message_dict["dst_phone"], "content": text_message }]}
		
		else: # Destination disconnected, save message to sql database
			sql_db.save_text_message(db_connection, message_dict["src_phone"], message_dict["dst_phone"], text_message)
		
	return ans_messages_dict	

def receive_messages(db_connection, client_socket, message_dict):
	'''
		Function returns all the messages of the user that is asking for new messages
		Input: database connection, the socket of the client that asked for the new messages and the message (request) 
			   that contains the phone number that the messages that has been sent to it should be returned
		Output: answer message dict
	'''
	ans_messages_dict = { }
	if "phone" not in message_dict: # Checks if details are missing
		ans_messages_dict[client_socket] = { "code" : DETAILS_MISSING_ERROR_CODE }
		return ans_messages_dict
		
	if message_dict["phone"] not in CONNECTED_CLIENTS or CONNECTED_CLIENTS[message_dict["phone"]] != client_socket: # Validates the source phone that hes been sent
		ans_messages_dict[client_socket] = { "code" : SOURCE_INVALID_ERROR_CODE }
		return ans_messages_dict
		
	ans_messages_dict[client_socket] = { "code" : RECEIVE_MESSAGES_CODE, "messages" : sql_db.get_new_messages(db_connection, message_dict["phone"]) }
	sql_db.delete_messages(db_connection, message_dict["phone"])

	return ans_messages_dict

def log_in(db_connection, client_socket, message_dict):
	'''
		Function checks if the log in details are correct and logs in the user if they are (adds the user to the connected clients)
		Input: database connection, the socket of the client that is trying to log in and the message (request) that contains phone number and password
		Output: answer message dict
	'''
	ans_messages_dict = { }
	if not ("phone" in message_dict and "password" in message_dict): # Checks if details are missing
		ans_messages_dict[client_socket] = { "code" : DETAILS_MISSING_ERROR_CODE }
		return ans_messages_dict
		
	if is_socket_phone_connected(client_socket, message_dict["phone"]): # Checks that the user is not already loged in
		ans_messages_dict[client_socket] = { "code" : ALREADY_CONNECTED_ERROR_CODE } 
		return ans_messages_dict
		
	if not sql_db.is_login_ok(db_connection, message_dict["phone"], message_dict["password"]): # Checks if the phone number and the password match (user can be loged in now)
		ans_messages_dict[client_socket] = { "code" : INCORRECT_LOGIN_ERROR_CODE }
		return ans_messages_dict
		
	ans_messages_dict[client_socket] = { "code" : LOG_IN_CODE }
	CONNECTED_CLIENTS[message_dict["phone"]] = client_socket
	return ans_messages_dict
	
def sign_up(db_connection, client_socket, message_dict):
	'''
		Function adds new user to database
		Input: Sqlite database connection, message (request) dict, contains phone number, password and name
				* assuming valid phone number, password and name (currently, optional future changes)
				* checking: all the necessary information included in the message, does user exist.
		Output: Answer messages dict
	'''
	ans_messages_dict = {}
	if not ("phone" in message_dict and "password" in message_dict and "name" in message_dict): # Checks if details are missing
		ans_messages_dict[client_socket] = { "code": DETAILS_MISSING_ERROR_CODE }
		
	elif not(isPhoneCorrect(message_dict["phone"]) and isPasswordCorrect(message_dict["password"]) and isNameCorrect(message_dict["name"])):
		ans_messages_dict[client_socket] = { "code": INCORRECT_SIGNUP_DETAILS_ERROR_CODE }
		
	elif sql_db.does_user_exist(db_connection, message_dict["phone"]): # Checks if phone number already exists
		ans_messages_dict[client_socket] = { "code": PHONE_EXISTS_ERROR_CODE }
		
	else: # Passed all the checks, new user is added
		ans_messages_dict[client_socket] = { "code": SIGN_UP_CODE }
		sql_db.sign_up(db_connection, message_dict["phone"], message_dict["password"], message_dict["name"])
		
	return ans_messages_dict

def handle_requests(db_connection):
	OPERATIONS_DICT = {SIGN_UP_CODE: sign_up, LOG_IN_CODE: log_in, RECEIVE_MESSAGES_CODE: receive_messages, SEND_TEXT_MESSAGE_CODE: send_text_message, SPEECH_TO_TEXT_CODE: speech_to_text}
	while True:
		if MESSAGES_QUEUE: # There are messages waiting
			client_socket, message_dict = MESSAGES_QUEUE.popleft() # Receive first message in dict format
			message_dict = json.loads(message_dict)
			ans_messages_dict = OPERATIONS_DICT[message_dict["code"]](db_connection, client_socket, message_dict)
			for socket in ans_messages_dict.keys():
				response = json.dumps(ans_messages_dict[socket])
				socket.send(str(len(response)).zfill(MAX_SIZE_LEN))
				socket.send(response)

def client_handler(client_socket):
	'''
		Handle incoming requests from client
		Input: client socket
		Output: None
	'''
	try:
		while True:
			# Receiving data size from client
			message_size = int(client_socket.recv(MAX_SIZE_LEN))

			# Receiving data from the client
			client_message = client_socket.recv(message_size)

			# Add message to messages queue with client's socket
			MESSAGES_QUEUE.append((client_socket, client_message))
		
	# Sending data back
	#client_socket.sendall("got it!")
	
	except Exception, e:
		user = "Unknown user"
		ip, port = client_socket.getpeername()
		#remove client from CONNECTED_CLIENTS if he is connected
		for key, value in CONNECTED_CLIENTS.items():
			if value == client_socket:
				user = key
				del CONNECTED_CLIENTS[key]
				
		print user, "disconnected. IP:", ip, "PORT:", port
	finally:
		# Closing the conversation socket
		client_socket.close()
	
def listen_and_accept(listening_socket):
	'''
	Function listens to received socket and creates threads for incoming clients
	Input: listening socket
	Output: None
	'''
	while True:
		# Create a new conversation socket
		client_socket, client_address = listening_socket.accept()
		# Start thread for client accepted
		thread.start_new_thread(client_handler, (client_socket, ))

def bind():
	'''
		The function binds to listening socket
		Input: None
		Output: The socket
	'''
	# TCP
	listening_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	# Binding to local port
	server_address = (HOST, PORT_NUM)
	listening_socket.bind(server_address)
	
	# Listen for incoming connections
	listening_socket.listen(MAX_QUEUE_CONNECTIONS)
	return listening_socket
	
def main():
	# Get database connection
	sql_database = sql_db.init_and_load('SpeakToMe.db')
	try:
		listening_socket = bind()
		
	except Exception, e:
		print e
		return 0
		
	try:
		# Start a thread for listening to clients
		thread.start_new_thread(listen_and_accept, (listening_socket, ))
		# Handle all incoming requests
		handle_requests(sql_database)
	except Exception, e:
		print e	
		
	finally:
		# Closing the listening socket
		listening_socket.close()
		# Closing the sqlite database connection
		sql_database.close()
	
main()