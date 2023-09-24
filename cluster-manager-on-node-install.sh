#!/bin/sh
# run on the node by cluster-manager-install.sh
set -e
DIR=$(dirname "$0")
cd $DIR

cat > /etc/systemd/system/bob-cluster.service << EOF
[Unit]
Description=bob-cluster

[Service]
WorkingDirectory=/bob/
ExecStart=/bob/cluster-manager-run.sh

[Install]
WantedBy=multi-user.target
EOF

systemctl enable bob-cluster
