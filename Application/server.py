import socket
import thread
from collections import deque
import json
import sqlite_database as sql_db

SOCKET_NUM = 3124

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
# Soon... :)

OPERATIONS_DICT = {SIGN_UP_CODE: sign_up, LOG_IN_CODE: log_in, RECEIVE_MESSAGES_CODE: receive_messages, SEND_MESSAGE_CODE: send_message}

def login():

def handle_requests():
	while True:
		if MESSAGES_QUEUE: # There are messages waiting
			message_dict = MESSAGES_QUEUE.popleft().loads() # Receive first message in dict format
			OPERATIONS_DICT[message_dict["code"]](message_dict)

def client_handler(client_socket):
	'''
		Handle incoming requests from client
		Input: client socket
		Output: None
	'''
	try:
		# Receiving data from the client
		client_message = client_soc.recv(MAX_MESSAGE_LEN)
		MESSAGES_QUEUE.append(client_message)
	# Sending data back
	#client_soc.sendall("got it!")
	
	except Exception, e:
		print e
	finally:
		# Closing the conversation socket
		client_soc.close()
	
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

	# Binding to local port 80
	server_address = ('localhost', SOCKET_NUM)
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
		thread.start_new_thread(handle_requests)
		listen_and_accept(listening_socket)
	except Exception, e:
		print e	
	finally:
		# Closing the listening socket
		listening_socket.close()
		
main()