module main (
    input wire clk,

    // movement controls
    input wire fwd_in,
    input wire bwd_in,
    input wire left_in,
    input wire right_in,
    input wire stoplight_in,
    input wire stopsign_in,
    input wire failsafe_in,

    // motors
    output wire [3:0] m1_out,
    output wire [3:0] m2_out,

    output wire [2:0] md_state,

    // parking sensors
    input  wire echo_fwd,
    output wire trig_fwd,
    input  wire echo_bwd1,
    output wire trig_bwd1,
    input  wire echo_bwd2,
    output wire trig_bwd2,

    // buzzer
    input  wire buzzer_in,
    output wire buzzer,

    // line detectors
    input wire ld_left,
    input wire ld_right
);
  localparam PS_MODE_STOP = 0, PS_MODE_BEEP = 1;

  reg ps_mode_fwd = PS_MODE_BEEP;
  reg ps_mode_bwd1 = PS_MODE_BEEP;
  reg ps_mode_bwd2 = PS_MODE_BEEP;

  wire stop_in, ps_signal_fwd, ps_signal_bwd1, ps_signal_bwd2;

  assign stop_in = stopsign_in | stoplight_in | failsafe_in | ps_signal_fwd;
  assign buzzer  = buzzer_in | ps_signal_bwd1 | ps_signal_bwd2;

  motor_driver md (
      .clk(clk),
      .fwd_in(fwd_in),
      .bwd_in(bwd_in),
      .left_in(left_in),
      .right_in(right_in),
      .stop_in(stop_in),
      .ld_left(ld_left),
      .ld_right(ld_right),
      .m1_out(m1_out),
      .m2_out(m2_out),
      .state(md_state)
  );

  parking_sensor ps_fwd (
      .clk(clk),
      .echo(echo_fwd),
      .trig(trig_fwd),
      .mode(ps_mode_fwd),
      .signal(ps_signal_fwd)
  );

  parking_sensor ps_bwd1 (
      .clk(clk),
      .echo(echo_bwd1),
      .trig(trig_bwd1),
      .mode(ps_mode_bwd1),
      .signal(ps_signal_bwd1)
  );

  parking_sensor ps_bwd2 (
      .clk(clk),
      .echo(echo_bwd2),
      .trig(trig_bwd2),
      .mode(ps_mode_bwd2),
      .signal(ps_signal_bwd2)
  );
endmodule
