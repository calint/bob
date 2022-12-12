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

# edit the firewall and allow access to the mysql port to your servers and your ip

# mysql allow connection from any host
# 	nano /etc/mysql/mysql.conf.d/mysqld.cnf
#		bind-address		= *
# systemctl restart mysql

# mysql create database, user and grant access
# 	create database testdb;create user 'c'@'%' identified by 'password';grant all on testdb.* to 'c'@'%';

# edit and add IPs of cluster members
# cat > /bob/cluster.cfg

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
