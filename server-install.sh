#!/bin/sh
set -e

# on server
apt-get -y install default-jdk git
cd /
git clone https://github.com/calint/bob
cd /bob
sh build.sh

cat > /etc/systemd/system/bob.service << EOF
[Unit]
Description=bob

[Service]
WorkingDirectory=/bob/
ExecStart=/bob/run.sh

[Install]
WantedBy=multi-user.target
EOF

systemctl enable bob

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

# reboot (logs from the service won't be available otherwise)
# add droplet to mysql cluster trusted sources
# add droplet to load balancer

# systemctl start bob
# journalctl -u bob -f
