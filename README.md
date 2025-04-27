# bob

a journey two decades back in time to Java 1.5

experimental web applications using server 'bee' and object persistence framework 'dbo'

can be compiled and run by legacy Java 1.5

contains
* a performant and compact web application server (package b)
* a framework for composing web applications with re-usable user interface components focused on back-end development
* an object-relational mapping framework for MySQL (package db) supporting cluster of servers, indexes, full-text searches, relations between objects, expandable field definitions for creating custom fields beyond the types provided, source code generator from definition (package jem)
* a framework for editing and viewing persistent objects (package bob) with focus on fast development of CRUD like tasks supporting relations
* an emulator of a retro 16 bit CPU ['zen-one'](https://github.com/calint/zen-one) (packages zen.*) implemented in web application at URI '/zen'


explored digitalocean.com and linode.com services
* load balancing using service provider
* SSL enabled using load balancer
* MySQL cluster implemented in package 'db'


how-to:
* setup database
`echo "create database testdb; create user 'c'@'%' identified by 'password'; grant all on testdb.* to 'c'@'%';" | mysql`
* prepare python for tests
`sudo pacman -S python-websockets`
* activate legacy Java
`export PATH=~/java/jdk1.5.0_22/bin:$PATH`
* start server
`./build-and-run.sh`
* run tests (note: websock test may take a while)
`qa/qa.sh`