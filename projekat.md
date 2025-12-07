# Opis projekta
Tema projekta je automobil koji se kontroliše putem mobilne aplikacije.
Automobil će imati 4 točka, 2 kočnice, parking senzore, LED svjetla, sposobnost detektovanja svjetla na semaforu, saobraćajnih znakova, prevenciju prelazka preko linije, itd.
Mobilna aplikacija će biti napisana u Kotlin programskom jeziku koristeći Android Studio i Jetpack Compose.
Komunikacija između mobilne aplikacije i Raspberry Pi će se odvijati putem HTTP protokola.
Komunikacija između Raspberry Pi i FPGA će se odvijati putem nekog serijskog transfer protokola (UART, I2C, SPI ili USB).

# Hardver sa tabelom ulaza i izlaza
| Upravljačke ploče       | Slika |
| ----------------------- | ----- |
| Raspberry Pi 4          | <img src="slike/RaspberryPi4.jpg" width="200"/> |
| Altera Cyclone 4 (FPGA) | <img src="slike/AlteraCyclone4.png" width="200"/> |
| ESP-01s                 | <img src="slike/ESP-01s.jpg" width="200"/> |

| Ulazi                             | Upravljačka ploča | Namjena                          | Slika |
| --------------------------------- | ----------------- | -------------------------------- | ----- |
| Ultrasonični senzor (HC-SR04P) x2 | FPGA              | Parking senzori                  | <img src="slike/UltrasonicSenzor.jpg" width="200"/> |
| Kamera (Logitech C512)            | Raspberry PI      | Detekcija znakova i video stream | <img src="slike/Kamera.jpg" width="200"/> |
| Infracrveni senzor x2             | FPGA              | Detekcija linija                 | <img src="slike/InfracrveniSenzor.jpg" width="200"/> |

| Izlazi               | Upravljačka ploča | Namjena                  | Slika |
| -------------------- | ----------------- | ------------------------ | ----- |
| DC motori x4         | FPGA              | Kontrola kretanja        | <img src="slike/DCMotori.jpg" width="200"/> |
| Servo motori x2      | FPGA              | Kočnice                  | <img src="slike/ServoMotor.jpg" width="200"/> |
| Aktivni piezo buzzer | FPGA              | Zvučna signalizacija     | <img src="slike/PiezoBuzzer.jpg" width="200"/> |
| LED (nekoliko)       | FPGA              | Svjetlosna signalizacija | <img src="slike/LEDs.jpg" width="200"/> |

| Dodatno                  | Upravljačka ploča | Namjena                  | Slika |
| ------------------------ | ----------------- | ------------------------ | ----- |
| DC motor driver L298N x2 | FPGA              | Kontrola motora          | <img src="slike/MotorDriver.jpg" width="200"/> |
| LED semafor              | ESP-01s           | Svjetlosna signalizacija | <img src="slike/Semafor.jpg" width="200"/> |

# Dijagram upravljanja
``` mermaid
%%{init: { "fontFamily": "GitLab Sans" }}%%
flowchart LR
    accTitle: Flowchart

    Camera(Kamera)
    RPi(Raspberry Pi)
    FPGA(Altera Cyclone 4<br>FPGA)
    Driver(DC motor driver)
    Motors(DC motori)
    Inputs(Ultrasonični senzor<br>Infracrveni senzor)
    Outputs(LED<br>Piezo buzzer<br>Servo motori)
    App(Mobilna aplikacija)
    ESP(ESP-01s)
    TL(Semafor)

    App --> RPi
    Camera --> RPi
    RPi --> FPGA
    FPGA --> Driver --> Motors
    Inputs --> FPGA
    FPGA --> Outputs

    ESP --> TL
```

Raspberry Pi će uzimati video stream od kamere i pomoću AI detektovati koje je svjetlo upaljeno na semaforu, kao i STOP znak.
Taj video stream će također slati na mobilnu aplikaciju.
Mobilna aplikacija će slati kontrolne signale (kretanje, kočenje i zvučni signal) na Raspberry Pi koji će dalje te signale proslijediti do FPGA.
FPGA će uzimati i procesirati signale sa Raspberry Pi, ultrasoničnih i infracrvenih senzora, i procesirati ih.
Nakon procesiranja signala će slati kontrole signale ostalim komponentama kao što su LED na autu, piezo buzzer, servo motore i DC motor driver koji će dalje pokretati DC motore na koje su pričvršćeni točkovi.

