# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import youtube_dl
import os
from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait

SUBTITLES_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Subtitles"
AUDIO_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Audio"
DOWNLOAD_AUDIO_PATH =  "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\AudioDownload"

def read_current_index(log_path):
	'''
		Read the last audio / subtitles index saved in log file
		Input: path to the log file
		Output: the index
	'''
	index = 0 # Default value
	try:
		log_file = open(log_path, "r")
		index = log_file.readline()
		log_file.close()
	except Exception, e:
		print e
	return int(index)
	
def download_subtitles(link, name_index):
	'''
		Download one subtitles file (vtt format) from link [File will be located at SUBTITLES_PATH]
		Input: the link, current index (used for naming the file)
		Output: None
	'''
	os.system("youtube-dl -o " + SUBTITLES_PATH + "\\" + str(name_index) + " --sub-lang iw --write-sub --skip-download " + link)
	
	
def download_audio(driver, link, name_index):
	'''
		Download one audio file (mp3 format) from link [File will be located at AUDIO_PATH]
		Input: driver (used to access the internet), the link, current index (used for naming the file)
		Output: None
	'''
	driver.get("https://www.easy-youtube-mp3.com/download.php?" + link[link.find("v="):]) # Request the audio download link
	download_link = driver.find_element_by_class_name('btn-success').get_attribute("href") # Extract the download link
	driver.get(download_link) # Download the link

	# Wait until the file download has finished and move it from temp download folder to folder containing all files
	download_finish = False
	while (not download_finish):
		try:
			for filename in os.listdir(DOWNLOAD_AUDIO_PATH):
				if (".crdownload" not in filename and ".mp3" in filename):
					os.rename(DOWNLOAD_AUDIO_PATH + "\\" + filename, AUDIO_PATH + "\\" + str(name_index) + ".mp3") # Move file to other files location
					download_finish = True
		except Exception, e:
			print e
	
def download_audio_and_subtitles(links_path, chunk_size=15, current_index=0):
	''' 
		Download audio from all YouTube videos found in received file
		Input: path to file with links to YouTube videos,
			   number of links to download (default 15)
			   index of the first link - download will start from it (default 0)
		Output: None
	'''
	# Set driver for downloading audio from the internet
	options = webdriver.ChromeOptions()
	options.add_experimental_option("prefs", {
	  "download.default_directory": DOWNLOAD_AUDIO_PATH,
	  "download.prompt_for_download": False,
	  "download.directory_upgrade": True,
	  "safebrowsing.enabled": True,
	})
	driver = webdriver.Chrome(chrome_options=options)
	
	# Read links from file into list (starting from the current index)
	links_file = open(links_path, "r")
	links_list = links_file.readlines()
	links_file.close()
	links_list_full_len = len(links_list)
	links_list = links_list[current_index:]
	# Download all links
	try:
		i = 0
		while (i < len(links_list) and i < chunk_size):
			print "hi"
			download_audio(driver, links_list[i], current_index + 1)
			download_subtitles(links_list[i], current_index + 1)
			current_index += 1
			i += 1
			print "\nFinished: " + str(current_index) + " Out of: " + str(links_list_full_len) + "\n"
	except Exception, e:
		print e
	finally:
		# Write current index to log file
		log_file = open("log.txt", "w")
		log_file.write(str(current_index))
		log_file.close()
		# Close opened driver
		driver.close()