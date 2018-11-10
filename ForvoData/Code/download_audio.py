#!/usr/bin/env python
# -*- coding: utf-8 -*- 

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC  
import os
import csv
import unicodedata
import re
import time

count = 1
bad_files = []

"""
	Downloads all the mp3 files of a word and adds them to the data 
	(mp3 file into Data folder and text + filename into csv data file)
	
	Input: 
		driver - to get the site
		word - the word to search
		csvfile - the csv file to write into

	Output: None
"""
def download_word(driver, word, csvfile):
	global count
	
	driver.get("https://he.forvo.com/word/" + word) #gets the word
	elements = driver.find_elements_by_class_name("download") #finds all the mp3 files to download
	
	if(len(elements) == 0): #no elements found - either the word is acutally a phrase or there are no recordings for the word
		driver.get("https://he.forvo.com/phrase/" + word) #gets the phrase
		elements = driver.find_elements_by_class_name("download") #finds all the mp3 files to download

	for element in elements:
		try:
			element.click() #downloads
		
			edited_word = unicodedata.normalize("NFKD", word.decode("utf-8"))        #
			edited_word = "".join([c for c in edited_word if not unicodedata.combining(c)]) # removes all the vowels from the word
			edited_word = edited_word.encode("utf-8")                                       #
			edited_word = re.sub('[^א-ת ]+', '', edited_word) #remove all non-hebrew letters ***NOTE*** SHOULD I KEEP ,;:!?	
			
			newfilename = "sample" + str(count) + ".mp3"
			downloaded = False
			start_time = time.time() #15 seconds max per word
			while not downloaded:
				for filename in os.listdir("E:\Project\Selenium\Download"):
					if "pronunciation" in filename and ".crdownload" not in filename and filename not in bad_files: #checks if file is already downloaded
						try:
							os.rename(os.path.join("E:\Project\Selenium\Download" ,filename), os.path.join("E:\Project\Selenium\Data" ,newfilename)) #rename file to "sample[INDEX]"
							count += 1
							add_to_csv(csvfile, edited_word, newfilename)
						except:
							bad_files.append(filename)
						
						downloaded = True
						
				if time.time() - start_time >= 15:
					append_to_file("cant_download.txt", word)
					downloaded = True

		except Exception as e:
			append_to_file("cant_download.txt", word)
"""
	Adds a word and its mp3 filename into the csv data file
	
	Input: 
		csvfile - the csv file to write into
		word - the word in text
		filename - the name of the mp3 file that says the word

	Output: None
"""		
def add_to_csv(csvfile, word, filename):
	writer = csv.writer(csvfile)
	writer.writerow([word] + [filename]) #column A - word, column B - filename
	
"""
	Checks if the word is in hebrew
	
	Input: 
		word - the word to check_if_hebrew
		
	Output: 
		True - if contains hebrew characters
		False - doesn't contain hebrew characters
"""
def check_if_hebrew(word):
	return any(u"\u0590" <= c <= u"\u05EA" for c in word.decode("utf-8"))
	
"""
	Appends text to file
	
	Input:
		filename - name of the file to append to
		text - text to append
		
	Output: None
"""
def append_to_file(filename, text):
	file = open(filename, "a")
	file.write(text + "\n")
	file.close()

def main():
	options = webdriver.ChromeOptions()                              #
	options.add_experimental_option("prefs", {                 	     #
	  "download.default_directory": r"E:\Project\Selenium\Download", #
	  "download.prompt_for_download": False,                    	 #
	  "download.directory_upgrade": True,                        	 #set options and open up chrome
	  "safebrowsing.enabled": True                               	 #
	})                                                           	 #
																	 #
	driver = webdriver.Chrome(chrome_options=options)			 	 #			

	driver.get("https://he.forvo.com/login/")                              #
	driver.find_element_by_id("login").send_keys("shelly1877@walla.co.il") #login
	driver.find_element_by_id("password").send_keys("shellynetanel")       #
	driver.find_element_by_id("password").send_keys("\n")                  #
		
	csvfile = open("data.csv", "wb") #open data file to write into
	wordfile = open("search_words.txt", "r") #open file to get words to download
	words = wordfile.readlines() #turn file into list of words
	wordfile.close()
	num = str(len(words)) #get the amount of words in the file
	i = 1 #keep track of progress
	for word in words:
		word = word[:-1] #remove \n
		if check_if_hebrew(word):
			download_word(driver, word, csvfile)
		else:
			append_to_file("non_hebrew.txt", word)
			
		print "DONE: " + str(i) + "/" + num
		i += 1
	
	csvfile.close()

main()
