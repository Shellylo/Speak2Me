# -*- coding: utf-8 -*-
import pydub
import sys
import re
import os
import pandas as pd
import downloadsetup as dsetup
import unicodedata

AUDIO_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Audio\\"
SUBTITLES_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Subtitles\\"
DATA_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Data\\"
LINKS_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\shelly-and-netanel\\YouTubeData\\audio_links.txt"
LOG_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\shelly-and-netanel\\YouTubeData\\log.txt"
CSV_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\shelly-and-netanel\\YouTubeData\\"

MS_TO_SEC = 1000
MS_TO_MIN = MS_TO_SEC * 60
MIN_TO_HR = MS_TO_MIN * 60

SEGMENT_START_TIME_END = 12
SEGMENT_END_TIME_START = 17
SEGMENT_END_TIME_END = SEGMENT_END_TIME_START + SEGMENT_START_TIME_END

VTT_SUBTITLES_START = 4
ENDNING_LENGTH = -4

def cut_and_export(index, audio, audio_start, audio_end):
	'''
		Cut the audio and export edited audio (mp3 format) to the current folder
		Input: Current index (for indexing the audio), audio start and end times
		Output: Path to audio location
	'''
	if (audio_end == 0):
		edited_audio = audio[audio_start:]
	else:
		edited_audio = audio[audio_start:audio_end]
	edited_audio_path = DATA_PATH + "sample" + str(index) + ".mp3"
	edited_audio.export(edited_audio_path, format="mp3")
	return edited_audio_path

def remove_punctuation(str):
	'''
		Removes unnecessary details (in order to create valid label)
		Input: subtitle
		Output: edited subtitle (without unnecessary punctuation)
	'''
	# Remove letters vowels signs
	str = unicodedata.normalize("NFKD", str.decode("utf-8"))
	str = "".join([c for c in str if not unicodedata.combining(c)]) 
	str = str.encode("utf-8")
	
	str = re.sub("[\(\[].*?[\)\]]", "", str) # Remove all brackets and what is between them
	str = re.sub('[^א-ת -]+', '', str) # Remove all non-hebrew letters
	str = str.strip(" ") # Remove spaces from begining and end
	str = str.replace("\xab", "") # Remove bad character
	
	return str
	
def contain_english_char(str):
	'''
		Check if received string contains english characters
		Input: the string for checking
		Output: true if contains english characters, false otherwise
	'''
	return any(('a' <= c <= 'z' or 'A' <= c <= 'Z') for c in str.decode("utf-8"))
	
def create_audio_segments(audio, audio_subtitles, count):
	'''
		Cut audio to segments (using segments defined in the vtt format subtitles file)
		Audio segments and matching subtitles will be saved into csv
		Input: Current audio, subtitles, audio segment count
		Output: Dataframe containing audio segments and their subtitles, Current audio segment count
	'''
	
	audio_segments_df = pd.DataFrame([], columns=['Subtitles','Audio']) # create csv format for the audio segments
	i = VTT_SUBTITLES_START # skip details vtt file
	while (i < len(audio_subtitles)):
	
		# init current segment's subtitles details
		invalid_titles = True
		subtitles = ""
		
		# cut audio segment and save it
		if (audio_subtitles[i] != '\n'):
			seg_start, seg_end = get_segment_time(audio_subtitles[i]) # receive segment time
			edited_audio_path = cut_and_export(count, audio, seg_start, seg_end) # cut and save segment
			i += 1 # continue to the subtitles
		
		# read and edit all the subtitles for current segment
		while (audio_subtitles[i] != '\n'):
			if (not contain_english_char(audio_subtitles[i])): # check subtitles doesn't contain english words
				invalid_titles = False
				subtitles += remove_punctuation(audio_subtitles[i]) + " " # remove unnecessary characters
			i += 1
		
		# add audio (if its subtitles are valid) to the csv format dataframe
		if (invalid_titles):
			print "Invalid Subtitles!", audio_subtitles[i]
			os.remove(edited_audio_path) # remove audio with invalid subtitles
		else:
			audio_segments_df = audio_segments_df.append(pd.DataFrame([[subtitles[:-1] , "sample" + str(count) + ".mp3"]], columns=['Subtitles', 'Audio']))
			count += 1
			print str(count-1) + " created!"
		i += 1 # continue to the next time (line after \n in segment count)
	return audio_segments_df, count
	
def get_time_in_ms(time_list):
	'''
		Input: list with string describing subtitle time (hr:min:sec:ms)
		Output: total time in MS
	'''
	return int(time_list[0]) * MIN_TO_HR + int(time_list[1]) * MS_TO_MIN + int(time_list[2]) * MS_TO_SEC + int(time_list[3])
	
def get_segment_time(time_line):
	'''
		Receive the beginning and end of the segment
		Input: Time line - the line of the time (in subtitles file)
		Output: Start time, end time 
	'''
	seg_start = time_line[:SEGMENT_START_TIME_END].replace(".", ":").split(":")
	seg_end = time_line[SEGMENT_END_TIME_START:SEGMENT_END_TIME_END].replace(".", ":").split(":") # exclude \n
	return (get_time_in_ms(seg_start), get_time_in_ms(seg_end))
	
def get_audio_titles(titles_path):
	'''
		Input: subitles file path (vtt format)
		Output: list of all the file contents (subtitles and its times)
	'''
	titles_file = open(titles_path, "r")
	audio_titles = titles_file.readlines()
	titles_file.close()
	return audio_titles
	
def load_audio(audio_path):
	'''
		Load audio from file (into python data structure)
		Input: path to the file
		Output: the audio object
	'''
	return pydub.AudioSegment.from_mp3(audio_path)

def main():
	dsetup.download_audio_and_subtitles(LINKS_PATH, dsetup.read_current_index(LOG_PATH)) # Download all the audio and its subtitles
	index = int(sys.argv[1]) # index (for naming the files) starts with input
	try:
		for file in os.listdir(AUDIO_PATH):
			print os.path.join(AUDIO_PATH + file)
			audio = load_audio(os.path.join(AUDIO_PATH, file))
			print "Audio Loaded!"
			audio_titles = get_audio_titles(SUBTITLES_PATH + file[:ENDNING_LENGTH] + ".iw.vtt")
			audio_df, index = (create_audio_segments(audio, audio_titles, index))
			audio_df.to_csv(CSV_PATH + "subtitles.csv", index = False, header = False, mode = "a")
	except Exception, e:
		print e

main()