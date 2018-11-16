from selenium import webdriver
#from selenium.webdriver.common.by import By
#from selenium.webdriver.support.ui import WebDriverWait
#from selenium.webdriver.support import expected_conditions as EC  

options = webdriver.ChromeOptions()
options.add_experimental_option("prefs", {
  "download.default_directory": "C:\\Users\\magshimim\\Documents\\Magshimim\\Project\\AudioDownload",
  "download.prompt_for_download": False,
  "download.directory_upgrade": True,
  "safebrowsing.enabled": True,
})
		
driver = webdriver.Chrome(chrome_options=options)
#driver.wait = WebDriverWait(driver, 50)

driver.get("https://www.easy-youtube-mp3.com/download.php?v=e9XUiF7VmLg")
download_link = driver.find_element_by_class_name('btn-success').get_attribute("href")
driver.get(download_link)
#driver.find_element_by_id("login").send_keys("shelly1877@walla.co.il")
#driver.find_element_by_id("password").send_keys("shellynetanel\n")
#driver.find_element_by_css_selector("[for='remember']").click()
#driver.find_element_by_id("password").send_keys("\n")

driver.implicitly_wait(10)
