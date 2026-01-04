from flask import Flask, jsonify, request
from picamera2 import Picamera2
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
FAILSAFE = 26
PIN_HORN = 17

PINS = [PIN_FWD, PIN_BWD, PIN_STOP, PIN_LEFT, PIN_RIGHT]

GPIO.setmode(GPIO.BCM)
GPIO.setup(PINS, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(FAILSAFE, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(PIN_HORN, GPIO.OUT, initial = GPIO.HIGH)

motor_state = {"fwd": False, "bwd": False, "left": False, "right": False, "stop": False}
state_lock = threading.Lock()
last_move_time = time.time()

last_heartbeat_time = time.time()  # vrijeme posljednjeg heartbeat-a
HEARTBEAT_TIMEOUT = 0.3  # 300 ms
last_seq = -1

def handle_header():
    global last_seq 
    seq = int(request.headers.get("sequence"))
    if(seq > last_seq):
        last_seq = seq
        print(seq)
        return True
    return False

picam = Picamera2()
picam.configure(picam.create_video_configuration())
picam.start()

def gen():
    while True:
        frame = picam.capture_array()
        _, jpeg = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' +
               jpeg.tobytes() + b'\r\n')

def set_motor(cmd, value=True):
    with state_lock:
        motor_state[cmd] = value

# ---------- Fail-safe heartbeat checker ----------
def heartbeat_loop():
    global last_heartbeat_time
    while True:
        now = time.time()
        if now - last_heartbeat_time > HEARTBEAT_TIMEOUT:
            # ako je > timeout, posalji na failsafe pin signal, zaustavi sve
            with state_lock:
                for key in motor_state:
                    motor_state[key] = False
            GPIO.output(FAILSAFE, GPIO.LOW)
        time.sleep(0.01)

threading.Thread(target=heartbeat_loop, daemon=True).start()

@app.route("/heartbeat", methods=["POST"])
def heartbeat():
    global last_heartbeat_time
    last_heartbeat_time = time.time()
    return jsonify({"status": "ok"})

@app.route("/forward/on")
def forward_on():
    global last_move_time
    last_move_time = time.time()
    if(handle_header()):
        GPIO.output(PIN_FWD, GPIO.HIGH)
    return jsonify({"status": "forward ON"})

@app.route("/is_moving")
def is_moving():
    moving = (time.time() - last_move_time) < 0.5
    return jsonify({"moving": moving})

@app.route("/forward/off")
def forward_off():
    if(handle_header()):
        GPIO.output(PIN_FWD, GPIO.LOW)
    return jsonify({"status": "forward OFF"})

@app.route("/backward/on")
def backward_on():
    if(handle_header()):
        GPIO.output(PIN_BWD, GPIO.HIGH)
    return jsonify({"status": "backward ON"})

@app.route("/backward/off")
def backward_off():
    if(handle_header()):
        GPIO.output(PIN_BWD, GPIO.LOW)
    return jsonify({"status": "backward OFF"})

@app.route("/left/on")
def left_on():
    global last_move_time
    last_move_time = time.time()
    if(handle_header()):
        GPIO.output(PIN_LEFT, GPIO.HIGH)
    return jsonify({"status": "left ON"})

@app.route("/left/off")
def left_off():
    if(handle_header()):
        GPIO.output(PIN_LEFT, GPIO.LOW)
    return jsonify({"status": "left OFF"})

@app.route("/right/on")
def right_on():
    global last_move_time
    last_move_time = time.time()
    if(handle_header()):
        GPIO.output(PIN_RIGHT, GPIO.HIGH)
    return jsonify({"status": "right ON"})

@app.route("/right/off")
def right_off():
    if(handle_header()):
        GPIO.output(PIN_RIGHT, GPIO.LOW)
    return jsonify({"status": "right OFF"})
    
@app.route("/horn/on")
def horn_on():
    if(handle_header()):
        GPIO.output(PIN_HORN, GPIO.HIGH)
    return jsonify({"status": "horn ON"})
    
@app.route("/horn/off")
def horn_off():
    if(handle_header()):
        GPIO.output(PIN_HORN, GPIO.LOW)
    return jsonify({"status" : "horn OFF"})

@app.route("/stop")
def stop_all():
    GPIO.output(PIN_FWD, GPIO.LOW)
    GPIO.output(PIN_BWD, GPIO.LOW)
    GPIO.output(PIN_LEFT, GPIO.LOW)
    GPIO.output(PIN_RIGHT, GPIO.LOW)
    return jsonify({"status": "all stopped"})

@app.route("/")
def index():
    return "Backend running"

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
