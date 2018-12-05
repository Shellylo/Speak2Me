import socket
import thread
from collections import deque
import json
import sqlite_database as sql_db

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
SEND_VOICE_MESSAGE_CODE = 201

# Errors
GENERAL_ERROR_CODE = 0
DETAILS_MISSING_ERROR_CODE = 1
PHONE_EXISTS_ERROR_CODE = 2
ALREADY_CONNECTED_ERROR_CODE = 3
INCORRECT_LOGIN_ERROR_CODE = 4
SOURCE_INVALID_ERROR_CODE = 5
DESTINATION_UNREACHABLE_ERROR_CODE = 6

def is_user_connected(client_socket):
	return client_socket in CONNECTED_CLIENTS.values()

def send_voice_message(db_connection, client_socket, message_dict):
	'''
		Function receives message to send, sends it to destination / saves it to database if destination not connected
		Text message is returned to client as well.
		Input: Sqlite database connection, message (request) dict, contains source, destination and the message
				* assuming phones and voice message are valid (optional future changes)
				* checking: all the necessary information included in the message, if both src and dst phones exist in db
		Output: Answer message dict
	'''
	ans_messages_dict = {}
	if not ("src_phone" in message_dict and "dst_phone" in message_dict and "content" in message_dict): # Checks if details are missing
		ans_messages_dict[client_socket] = { "code": DETAILS_MISSING_ERROR_CODE }
		
	elif message_dict["src_phone"] not in CONNECTED_CLIENTS or CONNECTED_CLIENTS[message_dict["src_phone"]] != client_socket: # Checks if source phone is client's true phone
		ans_messages_dict[client_socket] = { "code": SOURCE_INVALID_ERROR_CODE }
		
	elif not sql_db.does_user_exist(db_connection, message_dict["dst_phone"]): # Checks if destination phone exists in database
		ans_messages_dict[client_socket] = { "code": DESTINATION_UNREACHABLE_ERROR_CODE }
		
	else:
		# Speech to text - to do
		text_message = "test message"
		# Set message that will be returned to sender and receiver (including code, message source and the text message)
		ans_messages_dict[client_socket] = { "code": RECEIVE_MESSAGES_CODE, "messages": [{ "src_phone": message_dict["src_phone"], "dst_phone": message_dict["dst_phone"], "content": text_message }] }
		
		if message_dict["dst_phone"] in CONNECTED_CLIENTS: # Destination connected, send answer to destination
			# Copy message that will be returned to sender and receiver
			ans_messages_dict[CONNECTED_CLIENTS[message_dict["dst_phone"]]] = ans_messages_dict[client_socket]
		
		else: # Destination disconnected, save message to sql database
			sql_db.save_text_message(db_connection, message_dict["src_phone"], message_dict["dst_phone"], text_message)
		
	return ans_messages_dict	

def receive_messages(db_connection, client_socket, message_dict):
	ans_messages_dict = { }
	if "phone" not in message_dict: # Checks if details are missing
		ans_messages_dict[client_socket] = { "code" : DETAILS_MISSING_ERROR_CODE }
		return ans_messages_dict
		
	if message_dict["phone"] not in CONNECTED_CLIENTS or CONNECTED_CLIENTS[message_dict["phone"]] != client_socket:
		ans_messages_dict[client_socket] = { "code" : SOURCE_INVALID_ERROR_CODE }
		return ans_messages_dict
		
	ans_messages_dict[client_socket] = { "code" : RECEIVE_MESSAGES_CODE, "messages" : sqlite_database.get_new_messages(db_connection, message_dict["phone"]) }
	sqlite_database.delete_messages(db_connection, message_dict["phone"])
	return ans_messages_dict

def log_in(db_connection, client_socket, message_dict):
	ans_messages_dict = { }
	
	if not ("phone" in message_dict and "password" in message_dict): # Checks if details are missing
		ans_messages_dict[client_socket] = { "code" : DETAILS_MISSING_ERROR_CODE }
		return ans_messages_dict
		
	if client_socket in CONNECTED_CLIENTS.values() or message_dict["phone"] in CONNECTED_CLIENTS:
		ans_messages_dict[client_socket] = { "code" : ALREADY_CONNECTED_ERROR_CODE } 
		return ans_messages_dict
		
	if not sql_db.is_login_ok(db_connection, message_dict["phone"], message_dict["password"]):
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
		
	elif sql_db.does_user_exist(db_connection, message_dict["phone"]): # Checks if phone number already exists
		ans_messages_dict[client_socket] = { "code": PHONE_EXISTS_ERROR_CODE }
		
	else: # Passed all the checks, new user is added
		ans_messages_dict[client_socket] = { "code": SIGN_UP_CODE }
		sql_db.sign_up(db_connection, message_dict["phone"], message_dict["password"], message_dict["name"])
		
	return ans_messages_dict

def handle_requests(db_connection):
	OPERATIONS_DICT = {SIGN_UP_CODE: sign_up, LOG_IN_CODE: log_in, RECEIVE_MESSAGES_CODE: receive_messages, SEND_VOICE_MESSAGE_CODE: send_voice_message}
	while True:
		if MESSAGES_QUEUE: # There are messages waiting
			client_socket, message_dict = MESSAGES_QUEUE.popleft() # Receive first message in dict format
			message_dict = json.loads(message_dict)
			ans_messages_dict = OPERATIONS_DICT[message_dict["code"]](db_connection, client_socket, message_dict)
			for socket in ans_messages_dict.keys():
				socket.send(json.dumps(ans_messages_dict[socket]))

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
		
		# Test Examples:
		# print sign_up(sql_database, 8538, {"code": SIGN_UP_CODE, "phone": "0539948875", "password":"cool2", "name":"Netanel"})
		# CONNECTED_CLIENTS["0548827476"] = 8537
		# print send_voice_message(sql_database, 8537, {"code": SEND_VOICE_MESSAGE_CODE, "src_phone": "0548827476", "dst_phone": "0539948875", "content": "53478573485783447893"})
		
	except Exception, e:
		print e	
		
	finally:
		# Closing the listening socket
		listening_socket.close()
		# Closing the sqlite database connection
		sql_database.close()
	
main()