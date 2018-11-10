#!/usr/bin/env python
# -*- coding: utf-8 -*-

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC 
import pandas as pd

REGEX_SEARCH = ["^א", "^ב", "^ג", "^ד", "^ה", "^ו", "^ז", "^ח", "^ט", "^י", "^כ", "^ך", "^ל", "^מ", #all hebrew letters for regex search
				"^ם", "^נ", "^ן", "^ס", "^ע", "^פ", "^ף", "^צ", "^ץ", "^ק", "^ר", "^ש", "^ת"]		#

"""
	Searches a word and if it doesn't exists in the wordlist it adds the word to it and to the notwritten list
	
	Input:
		driver - to get the site
		word - the word to search
		wordlist - all words that we already got
		notwritten - all the words that we got and that have'nt been written to the file yet
		
	Output: None

"""				
def search_word(driver, word, wordlist, notwritten):
	page_num = 0
	stop = False
	while not stop: #until reach last page
		driver.get("https://he.forvo.com/search/" + word + "/page-" + str(page_num)) #get search page number [page_num]
		elements = driver.find_elements_by_class_name("word") #find all words in this page (will find phrases as well)
		
		if(len(elements) == 0): #no words found - previous page was last page
			stop = True
		else:
			for element in elements:
				if element.text not in wordlist: #word wasn't already in the wordlist (from another search)
					wordlist.append(element.text)
					notwritten.append(element.text)
					
			if(page_num == 0): #search pages are numbered 0,2,3,4,5...
				page_num = 2
			else:
				page_num += 1

"""
	Writes all the not written words into the file
	
	Input:
		notwritten - all the words that we got and that have'nt been written to the file yet
		
	Output: None

"""				
def write_to_file(notwritten):
	print "WRITING TO FILE"
	file = open("search_words.txt", "a")
	for word in notwritten:
		file.write(word.encode("utf-8") + "\n")
	file.close()

def main():
	options = webdriver.ChromeOptions()                          #
	options.add_experimental_option("prefs", {                   #
	  "download.default_directory": r"E:\Project\Selenium\Data", #
	  "download.prompt_for_download": False,                     #
	  "download.directory_upgrade": True,                        #set options and open up chrome
	  "safebrowsing.enabled": True                               #
	})                                                           #
                                                                 #
	driver = webdriver.Chrome(chrome_options=options)			 #			

	wordlist = [] #all the words that we got from the site
	notwritten = [] #all the words that we got from the site that haven't been written to the file yet

	i = 0 #keep track of progress
	for letter in REGEX_SEARCH: #search ^[letter] for each letter (find all the words that start with the letter)
		search_word(driver, letter, wordlist, notwritten)
		print "DONE: letters - " + str(i+1)
		i += 1

	write_to_file(notwritten)
	notwritten = []

	csv_file = open("hebrew_words.csv", "r") #all hebrew words that appear in the hebrew wikipedia from 2007
	df = pd.read_csv(csv_file, header=None)
	csv_file.close()
	i = 0 #keep track of progress
	for word in df[0][:150000]: #search 150000 most common words in hebrew wikipedia from 2007
		search_word(driver, word, wordlist, notwritten)
		print "DONE: words - " + str(i+1)
		i += 1
		if(i % 1000 == 0): #every 1000 words that we search we add the words that have been found to the file
			write_to_file(notwritten)
			notwritten = []
			
main()