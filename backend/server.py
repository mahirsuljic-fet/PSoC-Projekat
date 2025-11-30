from flask import Flask, jsonify, Response
import RPi.GPIO as GPIO
import time

app = Flask(__name__)

# PINOUT
# kako imamo jednostavne komande, jednostavnije je koristiti PIN high/low logiku nego UART protokol
# GPIO modul koristi GPIO numeraciju za interfejs sa pinovima
PIN_FWD = 4
PIN_BWD = 27
PIN_STOP = 22
PIN_LEFT = 23
PIN_RIGHT = 24

PINS = [PIN_FWD, PIN_BWD, PIN_STOP, PIN_LEFT, PIN_RIGHT]

GPIO.setmode(GPIO.BCM)
GPIO.setup(PINS, GPIO.OUT, initial=GPIO.LOW)

def reset_pins() -> None:
    """
    Resetuje sve output pinove konfigurisane u PINS na low.
    """
    for pin in PINS:
        GPIO.output(pin, GPIO.LOW)

def send_cmd(pin : int) -> str:
    """
    Salje naredbu na prosljedjeni pin.
    :param pin: Pin na koji se salje naredba
    :return: Debug string
    """
    reset_pins()
    GPIO.output(pin, GPIO.HIGH)
    time.sleep(0.1)
    return f"Command sent to pin {pin}."

@app.route("/forward")
def forward() -> Response:
    send_cmd(PIN_FWD)
    return jsonify({"status":"forward"})

@app.route("/backward")
def backward() -> Response:
    send_cmd(PIN_BWD)
    return jsonify({"status":"backward"})

@app.route("/stop")
def stop() -> Response:
    send_cmd(PIN_STOP)
    return jsonify({"status":"stop"})

@app.route("/left")
def left() -> Response:
    send_cmd(PIN_LEFT)
    return jsonify({"status":"left"})

@app.route("/right")
def right() -> Response:
    send_cmd(PIN_RIGHT)
    return jsonify({"status":"right"})

@app.route("/")
def index() -> str:
    return "Backend running"

if __name__ == "__main__": # izvrsava se samo ako nije importovano kao modul!
    app.run(host="0.0.0.0", port=5000)