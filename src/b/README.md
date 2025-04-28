# server bee

## disclaimers
* naming does not follow Java convention to stand out as low level part of the framework
* 'xwriter' has particularly obscure terse method names serving as the "assembler" of HTML and JavaScript generator for execution by the framework
* integrates object-relational mapping framework implemented in package 'db'
* intended to run one instance of bee server and dbo per virtual machine
* does not include protection from malicious clients that open threaded requests and keeps them open depleting the thread pool
* does not implement SSL assuming it will run in the cloud behind a gateway managing secure connections and denial of service attacks
* possibly scaled horizontally with sticky sessions or round robin with persistent state depending on the application
* it is a back in time so it uses a thread pool instead of the two decades later virtual threads implementation
