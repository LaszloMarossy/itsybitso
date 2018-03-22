# itsybitso
bitso trading wrapper implementation.

## setup

- build the application through maven using JDK 1.8
- deploy WAR artifact onto a Tomcat 9.0.6 app server
- start server
- monitor log file [CATALINA_HOME]/logs/itsybitso/main.log

## application startup

After a successful deployment execute the following steps in order, waiting a few secs in between 
(TODO - this can be automated and tied to server startup) 
- `http://localhost:8080/itsybitso/service/orderbook` - get a copy of the orderbook and save it internally
- `http://localhost:8080/itsybitso/service/startmonitor` - start the application monitoring async process
that is responsible for gathering data used by the UI
- `http://localhost:8080/itsybitso/service/populatequeue` - connect to the Bitso diff-orders websocket feed 
and save it to an in-memory queue (TODO later this could be migrated to an external message queue 
implementation or a no-sql type document DB like Redis); for currently observed Bitso traffic the existing solution
should be sufficient.
- `http://localhost:8080/itsybitso/service/consumequeue` - start consuming the diff-order messages 
from the internal queue and apply the diff-order messages to the internal orderbook
- `http://localhost:8080/itsybitso/service/consumewith/3` - (OPTIONAL) change the default number of threads consuming
the diff-order messages to the new value given; this is for performance adjustment if the size of the queue
keeps increasing under heavy load
- run the class `com.itsybitso.fx.UpdateUI` to start the JavaFX UI and push the _Start Monigoring!_
button.


