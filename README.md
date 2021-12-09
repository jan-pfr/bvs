# Components of distributed Systems

## Task
The task was to design an actor system that imports information from a .csv file (`jena.csv`), processes them and writes them to a relational database system. The data are temperature values that were collected over the course of several years. The moving average of a maximum of 24 hours should be determined and written into the database. The transmission should take place over the network and the actuators should not be dependent on each other. No data should be lost. In addition the determined data should be readable over a HTTP request.

## My approach
In order to guarantee an independency, I designed a `RegistryActor`, which serves as the input node for the actor system.
It stores the reference of an actor when it connects to the system and gives it the references it needs.
The data from the file is bundled and sent in packets over the network so as not to overload the mailboxes of the other actors. For the moving average, the values of the last 24 hours are collected in the `CalculateAverageActor`. This cache is limited to 24 hours, starting from the most recent value being processed. The processed data are also collected again, and sent in packets to the `DatabaseActor`, which writes them with prepared statements into a H2 database.
