`timescale 1ns / 1ns

module ultrasonic_beep1_test;

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
  parameter NS_PER_CM = 58300;

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
  always #10000 clk = ~clk;

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

    $display("--- Starting Phase 3: Back Sensor (15cm - Slow/Fast) ---");
    wait (trig_bwd1 == 1);
    wait (trig_bwd1 == 0);
    #1000;
    echo_bwd1 = 1;
    #(16 * NS_PER_CM);
    echo_bwd1 = 0;
    #1500000000;  // Observe 40ms of pulsing

    $display("--- Starting Phase 4: Back Sensor (5cm - Constant) ---");
    wait (trig_bwd1 == 1);
    wait (trig_bwd1 == 0);
    #1000;
    echo_bwd1 = 1;
    #(12 * NS_PER_CM);
    echo_bwd1 = 0;
    #1500000000;  // Observe 40ms of constant high

    $display("--- All Tests Complete ---");
    $finish;
  end

  initial begin
    $dumpfile("sim.vcd");
    $dumpvars(0);

    #4000000000 $finish;
  end

endmodule


