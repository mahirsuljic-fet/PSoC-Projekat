from flask import Flask, jsonify
import RPi.GPIO as GPIO
import time
import threading

app = Flask(__name__)

# PINOUT
PIN_FWD = 4
PIN_BWD = 27
PIN_STOP = 22
PIN_LEFT = 23
PIN_RIGHT = 24

PINS = [PIN_FWD, PIN_BWD, PIN_STOP, PIN_LEFT, PIN_RIGHT]

GPIO.setmode(GPIO.BCM)
GPIO.setup(PINS, GPIO.OUT, initial=GPIO.LOW)

motor_state = {"fwd": False, "bwd": False, "left": False, "right": False, "stop": False}
state_lock = threading.Lock()

def motor_loop():
    last_pin_state = {pin: False for pin in PINS}

    while True:
        with state_lock:
            active_pins = []
            if motor_state["fwd"]: active_pins.append(PIN_FWD)
            if motor_state["bwd"]: active_pins.append(PIN_BWD)
            if motor_state["left"]: active_pins.append(PIN_LEFT)
            if motor_state["right"]: active_pins.append(PIN_RIGHT)

        for pin in PINS:
            desired = pin in active_pins
            if last_pin_state[pin] != desired:
                GPIO.output(pin, GPIO.HIGH if desired else GPIO.LOW)
                last_pin_state[pin] = desired

        time.sleep(0.01)  # fast loop, smooth response

threading.Thread(target=motor_loop, daemon=True).start()

def set_motor(cmd, value=True):
    with state_lock:
        motor_state[cmd] = value

@app.route("/forward/on")
def forward_on():
    set_motor("fwd", True)
    return jsonify({"status": "forward ON"})

@app.route("/forward/off")
def forward_off():
    set_motor("fwd", False)
    return jsonify({"status": "forward OFF"})

@app.route("/backward/on")
def backward_on():
    set_motor("bwd", True)
    return jsonify({"status": "backward ON"})

@app.route("/backward/off")
def backward_off():
    set_motor("bwd", False)
    return jsonify({"status": "backward OFF"})

@app.route("/left/on")
def left_on():
    set_motor("left", True)
    return jsonify({"status": "left ON"})

@app.route("/left/off")
def left_off():
    set_motor("left", False)
    return jsonify({"status": "left OFF"})

@app.route("/right/on")
def right_on():
    set_motor("right", True)
    return jsonify({"status": "right ON"})

@app.route("/right/off")
def right_off():
    set_motor("right", False)
    return jsonify({"status": "right OFF"})

@app.route("/stop")
def stop_all():
    with state_lock:
        for key in motor_state:
            motor_state[key] = False
    return jsonify({"status": "all stopped"})

@app.route("/")
def index():
    return "Backend running"

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
