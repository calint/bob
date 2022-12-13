#!/bin/sh
set -e

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

# ! edit the firewall and allow access to the mysql port to your servers and your ip

# mysql allow connection from any host
cp -a /etc/mysql/mysql.conf.d/mysqld.cnf /etc/mysql/mysql.conf.d/mysqld.cnf.bak
cat /etc/mysql/mysql.conf.d/mysqld.cnf.bak | sed "s/\s*bind-address.*/bind-address=*/g" > /etc/mysql/mysql.conf.d/mysqld.cnf
systemctl restart mysql

# mysql create database, user and grant access
echo "create database testdb;create user 'c'@'%' identified by 'password';grant all on testdb.* to 'c'@'%';" | mysql

# add droplet to load balancer
# add droplet to mysql cluster trusted sources

# reboot (logs from the service won't be available otherwise)
reboot
