1.	Write a service to sample the device sensors.
	a.	Data sampling should be every 300 milliseconds
	b.	Data sampling should not be done on the main thread.
		(Note: the service should not directly communicate with the activities/fragments).
	c.	Save readings to Realm local DB.
		There should not be more than 500 records in the DB at a given time.
2.	Query the last record from Realm.
3.	Draw the result on the screen with the following logic:
	a.	If the device is facing screen down:
		Background color: red.
	b.	If the device is facing screen up:
		Background color: blue.
	c.	If the device is facing screen towards the user
		Background color: green

Requirements:

1.	New Data should reflect on the screen as it changes, without the user’s interaction.
2.	You can’t use external libraries other than the ones mentioned before.(Realm).
