`timescale 1ns / 1ns

module ultrasonic_stop_test;

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

  initial clk = 0;
  always #10000 clk = ~clk;

  initial begin
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

    wait (trig_fwd == 1);
    wait (trig_fwd == 0);
    #1000;
    echo_fwd = 1;
    #(10 * NS_PER_CM);
    echo_fwd = 0;
    #10000000;

    $finish;
  end

  initial begin
    $dumpfile("sim.vcd");
    $dumpvars(0);

    #20000000 $finish;
  end

endmodule
