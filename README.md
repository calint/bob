# bob
experimental web applications built using server 'b' and object persistence framework 'dbo'

contains
* a performant and compact web application server (package b)
* a framework for composing web applications with re-usable user interface components focused on back-end development
* an object-relational mapping framework for MySQL (package db) supporting cluster of servers, indexes, full-text searches, relations between objects, expandable field definitions for creating custom fields beyond the types provided
* a framework for editing and viewing persistent objects (package bob) with focus on fast development of CRUD like tasks
* and an emulator of a retro 16 bit CPU ['zen-one'](https://github.com/calint/zen-one) (package zen.*) implemented in a web application


explored digitalocean.com and linode.com services
* load balancing using service provider
* ssl enabled using load balancer
* mysql cluster implemented in db package

can be compiled and run by legacy java 1.5
