import mysql.connector

from twisted.internet.defer import inlineCallbacks

from autobahn.twisted.wamp import ApplicationSession
from autobahn.wamp.exception import ApplicationError

query = ("SELECT role FROM credentials WHERE login=%s AND password=%s LIMIT 1")
query_id = ("SELECT id FROM credentials WHERE login=%s AND password=%s LIMIT 1")

def checkCredential(user, passw):
	cursor = mysql.connector.connect(user='root', password='', host='mysqlbase', database='semiot').cursor()
	cursor.execute(query,(user,passw))
	result = cursor.fetchone()
	if(result is not None):
          result = result[0]
	cursor.close()
	return result

class AuthenticatorSession(ApplicationSession):

	@inlineCallbacks
	def onJoin(self, details):

		def authenticate(realm, authid, details):
			ticket = details['ticket']			

			role = checkCredential(authid, ticket)
			
			print("WAMP-Ticket custom authenticator invoked: realm='{}', authid='{}', ticket='{}', role='{}'".format(realm, authid, ticket, role))
			
			if(role == "internal"):
				print("Authorization for authid='{}', ticket='{}' was success! Role is 'internal'".format(authid, ticket))
				return u"internal"

			if(role == "admin" or role == "user"):
				print("Authorization for authid='{}', ticket='{}' was success! Role is 'listener'".format(authid, ticket))
				return u"listener"
			else:
				raise ApplicationError("ru.semiot.invalid_grants", "could not authenticate session - bad auth_id '{}' or ticket '{}'".format(authid, ticket))

		try:
			yield self.register(authenticate, 'ru.semiot.authenticate')
			print("WAMP-Ticket custom authenticator registered!")
		except Exception as e:
			print("Failed to register custom authenticator: {0}".format(e))
