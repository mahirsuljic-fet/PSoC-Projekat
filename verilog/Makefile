SRC = main.v motor_driver.v parking_sensor.v 

movement:
	iverilog -o vout ${SRC} movement_test.v
	vvp vout
	gtkwave sim.vcd

line:
	iverilog -o vout ${SRC} line_test.v
	vvp vout
	gtkwave sim.vcd

ultrasonic_stop:
	iverilog -o vout ${SRC} ultrasonic_stop_test.v
	vvp vout
	gtkwave sim.vcd

ultrasonic_beep1:
	iverilog -o vout ${SRC} ultrasonic_beep1_test.v
	vvp vout
	gtkwave sim.vcd

ultrasonic_beep2:
	iverilog -o vout ${SRC} ultrasonic_beep2_test.v
	vvp vout
	gtkwave sim.vcd
