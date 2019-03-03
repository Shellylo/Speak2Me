import security

while True:
	b = bytearray()
	b.extend(raw_input("Insert string: "))
	a = security.encrypt(b)
	f = open("encrypted", "wb")
	f.write(a)
	f.close()
	print a
	print security.decrypt(a)
print "Thanks for playing"