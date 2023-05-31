# bob
experimental web applications built using server 'b' and object persistence framework 'dbo'

contains
* a performant and compact web application server (package b)
* a framework for composing web applications with re-usable user interface components focused on back-end development
* an object-relational mapping framework for MySQL (package db) supporting clusters
- supports relations between objects
- indexes including full-text
* a framework for editing and viewing persistent objects (package bob)
* and ['zen-one'](https://github.com/calint/zen-one) emulator (package zen.*) 


explored digitalocean.com and linode.com services
* load balancing using service provider
* ssl enabled using load balancer
* mysql cluster implemented in db package

can be compiled and run by legacy java 1.5
