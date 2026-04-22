`timescale 1us / 1us

module ultrasonic_test;

  // --- 1. SIGNALS ---
  reg clk;
  reg fwd_in, bwd_in, left_in, right_in;
  reg stoplight_in, stopsign_in, failsafe_in;
  reg echo_fwd, echo_bwd1, echo_bwd2;
  reg buzzer_in, ld_left, ld_right;

  wire [3:0] m1_out, m2_out;
  wire [2:0] md_state;
  wire trig_fwd, trig_bwd1, trig_bwd2;
  wire buzzer;

  // Timing for 50MHz: 1cm = 2915 cycles = 58,300 ns
  parameter NS_PER_CM = 58;

  // --- 2. INSTANTIATION ---
  main uut (
      .clk(clk),
      .fwd_in(fwd_in),
      .bwd_in(bwd_in),
      .left_in(left_in),
      .right_in(right_in),
      .stoplight_in(stoplight_in),
      .stopsign_in(stopsign_in),
      .failsafe_in(failsafe_in),
      .m1_out(m1_out),
      .m2_out(m2_out),
      .md_state(md_state),
      .echo_fwd(echo_fwd),
      .trig_fwd(trig_fwd),
      .echo_bwd1(echo_bwd1),
      .trig_bwd1(trig_bwd1),
      .echo_bwd2(echo_bwd2),
      .trig_bwd2(trig_bwd2),
      .buzzer_in(buzzer_in),
      .buzzer(buzzer),
      .ld_left(ld_left),
      .ld_right(ld_right)
  );

  // --- 3. CLOCK GEN (50MHz) ---
  initial clk = 0;
  always #10 clk = ~clk;

  // --- 4. TEST PROCEDURE ---
  initial begin
    // Initialize Inputs
    fwd_in = 1;
    bwd_in = 0;
    left_in = 0;
    right_in = 0;
    stoplight_in = 0;
    stopsign_in = 0;
    failsafe_in = 0;
    echo_fwd = 0;
    echo_bwd1 = 0;
    echo_bwd2 = 0;
    buzzer_in = 0;
    ld_left = 0;
    ld_right = 0;

    $display("--- Starting Phase 1: Forward Sensor (5cm) ---");
    // Wait for Trigger pulse to finish
    wait (trig_fwd == 1);
    wait (trig_fwd == 0);
    #1;
    echo_fwd = 1;
    #(5 * NS_PER_CM);  // 5cm
    echo_fwd = 0;
    #100;  // Allow logic to update

    // The car should stop here because ps_signal_fwd triggers stop_in
    $display("[%0t] Front obstacle detected. Motor status: %b", $time, m1_out);

    // Switch direction for back sensor test
    fwd_in = 0;
    bwd_in = 1;
    #100;

    $display("--- Starting Phase 2: Back Sensor (25cm) ---");
    // Note: Your DIST_SLOW is 20cm, so 25cm will be silent (no beep).
    wait (trig_bwd1 == 1);
    wait (trig_bwd1 == 0);
    #1;
    echo_bwd1 = 1;
    #(25 * NS_PER_CM);
    echo_bwd1 = 0;
    #20000;  // Observe 20ms of silence

    $display("--- Starting Phase 3: Back Sensor (15cm - Slow/Fast) ---");
    wait (trig_bwd1 == 1);
    wait (trig_bwd1 == 0);
    #1;
    echo_bwd1 = 1;
    #(15 * NS_PER_CM);
    echo_bwd1 = 0;
    #40000;  // Observe 40ms of pulsing

    $display("--- Starting Phase 4: Back Sensor (5cm - Constant) ---");
    wait (trig_bwd1 == 1);
    wait (trig_bwd1 == 0);
    #1;
    echo_bwd1 = 1;
    #(5 * NS_PER_CM);
    echo_bwd1 = 0;
    #40000;  // Observe 40ms of constant high

    $display("--- All Tests Complete ---");
    $finish;
  end

  initial begin
    $dumpfile("sim.vcd");
    $dumpvars(0);

    #280302570 $finish;
  end

endmodule


