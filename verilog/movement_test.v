`timescale 1 ms / 1 ms
module movement_test;
  reg clk;

  // movement controls
  reg fwd_in;
  reg bwd_in;
  reg left_in;
  reg right_in;
  reg stoplight_in;
  reg stopsign_in;
  reg failsafe_in;

  // motors
  wire [3:0] m1_out;
  wire [3:0] m2_out;

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
      .m2_out(m2_out)
  );

  initial begin
    clk = 1'b0;
    forever #10 clk = ~clk;
  end

  initial begin
    fwd_in = 0;
    bwd_in = 0;
    left_in = 0;
    right_in = 0;

    stopsign_in = 0;
    stoplight_in = 0;
    failsafe_in = 0;

    #500 fwd_in = 1;
    #500 failsafe_in = 1;
    #500 failsafe_in = 0;
    fwd_in = 0;
    #500 bwd_in = 1;
    #500 failsafe_in = 1;
    #500 failsafe_in = 0;
    bwd_in = 0;
    #500 left_in = 1;
    #500 failsafe_in = 1;
    #500 failsafe_in = 0;
    left_in = 0;
    #500 right_in = 1;
    #500 failsafe_in = 1;
    #500 failsafe_in = 0;
    right_in = 0;
    #500 failsafe_in = 1;
    #500 failsafe_in = 0;
  end

  initial begin
    $dumpfile("sim.vcd");
    $dumpvars(0);

    #8_500 $finish;
  end

endmodule
