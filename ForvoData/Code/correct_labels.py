#!/usr/bin/env python
# -*- coding: utf-8 -*- 

import pandas as pd
from eyed3 import id3
import os
import unicodedata
import re
"""
	Returns what the label should be by the title
	
	Input: 
		filename - the name of the file
		
	Output: None
"""
def get_label_by_title(filename):
	tag = id3.Tag()
	tag.parse(os.path.join("E:\Project\Selenium\Data" , filename))
	word = tag.title
	if word == None:
		return ""
		
	edited_word = unicodedata.normalize("NFKD", word)        #
	edited_word = "".join([c for c in edited_word if not unicodedata.combining(c)]) #removes all the vowels from the word
	edited_word = edited_word.encode("utf-8")                                       #
	edited_word = re.sub("[\(\[].*?[\)\]]", "", edited_word) #remove all brackets and what is between them
	edited_word = re.sub('[^א-ת ]+', '', edited_word) #remove all non-hebrew letters ***NOTE*** SHOULD I KEEP ,;:!?	
	edited_word = edited_word.strip(" ") #remove spaces from begining and end
	return edited_word

def main():	
	errfile = open("no_label.txt", "w")

	csv_file = open("data.csv", "r") #all hebrew words that appear in the hebrew wikipedia from 2007
	df = pd.read_csv(csv_file, header=None)
	csv_file.close()
	
	for i in range(0, len(df)):
		print i
		title_label = get_label_by_title(df[1][i])
		if title_label == "":
			errfile.write(str(i+1) + "\n")
		if title_label != df[0][i]:
			df[0][i] = title_label
		
	df = df[df[0] != ""] #removes all labels that are None
	df.to_csv("new_data.csv", index=False)
	errfile.close()
	
main()