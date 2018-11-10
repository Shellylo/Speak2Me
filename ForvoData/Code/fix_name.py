#THIS CODE SHOULD IS A CORRECTION CODE - IT IS IN PYTHON 3 AND IT CORRECTED SOME BUGS THAT COULDN'T BE DONE IN PYTHON 2

import os

start_index = 41141
for filename in os.listdir("E:\Project\Selenium\Download"):
	if not filename.startswith("sample"):
		newfilename = "sample" + str(start_index) + ".mp3"
		os.rename(os.path.join("E:\Project\Selenium\Download" ,filename), os.path.join("E:\Project\Selenium\Download" ,newfilename))
		start_index += 1
		



