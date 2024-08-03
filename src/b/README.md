# server bee

## disclaimers
* naming does not follow Java convention to stand out as low level part of the framework
* 'xwriter' has particularly obscure terse method names serving as the "assembler" of HTML and JavaScript generator for execution by the framework
* integrates object-relational mapping framework implemented in package 'db'
* does not include protection from malicious client that opens threaded requests and keeps them open depleting the thread pool
* does not implement SSL assuming it will run in the cloud behind a gateway managing secure connections and denial of service attacks
* possibly scaled horizontally with sticky sessions or round robin with persistent state depending on the application
