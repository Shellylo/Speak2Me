import security

while True:
	print security.decrypt(security.encrypt(raw_input("Insert string: ")))
print "Thanks for playing"