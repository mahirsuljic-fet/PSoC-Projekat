# Flask backend 
Kako bi Kotlin aplikacija mogla jednostavno komunicirati sa backend-om potrebno je da backend uvijek bude dostupan na istom host-u i istom port-u.

RPi će od momenta boot-anja biti konfigurisan da aktivira svoj hotspot, a android aplikacija se direktno povezuje na tu mrežu.
RPi će imati static IP DHCP konfiguraciju tako da svaki put dobiva istu private IPv4 adresu, npr. 192.168.1.4.
Flask aplikaciji uvijek će biti dodijeljen port 5000.
Tako android aplikacija svaki request šalje na 192.168.1.4:5000/ + odgovarajući endpoint.

## Endpoints
Endpoint-i koje backend expose-a su sljedeći:
 + <code>/forward</code>
   + efektivno postavlja outpin pin na RPi na nivo HIGH, što FPGA dekodira kao kretanje unaprijed
 + <code>/backward</code>
   + efektivno postavlja output pin na RPi na nivo HIGH, što FPGA dekodira kao kretanje unazad
 + <code>/stop</code>
   + efektivno postavlja output pin na RPi na nivo HIGH, što FPGA dekodira kao zaustavljanje kretanja
 + <code>/left</code>
   + efektivno postavlja output pin na RPi na nivo HIGH, što FPGA dekodira kao kretanje lijevo
 + <code>/right</code>
   + efektivno postavlja output pin na RPi na nivo HIGH, što FPGA dekodira kao kretanje desno
 + <code>/</code>
   + index ili root path, ne radi ništa

## Startup
Kako ovaj backend servis treba da radi od samog starta, bit će kreiran sytemd servis koji će prilikom boot-a Raspbian sistema pokrenuti  i ovu Flask aplikaciju.


