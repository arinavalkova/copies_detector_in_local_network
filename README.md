# Copies detector in local network
Application locating copies of yourself on the local network
# About
The application discovers copies of itself on the local network by exchanging multicast UDP messages. The application keeps track of the moments of appearance and disappearance of other copies of itself in the local network and, when changed, displays a list of IP addresses of "live" copies.

The multicast group address is passed as a parameter to the application. The application supports work in both IPv4 and IPv6 networks, it will select the protocol automatically depending on the transmitted group address.
