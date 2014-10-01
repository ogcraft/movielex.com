import smtplib

fromaddr = 'movielex.com@gmail.com'
toaddrs  = 'ogcraft@gmail.com'
msg = 'There was a terrible error that occured and I wanted you to know!'


# Credentials (if needed)
username = 'movielex.com@gmail.com'
password = 'c17h19no3'

# The actual mail send
server = smtplib.SMTP('smtp.gmail.com:587')
server.starttls()
server.login(username,password)
server.sendmail(fromaddr, toaddrs, msg)
server.quit()


