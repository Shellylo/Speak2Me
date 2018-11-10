#!/usr/bin/env python
# -*- coding: utf-8 -*- 

import os
from eyed3 import id3
import unicodedata
import re
import csv

def add_to_csv(csvfile, word, filename):
	writer = csv.writer(csvfile)
	writer.writerow([word] + [filename]) #column A - word, column B - filename
	
def main():
	csvfile = open("data.csv", "ab") #open data file to write into
	for filename in os.listdir("E:\Project\Selenium\Download"):
		tag = id3.Tag()
		tag.parse(os.path.join("E:\Project\Selenium\Download" ,filename))
		word = tag.title
		
		edited_word = unicodedata.normalize("NFKD", word)        #
		edited_word = "".join([c for c in edited_word if not unicodedata.combining(c)]) # removes all the vowels from the word
		edited_word = edited_word.encode("utf-8")                                       #
		edited_word = re.sub('[^א-ת ]+', '', edited_word) #remove all non-hebrew letters ***NOTE*** SHOULD I KEEP ,;:!?	
		
		add_to_csv(csvfile, edited_word, filename)
		
main()