"""
detector.py

Detektor STOP znaka i crvenog svjetla na semaforu.
"""

import cv2
import numpy as np
import time
import threading
import collections
import argparse
import sys
import requests  # <--- dodano za pitati Flask server da li se auto kreće

try:
    import RPi.GPIO as GPIO
    GPIO.setmode(GPIO.BCM)
    HAVE_GPIO = True
except Exception:
    HAVE_GPIO = False

# ---------- CONFIG ----------
CASCADE_PATH = "stop_sign.xml"  # putanja do Haar Cascade modela za prepoznavanje STOP znaka
CAM_INDEX = 0

WORK_WIDTH = 640
WORK_HEIGHT = 480

STOP_HOLD_ACTIVE = False
STOP_HOLD_END = 0


USE_ROI = False  # za detekciju samo unutar odredjene regije
ROI = (200, 50, 440, 380)  # ima smisla samo ako je gornje True

# STOP znak
STOP_MIN_AREA = 1500  # minimalan povrsina u pixelima koju znak mora imat da bi bio detektovan
STOP_FRAMES_NEEDED = 3  # minimaln broj uzastopnih frame-ova da bi znak bio prepoznat
STOP_HOLD_SECONDS = 3.0  # koliko dugo zaustavljamo robot-a
STOP_COOLDOWN_SECONDS = 8.0 # koliko dugo imamo za pokrenut se

# konfiguracija detekcije crvenog svjetla
LOWER_RED1 = np.array([0, 120, 120])
UPPER_RED1 = np.array([10, 255, 255])
LOWER_RED2 = np.array([160, 120, 120])
UPPER_RED2 = np.array([179, 255, 255])

RED_MIN_AREA = 1000
RED_MAX_AREA = 3000
RED_MIN_BRIGHTNESS = 50

LED_MAX_SIZE = 80
CIRCULARITY_MIN = 0.3

RED_FRAMES_ON_NEEDED = 10
RED_FRAMES_OFF_NEEDED = 10

GPIO_RED_PIN = 12  # pin na koji saljemo signal crvenog svjetla
GPIO_STOP_PIN = 16  # pin na koji saljemo signal nakon detekcije stop znaka

CASCADE_SCALE_FACTOR = 1.1
CASCADE_MIN_NEIGHBORS = 5
CASCADE_MIN_SIZE = (40, 40)

MORPH_KERNEL = np.ones((3, 3), np.uint8)
# ---------- CONFIG ----------

if HAVE_GPIO:
    try:
        GPIO.setup(GPIO_RED_PIN, GPIO.OUT, initial=GPIO.LOW)
        GPIO.setup(GPIO_STOP_PIN, GPIO.OUT, initial=GPIO.LOW)
    except Exception:
        HAVE_GPIO = False

def gpio_write(pin, value):
    if HAVE_GPIO:
        try:
            GPIO.output(pin, GPIO.HIGH if value else GPIO.LOW)
        except Exception:
            pass

class VideoCaptureThread:
    def __init__(self, src, width, height):
        self.cap = cv2.VideoCapture(src)
        self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, width)
        self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, height)
        self.frame = None
        self.lock = threading.Lock()
        self.stop_flag = False
        threading.Thread(target=self._loop, daemon=True).start()

    def _loop(self):
        while not self.stop_flag:
            ret, f = self.cap.read()
            if ret:
                with self.lock:
                    self.frame = f

    def read(self):
        with self.lock:
            if self.frame is None:
                return None
            return self.frame.copy()

    def stop(self):
        self.stop_flag = True
        try:
            self.cap.release()
        except:
            pass

def apply_roi(img):
    if not USE_ROI:
        return img, (0, 0, img.shape[1], img.shape[0])
    x1, y1, x2, y2 = ROI
    roi_img = img[y1:y2, x1:x2]
    return roi_img, (x1, y1, x2, y2)

# ucitavanje Cascade modela za detekciju stop znaka
cascade = cv2.CascadeClassifier(CASCADE_PATH)
if cascade.empty():
    print("ERROR loading cascade")
    sys.exit(1)

# historija prepoznatih objekata
stop_history = collections.deque(maxlen=STOP_FRAMES_NEEDED)
red_on_history = collections.deque(maxlen=RED_FRAMES_ON_NEEDED)
red_off_history = collections.deque(maxlen=RED_FRAMES_OFF_NEEDED)
stop_cooldown_end = 0

last_stop_time = 0
red_state = False
stop_detected_once = False  # flag da STOP znak ne detektuje više puta dok auto ne krene

FLASK_URL = "http://192.168.1.103:5000"  # <--- postavit na static ip kad bude bio

def main():
    global last_stop_time, red_state, stop_detected_once, stop_cooldown_end

    parser = argparse.ArgumentParser()
    parser.add_argument("--no-gui", action="store_true")
    parser.add_argument("--camera", type=int, default=CAM_INDEX)
    args = parser.parse_args()

    cap = VideoCaptureThread(args.camera, WORK_WIDTH, WORK_HEIGHT)
    print("Running...")

    try:
        while True:
            frame = cap.read()
            if frame is None:
                time.sleep(0.01)
                continue

            frame = cv2.resize(frame, (WORK_WIDTH, WORK_HEIGHT))
            roi_frame, (rx1, ry1, rx2, ry2) = apply_roi(frame)

            # detekcija stop znaka
            gray = cv2.cvtColor(roi_frame, cv2.COLOR_BGR2GRAY)
            stops = cascade.detectMultiScale(
                gray,
                scaleFactor=CASCADE_SCALE_FACTOR,
                minNeighbors=CASCADE_MIN_NEIGHBORS,
                minSize=CASCADE_MIN_SIZE
            )

            best_stop = None
            best_area = 0
            for (x, y, w, h) in stops:
                area = w * h
                ar = w / float(h)
                if area >= STOP_MIN_AREA and 0.6 <= ar <= 1.6:
                    if area > best_area:
                        best_area = area
                        best_stop = (x, y, w, h)

            stop_history.append(1 if best_stop else 0)
            stop_confirmed = len(stop_history) == STOP_FRAMES_NEEDED and all(stop_history)

            # kad se auto pocne kretat poslije stop znaka, resetuj flag
            try:
                r = requests.get(f"{FLASK_URL}/is_moving", timeout=0.1)
                moving = r.json().get("moving", False)
            except:
                moving = False

            if moving:
                stop_detected_once = False

            # detekcija stop znaka
            # 1. Modify the STOP detection condition
            # Added check: time.time() > stop_cooldown_end
            global STOP_HOLD_ACTIVE, STOP_HOLD_END
            if stop_confirmed and not stop_detected_once and time.time() > stop_cooldown_end:
                last_stop_time = time.time()
                print("[EVENT] STOP SIGN detected")
                gpio_write(GPIO_STOP_PIN, True)
                STOP_HOLD_ACTIVE = True 
                STOP_HOLD_END = time.time() + STOP_HOLD_SECONDS
                stop_detected_once = True

            # 2. Modify the STOP HOLD release logic
            if STOP_HOLD_ACTIVE and time.time() >= STOP_HOLD_END:
                gpio_write(GPIO_STOP_PIN, False)
                STOP_HOLD_ACTIVE = False 
                # Set the cooldown to start NOW for 5 seconds
                stop_cooldown_end = time.time() + STOP_COOLDOWN_SECONDS
                print(f"[EVENT] STOP HOLD ended. Cooldown active for {STOP_COOLDOWN_SECONDS}s")

            # 3. Optional: Clean up the moving reset
            # You might want to keep stop_detected_once = True until the cooldown ends
            # to be doubly sure it doesn't flicker.
            try:
                r = requests.get(f"{FLASK_URL}/is_moving", timeout=0.1)
                moving = r.json().get("moving", False)
            except:
                moving = False

            # Only reset the flag if the car is moving AND the cooldown has passed
            if moving and time.time() > stop_cooldown_end:
                stop_detected_once = False
                
            # detekcija crvenog svjetla
            hsv = cv2.cvtColor(roi_frame, cv2.COLOR_BGR2HSV)
            m1 = cv2.inRange(hsv, LOWER_RED1, UPPER_RED1)
            m2 = cv2.inRange(hsv, LOWER_RED2, UPPER_RED2)
            mask = cv2.bitwise_or(m1, m2)

            mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, MORPH_KERNEL)
            mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, MORPH_KERNEL)

            contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

            detected_red = False
            red_bbox = None

            if contours:
                c = max(contours, key=cv2.contourArea)
                area = cv2.contourArea(c)

                if RED_MIN_AREA <= area <= RED_MAX_AREA:
                    x, y, w, h = cv2.boundingRect(c)
                    if w <= LED_MAX_SIZE and h <= LED_MAX_SIZE:
                        v = hsv[y:y+h, x:x+w, 2]
                        mean_v = int(np.mean(v)) if v.size else 0
                        if mean_v >= RED_MIN_BRIGHTNESS:
                            per = cv2.arcLength(c, True)
                            if per > 0:
                                circularity = 4 * np.pi * area / (per * per)
                            else:
                                circularity = 0
                            if circularity >= CIRCULARITY_MIN:
                                detected_red = True
                                red_bbox = (x, y, w, h)

            red_on_history.append(1 if detected_red else 0)
            if detected_red:
                red_off_history.clear()
            else:
                red_off_history.append(1)

            if not red_state:
                if len(red_on_history) == RED_FRAMES_ON_NEEDED and all(red_on_history):
                    red_state = True
                    print("[EVENT] RED LED ON")
                    gpio_write(GPIO_RED_PIN, True)
            else:
                if len(red_off_history) == RED_FRAMES_OFF_NEEDED and all(red_off_history):
                    red_state = False
                    print("[EVENT] RED LED OFF")
                    gpio_write(GPIO_RED_PIN, False)

            display = frame.copy()
            if USE_ROI:
                cv2.rectangle(display, (rx1, ry1), (rx2, ry2), (255, 255, 0), 1)

            if best_stop:
                x, y, w, h = best_stop
                x += rx1; y += ry1
                cv2.rectangle(display, (x, y), (x+w, y+h), (0, 255, 0), 2)
                cv2.putText(display, "STOP", (x, y-6), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

            if red_bbox:
                x, y, w, h = red_bbox
                x += rx1; y += ry1
                cv2.rectangle(display, (x, y), (x+w, y+h), (0, 0, 255), 2)
                cv2.putText(display, "RED", (x, y-6), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)

            status = "RED: ON" if red_state else "RED: OFF"
            cv2.putText(display, status, (10, display.shape[0]-10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.8,
                        (0, 0, 255) if red_state else (0, 255, 0), 2)

            if not args.no_gui:
                cv2.imshow("Detection", display)
                cv2.imshow("Mask", mask)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break

            time.sleep(0.001)

    except KeyboardInterrupt:
        print("Stopping...")

    finally:
        cap.stop()
        if not args.no_gui:
            cv2.destroyAllWindows()
        if HAVE_GPIO:
            GPIO.cleanup()


if __name__ == "__main__":
    main()
