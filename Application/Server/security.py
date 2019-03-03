import random

BYTE_RANGE = 256
MAX_BYTE_VALUE = 255
MAX_POSITION = 8

NUMBERS_COUNT = 2
VALID_POSITIONS_LEN = 6

def encrypt(byte_array):
	'''
		Function encrypts bytes
		Input: Byte array for encryption
		Output: Encrypted string
	'''
	encrypted_bytes = bytearray() # initialize byte array for encrypted string
	random_byte, positions = get_valid_random_byte(6, 7) # Start skip positions are 6, 7
	encrypted_bytes.append(random_byte)
	for i in range(1, len(byte_array) * 4 + 1):
		random_byte, next_positions = get_valid_random_byte(positions[0], positions[1])
		current_byte = byte_array[(i-1)/4] # Current byte in input

		random_byte = set_bit(random_byte, positions[0], get_bit(current_byte, ((i - 1) % 4) * 2))
		random_byte = set_bit(random_byte, positions[1], get_bit(current_byte, ((i - 1) % 4) * 2 + 1))
		
		positions = next_positions
		encrypted_bytes.append(random_byte)
		
	return str(encrypted_bytes)
	
def decrypt(encrypted_bytes):
	'''
		Function decrypts bytes
		Input: encrypted bytes array
		Output: decrypted bytes array
	'''
	decrypted_bytes = bytearray((len(encrypted_bytes) - 1)/4)
	
	nextPos1, nextPos2 = 6, 7
	
	for i in range(len(encrypted_bytes) - 1):
		byte = decrypted_bytes[i/4]
		nextPos1, nextPos2 = get_nums(encrypted_bytes[i], get_positions_array(nextPos1, nextPos2))
		byte = set_bit(byte, i%4*2, get_bit(encrypted_bytes[i+1], nextPos1))
		byte = set_bit(byte, i%4*2+1, get_bit(encrypted_bytes[i+1], nextPos2))
		
		decrypted_bytes[i/4] = byte
	
	return str(decrypted_bytes)
	
def get_valid_random_byte(pos1, pos2):
	'''
		Get byte which contains two different positions
		Input: non-available positions (such as previous positions)
		Output: valid byte (contains two different positions)
	'''
	random_byte = get_random_byte()
	valid_positions = get_positions_array(pos1, pos2)
	nextPos1, nextPos2 = get_nums(random_byte, valid_positions) # Get 3 bits nums from positions

	while (nextPos1 == nextPos2): # While the two number equal (not valid)
		random_byte = get_random_byte()
		nextPos1, nextPos2 = get_nums(random_byte, valid_positions)
	return (random_byte,(nextPos1, nextPos2))
	
def get_nums(byte, valid_positions):
	'''
		Gets two numbers, 3 bits each from received byte array
		Input: byte, positions array (numbers will be calculated from received positions, length must be 6)
		Output: two numbers (tuple, contains 0,0 if invalid parameters)
	'''
	nums = [0, 0]
	if (len(valid_positions) == VALID_POSITIONS_LEN): # Six indexes, 3 bits for each num
		for i in range(NUMBERS_COUNT):
			for k in range(VALID_POSITIONS_LEN/NUMBERS_COUNT):
				nums[i] = nums[i] << 1
				nums[i] += get_bit(byte, valid_positions[i*3+k])
	return tuple(nums)
	
def get_bit(byte, index):
	'''
		Get one bit (1/0) from specified index
		Input: byte, index
		Output: 1 / 0 (according to specified bit)
	'''
	index = 7 - index # Reverse direction (left to right)
	return (byte >> index) & 1
	
	
def set_bit(byte, pos, val):
	'''
		Sets bit in chosen position
		Input: byte to edit, bit position, bit value (1/0)
		Output: edited byte
	'''
	pos = 7 - pos # Reverse direction (left to right)
	
	byte = byte & ~(1 << pos)
	byte = byte | (val << pos)
	
	return byte
	
def get_positions_array(pos1, pos2):
	'''
		Returns array from (0-7) in increasing order,
		not including received positions
		Input: two positions to skip
		Output: Increasing array not including pos1, pos2
	'''
	skip_list = [pos1, pos2]
	return [i for i in range(MAX_POSITION) if i not in skip_list]
	
def get_random_byte():
	'''
		Returns a random byte
        Input: none
        Output: the random byte
	'''
	return random.randint(0, MAX_BYTE_VALUE)