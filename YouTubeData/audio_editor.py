# -*- coding: utf-8 -*-
import pydub
import sys
import re
import os
import pandas as pd

AUDIO_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Audio\\"
SUBTITLES_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Subtitles\\"
DATA_PATH = "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\Data\\"
MS_TO_SEC = 1000
MS_TO_MIN = MS_TO_SEC * 60
MIN_TO_HR = MS_TO_MIN * 60

SEGMENT_START_TIME_END = 12
SEGMENT_END_TIME_START = 17

def cut_and_export(index, audio, audio_start, audio_end):
	"Cut the video and export edited video (mp3 format) to the current folder"
	if (audio_end == 0):
		edited_audio = audio[audio_start:]
	else:
		edited_audio = audio[audio_start:audio_end]
	edited_audio_path = DATA_PATH + "sample" + str(index) + ".mp3"
	edited_audio.export(edited_audio_path, format="mp3")
	return edited_audio_path

def remove_punctuation(str):
	str = re.sub("[\(\[].*?[\)\]]", "", str) #remove all brackets and what is between them
	str = re.sub('[^א-ת -]+', '', str) #remove all non-hebrew letters
	str = str.strip(" ") #remove spaces from begining and end
	return str
	
def contain_english_char(str):
	return any(('a' <= c <= 'z' or 'A' <= c <= 'Z') for c in str.decode("utf-8"))
	
def create_audio_segments(audio, audio_titles, count):
	audio_segments_df = pd.DataFrame([], columns=['Subtitles','Audio']) # create csv format for the audio segments
	audio_titles.append('\n') # make sure that the titles end with \n (prevent endless loop)
	i = 1 # first time in file
	while (i < len(audio_titles)):
	
		# init current segment's subtitles details
		invalid_titles = False
		subtitles = ""
		
		# cut audio segment and save it
		if (audio_titles[i] != '\n'):
			seg_start, seg_end = get_segment_time(audio_titles[i]) # receive segment time
			edited_audio_path = cut_and_export(count, audio, seg_start, seg_end) # cut and save segment
			i += 1 # continue to the subtitles
		
		# read and edit all the subtitles for current segment
		while (audio_titles[i] != '\n'):
			if (contain_english_char(audio_titles[i])): # check if subtitles contain english words
				invalid_titles = True
			else:
				subtitles += remove_punctuation(audio_titles[i]) + " " # remove unnecessary characters
			i += 1
		
		# add audio (if its subtitles are valid) to the csv format dataframe
		if (invalid_titles):
			print "Invalid Subtitles"
			os.remove(edited_audio_path) # remove audio with invalid subtitles
		else:
			#print subtitles[:-1]
			audio_segments_df = audio_segments_df.append(pd.DataFrame([[subtitles[:-1] , "sample" + str(count) + ".mp3"]], columns=['Subtitles', 'Audio']))
			count += 1
			print str(count-1) + " created!"
		i += 2 # continue to the next time (line after \n in segment count)
	return audio_segments_df
	
def get_time_in_ms(time_list):
	return int(time_list[0]) * MIN_TO_HR + int(time_list[1]) * MS_TO_MIN + int(time_list[2]) * MS_TO_SEC + int(time_list[3])
	
def get_segment_time(time_line):
	"receive the beginning and end of the segment"
	seg_start = time_line[:SEGMENT_START_TIME_END].replace(",", ":").split(":")
	seg_end = time_line[SEGMENT_END_TIME_START:-1].replace(",", ":").split(":") # exclude \n
	return (get_time_in_ms(seg_start), get_time_in_ms(seg_end))
	
def get_audio_titles(titles_path):
	titles_file = open(titles_path, "r")
	audio_titles = titles_file.readlines()
	titles_file.close()
	return audio_titles
	
def load_audio(audio_path):
	return pydub.AudioSegment.from_mp3(audio_path)

def main():
	audio = load_audio(AUDIO_PATH + "1.mp3")
	print "Audio Loaded!"
	audio_titles = get_audio_titles(SUBTITLES_PATH + "1.srt")
	audio_df = create_audio_segments(audio, audio_titles, int(sys.argv[1]))
	audio_df.to_csv(SUBTITLES_PATH + "subtitles.csv", index = False)

main()