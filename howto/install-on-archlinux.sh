#!/bin/sh
sudo pacman -S mariadb
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
sudo sed -i '/\[mysqld\]/a bind-address=0.0.0.0' /etc/my.cnf.d/server.cnf
sudo systemctl start mariadb
sudo mariadb << EOF
create user 'c'@'localhost' identified by 'password';
create user c identified by 'password';
create database testdb;
grant all privileges on testdb.* to 'c'@'localhost';
grant all privileges on testdb.* to 'c'@'%';
flush privileges;
EOF

# note: user 'c' is created both for access from anywhere and localhost
#       https://mariadb.com/kb/en/troubleshooting-connection-issues/#localhost-and
