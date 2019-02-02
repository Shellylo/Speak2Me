# Built-in modules
#import logging
import Tkinter
import threading
import GUI_log
from collections import deque
import time
	
def main():
	root, logger = GUI_log.init_logger()
	queue = deque()
	t1 = threading.Thread(target=GUI_log.listen_and_update, args=(root, logger, queue))
	t1.start()
	i = 0
	while True:
		i += 1
		queue.append(("hi" + str(i), {"IP": 162}))
		time.sleep(1)
main()