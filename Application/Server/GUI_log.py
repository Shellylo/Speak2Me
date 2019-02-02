import logging 
import Tkinter
import ScrolledText

class TextHandler(logging.Handler):
	"""This class allows you to log to a Tkinter scroll_text or Scrolledscroll_text widget"""
	def __init__(self, scroll_text):
		# run the regular Handler __init__
		logging.Handler.__init__(self)

		# Store a reference to the scroll_text it will log to
		self.scroll_text = scroll_text

	def emit(self, record):
		msg = self.format(record)
		def append():
			self.scroll_text.configure(state='normal')
			self.scroll_text.insert(Tkinter.END, msg + '\n')
			self.scroll_text.configure(state='disabled')
			# Autoscroll to the bottom
			self.scroll_text.yview(Tkinter.END)		
		# This is necessary because we can't modify the scroll_text from other threads
		self.scroll_text.after(0, append)
		
def init_logger():
	# Create Tkinter object and ScrolledText
	root =Tkinter.Tk()
	st = ScrolledText.ScrolledText(root, state='disabled')
	st.configure(font='TkFixedFont')
	st.pack()

    # Create text handler
	text_handler = TextHandler(st)
	formatter = logging.Formatter('%(asctime)s.%(msecs)03d %(levelname)s %(IP)s %(message)s', datefmt='%Y-%m-%d,%H:%M:%S')
	text_handler.setFormatter(formatter)
	
    # Create logger
	logger = logging.getLogger()
	logger.addHandler(text_handler)
	logger.setLevel(logging.DEBUG)
	
	return (root, logger)
	
def listen_and_update(root, logger, log_queue):
	while True:
		if log_queue:
			msg, extra = log_queue.popleft()
			logger.info(msg, extra=extra)
			
		root.update_idletasks() #do we really need it?
		root.update()

