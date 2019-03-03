# -*- coding: utf-8 -*-
import sys
import os
sys.path.append(os.getcwd() + "\\GUI")
sys.path.append(os.getcwd() + "\\SQL")
import socket
import thread
from collections import deque
import json
import sqlite_database as sql_db
import base64
import re
import speech_recognition
from pydub import AudioSegment
import GUI_log
import msvcrt
import security

HOST = "10.0.0.7" #10.0.0.7, localhost
PORT_NUM = 3124

MAX_QUEUE_CONNECTIONS = 5
MAX_SIZE_LEN = 10

M4A_FILE_ENDING_LEN = -4

LOGGING_QUEUE = deque()

MESSAGES_QUEUE = deque()
CONNECTED_CLIENTS = {}

ACTION_DICT = {100 : "SIGN UP", 101 : "LOG IN", 200 : "RECEIVE MESSAGES", 201 : "SEND TEXT", 203 : "SPEECH TO TEXT"}

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
	
def m4a_to_wav(audio_path):
	sound = AudioSegment.from_file(audio_path, format="m4a")
	audio_path = audio_path[:M4A_FILE_ENDING_LEN] + ".wav"
	sound.export(audio_path, format="wav")
	return audio_path
	
def voice_recognition(audio_path):
	recognizer = speech_recognition.Recognizer()

	with speech_recognition.AudioFile(audio_path) as source:
		audio = recognizer.record(source)
	text_message = recognizer.recognize_google(audio, language="he") #.encode("UTF-8") -he
	
	if text_message.lower() == "fatigue":
		text_message = "pizza"

	return text_message
	
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
		# Create mp3 file which contains the record
		audio_path = "Recordings\\" + message_dict["src_phone"] + ".m4a"
		audio_file = open(audio_path, "w+b")
		audio_file.write(message_dict["content"].decode("base64"))
		audio_file.close()
		
		text_message = "Error: could not recognize" # TODO: error message
		try:
			# Convert mp3 file to wav file and recognize voice
			audio_path = m4a_to_wav(audio_path)	
			text_message = voice_recognition(audio_path)
		except Exception, e:
			ip, port = client_socket.getpeername()
			LOGGING_QUEUE.append(("COULD NOT RECOGNIZE", {"IP" : ip, "PORT" : port}, 3))
		
		ans_messages_dict[client_socket] = { "code": SPEECH_TO_TEXT_CODE, "messages": [{"src_phone": message_dict["src_phone"], "dst_phone": message_dict["dst_phone"], "content": text_message}] }
		
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

def recvall(sock, n):
    '''
		Function to recv n bytes or return None if EOF is hit
		Input: Socket, message size
		Output: The data that was read
	'''
    data = b''
    while len(data) < n:
        packet = sock.recv(n - len(data))
        if not packet:
            return None
        data += packet
    return data	
	
def handle_requests(db_connection):
	OPERATIONS_DICT = {SIGN_UP_CODE: sign_up, LOG_IN_CODE: log_in, RECEIVE_MESSAGES_CODE: receive_messages, SEND_TEXT_MESSAGE_CODE: send_text_message, SPEECH_TO_TEXT_CODE: speech_to_text}
	while not (msvcrt.kbhit() and msvcrt.getch() == 'q'):
		try:
			if MESSAGES_QUEUE: # There are messages waiting
				client_socket, message_dict = MESSAGES_QUEUE.popleft() # Receive first message in dict format
				ans_messages_dict = OPERATIONS_DICT[message_dict["code"]](db_connection, client_socket, message_dict)
				for socket in ans_messages_dict.keys():
					response = json.dumps(ans_messages_dict[socket]) # Response to json format
					response = security.encode(bytearray().extend(response)) # Encode response message
					socket.send(str(len(response)).zfill(MAX_SIZE_LEN))
					socket.send(response)
		except Exception, e:
			print "Error:", e

def client_handler(client_socket):
	'''
		Handle incoming requests from client
		Input: client socket
		Output: None
	'''
	ip, port = client_socket.getpeername()
	try:
		LOGGING_QUEUE.append(("CONNECTED", {"IP" : ip, "PORT" : port}, 1))
		while True:
			# Receiving data size from client
			message_size = int(client_socket.recv(MAX_SIZE_LEN))
			
			# Receiving data from the client
			client_message = recvall(client_socket, message_size) # Receive raw message
			client_message = security.decode(bytearray().extend(client_message)) # Decode message
			message_dict = json.loads(client_message) # Load from json format to dict
			
			LOGGING_QUEUE.append((ACTION_DICT.get(message_dict.get("code", 0), "NOT DEFINED ACTION"), {"IP" : ip, "PORT" : port}, 2))
			
			# Add message to messages queue with client's socket
			MESSAGES_QUEUE.append((client_socket, message_dict))
	
	except Exception, e:
		#remove client from CONNECTED_CLIENTS if he is connected
		for key, value in CONNECTED_CLIENTS.items():
			if value == client_socket:
				user = key
				del CONNECTED_CLIENTS[key]
				
		LOGGING_QUEUE.append(("DISCONNECTED", {"IP" : ip, "PORT" : port}, 1))
	finally:
		# Closing the conversation socket
		try:
			client_socket.close()
		except Exception, e:
			print "Error:", e
	
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
	sql_database = sql_db.init_and_load('SQL\\SpeakToMe.db')
	try:
		listening_socket = bind()
		
	except Exception, e:
		print "Error:", e
		return 0
	
	LOGGING_QUEUE.append(("SERVER STARTED", {"IP" : HOST, "PORT" : PORT_NUM}, 1))
	
	try:
		# Start a thread for listening to clients
		thread.start_new_thread(listen_and_accept, (listening_socket, ))
		# Start a thread for logging
		thread.start_new_thread(GUI_log.listen_and_update, (LOGGING_QUEUE, ))
		# Handle all incoming requests
		handle_requests(sql_database)
	except Exception, e:
		print "Error:", e	
		
	finally:
		# Closing all existing connections
		for socket in CONNECTED_CLIENTS.values():
			try:
				socket.close()
			except Exception, e:
				print "Error:", e
		# Closing the listening socket
		listening_socket.close()
		# Closing the sqlite database connection
		sql_database.close()
	
main()