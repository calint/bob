#!/bin/sh
set -e

# on server
apt-get update
apt-get -y install default-jdk git default-mysql-server
cd /
git clone https://github.com/calint/bob
cd /bob
sh build.sh

cat > /etc/systemd/system/bob.service << EOF
[Unit]
Description=bob
After=mysql.service

[Service]
WorkingDirectory=/bob/
ExecStart=/bob/run.sh

[Install]
WantedBy=multi-user.target
EOF

systemctl enable bob

# mysql allow connection from any host
# 	nano /etc/mysql/mysql.conf.d/mysqld.cnf
#		bind-address		= 0.0.0.0
#   	mysqlx-bind-address	= 0.0.0.0
# restart mysql
# 	systemctl restart mysql

# edit dbcluster.txt and add ip:port of cluster members
# cat > /bob/dbcluster.txt


# mysql create database, user and grant access
# 	create database testdb;create user 'c'@'%' identified by 'password';grant all on testdb.* to 'c'@'%';

# change the run.cfg with login for db
# MYSQL_HOST=localhost:3306
# MYSQL_DB=testdb
# MYSQL_USER=c
# MYSQL_PASSWORD=password
# MYSQL_NCONS=10
# B_NTHREADS=10
# B_PORT=8888

# to pull after changing run.cfg
# git stash
# git pull

# add droplet to load balancer
# add droplet to mysql cluster trusted sources
# reboot (logs from the service won't be available otherwise)

# systemctl start bob
# journalctl -u bob -f
