# server b

## disclaimers
* naming does not follow Java convention to stand out as low level part of the framework
* 'xwriter' has particularly obscure brief method names serving as the "assembler"
* integrates object-relational mapping framework implemented in package 'db'
* does not include protection from malicious client that opens threaded requests and keeps them open depleting the thread pool
* does not implement SSL assuming it will run in the cloud behind a gateway managing secure connections and possibly denial of service attacks
* possibly scaled horizontally with sticky sessions or round robin with persistent state depending on the application
