import logging 
import Tkinter
import ScrolledText

ACTION = 5 

class TextHandler(logging.Handler):
	"""This class allows you to log to a Tkinter scroll_text or Scrolledscroll_text widget"""
	def __init__(self, scroll_text):
		# run the regular Handler __init__
		logging.Handler.__init__(self)

		# Store a reference to the scroll_text it will log to
		self.scroll_text = scroll_text

	def emit(self, record):
		msg = self.format(record)
		tag = ""
		if record.levelno == ACTION:
			tag = "ACTION"
		elif record.levelno == logging.ERROR:
			tag = "ERROR"
		def append():
			self.scroll_text.configure(state='normal')
			self.scroll_text.insert(Tkinter.END, msg + '\n', tag)
			self.scroll_text.configure(state='disabled')
			# Autoscroll to the bottom
			self.scroll_text.yview(Tkinter.END)		
		# This is necessary because we can't modify the scroll_text from other threads
		self.scroll_text.after(0, append)
		
def init_logger():
	# Create Tkinter object and ScrolledText
	root =Tkinter.Tk()
	st = ScrolledText.ScrolledText(root, state='disabled', height=30, width = 100)
	st.configure(font='TkFixedFont')
	st.pack()
	st.tag_config("ACTION", foreground="dark green")
	st.tag_config("ERROR", foreground="red")

    # Create text handler
	text_handler = TextHandler(st)
	formatter = logging.Formatter('%(asctime)s     IP: %(IP)-15s   PORT: %(PORT)-5s     %(levelname)s: %(message)s', datefmt='%Y-%m-%d,%H:%M:%S') #.%(msecs)03d
	text_handler.setFormatter(formatter)
	
	# Add action level
	logging.addLevelName(ACTION, "ACTION")
	def action(self, message, *args, **kws):
		self._log(ACTION, message, args, **kws) 
	logging.Logger.action = action
	
    # Create logger
	logger = logging.getLogger("GUI")
	logger.addHandler(text_handler)
	logger.setLevel(logging.DEBUG)
	
	return (root, logger)
	
def listen_and_update(root, logger, log_queue):
	while True:
		if log_queue:
			msg, extra, level = log_queue.popleft()
			if level == 1:
				logger.info(msg, extra=extra)
			elif level == 2:
				logger.action(msg, extra=extra)
			elif level == 3:
				logger.error(msg, extra=extra)
			
		root.update_idletasks() #do we really need it?
		root.update()

